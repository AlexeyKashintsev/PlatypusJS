/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.scripts.ole;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author vv
 */
public class ComObjectTest {

    Context cx;
    Scriptable scope;

    public ComObjectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    /**
     * Test of get method, of class ComObject.
     */
    @Ignore
    @Test
    public void testOleAutomationScripting() {
        cx = Context.enter();
        String s = null;
        try {
            scope = cx.initStandardObjects();
            s = "Logger = java.util.logging.Logger.getLogger(\"Application\");\n";
            s += "Logger.info('OLE automation scripting test');\n";
            s += "ComSession = com.eas.client.scripts.ole.ComSession;\n";
            s += "ComObject = com.eas.client.scripts.ole.ComObject;\n";
            s += "var session = new ComSession('', 'comTest', 'comTest');\n";
            s += "var excel = session.createObject('Excel.Application', 'localhost');\n";
            s += "excel.Visible = true;\n";
            s += "Logger.info('typeof(excel.Visible) >> ' + typeof(excel.Visible()));\n";
            s += "Logger.info('excel.Visible >> ' + excel.Visible());\n";
            evaluateString(s);
            assertTrue("boolean".equals(evaluateString("typeof(excel.Visible())")));
            assertTrue(Boolean.TRUE.equals(evaluateString("excel.Visible()")));

            s = "Logger.info('typeof(excel.Workbooks().Count()) >> ' + typeof(excel.Workbooks().Count()));\n";
            evaluateString(s);
            assertTrue("number".equals(evaluateString("typeof(excel.Workbooks().Count())")));
            assertTrue(Integer.valueOf(0).equals(evaluateString("excel.Workbooks().Count()")));

            s = "excel.Workbooks().Add();\n";
            s += "Logger.info('excel.Workbooks().Count() >> ' + excel.Workbooks().Count());\n";
            evaluateString(s);
            assertTrue(Integer.valueOf(1).equals(evaluateString("excel.Workbooks().Count()")));

            s = "Logger.info('excel.Workbooks()[1].Name() >> ' + excel.Workbooks()[1].Name());\n";
            evaluateString(s);
            assertTrue("string".equals(evaluateString("typeof(excel.Workbooks()[1].Name())")));

            s = "var ws1 = excel.Workbooks()[1].Worksheets()[1];\n";
            s += "ws1.Range('A1').Value = 'hello Excel';\n";
            s += "ws1.Range('A2').Value = 12345;\n";
            s += "ws1.Range('A3').Value = 'hello Excel3';\n";
            s += "ws1.Range('A4').Value = 'hello Excel4';\n";
            evaluateString(s);
            assertTrue("hello Excel".equals(evaluateString("ws1.Range('A1').Value()")));
            assertTrue(Double.valueOf(12345).equals(evaluateString("ws1.Range('A2').Value()")));
            assertTrue("hello Excel3".equals(evaluateString("ws1.Range('A3').Value()")));
            assertTrue("hello Excel4".equals(evaluateString("ws1.Range('A4').Value()")));
        } finally {
            s = "session.destroy();\n";
            evaluateString(s);
            Context.exit();
        }
    }

    private Object evaluateString(String str) {
        return cx.evaluateString(scope, str, "<cmd>", 1, null);
    }
}