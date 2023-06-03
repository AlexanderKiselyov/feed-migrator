package polis.posting;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.util.Emojis;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface IPostProcessor {
    String SUCCESS_POST_MSG = "Успешно опубликовал пост в социальной сети %s";
    String ERROR_POST_MSG = "Упс, что-то пошло не так " + Emojis.SAD_FACE + " \n"
            + "Не удалось опубликовать пост в социальной сети %s";

    /**
     * Занимается логикой обработки и публикации поста в социальную сеть.
     * @param post Загруженный из телеграмма пост
     * @return Сообщение об успешности публикации поста или текст ошибки
     */
    String processPostInChannel(
            Post post,
            long ownerChatId,
            long groupId,
            long accountId,
            String accessToken
    );

    default String processPostInChannel(
            Post post,
            long ownerChatId,
            long groupId,
            long accountId,
            String accessToken,
            String refreshToken,
            Consumer<Pair<String, String>> tokenRefreshedCallback
    ) {
        return processPostInChannel(post, ownerChatId, groupId, accountId, accessToken);
    }

    static String successfulPostMsg(String social, String what) {
        return String.format(SUCCESS_POST_MSG, social) + " " + what;
    }

    static String failPostToGroupMsg(String social, String where) {
        return String.format(ERROR_POST_MSG, social) + " " + where;
    }

    record Post(
            @NotNull List<File> videos,
            @NotNull List<File> photos,
            @NotNull List<File> animations,
            @NotNull List<File> documents,
            @NotNull List<MessageEntity> textLinks,
            @Nullable String text,
            @Nullable Poll poll
    ) {
    }
}
