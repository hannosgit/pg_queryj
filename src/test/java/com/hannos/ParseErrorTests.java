package com.hannos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ParseErrorTests {

    @Test
    void test_parse_error_includes_line_and_column() {
        String sql = "SELECT FROM FROM";

        Throwable thrown = catchThrowable(() -> PgParser.parse(sql));

        assertThat(thrown).isInstanceOf(ParseException.class);
        ParseException parseException = (ParseException) thrown;
        ParseError error = parseException.getError();

        assertThat(error.getKind()).isEqualTo(ParseError.Kind.SYNTAX);
        assertThat(error.getCursorPosition()).isGreaterThan(0);
        assertThat(error.getLine()).isEqualTo(1);
        assertThat(error.getColumn()).isEqualTo(error.getCursorPosition());
        assertThat(error.getLineText()).isEqualTo(sql);
        assertThat(error.getCaretColumn()).isEqualTo(error.getColumn());
    }

    @Test
    void test_parse_protobuf_error_kind() {
        String sql = "SELECT FROM";

        Throwable thrown = catchThrowable(() -> PgParser.parseProtobuf(sql));

        assertThat(thrown).isInstanceOf(ParseException.class);
        ParseException parseException = (ParseException) thrown;
        ParseError error = parseException.getError();
        assertThat(error.getKind()).isEqualTo(ParseError.Kind.PROTOBUF);
    }

    @Test
    void test_from_sql_multiline_position() {
        String sql = "SELECT 1;\nSELECT FROM x;\nSELECT 3;";
        int cursor = sql.indexOf("FROM") + 1;

        ParseError error = ParseError.fromSql(ParseError.Kind.SYNTAX, "syntax error", cursor, sql);

        assertThat(error.getLine()).isEqualTo(2);
        assertThat(error.getColumn()).isEqualTo("SELECT ".length() + 1);
        assertThat(error.getLineText()).isEqualTo("SELECT FROM x;");
        assertThat(error.getCaretColumn()).isEqualTo(error.getColumn());
    }

    @Test
    void test_from_sql_out_of_range_falls_back_to_basic() {
        String sql = "SELECT 1";

        ParseError error = ParseError.fromSql(ParseError.Kind.SYNTAX, "syntax error", 0, sql);

        assertThat(error.getLine()).isEqualTo(-1);
        assertThat(error.getColumn()).isEqualTo(-1);
        assertThat(error.getLineText()).isNull();
    }

    @Test
    void test_parse_exception_wraps_error() {
        ParseError error = ParseError.basic(ParseError.Kind.DEPARSE, "boom", 12);

        ParseException exception = new ParseException(error);

        assertThat(exception.getError()).isSameAs(error);
        assertThat(exception.getCursorPosition()).isEqualTo(12);
        assertThat(exception.getMessage()).isEqualTo("boom");
    }
}
