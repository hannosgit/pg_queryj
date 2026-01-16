package com.hannos;

import com.hannos.pgquery.ParseResult;

public class Main {


    public static void main(String[] args) {
        ParseResult parseResult = PgParser.parseToAst("SELECT 1");
        System.out.println(parseResult.getStmtsList().getFirst().getStmt());
    }


}
