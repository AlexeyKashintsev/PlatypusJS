/* Datamodel license.
 * Exclusive rights on this code in any form
 * are belong to it's author. This code was
 * developed for commercial purposes only. 
 * For any questions and any actions with this
 * code in any form you have to contact to it's
 * author.
 * All rights reserved.
 */
package com.eas.client.sqldrivers;

import com.bearsoft.rowset.Converter;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.ForeignKeySpec;
import com.bearsoft.rowset.metadata.PrimaryKeySpec;
import com.eas.client.ClientConstants;
import com.eas.client.SQLUtils;
import com.eas.client.metadata.DbTableIndexSpec;
import com.eas.client.settings.SettingsConstants;
import com.eas.client.sqldrivers.resolvers.TypesResolver;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mg
 */
public abstract class SqlDriver {

    // символы для экранирования имен схем,таблиц,колонок,индексов и т.д.
    private String leftCharForWrap = "\"";
    private String rightCharForWrap = "\"";
    private String[] wrappedChars = new String[]{" "};
    // error codes
    protected static final String EAS_TABLE_ALREADY_EXISTS = "EAS_TABLE_ALREADY_EXISTS";
    protected static final String EAS_TABLE_DOESNT_EXISTS = "EAS_TABLE_DOESNT_EXISTS";
    // misc
    protected static final String EAS_SQL_SCRIPT_DELIMITER = "#GO";
    public static final String DROP_FIELD_SQL_PREFIX = "alter table %s drop column ";
    public static final String ADD_FIELD_SQL_PREFIX = "alter table %s add ";

    public SqlDriver() {
        super();
    }

    public void initializeApplicationSchema(Connection aConnection) throws Exception {
        if (!checkApplicationSchemaInitialized(aConnection)) {
            String scriptText = readInitScriptResource();
            applyScript(scriptText, aConnection);
        }
    }

    /**
     * *
     * The database supports deferrable constraints to enable constrains check
     * on transaction commit.
     *
     * @return true if constraints is deferrable
     */
    public abstract boolean isConstraintsDeferrable();

    /**
     * *
     * Gets converter to convert some value of any compatible class to value of
     * predefined class, according to sql type.
     *
     * @return converter instance
     */
    public abstract Converter getConverter();

    /**
     * *
     * Gets type resolver to convert SQL types to JDBC types and vice-versa.
     *
     * @return TypesResolver instance
     */
    public abstract TypesResolver getTypesResolver();

    /**
     * *
     * Gets database initial script location and file name.
     *
     * @return
     */
    public abstract String getApplicationSchemaInitResourceName();

    /**
     * Returns subset of jdbc types, supported by particular database. The trick
     * is that database uses own identifiers for it's types and we need an extra
     * abstraction level.
     *
     * @return Subset of jdbc types, supported by particular database.
     */
    public abstract Set<Integer> getSupportedJdbcDataTypes();

    /**
     * *
     * Sets current schema for current session.
     *
     * @param aConnection JDBC connection
     * @param aSchema Schema name
     * @throws Exception Exception in the case of operation failure
     */
    public abstract void applyContextToConnection(Connection aConnection, String aSchema) throws Exception;

