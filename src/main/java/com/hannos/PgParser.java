package com.hannos;

import com.hannos.jna.PgQuery;
import com.hannos.jna.PgQueryParseResult;

public class PgParser {

    public static PgQueryParseResult parse(String sql){
        PgQueryParseResult parse = PgQuery.INSTANCE.pg_query_parse(sql);
        PgQuery.INSTANCE.pg_query_free_parse_result(parse);

        return parse;
    }

}
