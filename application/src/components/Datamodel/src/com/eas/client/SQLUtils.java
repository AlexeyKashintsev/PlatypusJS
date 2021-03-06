/* Datamodel license.
 * Exclusive rights on this code in any form
 * are belong to it's author. This code was
 * developed for commercial purposes only. 
 * For any questions and any actions with this
 * code in any form you have to contact to it's
 * author.
 * All rights reserved.
 */
package com.eas.client;

import com.eas.client.sqldrivers.SqlDriver;
import com.eas.client.sqldrivers.H2SqlDriver;
import com.eas.client.sqldrivers.Db2SqlDriver;
import com.eas.client.sqldrivers.MySqlSqlDriver;
import com.eas.client.sqldrivers.MsSqlSqlDriver;
import com.eas.client.sqldrivers.OracleSqlDriver;
import com.eas.client.sqldrivers.PostgreSqlDriver;
import com.eas.client.metadata.Fields;
import com.eas.client.sqldrivers.GenericSqlDriver;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mg
 */
public class SQLUtils {

    public static final String TABLE_NAME_2_SQL = "select * from %s";
    public static final String PARAMETER_NAME_REGEXP = ":{1}([A-za-z]\\w*\\b)";
    public static final String PROPERTIES_VALUE_REGEXP = "={1}([A-za-z]\\w+\\b)";
    public static final String SQL_SELECT_COMMON_WHERE_BY_FIELD = "select * from %s where %s.%s = :%s";
    public static final String SQL_SELECT_COMMON_WHERE_ISNULL_FIELD = "select * from %s where %s.%s is null";
    public static final String SQL_PARAMETER_FIELD_VALUE = "fieldValue";
    public static final String SQL_UPDATE_COMMON_WHERE_BY_FIELD = "update %s set %s = %s where %s.%s = :" + SQL_PARAMETER_FIELD_VALUE;
    public static final String SQL_UPDATE2_COMMON_WHERE_BY_FIELD = "update %s set %s = %s, %s = %s where %s.%s = :" + SQL_PARAMETER_FIELD_VALUE;
    public static final String SQL_UPDATE3_COMMON_WHERE_BY_FIELD = "update %s set %s = %s, %s = %s, %s = %s where %s.%s = :" + SQL_PARAMETER_FIELD_VALUE;
    public static final String SQL_UPDATE4_COMMON_WHERE_BY_FIELD = "update %s set %s = %s, %s = %s, %s = %s, %s = %s where %s.%s = :" + SQL_PARAMETER_FIELD_VALUE;
    public static final String SQL_DELETE_COMMON_WHERE_BY_FIELD = "delete from %s where %s.%s = :" + SQL_PARAMETER_FIELD_VALUE;
    public static final String SQL_INSERT_COMMON_ID_FIELD = "insert into %s columns = (%s) values = ( :" + SQL_PARAMETER_FIELD_VALUE + ")";
    public static final String SQL_MAX_COMMON_BY_FIELD = "select max(%s) %s from %s";

    public static String dialectByUrl(String aJdbcUrl) {
        String dialect = null;
        if (aJdbcUrl != null) {
            aJdbcUrl = aJdbcUrl.toLowerCase();
            if (aJdbcUrl.contains("jdbc:oracle")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_ORACLE_DIALECT;
            } else if (aJdbcUrl.contains("jdbc:jtds:sqlserver")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_MSSQL_DIALECT;
            } else if (aJdbcUrl.contains("jdbc:postgre")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_POSTGRE_DIALECT;
            } else if (aJdbcUrl.contains("jdbc:db2")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_DB2_DIALECT;
            } else if (aJdbcUrl.contains("jdbc:mysql")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_MYSQL_DIALECT;
            } else if (aJdbcUrl.contains("jdbc:h2")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_H2_DIALECT;
            }
        }
        return dialect;
    }

    public static String dialectByProductName(String aName) {
        String dialect = null;
        if (aName != null) {
            aName = aName.toLowerCase();
            if (aName.contains("oracle")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_ORACLE_DIALECT;
            } else if (aName.contains("microsoft")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_MSSQL_DIALECT;
            } else if (aName.contains("postgre")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_POSTGRE_DIALECT;
            } else if (aName.contains("db2")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_DB2_DIALECT;
            } else if (aName.contains("mysql")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_MYSQL_DIALECT;
            } else if (aName.contains("h2")) { //NOI18N
                dialect = ClientConstants.SERVER_PROPERTY_H2_DIALECT;
            }
        }
        return dialect;
    }

