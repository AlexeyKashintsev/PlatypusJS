/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.serial;

import com.eas.client.serial.JsonReader;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.Parameters;
import com.eas.client.queries.Query;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 *
 * @author mg
 */
public class QueryReader extends JsonReader{

    private static final String TITLE_PROP_NAME = "title";
    private static final String APP_ELEMENT_PROP_NAME = "appelement";
    private static final String PARAMETERS_PROP_NAME = "parameters";
    private static final String FIELDS_PROP_NAME = "fields";

    public static Query read(JSONValue aValue) throws Exception {
        JSONObject o = aValue.isObject();
        assert o != null;
        Query query = new Query();
        query.setTitle(o.get(TITLE_PROP_NAME).isString().stringValue());
        query.setEntityId(o.get(APP_ELEMENT_PROP_NAME).isString().stringValue());
        // parameters
        assert o.containsKey(PARAMETERS_PROP_NAME);
        JSONArray pa = o.get(PARAMETERS_PROP_NAME).isArray();
        assert pa != null;
        Parameters params = new Parameters();
        readFields(pa, params);
        query.setParameters(params);
        // fields
        assert o.containsKey(FIELDS_PROP_NAME);
        JSONArray fa = o.get(FIELDS_PROP_NAME).isArray();
        assert fa != null;
        Fields fields = new Fields();
        readFields(fa, fields);
        query.setFields(fields);
        return query;
    }
}