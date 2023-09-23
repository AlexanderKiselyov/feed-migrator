package polis.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContextFullCommandRegistry implements CommandRegistry<ContextFullCommand> {
    private final Map<String, ? extends ContextFullCommand> commands;

    public ContextFullCommandRegistry(List<ContextFullCommand> commands) {
        this.commands = commands.stream().collect(Collectors.toMap(
                IBotCommand::getCommandIdentifier,
                Function.identity()
        ));
    }

    @Override
    public ContextFullCommand getRegisteredCommand(String commandIdentifier) {
        return commands.get(commandIdentifier);
    }
}
