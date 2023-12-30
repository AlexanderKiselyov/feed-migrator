package polis.util;

//TODO The logic tied to this class most likely needs refactoring

/**
 * Хранит ответ бота с индикатором ранее присланного ошибочного сообщения от пользователя.
 */
public class AnswerPair {
    private final String answer;
    private final Boolean isError;

    public AnswerPair(String answer, boolean isError) {
        this.answer = answer;
        this.isError = isError;
    }

    public String getAnswer() {
        return answer;
    }

    public Boolean getError() {
        return isError;
    }
}
