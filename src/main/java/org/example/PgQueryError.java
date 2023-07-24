package org.example;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * typedef struct {
 * char* message; // exception message
 * char* funcname; // source function of exception (e.g. SearchSysCache)
 * char* filename; // source of exception (e.g. parse.l)
 * int lineno; // source of exception (e.g. 104)
 * int cursorpos; // char in query at which exception occurred
 * char* context; // additional context (optional, can be NULL)
 * } PgQueryError;
 */
@Structure.FieldOrder({"message", "funcname", "filename", "lineno", "cursorpos", "context"})
public class PgQueryError extends Structure implements Structure.ByReference {
    public String message;
    public String funcname;
    public String filename;
    public int lineno;
    public int cursorpos;
    public String context;

    public PgQueryError() {

    }
}
