package polis.posting.ok;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.exceptions.OkApiException;
import polis.ok.api.exceptions.TokenExpiredException;
import polis.posting.ApiException;
import polis.posting.IPostProcessor;
import polis.util.SocialMedia;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

@Component
public class OkPostProcessor implements IPostProcessor {
    private static final String DOCUMENTS_ARENT_SUPPORTED =
            "Тип файла 'Документ' не поддерживается в социальной сети Одноклассники";
    private static final String GROUPS_LINK = "ok.ru/group/";
    private static final String POST_PREFIX = "/topic/";
    private static final String VIDEO_WARNING = """
            Для показа видео в ленте группы необходимо принять условия соглашения для каждого из опубликованных видео:
            1. Перейдите в раздел 'Все видео'
            2. Выберите пункт 'Редактировать' у опубликованного видео
            3. Отредактируйте поля с названием видео, описанием и другими полями, если необходимо
            4. Нажмите на кнопку 'Сохранить'""";
    private static final String OK_SOCIAL_NAME = SocialMedia.OK.getName();

    private final OkPoster okPoster;
    private final OkAuthorizator okAuthorizator;

    public OkPostProcessor(OkPoster okPoster, OkAuthorizator okAuthorizator) {
        this.okPoster = okPoster;
        this.okAuthorizator = okAuthorizator;
    }

    @Override
    public String processPostInChannel(
            Post post,
            long ownerChatId,
            long groupId,
            long accountId,
            String accessToken,
            String refreshToken,
            Consumer<Pair<String, String>> tokenRefreshedCallback
    ) {
        if (!post.documents().isEmpty() && post.animations().isEmpty()) {
            return DOCUMENTS_ARENT_SUPPORTED;
        }
        try {

            List<String> videoIds = executeTokenExpirationAware((token) ->
                            okPoster.uploadVideos(post.videos(), (int) accountId, token, groupId),
                    accessToken, refreshToken, tokenRefreshedCallback
            );
            List<String> photoIds = executeTokenExpirationAware((token) ->
                            okPoster.uploadPhotos(post.photos(), (int) accountId, token, groupId),
                    accessToken, refreshToken, tokenRefreshedCallback
            );
            String formattedText = executeTokenExpirationAware(
                    (token) -> okPoster.getTextLinks(post.text(), post.textLinks(), token),
                    accessToken, refreshToken, tokenRefreshedCallback
            );

            OkPoster.OkPost okPost = okPoster.newPost(accessToken)
                    .addVideos(videoIds)
                    .addPhotos(photoIds)
                    .addPoll(post.poll())
                    .addTextWithLinks(formattedText);

            long postId = executeTokenExpirationAware(
                    (token) -> okPost.post(groupId, token),
                    accessToken, refreshToken, tokenRefreshedCallback
            );

            if (videoIds == null || videoIds.isEmpty()) {
                return IPostProcessor.successfulPostMsg(OK_SOCIAL_NAME, postLink(groupId, postId));
            } else {
                return IPostProcessor.successfulPostMsg(OK_SOCIAL_NAME, postLinkWithVideoWarning(groupId, postId));
            }
        } catch (URISyntaxException | IOException | ApiException e) {
            return IPostProcessor.failPostToGroupMsg(OK_SOCIAL_NAME, groupLink(groupId));
        }
    }

    @Override
    public String processPostInChannel(
            Post post,
            long ownerChatId,
            long groupId,
            long accountId,
            String accessToken
    ) {
        return processPostInChannel(post, ownerChatId, groupId, accountId, accessToken, null, null);
    }

    private interface OkApiAction<T> {
        T execute(String accessToken) throws URISyntaxException, IOException, ApiException;
    }

    private <T> T executeTokenExpirationAware(
            OkApiAction<T> action,
            String accessToken,
            String refreshToken,
            Consumer<Pair<String, String>> tokenRefreshedCallback
    ) throws ApiException, URISyntaxException, IOException {
        try {
            return action.execute(accessToken);
        } catch (ApiException e) {
            if (!(e.getCause() instanceof TokenExpiredException)) {
                throw e;
            }
            OkAuthorizator.TokenPair refreshedTokens;
            try {
                refreshedTokens = okAuthorizator.refreshToken(refreshToken);
            } catch (OkApiException ex) {
                throw new ApiException(ex);
            }
            tokenRefreshedCallback.accept(Pair.of(refreshedTokens.accessToken(), refreshedTokens.refreshToken()));
            return action.execute(refreshedTokens.accessToken());
        }
    }

    private static String groupLink(long groupId) {
        return GROUPS_LINK + groupId;
    }

    private static String postLink(long groupId, long postId) {
        return groupLink(groupId) + POST_PREFIX + postId;
    }

    private static String postLinkWithVideoWarning(long groupId, long postId) {
        return postLink(groupId, postId) + "\n\n" + VIDEO_WARNING;
    }
}
