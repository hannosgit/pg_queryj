package com.hannos;

public class ParseException extends RuntimeException {
    private final int cursorPosition;
    private final ParseError error;

    public ParseException(String message, int cursorPosition) {
        super(message);
        this.cursorPosition = cursorPosition;
        this.error = ParseError.basic(ParseError.Kind.SYNTAX, message, cursorPosition);
    }

    public ParseException(ParseError error) {
        super(error.getMessage());
        this.cursorPosition = error.getCursorPosition();
        this.error = error;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public ParseError getError() {
        return error;
    }
}
