package com.hannos.jna;

import com.sun.jna.Structure;

/**
 * typedef struct {
 *   unsigned int len;
 *   char* data;
 * } PgQueryProtobuf;
 */
@Structure.FieldOrder({"len", "data"})
public class PgQueryProtobuf extends Structure implements Structure.ByValue {

    public int len;

    public String data;

}
