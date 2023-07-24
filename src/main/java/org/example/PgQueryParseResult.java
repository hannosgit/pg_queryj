package org.example;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * typedef struct {
 * char* parse_tree;
 * char* stderr_buffer;
 * PgQueryError* error;
 * } PgQueryParseResult;
 */
@Structure.FieldOrder({"parse_tree", "stderr_buffer", "error"})
public class PgQueryParseResult extends Structure implements Structure.ByValue {
    public String parse_tree;
    public String stderr_buffer;
    public PgQueryError error;

    public PgQueryParseResult() {
        read();
    }

    public PgQueryParseResult(Pointer p) {
        super(p);
        read();
    }
}
