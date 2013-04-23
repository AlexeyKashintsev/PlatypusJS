/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.queries;

import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.eas.client.SQLUtils;
import com.eas.client.exceptions.UnboundSqlParameterException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author pk
 */
public class SqlQueryTest {

    public static final String PARAM2_VALUE = "qwerty";
    private static final String TWO_PARAMS_QUERY = "select * from ATABLE where FIELD1 > :param1 and FIELD2 = :param2 or FIELD1 < :param1";

    public SqlQueryTest() {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreation() {
        SqlQuery b = new SqlQuery();
        assertNull(b.getSqlText());
        assertTrue(b.getParametersBinds().isEmpty());
        b.setSqlText(TWO_PARAMS_QUERY);
        assertEquals(b.getSqlText(), TWO_PARAMS_QUERY);
        assertTrue(b.getParametersBinds().isEmpty());
        b.putParameter("param1", DataTypeInfo.INTEGER, 1);
        b.putParameter("param2", DataTypeInfo.VARCHAR, PARAM2_VALUE);
        assertEquals(2, b.getParameters().getParametersCount());
    }

    @Test
    public void testCompiling() throws UnboundSqlParameterException, Exception {
        SqlQuery b = new SqlQuery();
        b.setSqlText(TWO_PARAMS_QUERY);
        b.putParameter("param1", DataTypeInfo.INTEGER, 1);
        b.putParameter("param2", DataTypeInfo.VARCHAR, PARAM2_VALUE);
        SqlCompiledQuery q = b.compile();
        assertEquals(q.getSqlClause(), "select * from ATABLE where FIELD1 > ? and FIELD2 = ? or FIELD1 < ?");
        assertEquals(3, q.getParameters().getParametersCount());
        assertEquals(java.sql.Types.INTEGER, q.getParameters().get(1).getTypeInfo().getSqlType());
        assertEquals(1, q.getParameters().get(1).getValue());
        assertEquals(java.sql.Types.VARCHAR, q.getParameters().get(2).getTypeInfo().getSqlType());
        assertEquals(PARAM2_VALUE, q.getParameters().get(2).getValue());
        assertEquals(java.sql.Types.INTEGER, q.getParameters().get(3).getTypeInfo().getSqlType());
        assertEquals(1, q.getParameters().get(3).getValue());
    }

    @Test
    public void testCheckingForSubQuery() {
        String a = "(T_Q125051387971687_1.LAST_OP_TIME IS NOT NULL)", b = "Q125051387971687_1", c = "Q125051387971687";
        Pattern pattern = Pattern.compile(SQLUtils.SUBQUERY_TABLE_NAME_REGEXP);
        assertFalse(pattern.matcher(a).matches());
        assertFalse(pattern.matcher(b).matches());
        assertTrue(pattern.matcher(c).matches());
    }
}