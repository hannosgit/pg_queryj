package com.hannos;


import com.google.protobuf.InvalidProtocolBufferException;
import com.hannos.ffm.PgQueryDeparseResult;
import com.hannos.pgquery.ParseResult;
import com.hannos.ffm.PgQueryError;
import com.hannos.ffm.PgQueryParseResult;
import com.hannos.ffm.PgQueryProtobuf;
import com.hannos.ffm.PgQueryProtobufParseResult;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.hannos.ffm.pg_query_h.*;


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
                throw new ParseException(ParseError.fromSql(ParseError.Kind.SYNTAX, message, cursorPos, sql));
            }

            MemorySegment parseTree = PgQueryParseResult.parse_tree(memorySegment);
            String parseTreeStr = parseTree.getString(0);
            pg_query_free_parse_result(memorySegment);
            return new QueryParseResult(parseTreeStr);
        }
    }

    public static String deparse(byte[] protobufParseTree) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment protobufStruct = PgQueryProtobuf.allocate(arena);
            MemorySegment dataSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, protobufParseTree);
            PgQueryProtobuf.len(protobufStruct, protobufParseTree.length);
            PgQueryProtobuf.data(protobufStruct, dataSegment);

            MemorySegment result = pg_query_deparse_protobuf(arena, protobufStruct);

            MemorySegment errorPtr = PgQueryDeparseResult.error(result);
            if (!errorPtr.equals(MemorySegment.NULL)) {
                MemorySegment error = errorPtr.reinterpret(PgQueryError.sizeof(), arena, null);
                String message = PgQueryError.message(error).getString(0);
                int cursorPos = PgQueryError.cursorpos(error);
                pg_query_free_deparse_result(result);
                throw new ParseException(ParseError.basic(ParseError.Kind.DEPARSE, message, cursorPos));
            }

            String query = PgQueryDeparseResult.query(result).getString(0);
            pg_query_free_deparse_result(result);
            return query;
        }
    }

    public static byte[] parseProtobuf(String sql) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment result = pg_query_parse_protobuf(arena, arena.allocateFrom(sql));

            MemorySegment errorPtr = PgQueryProtobufParseResult.error(result);
            if (!errorPtr.equals(MemorySegment.NULL)) {
                MemorySegment error = errorPtr.reinterpret(PgQueryError.sizeof(), arena, null);
                String message = PgQueryError.message(error).getString(0);
                int cursorPos = PgQueryError.cursorpos(error);
                pg_query_free_protobuf_parse_result(result);
                throw new ParseException(ParseError.fromSql(ParseError.Kind.PROTOBUF, message, cursorPos, sql));
            }

            MemorySegment parseTree = PgQueryProtobufParseResult.parse_tree(result);
            int len = PgQueryProtobuf.len(parseTree);
            MemorySegment dataPtr = PgQueryProtobuf.data(parseTree).reinterpret(len, arena, null);
            byte[] protobufBytes = dataPtr.toArray(ValueLayout.JAVA_BYTE);
            pg_query_free_protobuf_parse_result(result);
            return protobufBytes;
        }
    }

    public static ParseResult parseToAst(String sql) {
        byte[] protobuf = parseProtobuf(sql);
        try {
            return ParseResult.parseFrom(protobuf);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse protobuf", e);
        }
    }

}
