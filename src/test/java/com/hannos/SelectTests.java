package com.hannos;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

public class SelectTests {

    @Test
    void test_parse() {
        TestFileHelper.runStatement("/sql/select.sql",s -> {
            assertThatCode(() -> PgParser.parse(s)).doesNotThrowAnyException();
        });
    }

}