    /**
     * Gets current schema for connection
     *
     * @param aConnection JDBC connection
     * @return Schema name
     * @throws Exception Exception Exception in the case of operation failure
     */
    public String getConnectionContext(Connection aConnection) throws Exception {
        try (PreparedStatement stmt = aConnection.prepareStatement(getSql4GetConnectionContext())) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getString(1);
        }
    }

    protected abstract String getSql4GetConnectionContext();

    /**
     * Returns sql query text, usable for enumerating tables in particular
     * schema
     *
     * @param schema4Sql Schema name. If this parameter is null sql for all
     * tables in all schemas will be returned.
     * @return Sql query text
     */
    public abstract String getSql4TablesEnumeration(String schema4Sql);

    /**
     * Returns sql text for retriving schemas list.
     *
     * @return Sql text.
     */
    public abstract String getSql4SchemasEnumeration();

    /**
     * Returns sql text for create new schema.
     *
     * @param aSchemaName schema name
     * @param aPassword owner password, required for some databases (Oracle)
     * @return Sql text.
     */
    public abstract String getSql4CreateSchema(String aSchemaName, String aPassword);

    /**
     * Returns sql query text for getting columns metadata for tables. TODO:
     * Implement result set fields description.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4TableColumns(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql query text for getting primary keys metadata for tables.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4TablePrimaryKeys(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql query text for getting foreign keys metadata for tables.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4TableForeignKeys(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql query text for getting column comments metadata for tables.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4ColumnsComments(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql query text for getting indexes metadata for tables.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4Indexes(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql clause array to set column's comment. Eeach sql clause from
     * array executed consequentially
     *
     * @param aOwnerName Schema name
     * @param aTableName Table name
     * @param aFieldName Column name
     * @param aDescription Comment
     * @return Sql texts array
     */
    public abstract String[] getSql4CreateColumnComment(String aOwnerName, String aTableName, String aFieldName, String aDescription);

    /**
     * *
     * Returns sql query text for getting comments metadata for tables.
     *
     * @param aOwnerName Schema name
     * @param aTableNames Tables names set
     * @return
     */
    public abstract String getSql4TableComments(String aOwnerName, Set<String> aTableNames);

    /**
     * *
     * Returns sql clause to set table's comment.
     *
     * @param aOwnerName Schema name
     * @param aTableName Table name
     * @param aDescription Comment
     * @return Sql text
     */
    public abstract String getSql4CreateTableComment(String aOwnerName, String aTableName, String aDescription);

    /**
     * *
     * Finds column name from comment's rowset on current cursor position.
     *
     * @param rs Comments rowset
     * @return Column name
     * @throws RowsetException Rowset exception
     */
    public abstract String getColumnNameFromCommentsDs(Rowset rs) throws RowsetException;

    /**
     * *
     * Finds column comment from comment's rowset on current cursor position.
     *
     * @param rs Comments rowset
     * @return Column name
     * @throws RowsetException Rowset exception
     */
    public abstract String getColumnCommentFromCommentsDs(Rowset rs) throws RowsetException;

    /**
     * *
     * Finds table name from comment's rowset on current cursor position.
     *
     * @param rs Comments rowset
     * @return Column name
     * @throws RowsetException Rowset exception
     */
    public abstract String getTableNameFromCommentsDs(Rowset rs) throws RowsetException;

    /**
     * *
     * Finds table comment from comment's rowset on current cursor position.
     *
     * @param rs Comments rowset
     * @return Column name
     * @throws RowsetException Rowset exception
     */
    public abstract String getTableCommentFromCommentsDs(Rowset rs) throws RowsetException;

    /**
     * *
     * Gets sql for getting parents nodes from MTD_ENTITIES table.
     *
     * @param aChildParamName id of the child node
     * @return sql text
     */
    public abstract String getSql4MtdEntitiesParentsList(String aChildParamName);

    /**
     * *
     * Gets sql for getting children nodes from MTD_ENTITIES table.
     *
     * @param aChildParamName id of the parent node
     * @return sql text
     */
    public abstract String getSql4MtdEntitiesChildrenList(String aParentParamName);

    /**
     * *
     * Gets sql clause for dropping the table.
     *
     * @param aSchemaName Schema name
     * @param aTableName Table name
     * @return sql text
     */
    public abstract String getSql4DropTable(String aSchemaName, String aTableName);

    /**
     * *
     * Gets sql clause for dropping the index on the table.
     *
     * @param aSchemaName Schema name
     * @param aTableName Table name
     * @param aIndexName Index name
     * @return sql text
     */
    public abstract String getSql4DropIndex(String aSchemaName, String aTableName, String aIndexName);

    /**
     * *
     * Gets sql clause for dropping the foreign key constraint.
     *
     * @param aSchemaName Schema name
     * @param aFk Foreign key specification object
     * @return Sql text
     */
    public abstract String getSql4DropFkConstraint(String aSchemaName, ForeignKeySpec aFk);

    /**
     * *
     * Gets sql clause for creating the primary key.
     *
     * @param aSchemaName Schema name
     * @param listPk Primary key columns specifications list
     * @return Sql text
     */
    public abstract String getSql4CreatePkConstraint(String aSchemaName, List<PrimaryKeySpec> listPk);

    /**
     * *
     * Gets sql clause for dropping the primary key.
     *
     * @param aSchemaName Schema name
     * @param aPk Primary key specification
     * @return Sql text
     */
    public abstract String getSql4DropPkConstraint(String aSchemaName, PrimaryKeySpec aPk);

    /**
     * *
     * Gets sql clause for creating the foreign key constraint.
     *
     * @param aSchemaName Schema name
     * @param aFk Foreign key specification
     * @return Sql text
     */
    public abstract String getSql4CreateFkConstraint(String aSchemaName, ForeignKeySpec aFk);

    /**
     * *
     * Gets sql clause for creating the foreign key constraint.
     *
     * @param aSchemaName Schema name
     * @param listFk Foreign key columns specifications list
     * @return Sql text
     */
    public abstract String getSql4CreateFkConstraint(String aSchemaName, List<ForeignKeySpec> listFk);

    /**
     * *
     * Gets sql clause for creating the index
     *
     * @param aSchemaName Schema name
     * @param aTableName Table name
     * @param aIndex Index specification
     * @return Sql text
     */
    public abstract String getSql4CreateIndex(String aSchemaName, String aTableName, DbTableIndexSpec aIndex);

    /**
     * *
     * Gets sql clause for creating an empty table.
     *
     * @param aSchemaName Schema name
     * @param aTableName Table name
     * @param aPkFieldName Column name for primary key
     * @return Sql text
     */
    public abstract String getSql4EmptyTableCreation(String aSchemaName, String aTableName, String aPkFieldName);

    /**
     * *
     * Gets specific exception message.
     *
     * @param ex Exception
     * @return Exception message
     */
    public abstract String parseException(Exception ex);

    /**
     * Generates Sql string fragment for field definition, according to specific
     * features of particular database. If it meets any strange type, such
     * java.sql.Types.OTHER or java.sql.Types.STRUCT, it uses the field's type
     * name.
     *
     * @param aField A field information to deal with.
     * @return Sql string for field definition
     */
    public abstract String getSql4FieldDefinition(Field aField);

    /**
     * Generates sql texts array for dropping a field. Sql clauses from array
     * will execute consequentially
     *
     * @param aTableName Name of a table the field to dropped from.
     * @param aFieldName Field name to drop
     * @return Sql string generted.
     */
    public String[] getSql4DroppingField(String aTableName, String aFieldName) {
        return new String[]{
                    String.format(DROP_FIELD_SQL_PREFIX, aTableName) + aFieldName
                };
    }

    /**
     * Generates Sql string to modify a field, according to specific features of
     * particular database. If it meats any strange type, such
     * java.sql.Types.OTHER or java.sql.Types.STRUCT, it uses the field's type
     * name.
     *
     * @param aTableName Name of the table with that field
     * @param aOldFieldMd A field information to migrate from.
     * @param aNewFieldMd A field information to migrate to.
     * @return Sql array string for field modification.
     */
    public abstract String[] getSqls4ModifyingField(String aTableName, Field aOldFieldMd, Field aNewFieldMd);

    /**
     * *
     * Generates Sql string to rename a field, according to specific features of
     * particular database.
     *
     * @param aTableName Table name
     * @param aOldFieldName Old column name
     * @param aNewFieldMd New field
     * @return Sql array string for field modification.
     */
    public abstract String[] getSqls4RenamingField(String aTableName, String aOldFieldName, Field aNewFieldMd);

    /**
     * Converts JDBC type to specific database type
     *
     * @param aLowLevelTypeName Specific database name
     * @return JDBC type
     */
    public abstract Integer getJdbcTypeByRDBMSTypename(String aLowLevelTypeName);

    public static void applyScript(String scriptText, Connection aConnection) throws Exception {
        String[] commandsTexts = scriptText.split(EAS_SQL_SCRIPT_DELIMITER);
        if (commandsTexts != null) {
            try (Statement stmt = aConnection.createStatement()) {
                for (int i = 0; i < commandsTexts.length; i++) {
                    try {
                        String queryText = commandsTexts[i];
                        queryText = queryText.replace('\r', ' ');
                        queryText = queryText.replace('\n', ' ');
                        if (!queryText.isEmpty()) {
                            stmt.execute(queryText);
                            aConnection.commit();
                        }
                    } catch (Exception ex) {
                        aConnection.rollback();
                        Logger.getLogger(SqlDriver.class.getName()).log(Level.WARNING, "Error applying SQL script. {0}", ex.getMessage());       
                    }
                }
            }
        }
    }

    private boolean checkApplicationSchemaInitialized(Connection aConnection) {
        try {
            try (PreparedStatement stmt = aConnection.prepareStatement(String.format(SQLUtils.SQL_MAX_COMMON_BY_FIELD, ClientConstants.F_MDENT_ID, ClientConstants.F_MDENT_ID, ClientConstants.T_MTD_ENTITIES))) {
                ResultSet res = stmt.executeQuery();
                res.close();
            }
            return true;
        } catch (SQLException ex) {
            try {
                aConnection.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(SqlDriver.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(SqlDriver.class.getName()).log(Level.SEVERE, "Application schema seems to be uninitialized. {0}", ex.getMessage());
        }
        return false;
    }

    private String readInitScriptResource() throws IOException {
        String resName = getApplicationSchemaInitResourceName();
        return readScriptResource(resName);
    }

    protected String readScriptResource(String resName) throws IOException {
        try (InputStream is = SqlDriver.class.getResourceAsStream(resName)) {
            byte[] data = new byte[is.available()];
            is.read(data);
            return new String(data, SettingsConstants.COMMON_ENCODING);
        }
    }

    protected String constructIn(Set<String> strings) {
        String tablesIn = "";
        for (String lString : strings) {
            if (tablesIn.isEmpty()) {
                tablesIn = "'" + lString + "'";
            } else {
                tablesIn += ", '" + lString + "'";
            }
        }
        return tablesIn;
    }

    /**
     * *
     * Wrapping names containing restricted symbols.
     *
     * @param aName Name to wrap
     * @return Wrapped text
     */
    public String wrapName(String aName) {
        if (aName != null && !aName.isEmpty()
                && !aName.startsWith(leftCharForWrap) && !aName.endsWith(rightCharForWrap)) {
            if (wrappedChars != null && wrappedChars.length > 0) {
                for (String s : wrappedChars) {
                    if (aName.indexOf(s) >= 0) {
                        return leftCharForWrap + aName + rightCharForWrap;
                    }
                }
            }
            return aName;
        } else {
            return aName;
        }
    }

    /**
     * *
     * Sets wrapping symbols
     *
     * @param aLeftChar Left char
     * @param aRightChar Right char
     * @param aWrappedSpecChars Array of chars to wrap
     */
    public void setWrap(String aLeftChar, String aRightChar, String[] aWrappedSpecChars) {
        leftCharForWrap = aLeftChar;
        rightCharForWrap = aRightChar;
        wrappedChars = aWrappedSpecChars;
    }
}