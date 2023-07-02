package polis.keyboards.callbacks.parsers;

public class Util {
    static boolean isClickForDeletion(String flag) {
        return switch (Byte.parseByte(flag)) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new NumberFormatException();
        };
    }
}
