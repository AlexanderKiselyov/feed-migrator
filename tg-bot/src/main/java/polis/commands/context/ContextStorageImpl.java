package polis.commands.context;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class ContextStorageImpl implements ContextStorage {
    private final Map<Long, Context> contextStorage = new ConcurrentHashMap<>();

    @Override
    public Context getContext(long key) {
        return contextStorage.computeIfAbsent(key, id -> new ContextImpl());
    }

    @Override
    public void setContext(long key, Context context) {
        contextStorage.put(key, context);
    }
}
