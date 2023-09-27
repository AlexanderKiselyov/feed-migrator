package polis.callbacks.inlinekeyboard.parsers;

public class Util {
    static boolean booleanFlag(String flag) {
        return switch (Byte.parseByte(flag)) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new NumberFormatException();
        };
    }

    static String booleanFlag(boolean flag) {
        return flag ? "1" : "0";
    }
}
