package com.hannos;


import com.hannos.jna.PgQueryParseResult;
import org.junit.jupiter.api.Test;

public class SimpleTests {

    @Test
    void test_parse() {
        final PgQueryParseResult pgQueryParseResult = PgParser.parse("select * from onek2 where unique2 = 11 and stringu1 < 'B';");
        System.out.println(pgQueryParseResult.parse_tree);
    }

}
