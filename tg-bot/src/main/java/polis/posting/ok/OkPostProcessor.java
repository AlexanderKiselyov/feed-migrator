package polis.posting.ok;

import org.springframework.stereotype.Component;
import polis.posting.ApiException;
import polis.posting.PostProcessor;
import polis.util.SocialMedia;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class OkPostProcessor implements PostProcessor {
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

    public OkPostProcessor(OkPoster okPoster) {
        this.okPoster = okPoster;
    }

    @Override
    public String processPostInChannel(
            Post post,
            long ownerChatId,
            long groupId,
            long accountId,
            String accessToken
    ) {
        //Здесь можно будет сделать маленькие трайи, чтобы пользователю писать более конкретную ошибку
        try {
            if (!post.documents().isEmpty() && post.animations().isEmpty()) {
                return DOCUMENTS_ARENT_SUPPORTED;
            }

            List<String> videoIds = okPoster.uploadVideos(post.videos(), (int) accountId, accessToken, groupId);
            List<String> photoIds = okPoster.uploadPhotos(post.photos(), (int) accountId, accessToken, groupId);
            String formattedText = okPoster.getTextLinks(post.text(), post.textLinks(), accessToken);

            long postId = okPoster.newPost(accessToken)
                    .addVideos(videoIds)
                    .addPhotos(photoIds)
                    .addPoll(post.poll())
                    .addTextWithLinks(formattedText)
                    .post(groupId);
            if (videoIds == null || videoIds.isEmpty()) {
                return PostProcessor.successfulPostMsg(OK_SOCIAL_NAME, postLink(groupId, postId));
            } else {
                return PostProcessor.successfulPostMsg(OK_SOCIAL_NAME, postLinkWithVideoWarning(groupId, postId));
            }
        } catch (URISyntaxException | IOException | ApiException e) {
            return PostProcessor.failPostToGroupMsg(OK_SOCIAL_NAME, groupLink(groupId));
        }

    }

    private static String groupLink(long groupId) {
        return GROUPS_LINK + groupId;
    }

    private static String postLink(long groupId, long postId) {
        return groupLink(groupId) + POST_PREFIX + postId;
    }

    private static String postLinkWithVideoWarning(long groupId, long postId) {
        return postLink(groupId, postId) + ". " + VIDEO_WARNING;
    }
}
