package com.hannos;


import com.hannos.pgquery.ParseResult;
import com.hannos.pgquery.SelectStmt;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SimpleTests {

    @Test
    void test_parse() {
        final QueryParseResult pgQueryParseResult = PgParser.parse("select * from onek2 where unique2 = 11 and stringu1 < 'B';");
        System.out.println(pgQueryParseResult.parse_tree());

        assertThat(pgQueryParseResult.parse_tree()).isEqualTo(
                "{\"version\":170007,\"stmts\":[{\"stmt\":{\"SelectStmt\":{\"targetList\":[{\"ResTarget\":{\"val\":{\"ColumnRef\":{\"fields\":[{\"A_Star\":{}}],\"location\":7}},\"location\":7}}],\"fromClause\":[{\"RangeVar\":{\"relname\":\"onek2\",\"inh\":true,\"relpersistence\":\"p\",\"location\":14}}],\"whereClause\":{\"BoolExpr\":{\"boolop\":\"AND_EXPR\",\"args\":[{\"A_Expr\":{\"kind\":\"AEXPR_OP\",\"name\":[{\"String\":{\"sval\":\"=\"}}],\"lexpr\":{\"ColumnRef\":{\"fields\":[{\"String\":{\"sval\":\"unique2\"}}],\"location\":26}},\"rexpr\":{\"A_Const\":{\"ival\":{\"ival\":11},\"location\":36}},\"location\":34}},{\"A_Expr\":{\"kind\":\"AEXPR_OP\",\"name\":[{\"String\":{\"sval\":\"\\u003c\"}}],\"lexpr\":{\"ColumnRef\":{\"fields\":[{\"String\":{\"sval\":\"stringu1\"}}],\"location\":43}},\"rexpr\":{\"A_Const\":{\"sval\":{\"sval\":\"B\"},\"location\":54}},\"location\":52}}],\"location\":39}},\"limitOption\":\"LIMIT_OPTION_DEFAULT\",\"op\":\"SETOP_NONE\"}},\"stmt_len\":57}]}"
        );
    }

    @Test
    void test_parse_malformed_input() {
        assertThatThrownBy(() ->
                PgParser.parse("SELECT FROM FROM")).isInstanceOf(ParseException.class);
    }

    @Test
    void test_parseToAst() {
        ParseResult result = PgParser.parseToAst("SELECT 1, 2, 3");

        assertThat(result.getStmtsCount()).isEqualTo(1);
        assertThat(result.getStmts(0).getStmt().hasSelectStmt()).isTrue();

        SelectStmt selectStmt = result.getStmts(0).getStmt().getSelectStmt();
        assertThat(selectStmt.getTargetListCount()).isEqualTo(3);
    }

}
