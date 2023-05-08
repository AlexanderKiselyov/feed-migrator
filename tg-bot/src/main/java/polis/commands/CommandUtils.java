package polis.commands;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandUtils {
    static List<String> getButtonsForSyncOptions() {
        return List.of(
                "Да",
                "yesNo 0",
                "Нет",
                "yesNo 1"
        );
    }
}
