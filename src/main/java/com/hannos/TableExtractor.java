package com.hannos;

import com.hannos.pgquery.JoinExpr;
import com.hannos.pgquery.Node;
import com.hannos.pgquery.ParseResult;
import com.hannos.pgquery.RangeVar;
import com.hannos.pgquery.RawStmt;
import com.hannos.pgquery.SelectStmt;

import java.util.HashSet;
import java.util.Set;

public class TableExtractor {

    public Set<String> extractTables(String sql) {
        Set<String> tables = new HashSet<>();
        ParseResult result = PgParser.parseToAst(sql);

        for (RawStmt stmt : result.getStmtsList()) {
            if (stmt.getStmt().hasSelectStmt()) {
                extractFromSelect(stmt.getStmt().getSelectStmt(), tables);
            }
        }
        return tables;
    }

    private void extractFromSelect(SelectStmt select, Set<String> tables) {
        // Process FROM clause
        for (Node node : select.getFromClauseList()) {
            extractFromNode(node, tables);
        }

        // Handle UNION/INTERSECT/EXCEPT (left and right args)
        if (select.hasLarg()) extractFromSelect(select.getLarg(), tables);
        if (select.hasRarg()) extractFromSelect(select.getRarg(), tables);

        // Handle subqueries in WHERE, etc.
        if (select.hasWhereClause()) extractFromNode(select.getWhereClause(), tables);
    }

    private void extractFromNode(Node node, Set<java.lang.String> tables) {
        if (node.hasRangeVar()) {
            RangeVar rv = node.getRangeVar();
            java.lang.String tableName = rv.getSchemaname().isEmpty()
                    ? rv.getRelname()
                    : rv.getSchemaname() + "." + rv.getRelname();
            tables.add(tableName);
        } else if (node.hasJoinExpr()) {
            JoinExpr join = node.getJoinExpr();
            extractFromNode(join.getLarg(), tables);
            extractFromNode(join.getRarg(), tables);
        } else if (node.hasRangeSubselect()) {
            // Subquery in FROM clause
            extractFromSelect(node.getRangeSubselect().getSubquery().getSelectStmt(), tables);
        } else if (node.hasSubLink()) {
            // Subquery in WHERE/SELECT (e.g., EXISTS, IN)
            extractFromSelect(node.getSubLink().getSubselect().getSelectStmt(), tables);
        }
    }
}
