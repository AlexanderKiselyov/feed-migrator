package polis.util;

/**
 * Сущность описывающее состояние бота. Характеризует текущий этап общения с пользователем
 */
public interface IState {
    String getIdentifier();

    String getDescription();
}
