package com.hannos;

public final class ParseError {
    public enum Kind {
        SYNTAX,
        DEPARSE,
        PROTOBUF
    }

    private final Kind kind;
    private final String message;
    private final int cursorPosition;
    private final int line;
    private final int column;
    private final String lineText;
    private final int caretColumn;

    private ParseError(Kind kind, String message, int cursorPosition, int line, int column, String lineText, int caretColumn) {
        this.kind = kind;
        this.message = message;
        this.cursorPosition = cursorPosition;
        this.line = line;
        this.column = column;
        this.lineText = lineText;
        this.caretColumn = caretColumn;
    }

    public static ParseError basic(Kind kind, String message, int cursorPosition) {
        return new ParseError(kind, message, cursorPosition, -1, -1, null, -1);
    }

    public static ParseError fromSql(Kind kind, String message, int cursorPosition, String sql) {
        if (sql == null || sql.isEmpty() || cursorPosition <= 0 || cursorPosition > sql.length()) {
            return basic(kind, message, cursorPosition);
        }

        int index = cursorPosition - 1;
        int lineStart = sql.lastIndexOf('\n', index);
        if (lineStart < 0) {
            lineStart = 0;
        } else {
            lineStart += 1;
        }
        int lineEnd = sql.indexOf('\n', index);
        if (lineEnd < 0) {
            lineEnd = sql.length();
        }

        String lineText = sql.substring(lineStart, lineEnd);
        int line = 1;
        for (int i = 0; i < lineStart; i++) {
            if (sql.charAt(i) == '\n') {
                line++;
            }
        }
        int column = (index - lineStart) + 1;
        int caretColumn = column;
        return new ParseError(kind, message, cursorPosition, line, column, lineText, caretColumn);
    }

    public Kind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getLineText() {
        return lineText;
    }

    public int getCaretColumn() {
        return caretColumn;
    }
}
