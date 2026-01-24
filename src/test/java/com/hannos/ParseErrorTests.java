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
}
