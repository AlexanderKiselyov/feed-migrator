package polis.commands.context;

public interface ContextStorage {
    Context getContext(long userChatId);

    void setContext(long userChatId, Context context);
}
