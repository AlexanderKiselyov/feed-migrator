package polis.commands.context;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface ContextStorage {
    Context getContext(long key);

    void setContext(long key, Context context);

    default Context getByMessage(Message message) {
        long key = message.getChatId();
        return getContext(key);
    }
}
