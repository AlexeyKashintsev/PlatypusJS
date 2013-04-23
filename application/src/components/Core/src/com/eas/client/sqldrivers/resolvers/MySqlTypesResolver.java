/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.sqldrivers.resolvers;

import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
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
public class MySqlTypesResolver implements TypesResolver {

    protected static final Map<Integer, String> jdbcTypes2RdbmsTypes = new HashMap<>();
    protected static final Map<String, Integer> rdbmsTypes2JdbcTypes = new HashMap<>();
    protected static final Set<String> gisTypes = new HashSet<>();
    protected static final Set<Integer> jdbcTypesWithSize = new HashSet<>();
    protected static final Set<Integer> jdbcTypesWithScale = new HashSet<>();
    protected static final int MAXIMUM_NUMBERS_PRECISION = 65;

    static {

        // gis types
        gisTypes.add("point");
        gisTypes.add("linestring");
        gisTypes.add("polygon");
        gisTypes.add("geometry");
        gisTypes.add("multipoint");
        gisTypes.add("multilinestring");
        gisTypes.add("multipolygon");
        gisTypes.add("geometrycollection");

        // rdbms -> jdbc
        rdbmsTypes2JdbcTypes.put("tinyint", Types.TINYINT);
        rdbmsTypes2JdbcTypes.put("bool", Types.TINYINT);
        rdbmsTypes2JdbcTypes.put("boolean", Types.TINYINT);
        rdbmsTypes2JdbcTypes.put("smallint", Types.SMALLINT);
        rdbmsTypes2JdbcTypes.put("mediumint", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("int", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("integer", Types.INTEGER);
        rdbmsTypes2JdbcTypes.put("bigint", Types.BIGINT);
        rdbmsTypes2JdbcTypes.put("serial", Types.BIGINT);
        rdbmsTypes2JdbcTypes.put("bit", Types.BIT);
        rdbmsTypes2JdbcTypes.put("float", Types.REAL);
        rdbmsTypes2JdbcTypes.put("real", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("double", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("double precision", Types.DOUBLE);
        rdbmsTypes2JdbcTypes.put("decimal", Types.DECIMAL);
        rdbmsTypes2JdbcTypes.put("dec", Types.DECIMAL);
        rdbmsTypes2JdbcTypes.put("numeric", Types.DECIMAL);
        rdbmsTypes2JdbcTypes.put("char", Types.CHAR);
        rdbmsTypes2JdbcTypes.put("varchar", Types.VARCHAR);
        rdbmsTypes2JdbcTypes.put("tinytext", Types.LONGVARCHAR);
        rdbmsTypes2JdbcTypes.put("text", Types.LONGVARCHAR);
        rdbmsTypes2JdbcTypes.put("mediumtext", Types.LONGVARCHAR);
        rdbmsTypes2JdbcTypes.put("longtext", Types.LONGVARCHAR);
        rdbmsTypes2JdbcTypes.put("binary", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("varbinary", Types.VARBINARY);
        rdbmsTypes2JdbcTypes.put("tinyblob", Types.VARBINARY);
        rdbmsTypes2JdbcTypes.put("blob", Types.LONGVARBINARY);
        rdbmsTypes2JdbcTypes.put("mediumblob", Types.LONGVARBINARY);
        rdbmsTypes2JdbcTypes.put("longblob", Types.LONGVARBINARY);
        rdbmsTypes2JdbcTypes.put("date", Types.DATE);
        rdbmsTypes2JdbcTypes.put("time", Types.TIME);
        rdbmsTypes2JdbcTypes.put("year", Types.DATE);
        rdbmsTypes2JdbcTypes.put("datetime", Types.TIMESTAMP);
        rdbmsTypes2JdbcTypes.put("timestamp", Types.TIMESTAMP);
        rdbmsTypes2JdbcTypes.put("point", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("linestring", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("polygon", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("geometry", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("multipoint", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("multilinestring", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("multipolygon", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("geometrycollection", Types.BINARY);
        rdbmsTypes2JdbcTypes.put("enum", Types.CHAR);
        rdbmsTypes2JdbcTypes.put("set", Types.CHAR);

        // jdbc -> rdbms
        jdbcTypes2RdbmsTypes.put(Types.BIT, "bit");
        jdbcTypes2RdbmsTypes.put(Types.TINYINT, "tinyint");
        jdbcTypes2RdbmsTypes.put(Types.SMALLINT, "smallint");
        jdbcTypes2RdbmsTypes.put(Types.INTEGER, "int");
        jdbcTypes2RdbmsTypes.put(Types.BIGINT, "bigint");
        jdbcTypes2RdbmsTypes.put(Types.FLOAT, "float");
        jdbcTypes2RdbmsTypes.put(Types.REAL, "float");
        jdbcTypes2RdbmsTypes.put(Types.DOUBLE, "double");
        jdbcTypes2RdbmsTypes.put(Types.NUMERIC, "decimal");
        jdbcTypes2RdbmsTypes.put(Types.DECIMAL, "decimal");
        jdbcTypes2RdbmsTypes.put(Types.CHAR, "char");
        jdbcTypes2RdbmsTypes.put(Types.VARCHAR, "varchar");
        jdbcTypes2RdbmsTypes.put(Types.LONGVARCHAR, "longtext");
        jdbcTypes2RdbmsTypes.put(Types.DATE, "date");
        jdbcTypes2RdbmsTypes.put(Types.TIME, "time");
        jdbcTypes2RdbmsTypes.put(Types.TIMESTAMP, "timestamp");
        jdbcTypes2RdbmsTypes.put(Types.BINARY, "binary");
        jdbcTypes2RdbmsTypes.put(Types.VARBINARY, "varbinary");
        jdbcTypes2RdbmsTypes.put(Types.LONGVARBINARY, "longblob");
        jdbcTypes2RdbmsTypes.put(Types.BLOB, "longblob");
        jdbcTypes2RdbmsTypes.put(Types.CLOB, "longtext");
        jdbcTypes2RdbmsTypes.put(Types.BOOLEAN, "int");
        jdbcTypes2RdbmsTypes.put(Types.NCHAR, "char");
        jdbcTypes2RdbmsTypes.put(Types.NVARCHAR, "varchar");
        jdbcTypes2RdbmsTypes.put(Types.LONGNVARCHAR, "longtext");
        jdbcTypes2RdbmsTypes.put(Types.NCLOB, "longtext");
        jdbcTypes2RdbmsTypes.put(Types.SQLXML, "longtext");

        //typeName(M,D)
        jdbcTypesWithScale.add(Types.FLOAT);
        jdbcTypesWithScale.add(Types.REAL);
        jdbcTypesWithScale.add(Types.DOUBLE);
        jdbcTypesWithScale.add(Types.NUMERIC);
        jdbcTypesWithScale.add(Types.DECIMAL);

        //typeName(M)
        jdbcTypesWithSize.add(Types.FLOAT);
        jdbcTypesWithSize.add(Types.REAL);
        jdbcTypesWithSize.add(Types.DOUBLE);
        jdbcTypesWithSize.add(Types.NUMERIC);
        jdbcTypesWithSize.add(Types.DECIMAL);
        //jdbcTypesWithSize.add(Types.BIT);
        //jdbcTypesWithSize.add(Types.TINYINT);
        //jdbcTypesWithSize.add(Types.SMALLINT);
        //jdbcTypesWithSize.add(Types.INTEGER);
        //jdbcTypesWithSize.add(Types.BIGINT);
        jdbcTypesWithSize.add(Types.CHAR);
        jdbcTypesWithSize.add(Types.VARCHAR);
        jdbcTypesWithSize.add(Types.NCHAR);
        jdbcTypesWithSize.add(Types.NVARCHAR);
        jdbcTypesWithSize.add(Types.VARBINARY);
        jdbcTypesWithSize.add(Types.BINARY);
    }

    @Override
    public void resolve2RDBMS(Field aField) {
        DataTypeInfo typeInfo = aField.getTypeInfo();
        if (typeInfo == null) {
            typeInfo = DataTypeInfo.VARCHAR;
            Logger.getLogger(MySqlTypesResolver.class.getName()).log(Level.SEVERE, "sql jdbc type {0} have no mapping to rdbms type. substituting with string type (Varchar)", new Object[]{aField.getTypeInfo().getSqlType()});
        }
        DataTypeInfo copyTypeInfo = typeInfo.copy();
        String sqlTypeName = jdbcTypes2RdbmsTypes.get(typeInfo.getSqlType());
        if (sqlTypeName != null) {
            copyTypeInfo.setSqlType(getJdbcTypeByRDBMSTypename(sqlTypeName));
            copyTypeInfo.setSqlTypeName(sqlTypeName.toLowerCase());
            copyTypeInfo.setJavaClassName(typeInfo.getJavaClassName());
        }
        aField.setTypeInfo(copyTypeInfo);
        if (aField.getSize() > MAXIMUM_NUMBERS_PRECISION) {// MySql treats size as presion in error messages
            aField.setSize(MAXIMUM_NUMBERS_PRECISION);
        }
    }

    @Override
    public void resolve2Application(Field aField) {
    }

    @Override
    public boolean isGeometryTypeName(String aTypeName) {
        return (aTypeName != null ? gisTypes.contains(aTypeName.toLowerCase()) : false);
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
    public boolean isSized(Integer aSqlType) {
        return jdbcTypesWithSize.contains(aSqlType);
    }

    @Override
    public boolean isScaled(Integer aSqlType) {
        return jdbcTypesWithScale.contains(aSqlType);
    }
}