package polis.posting.vk;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import polis.posting.ApiException;
import polis.posting.PostProcessor;
import polis.util.SocialMedia;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class VkPostProcessor implements PostProcessor {
    private static final String VK_GROUP_URL = "vk.com/club";
    private static final String GROUP_POSTFIX = "?w=wall-";
    private static final String POST_PREFIX = "_";
    private static final String DOCUMENT_WARNING = """
            Возможно, Вы пытались опубликовать пост с прикрепленными к нему документами.
            В таком случае выполните следующие действия:
            1. Переведите страницу в группу (публикация документов на страницах не доступна):
            1.1. Проверьте, что адрес в строке поиска начинается со слова 'club' (для групп), а не 'public' (для страниц)
            1.2. Если адрес начинается со слова 'public', то на странице сообщества необходимо выбрать 'Ещё' и далее 'Перевести в группу'
            2. Включите раздел 'Файлы':
            2.1. Зайдите в раздел 'Управление'
            2.1. Выберите подраздел 'Настройки' и далее 'Разделы'
            2.2. Откройте доступ к разделу 'Файлы' - либо сделайте его открытым для всех пользователей, либо ограниченным и доступным только для администраторов и редакторов сообщества""";
    private static final int DOCUMENT_POST_ERROR_CODE = 15;
    private static final String VK_SOCIAL_NAME = SocialMedia.VK.getName();
    private final VkPoster vkPoster;

    public VkPostProcessor(VkPoster vkPoster) {
        this.vkPoster = vkPoster;
    }

    @Override
    public String processPostInChannel(
            Post post,
            long ownerChatId,
            long channelId,
            long groupId,
            long accountId,
            String accessToken
    ) {
        try {
            String pollId = null;
            Poll poll = post.poll();
            if (poll != null) {
                pollId = vkPoster.uploadPoll(
                        (int) accountId,
                        accessToken,
                        poll.getQuestion(),
                        poll.getIsAnonymous(),
                        poll.getAllowMultipleAnswers(),
                        poll.getIsClosed(),
                        poll.getOptions().stream().map(PollOption::getText).toList()
                );
            }
            List<String> documentIds = vkPoster.uploadDocuments(post.documents(), (int) accountId, accessToken, groupId);
            List<String> videoIds = vkPoster.uploadVideos(post.videos(), (int) accountId, accessToken, groupId);
            List<String> photoIds = vkPoster.uploadPhotos(post.photos(), (int) accountId, accessToken, groupId);
            String formattedText = vkPoster.getTextLinks(post.text(), post.textLinks(), accessToken, (int) accountId);

            long postId = vkPoster.newPost(accountId, accessToken)
                    .addPhotos(photoIds)
                    .addVideos(videoIds, groupId)
                    .addTextWithLinks(formattedText)
                    .addPoll(poll, pollId)
                    .addDocuments(documentIds, groupId)
                    .post((int) accountId, groupId);
            return PostProcessor.successfulPostMsg(VK_SOCIAL_NAME, postLink(groupId, postId));
        } catch (ApiException e) {
            if (e.getCode() == DOCUMENT_POST_ERROR_CODE) {
                return PostProcessor.failPostToGroupMsg(VK_SOCIAL_NAME, groupLinkWithDocumentWarning(groupId));
            } else {
                return PostProcessor.failPostToGroupMsg(VK_SOCIAL_NAME, groupLink(groupId));
            }
        } catch (URISyntaxException | IOException e) {
            return PostProcessor.failPostToGroupMsg(VK_SOCIAL_NAME, groupLink(groupId));
        }
    }

    private static String groupLink(long groupId) {
        return VK_GROUP_URL + groupId;
    }

    private static String postLink(long groupId, long postId) {
        return groupLink(groupId) + GROUP_POSTFIX + groupId + POST_PREFIX + postId;
    }

    private static String groupLinkWithDocumentWarning(long groupId) {
        return groupLink(groupId) + ". " + DOCUMENT_WARNING;
    }
}
