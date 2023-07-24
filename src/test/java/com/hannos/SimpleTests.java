package com.hannos;


import static org.assertj.core.api.Assertions.*;

import com.hannos.jna.PgQuery;
import org.junit.jupiter.api.Test;

public class SimpleTests {

    @Test
    void test_parse() {
        assertThatCode(() -> PgQuery.INSTANCE.pg_query_parse("SELECT * FROM foo")).doesNotThrowAnyException();
    }

}
