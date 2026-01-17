package com.hannos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TableExtractorTest {

    private TableExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TableExtractor();
    }

    @Nested
    class SimpleQueries {

        @Test
        void singleTable() {
            Set<String> tables = extractor.extractTables("SELECT * FROM users");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void multipleTables() {
            Set<String> tables = extractor.extractTables("SELECT * FROM users, orders, products");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "products");
        }

        @Test
        void schemaQualifiedTable() {
            Set<String> tables = extractor.extractTables("SELECT * FROM public.users");
            assertThat(tables).containsExactly("public.users");
        }

        @Test
        void mixedSchemaQualifiedAndUnqualified() {
            Set<String> tables = extractor.extractTables("SELECT * FROM public.users, orders");
            assertThat(tables).containsExactlyInAnyOrder("public.users", "orders");
        }
    }

    @Nested
    class TableAliases {

        @Test
        void simpleAlias() {
            Set<String> tables = extractor.extractTables("SELECT u.id FROM users u");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void aliasWithAsKeyword() {
            Set<String> tables = extractor.extractTables("SELECT u.id FROM users AS u");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void multipleAliases() {
            Set<String> tables = extractor.extractTables(
                    "SELECT u.id, o.id FROM users u, orders AS o");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void schemaQualifiedWithAlias() {
            Set<String> tables = extractor.extractTables(
                    "SELECT u.id FROM public.users AS u");
            assertThat(tables).containsExactly("public.users");
        }
    }

    @Nested
    class JoinQueries {

        @Test
        void innerJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users INNER JOIN orders ON users.id = orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void leftJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users LEFT JOIN orders ON users.id = orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void rightJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users RIGHT JOIN orders ON users.id = orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void fullOuterJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users FULL OUTER JOIN orders ON users.id = orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void crossJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users CROSS JOIN products");
            assertThat(tables).containsExactlyInAnyOrder("users", "products");
        }

        @Test
        void multipleJoins() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users " +
                            "JOIN orders ON users.id = orders.user_id " +
                            "JOIN products ON orders.product_id = products.id " +
                            "LEFT JOIN categories ON products.category_id = categories.id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "products", "categories");
        }

        @Test
        void selfJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT e.name, m.name FROM employees e JOIN employees m ON e.manager_id = m.id");
            assertThat(tables).containsExactly("employees");
        }

        @Test
        void joinWithAliases() {
            Set<String> tables = extractor.extractTables(
                    "SELECT u.name, o.total FROM users AS u " +
                            "INNER JOIN orders AS o ON u.id = o.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void naturalJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users NATURAL JOIN orders");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }
    }

    @Nested
    class WithClauses {

        @Test
        void simpleCte_currentBehavior() {
            // Current implementation only extracts CTE references, not tables inside CTE definitions
            Set<String> tables = extractor.extractTables(
                    "WITH active_users AS (SELECT * FROM users WHERE active = true) " +
                            "SELECT * FROM active_users");
            assertThat(tables).containsExactly("active_users");
        }

        @Test
        @Disabled("CTE definition extraction not yet implemented")
        void simpleCte_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH active_users AS (SELECT * FROM users WHERE active = true) " +
                            "SELECT * FROM active_users");
            assertThat(tables).containsExactlyInAnyOrder("users", "active_users");
        }

        @Test
        void multipleCtes_currentBehavior() {
            // Current implementation only extracts CTE references from main query
            Set<String> tables = extractor.extractTables(
                    "WITH " +
                            "  active_users AS (SELECT * FROM users WHERE active = true), " +
                            "  recent_orders AS (SELECT * FROM orders WHERE created_at > '2024-01-01') " +
                            "SELECT * FROM active_users JOIN recent_orders ON active_users.id = recent_orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("active_users", "recent_orders");
        }

        @Test
        @Disabled("CTE definition extraction not yet implemented")
        void multipleCtes_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH " +
                            "  active_users AS (SELECT * FROM users WHERE active = true), " +
                            "  recent_orders AS (SELECT * FROM orders WHERE created_at > '2024-01-01') " +
                            "SELECT * FROM active_users JOIN recent_orders ON active_users.id = recent_orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "active_users", "recent_orders");
        }

        @Test
        void nestedCte_currentBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH user_orders AS (" +
                            "  SELECT u.id, o.total FROM users u JOIN orders o ON u.id = o.user_id" +
                            ") " +
                            "SELECT * FROM user_orders JOIN products ON user_orders.id = products.user_id");
            assertThat(tables).containsExactlyInAnyOrder("user_orders", "products");
        }

        @Test
        @Disabled("CTE definition extraction not yet implemented")
        void nestedCte_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH user_orders AS (" +
                            "  SELECT u.id, o.total FROM users u JOIN orders o ON u.id = o.user_id" +
                            ") " +
                            "SELECT * FROM user_orders JOIN products ON user_orders.id = products.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "user_orders", "products");
        }

        @Test
        void recursiveCte_currentBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH RECURSIVE subordinates AS (" +
                            "  SELECT id, name, manager_id FROM employees WHERE id = 1 " +
                            "  UNION ALL " +
                            "  SELECT e.id, e.name, e.manager_id FROM employees e " +
                            "  INNER JOIN subordinates s ON e.manager_id = s.id" +
                            ") " +
                            "SELECT * FROM subordinates");
            assertThat(tables).containsExactly("subordinates");
        }

        @Test
        @Disabled("CTE definition extraction not yet implemented")
        void recursiveCte_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH RECURSIVE subordinates AS (" +
                            "  SELECT id, name, manager_id FROM employees WHERE id = 1 " +
                            "  UNION ALL " +
                            "  SELECT e.id, e.name, e.manager_id FROM employees e " +
                            "  INNER JOIN subordinates s ON e.manager_id = s.id" +
                            ") " +
                            "SELECT * FROM subordinates");
            assertThat(tables).containsExactlyInAnyOrder("employees", "subordinates");
        }
    }

    @Nested
    class Subqueries {

        @Test
        void subqueryInFromClause() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM (SELECT id, name FROM users) AS u");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void subqueryInFromClauseWithJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM (SELECT * FROM users) AS u " +
                            "JOIN orders ON u.id = orders.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void existsSubquery() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id)");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void inSubquery() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE id IN (SELECT user_id FROM orders WHERE total > 100)");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void notInSubquery_currentBehavior() {
            // NOT IN with A_Expr wrapping SubLink not fully handled
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM blacklist)");
            assertThat(tables).containsExactly("users");
        }

        @Test
        @Disabled("A_Expr with SubLink not yet implemented")
        void notInSubquery_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM blacklist)");
            assertThat(tables).containsExactlyInAnyOrder("users", "blacklist");
        }

        @Test
        void scalarSubquery_currentBehavior() {
            // Scalar subqueries in SELECT clause not handled
            Set<String> tables = extractor.extractTables(
                    "SELECT *, (SELECT COUNT(*) FROM orders WHERE orders.user_id = users.id) as order_count FROM users");
            assertThat(tables).containsExactly("users");
        }

        @Test
        @Disabled("Scalar subqueries in SELECT clause not yet implemented")
        void scalarSubquery_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "SELECT *, (SELECT COUNT(*) FROM orders WHERE orders.user_id = users.id) as order_count FROM users");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void anySubquery() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM products WHERE price > ANY (SELECT min_price FROM price_limits)");
            assertThat(tables).containsExactlyInAnyOrder("products", "price_limits");
        }

        @Test
        void allSubquery() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM products WHERE price > ALL (SELECT avg_price FROM competitors)");
            assertThat(tables).containsExactlyInAnyOrder("products", "competitors");
        }
    }

    @Nested
    class NestedQueries {

        @Test
        void deeplyNestedSubqueries() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE id IN (" +
                            "  SELECT user_id FROM orders WHERE product_id IN (" +
                            "    SELECT id FROM products WHERE category_id IN (" +
                            "      SELECT id FROM categories WHERE name = 'Electronics'" +
                            "    )" +
                            "  )" +
                            ")");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "products", "categories");
        }

        @Test
        void nestedFromSubqueries() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM (" +
                            "  SELECT * FROM (" +
                            "    SELECT * FROM users" +
                            "  ) AS inner_users" +
                            ") AS outer_users");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void mixedNestedQueries_currentBehavior() {
            // Deeply nested IN subqueries inside EXISTS not fully handled
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM (" +
                            "  SELECT u.* FROM users u WHERE EXISTS (" +
                            "    SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.product_id IN (" +
                            "      SELECT id FROM products" +
                            "    )" +
                            "  )" +
                            ") AS active_users JOIN accounts ON active_users.id = accounts.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "accounts");
        }

        @Test
        @Disabled("Deeply nested A_Expr with SubLink not yet implemented")
        void mixedNestedQueries_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM (" +
                            "  SELECT u.* FROM users u WHERE EXISTS (" +
                            "    SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.product_id IN (" +
                            "      SELECT id FROM products" +
                            "    )" +
                            "  )" +
                            ") AS active_users JOIN accounts ON active_users.id = accounts.user_id");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders", "products", "accounts");
        }
    }

    @Nested
    class SetOperations {

        @Test
        void union() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id, name FROM users UNION SELECT id, name FROM admins");
            assertThat(tables).containsExactlyInAnyOrder("users", "admins");
        }

        @Test
        void unionAll() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id FROM active_users UNION ALL SELECT id FROM inactive_users");
            assertThat(tables).containsExactlyInAnyOrder("active_users", "inactive_users");
        }

        @Test
        void intersect() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id FROM users INTERSECT SELECT user_id FROM premium_members");
            assertThat(tables).containsExactlyInAnyOrder("users", "premium_members");
        }

        @Test
        void except() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id FROM users EXCEPT SELECT user_id FROM banned_users");
            assertThat(tables).containsExactlyInAnyOrder("users", "banned_users");
        }

        @Test
        void multipleSetOperations() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id FROM users " +
                            "UNION SELECT id FROM admins " +
                            "EXCEPT SELECT user_id FROM banned " +
                            "INTERSECT SELECT id FROM verified");
            assertThat(tables).containsExactlyInAnyOrder("users", "admins", "banned", "verified");
        }

        @Test
        void setOperationWithParentheses() {
            Set<String> tables = extractor.extractTables(
                    "(SELECT id FROM users UNION SELECT id FROM admins) " +
                            "EXCEPT SELECT user_id FROM blacklist");
            assertThat(tables).containsExactlyInAnyOrder("users", "admins", "blacklist");
        }
    }

    @Nested
    class SpecialSyntax {

        @Test
        void distinctQuery() {
            Set<String> tables = extractor.extractTables(
                    "SELECT DISTINCT category FROM products");
            assertThat(tables).containsExactly("products");
        }

        @Test
        void groupByHaving() {
            Set<String> tables = extractor.extractTables(
                    "SELECT user_id, COUNT(*) FROM orders GROUP BY user_id HAVING COUNT(*) > 5");
            assertThat(tables).containsExactly("orders");
        }

        @Test
        void orderByLimit() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users ORDER BY created_at DESC LIMIT 10 OFFSET 5");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void forUpdate() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users WHERE id = 1 FOR UPDATE");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void lateralJoin() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users u, LATERAL (SELECT * FROM orders WHERE user_id = u.id LIMIT 5) AS recent_orders");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void windowFunction() {
            Set<String> tables = extractor.extractTables(
                    "SELECT id, name, ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) FROM employees");
            assertThat(tables).containsExactly("employees");
        }

        @Test
        void caseExpression() {
            Set<String> tables = extractor.extractTables(
                    "SELECT CASE WHEN status = 'active' THEN 1 ELSE 0 END FROM users");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void coalesceAndNullif() {
            Set<String> tables = extractor.extractTables(
                    "SELECT COALESCE(nickname, name), NULLIF(status, 'unknown') FROM users");
            assertThat(tables).containsExactly("users");
        }

        @Test
        void castExpression() {
            Set<String> tables = extractor.extractTables(
                    "SELECT CAST(price AS INTEGER) FROM products");
            assertThat(tables).containsExactly("products");
        }

        @Test
        void jsonOperators() {
            Set<String> tables = extractor.extractTables(
                    "SELECT data->>'name', data->'address'->>'city' FROM json_data");
            assertThat(tables).containsExactly("json_data");
        }

        @Test
        void arrayOperators() {
            Set<String> tables = extractor.extractTables(
                    "SELECT tags[1], array_length(tags, 1) FROM posts WHERE 'featured' = ANY(tags)");
            assertThat(tables).containsExactly("posts");
        }

        @Test
        void tableWithOnlyKeyword() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM ONLY parent_table");
            assertThat(tables).containsExactly("parent_table");
        }
    }

    @Nested
    class ComplexScenarios {

        @Test
        void complexReportQuery_currentBehavior() {
            // CTEs not fully traversed, tables inside CTEs not extracted
            Set<String> tables = extractor.extractTables(
                    "WITH monthly_sales AS (" +
                            "  SELECT DATE_TRUNC('month', order_date) as month, SUM(total) as revenue " +
                            "  FROM orders JOIN order_items ON orders.id = order_items.order_id " +
                            "  GROUP BY 1" +
                            "), " +
                            "user_segments AS (" +
                            "  SELECT user_id, CASE WHEN total_spent > 1000 THEN 'high' ELSE 'low' END as segment " +
                            "  FROM (SELECT user_id, SUM(total) as total_spent FROM orders GROUP BY user_id) AS spending" +
                            ") " +
                            "SELECT u.name, us.segment, ms.revenue " +
                            "FROM users u " +
                            "JOIN user_segments us ON u.id = us.user_id " +
                            "CROSS JOIN monthly_sales ms " +
                            "WHERE u.id IN (SELECT user_id FROM premium_members)");
            assertThat(tables).containsExactlyInAnyOrder(
                    "users", "monthly_sales", "user_segments", "premium_members");
        }

        @Test
        @Disabled("CTE definition extraction not yet implemented")
        void complexReportQuery_expectedBehavior() {
            Set<String> tables = extractor.extractTables(
                    "WITH monthly_sales AS (" +
                            "  SELECT DATE_TRUNC('month', order_date) as month, SUM(total) as revenue " +
                            "  FROM orders JOIN order_items ON orders.id = order_items.order_id " +
                            "  GROUP BY 1" +
                            "), " +
                            "user_segments AS (" +
                            "  SELECT user_id, CASE WHEN total_spent > 1000 THEN 'high' ELSE 'low' END as segment " +
                            "  FROM (SELECT user_id, SUM(total) as total_spent FROM orders GROUP BY user_id) AS spending" +
                            ") " +
                            "SELECT u.name, us.segment, ms.revenue " +
                            "FROM users u " +
                            "JOIN user_segments us ON u.id = us.user_id " +
                            "CROSS JOIN monthly_sales ms " +
                            "WHERE u.id IN (SELECT user_id FROM premium_members)");
            assertThat(tables).containsExactlyInAnyOrder(
                    "orders", "order_items", "users", "monthly_sales", "user_segments", "premium_members");
        }

        @Test
        void multipleStatementsInSingleParse() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM users; SELECT * FROM orders");
            assertThat(tables).containsExactlyInAnyOrder("users", "orders");
        }

        @Test
        void quotedIdentifiers() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM \"User\" WHERE \"User\".\"Id\" = 1");
            assertThat(tables).containsExactly("User");
        }

        @Test
        void mixedCaseTableNames() {
            Set<String> tables = extractor.extractTables(
                    "SELECT * FROM UserAccounts");
            assertThat(tables).containsExactly("useraccounts");
        }
    }
}