    public static SqlDriver getSqlDriver(String aDialect) {
        if (ClientConstants.SERVER_PROPERTY_ORACLE_DIALECT.equalsIgnoreCase(aDialect)) {
            return new OracleSqlDriver();
        } else if (ClientConstants.SERVER_PROPERTY_MSSQL_DIALECT.equalsIgnoreCase(aDialect)) {
            return new MsSqlSqlDriver();
        } else if (ClientConstants.SERVER_PROPERTY_POSTGRE_DIALECT.equalsIgnoreCase(aDialect)) {
            return new PostgreSqlDriver();
        } else if (ClientConstants.SERVER_PROPERTY_MYSQL_DIALECT.equalsIgnoreCase(aDialect)) {
            return new MySqlSqlDriver();
        } else if (ClientConstants.SERVER_PROPERTY_DB2_DIALECT.equalsIgnoreCase(aDialect)) {
            return new Db2SqlDriver();
        } else if (ClientConstants.SERVER_PROPERTY_H2_DIALECT.equalsIgnoreCase(aDialect)) {
            return new H2SqlDriver();
        } else {
            return new GenericSqlDriver();
        }
    }

    public static SqlQuery validateTableSqlQuery(String aTableDatasource, String tableName, String tableSchemaName, DatabasesClient aClient) throws Exception {
        return validateTableSqlQuery(aTableDatasource, tableName, tableSchemaName, aClient, true);
    }

    public static SqlQuery validateTableSqlQuery(String aTableDatasource, String aTableName, String aTableSchemaName, DatabasesClient aClient, boolean forceMdQuery) throws Exception {
        String fullTableName = aTableName;
        if (aTableSchemaName != null && !aTableSchemaName.isEmpty()) {
            fullTableName = aTableSchemaName + "." + fullTableName;
        }
        SqlQuery query = new SqlQuery(aClient, aTableDatasource, SQLUtils.makeQueryByTableName(fullTableName));
        MetadataCache mdCache = aClient.getMetadataCache(aTableDatasource);
        if (mdCache != null) {
            Fields tableFields = forceMdQuery || mdCache.containsTableMetadata(fullTableName) ? mdCache.getTableMetadata(fullTableName) : null;
            if (tableFields != null) {
                query.setFields(tableFields.copy());
            } else {
                throw new Exception("Table " + fullTableName + " doesn't exist. Datasource: " + aTableDatasource);
            }
        }
        return query;
    }

    public static String extractSchemaName(String aName) {
        int indexOfDot = aName.indexOf('.');
        if (indexOfDot != -1) {
            return aName.substring(0, indexOfDot);
        }
        return "";
    }

    public static String extractTableName(String aName) {
        int indexOfDot = aName.indexOf('.');
        if (indexOfDot != -1) {
            return aName.substring(indexOfDot + 1);
        }
        return aName;
    }

    public static String getLocalizedPkName() {
        return DbLocalizations.getString("PRIMARY_KEY");
    }

    public static String getLocalizedFkName() {
        return DbLocalizations.getString("FOREIGN_KEY");
    }

    public static String makeQueryByTableName(String aTableName) {
        return String.format(TABLE_NAME_2_SQL, aTableName);
    }
    /*
     public static String makeTableNameMetadataQuery(String aTableName) {
     return makeQueryMetadataQuery(makeQueryByTableName(aTableName));
     }
    
     public static final String SQL_FALSE_CONDITION = " where 1=0";
     public static final String SQL_2_METADTA_TAIL = "t01010101" + SQL_FALSE_CONDITION;
     public static final String SQL_2_METADTA = "select * from ( %s ) " + SQL_2_METADTA_TAIL;
    
     public static String makeQueryMetadataQuery(String sql) {
     if (sql != null && !sql.isEmpty()) {
     if (!sql.endsWith(SQL_2_METADTA_TAIL)) {
     String lsql = sql.toLowerCase().replaceAll("[\n\r]", "");
     if (lsql.matches(".+\\bwhere\\b.+")
     // complex queries
     || lsql.matches(".+\\border\\b.+")
     || lsql.matches(".+\\bgroup\\b.+")
     || lsql.matches(".+\\bconnect\\b.+")) {
     return String.format(SQL_2_METADTA, sql);
     } else // simple queries
     {
     return sql + SQL_FALSE_CONDITION;
     }
     } else {// bypass
     return sql;
     }
     }
     return "";
     }
     */
    public static final ResourceBundle DbLocalizations = ResourceBundle.getBundle("com/eas/client/DbLocalizations");

    public static String clob2String(Clob source) {
        if (source != null) {
            try {
                return source.getSubString(1, (int) source.length());
            } catch (SQLException sqlEx) {
                Logger.getLogger(SQLUtils.class.getName()).log(Level.SEVERE, null, sqlEx);
            }
        }
        return null;
    }

    public static SqlQuery constructQueryByTableName(DatabasesClient aClient, String aTableName) {
        if (aTableName != null) {
            SqlQuery query = new SqlQuery(aClient, SQLUtils.makeQueryByTableName(aTableName));
            return query;
        }
        return null;
    }
}
