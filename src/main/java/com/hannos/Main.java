package com.hannos;


import com.hannos.ffm.PgQueryParseResult;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static com.hannos.ffm.pg_query_h.pg_query_parse;

public class Main {


    public static void main(String[] args) {
        System.out.println("Hello pg query!");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = pg_query_parse(arena, arena.allocateFrom("SELECT 1"));
            MemorySegment parseTree = PgQueryParseResult.parse_tree(memorySegment);
            System.out.println(parseTree.getString(0, StandardCharsets.UTF_8));
        }
    }


}
