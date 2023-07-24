package com.hannos.jna;

import com.sun.jna.*;

public interface PgQuery extends Library {
    PgQuery INSTANCE = Native.load("pg_query", PgQuery.class);
    PgQueryParseResult pg_query_parse(String sql);

    void pg_query_free_parse_result(PgQueryParseResult result);

}

