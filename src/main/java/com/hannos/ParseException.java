package com.hannos;

public class ParseException extends RuntimeException {
    private final int cursorPosition;

    public ParseException(String message, int cursorPosition) {
        super(message);
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
}
