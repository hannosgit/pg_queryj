package com.hannos.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * typedef struct {
 *   PgQueryProtobuf parse_tree;
 *   char* stderr_buffer;
 *   PgQueryError* error;
 * } PgQueryProtobufParseResult;
 */
@Structure.FieldOrder({"parse_tree", "stderr_buffer", "error"})
public class PgQueryProtobufParseResult extends Structure implements Structure.ByValue  {

    public PgQueryProtobuf parse_tree;
    public String stderr_buffer;
    public PgQueryError error;

    public PgQueryProtobufParseResult() {
        read();
    }

    public PgQueryProtobufParseResult(Pointer p) {
        super(p);
        read();
    }

}
