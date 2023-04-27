package polis.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

@Component
public class AddVkGroup extends Command {

    public AddVkGroup() {
        super(State.AddVkGroup.getIdentifier(), State.AddVkGroup.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

    }
}
