package polis.posting;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.util.Emojis;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface PostProcessor {
    static final String SUCCESS_POST_MSG = "Успешно опубликовал пост в социальной сети %s";
    static final String ERROR_POST_MSG = "Упс, что-то пошло не так " + Emojis.SAD_FACE + " \n"
            + "Не удалось опубликовать пост в социальной сети %s";

    String processPostInChannel(
            Post post,
            long ownerChatId,
            long channelId,
            long groupId,
            long accountId,
            String accessToken
    );

    static String successfulPostMsg(String social, String what) {
        return String.format(SUCCESS_POST_MSG, social) + " " + what;
    }

    static String failPostToGroupMsg(String social, String where) {
        return String.format(ERROR_POST_MSG, social) + " " + where;
    }

    record Post(
            List<File> videos,
            List<File> photos,
            List<File> animations,
            List<File> documents,
            List<MessageEntity> textLinks,
            String text,
            Poll poll
    ) {

    }
}
