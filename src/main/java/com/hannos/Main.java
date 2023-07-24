package com.hannos;

import com.hannos.jna.PgQuery;
import com.hannos.jna.PgQueryParseResult;
import com.hannos.jna.PgQueryProtobufParseResult;

public class Main {
    public static void main(String[] args) {
        main3();
    }

    private static void main2() {
        System.out.println("Hello pg query!");
        PgQueryParseResult parse = PgQuery.INSTANCE.pg_query_parse("SELECT 1");
        PgQuery.INSTANCE.pg_query_free_parse_result(parse);
        System.out.println(parse.parse_tree);
        System.out.println(parse.stderr_buffer);
        System.out.println(parse.error);
    }

    private static void main3() {
        System.out.println("Hello pg query!");
        PgQueryProtobufParseResult parse = PgQuery.INSTANCE.pg_query_parse_protobuf("SELECT 1");
        PgQuery.INSTANCE.pg_query_free_protobuf_parse_result(parse);
        System.out.println(parse.parse_tree);
        System.out.println(parse.stderr_buffer);
        System.out.println(parse.error);
    }
}
