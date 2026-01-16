package com.hannos;


import com.hannos.ffm.PgQueryError;
import com.hannos.ffm.PgQueryParseResult;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.hannos.ffm.pg_query_h.pg_query_free_parse_result;
import static com.hannos.ffm.pg_query_h.pg_query_parse;

public class PgParser {

    static {
        NativeLoader.ensureLoaded();
    }

    public static QueryParseResult parse(String sql) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = pg_query_parse(arena, arena.allocateFrom(sql));

            MemorySegment errorPtr = PgQueryParseResult.error(memorySegment);
            if (!errorPtr.equals(MemorySegment.NULL)) {
                MemorySegment error = errorPtr.reinterpret(PgQueryError.sizeof(), arena, null);
                String message = PgQueryError.message(error).getString(0);
                int cursorPos = PgQueryError.cursorpos(error);
                pg_query_free_parse_result(memorySegment);
                throw new ParseException(message, cursorPos);
            }

            MemorySegment parseTree = PgQueryParseResult.parse_tree(memorySegment);
            String parseTreeStr = parseTree.getString(0);
            pg_query_free_parse_result(memorySegment);
            return new QueryParseResult(parseTreeStr);
        }
    }

}
