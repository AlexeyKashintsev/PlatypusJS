/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.sqldrivers.resolvers;

import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
import com.eas.client.SQLUtils;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mg
 */
public class PostgreTypesResolver implements TypesResolver {

    protected static final Map<Integer, String> jdbcTypes2RdbmsTypes = new HashMap<>();
    protected static final Map<String, Integer> rdbmsTypes2JdbcTypes = new HashMap<>();
    protected static final Set<String> gisTypes = new HashSet<>();
    protected static final Set<Integer> jdbcTypesWithSize = new HashSet<>();
    protected static final Set<Integer> jdbcTypesWithScale = new HashSet<>();

    static {

        // gis types
        gisTypes.add("point");
        gisTypes.add("line");
        gisTypes.add("lseg");
        gisTypes.add("box");
        gisTypes.add("path");
        gisTypes.add("polygon");
        gisTypes.add("circle");


        // rdbms -> jdbc
        rdbmsTypes2JdbcTypes.put("decimal", Types.DECIMAL);
        //-7
        rdbmsTypes2JdbcTypes.put("bit", Types.BIT);
        //-7  ->  16 
        rdbmsTypes2JdbcTypes.put("bool", Types.BOOLEAN);
        rdbmsTypes2JdbcTypes.put("boolean", Types.BOOLEAN);
        //-5
        rdbmsTypes2JdbcTypes.put("int8", Types.BIGINT);
        rdbmsTypes2JdbcTypes.put("bigint", Types.BIGINT);
        rdbmsTypes2JdbcTypes.put("bigserial", Types.BIGINT);
        rdbmsTypes2JdbcTypes.put("oid", Types.BIGINT);
        //-2
//???        rdbmsTypes2JdbcTypes.put("bytea", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("bytea", Types.BLOB);   //???? LONGVARBINARY       TEXT-???????!!!!!!
        // 1
        rdbmsTypes2JdbcTypes.put("bpchar", Types.CHAR);
        rdbmsTypes2JdbcTypes.put("char", Types.CHAR);
        rdbmsTypes2JdbcTypes.put("character", Types.CHAR);
        // 2
        rdbmsTypes2JdbcTypes.put("numeric", Types.NUMERIC);
        // 4
        rdbmsTypes2JdbcTypes.put("integer", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("int", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("int4", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("serial", Types.INTEGER);
        // 5
        rdbmsTypes2JdbcTypes.put("smallint", Types.SMALLINT);
        rdbmsTypes2JdbcTypes.put("int2", Types.SMALLINT);
        // 7
        rdbmsTypes2JdbcTypes.put("real", Types.REAL);
        rdbmsTypes2JdbcTypes.put("float4", Types.REAL);
        // 8
        rdbmsTypes2JdbcTypes.put("double precision", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("float", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("float8", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("money", Types.DOUBLE);
        // 12
        rdbmsTypes2JdbcTypes.put("varchar", Types.VARCHAR);
        rdbmsTypes2JdbcTypes.put("character varying", Types.VARCHAR);
        rdbmsTypes2JdbcTypes.put("name", Types.VARCHAR);
        // 12  ->  -1
//        rdbmsTypes2JdbcTypes.put("text", Types.LONGVARCHAR); //????????????!!!!!!!!
        rdbmsTypes2JdbcTypes.put("text", Types.CLOB);
        // 91
        rdbmsTypes2JdbcTypes.put("date", Types.DATE);
        // 92
        rdbmsTypes2JdbcTypes.put("time", Types.TIME);
        rdbmsTypes2JdbcTypes.put("timetz", Types.TIME);
        rdbmsTypes2JdbcTypes.put("time with time zone", Types.TIME);
        rdbmsTypes2JdbcTypes.put("time without time zone", Types.TIME);
        // 93
        rdbmsTypes2JdbcTypes.put("timestamp", Types.TIMESTAMP);
        rdbmsTypes2JdbcTypes.put("timestamptz", Types.TIMESTAMP);
        rdbmsTypes2JdbcTypes.put("timestamp with time zone", Types.TIMESTAMP);
        rdbmsTypes2JdbcTypes.put("timestamp without time zone", Types.TIMESTAMP);

        for (String typeName : gisTypes) {
            rdbmsTypes2JdbcTypes.put(typeName, Types.OTHER);
        }


        // jdbc -> rdbms
        jdbcTypes2RdbmsTypes.put(Types.BIT, "bit");
        jdbcTypes2RdbmsTypes.put(Types.TINYINT, "smallint");
        jdbcTypes2RdbmsTypes.put(Types.SMALLINT, "smallint");
        jdbcTypes2RdbmsTypes.put(Types.INTEGER, "integer");
        jdbcTypes2RdbmsTypes.put(Types.BIGINT, "bigint");
        jdbcTypes2RdbmsTypes.put(Types.FLOAT, "float");
        jdbcTypes2RdbmsTypes.put(Types.REAL, "real");
        jdbcTypes2RdbmsTypes.put(Types.DOUBLE, "double precision");
        jdbcTypes2RdbmsTypes.put(Types.NUMERIC, "numeric");
        jdbcTypes2RdbmsTypes.put(Types.DECIMAL, "decimal");
        jdbcTypes2RdbmsTypes.put(Types.CHAR, "char");
        jdbcTypes2RdbmsTypes.put(Types.VARCHAR, "varchar");
        jdbcTypes2RdbmsTypes.put(Types.LONGVARCHAR, "text");
        jdbcTypes2RdbmsTypes.put(Types.DATE, "date");
        jdbcTypes2RdbmsTypes.put(Types.TIME, "time");
        jdbcTypes2RdbmsTypes.put(Types.TIMESTAMP, "timestamp");
        jdbcTypes2RdbmsTypes.put(Types.BINARY, "bytea");
        jdbcTypes2RdbmsTypes.put(Types.VARBINARY, "bytea");
        jdbcTypes2RdbmsTypes.put(Types.LONGVARBINARY, "bytea");
        jdbcTypes2RdbmsTypes.put(Types.BLOB, "bytea");
        jdbcTypes2RdbmsTypes.put(Types.CLOB, "text");
        jdbcTypes2RdbmsTypes.put(Types.REF, "refcursor");
        jdbcTypes2RdbmsTypes.put(Types.BOOLEAN, "boolean");
        jdbcTypes2RdbmsTypes.put(Types.NCHAR, "char");
        jdbcTypes2RdbmsTypes.put(Types.NVARCHAR, "varchar");
        jdbcTypes2RdbmsTypes.put(Types.LONGNVARCHAR, "text");
        jdbcTypes2RdbmsTypes.put(Types.NCLOB, "text");

        //typeName(M,D)
        jdbcTypesWithScale.add(Types.DECIMAL);
        
        //typeName(M)
        jdbcTypesWithSize.add(Types.CHAR);
        jdbcTypesWithSize.add(Types.VARCHAR);
        jdbcTypesWithSize.add(Types.NUMERIC);
        jdbcTypesWithSize.add(Types.DECIMAL);
        
        
    }

    @Override
    public int getJdbcTypeByRDBMSTypename(String aTypeName) {
        Integer jdbcType = (aTypeName != null ? rdbmsTypes2JdbcTypes.get(aTypeName.toLowerCase()) : null);
        if (jdbcType == null) {
            jdbcType = Types.OTHER;
        }
        return jdbcType;
    }

    @Override
    public Set<Integer> getSupportedJdbcDataTypes() {
        Set<Integer> supportedTypes = new HashSet<>();
        supportedTypes.addAll(rdbmsTypes2JdbcTypes.values());
        return supportedTypes;
    }

    @Override
    public void resolve2RDBMS(Field aField) {
        DataTypeInfo typeInfo = aField.getTypeInfo();
        if (typeInfo == null) {
            typeInfo = DataTypeInfo.VARCHAR;
            Logger.getLogger(PostgreTypesResolver.class.getName()).log(Level.SEVERE, "sql jdbc type {0} have no mapping to rdbms type. substituting with string type (Varchar)", new Object[]{aField.getTypeInfo().getSqlType()});
        }
        DataTypeInfo copyTypeInfo = typeInfo.copy();
        String sqlTypeName = jdbcTypes2RdbmsTypes.get(typeInfo.getSqlType());
        if (sqlTypeName != null) {
            String sqlTypeNameLower = sqlTypeName.toLowerCase();
            copyTypeInfo.setSqlType(getJdbcTypeByRDBMSTypename(sqlTypeName));
            copyTypeInfo.setSqlTypeName(sqlTypeNameLower);
            copyTypeInfo.setJavaClassName(typeInfo.getJavaClassName());
        }
        aField.setTypeInfo(copyTypeInfo);
    }

    @Override
    public void resolve2Application(Field aField) {
        if (aField != null) {
            int lSize = aField.getSize();
            int size = lSize >> 16;
            int scale = (lSize << 16) >> 16;
            if (SQLUtils.isSameTypeGroup(aField.getTypeInfo().getSqlType(), java.sql.Types.VARCHAR)) {
                if (scale > 0) {
                    aField.setSize(scale);
                } else {
                    aField.setSize(0);
                }
                aField.setScale(0);
                aField.setPrecision(0);
            } else {
                if (size > 0) {
                    aField.setSize(size);
                } else {
                    aField.setSize(0);
                }
                if (scale > 0) {
                    aField.setScale(scale);
                    aField.setPrecision(scale);
                } else {
                    aField.setScale(0);
                    aField.setPrecision(0);
                }
            }
        }
    }

    @Override
    public boolean isGeometryTypeName(String aTypeName) {
        return (aTypeName != null ? gisTypes.contains(aTypeName.toLowerCase()) : false);
    }
    
    @Override
    public boolean isSized(Integer aSqlType)   
    {
        return jdbcTypesWithSize.contains(aSqlType);
    }        

    @Override
    public boolean isScaled(Integer aSqlType)   
    {
        return jdbcTypesWithScale.contains(aSqlType);
    }        
    
}