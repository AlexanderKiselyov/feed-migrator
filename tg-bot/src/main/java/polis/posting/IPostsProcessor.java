package polis.posting;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface IPostsProcessor {
    void processPostsInChannel(long channelId, List<Message> posts);
}
