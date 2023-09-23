package polis.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import polis.keyboards.callbacks.CallbackType;
import polis.util.IState;

import java.util.Collections;
import java.util.List;

/**
 * Пусть команда - это сущность, которая:
 * - Умеет кратко рассказать, что она делает и как-то поприветствовать пользователя.
 * - Знает о командах, к которым возможен дальнейший переход.
 * - Умеет кратко описать команды, к которым возможен дальнейший переход, что будет при переходе к ним и т.п.
 */
public interface ContextLessCommand extends IBotCommand {
    List<IState> TRANSITION_TO_NON_COMMAND = Collections.emptyList();
    List<IState> TRANSITION_WITH_CALLBACK = Collections.emptyList();

    String helloMessage();

    /**
     * @return List of commands that can be navigated to from the current command.
     */
    List<IState> nextPossibleCommands();

    /**
     * @param unformattedMessage Unformatted message containing information of nextPossibleCommands.size() commands
     *                           and has placeholders for identifiers of each nextPossibleCommands.size() commands
     * @return command main message containing base hello message and description of all nextPossibleCommands()
     */
    default String commandMainMessage(String unformattedMessage) {
        return helloMessage() + "\n" + unformattedMessage.formatted(
                nextCommandsIdentifiers()
        );
    }

    default CallbackType callbackType() {
        return null;
    }

    default List<String> nextCommandsIdentifiers() {
        return nextPossibleCommands().stream().map(IState::getIdentifier).toList();
    }
}
