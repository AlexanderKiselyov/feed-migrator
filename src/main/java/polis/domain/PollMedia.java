package polis.domain;

import java.util.Collection;
import java.util.stream.Collectors;

public class PollMedia extends Media {
    public final String question;
    public final Collection<Answer> answers;
    public final String options;

    public PollMedia(String question, Collection<Answer> answers, Collection<Option> options) {
        super(Type.POLL);
        this.question = question;
        this.answers = answers;
        this.options = options.stream().map(option -> option.value).collect(Collectors.joining(","));
    }

    public static class Answer {
        public final String text;

        public Answer(String text) {
            this.text = text;
        }
    }

    public enum Option {
        SINGLE_CHOICE("SingleChoice"),
        ANONYMOUS_VOTING("AnonymousVoting"),
        RESULTS_AFTER_VOTING("ResultsAfterVoting"),
        VOTING_CLOSED("VotingClosed"),
        AVATAR_BATTLE("AvatarBattle"),
        BATTLE("Battle");

        public final String value;

        Option(String value) {
            this.value = value;
        }
    }
}
