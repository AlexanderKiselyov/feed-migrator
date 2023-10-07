package polis.util;

/**
 * Характерезует "подсостояние"
 * Поможет на каком этапе обещния с пользоватем мы находися в рамках другого большого состояния.
 * Сейчас в данном энаме нет необходимости - нигде не используется
 *
 * @implNote Может помочь реализовать сценарий, когда в рамках одного State мы имеем очень длинный процесс общения с пользователем
 * (отправляем сообщение, ждём ответ, получаем ответ, снова отправляем сообщение и так далее)
 */
public enum Substate implements IState {
    ;

    private final String identifier;
    private final String description;

    Substate(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }
}
