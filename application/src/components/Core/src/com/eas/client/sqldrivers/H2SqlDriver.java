package com.eas.client.sqldrivers;

import com.bearsoft.rowset.Converter;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.ForeignKeySpec;
import com.bearsoft.rowset.metadata.PrimaryKeySpec;
import com.eas.client.ClientConstants;
import com.eas.client.metadata.DbTableIndexColumnSpec;
import com.eas.client.metadata.DbTableIndexSpec;
import com.eas.client.sqldrivers.converters.H2Converter;
import com.eas.client.sqldrivers.resolvers.H2TypesResolver;
import com.eas.client.sqldrivers.resolvers.TypesResolver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author vv
 */
public class H2SqlDriver extends SqlDriver {

    protected Converter converter = new H2Converter();
    protected TypesResolver resolver = new H2TypesResolver();
    protected static final int[] h2ErrorCodes = {};
    protected static final String[] platypusErrorMessages = {};
    protected static final String SET_SCHEMA_CLAUSE = "SET SCHEMA %s";
    protected static final String GET_SCHEMA_CLAUSE = "SELECT SCHEMA()";
    protected static final String CREATE_SCHEMA_CLAUSE = "CREATE SCHEMA IF NOT EXISTS %s";
    protected static final String SQL_SCHEMAS = ""
            + "SELECT " + ClientConstants.JDBCCOLS_TABLE_SCHEM + " FROM "
            + "(SELECT SCHEMA_NAME AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + " FROM INFORMATION_SCHEMA.SCHEMATA) schemas_alias "
            + "ORDER BY " + ClientConstants.JDBCCOLS_TABLE_SCHEM;
    protected static final String SQL_TABLES_VIEWS = ""
            + "SELECT"
            + "  " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCPKS_TABLE_TYPE_FIELD_NAME + " "
            + "FROM"
            + "("
            + "(SELECT"
            + "  TABLE_NAME AS " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  TABLE_SCHEMA AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "'TABLE' AS " + ClientConstants.JDBCPKS_TABLE_TYPE_FIELD_NAME + " "
            + "FROM  INFORMATION_SCHEMA.TABLES tables) "
            + "UNION"
            + "(SELECT"
            + "  TABLE_NAME AS " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  TABLE_SCHEMA AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "'VIEW' AS " + ClientConstants.JDBCPKS_TABLE_TYPE_FIELD_NAME + " "
            + "FROM  INFORMATION_SCHEMA.VIEWS views)"
            + ") tables_views_alias ";
    protected static final String SQL_ALL_TABLES_VIEWS = SQL_TABLES_VIEWS
            + "ORDER BY " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ", " + ClientConstants.JDBCCOLS_TABLE_NAME + " ";
    protected static final String SQL_SCHEMA_TABLES_VIEWS = SQL_TABLES_VIEWS
            + "WHERE UPPER(" + ClientConstants.JDBCCOLS_TABLE_SCHEM + ") = UPPER('%s') "
            + "ORDER BY " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ", " + ClientConstants.JDBCCOLS_TABLE_NAME + " ";
    protected static final String SQL_COLUMNS = ""
            + "SELECT"
            + "  TABLE_CAT,"
            + "  " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  " + ClientConstants.JDBCCOLS_COLUMN_NAME + ","
            + "  " + ClientConstants.JDBCCOLS_TYPE_NAME + ","
            + "  " + ClientConstants.JDBCCOLS_DATA_TYPE + ","
            + "  " + ClientConstants.JDBCCOLS_COLUMN_SIZE + ","
            + "  " + ClientConstants.JDBCCOLS_NULLABLE + ","
            + "  " + ClientConstants.JDBCCOLS_DECIMAL_DIGITS + ","
            + "  " + ClientConstants.JDBCCOLS_NUM_PREC_RADIX + ","
            + "  " + ClientConstants.JDBCIDX_ORDINAL_POSITION + ","
            + "  " + ClientConstants.JDBCCOLS_REMARKS + ","
            + "  COLUMN_DEFAULT_VALUE "
            + "FROM"
            + "("
            + "SELECT"
            + "  table_catalog AS TABLE_CAT,"
            + "  table_schema AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  table_name AS " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  column_name AS " + ClientConstants.JDBCCOLS_COLUMN_NAME + ","
            + "  data_type AS " + ClientConstants.JDBCCOLS_DATA_TYPE + ","
            + "  type_name AS " + ClientConstants.JDBCCOLS_TYPE_NAME + ","
            + "  CHARACTER_MAXIMUM_LENGTH AS " + ClientConstants.JDBCCOLS_COLUMN_SIZE + ","
            + "  (CASE is_nullable WHEN 'YES'  then 1 else 0 end) AS " + ClientConstants.JDBCCOLS_NULLABLE + ","
            + "  numeric_scale AS " + ClientConstants.JDBCCOLS_DECIMAL_DIGITS + ","
            + "  10 AS " + ClientConstants.JDBCCOLS_NUM_PREC_RADIX + ","
            + "  ordinal_position AS " + ClientConstants.JDBCIDX_ORDINAL_POSITION + ","
            + "  REMARKS AS " + ClientConstants.JDBCCOLS_REMARKS + ","
            + "  column_default AS COLUMN_DEFAULT_VALUE "
            + "FROM information_schema.columns "
            + "WHERE UPPER(table_schema) = UPPER('%s') AND table_name in (%s) "
            + "ORDER BY table_schema, table_name, ordinal_position"
            + ") columns_alias";
    protected static final String SQL_ALL_OWNER_TABLES_COMMENTS = ""
            + "SELECT"
            + "  " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  " + ClientConstants.F_TABLE_COMMENTS_NAME_FIELD_NAME + ","
            + "  " + ClientConstants.F_TABLE_COMMENTS_COMMENT_FIELD_NAME + " "
            + "FROM"
            + "("
            + "SELECT"
            + "  table_schema AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  table_name AS " + ClientConstants.F_TABLE_COMMENTS_NAME_FIELD_NAME + ","
            + "  REMARKS AS " + ClientConstants.F_TABLE_COMMENTS_COMMENT_FIELD_NAME + " "
            + "FROM  information_schema.tables "
            + ") table_comments_alias "
            + "WHERE "
            + "  UPPER(" + ClientConstants.JDBCCOLS_TABLE_SCHEM + ") = UPPER('%s') ";
    protected static final String SQL_TABLE_COMMENTS = SQL_ALL_OWNER_TABLES_COMMENTS
            + "AND " + ClientConstants.JDBCCOLS_TABLE_NAME + " in (%s)";
    protected static final String SQL_COLUMNS_COMMENTS = ""
            + "SELECT"
            + "  " + ClientConstants.F_COLUMNS_COMMENTS_COMMENT_FIELD_NAME + ","
            + "  " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  " + ClientConstants.F_COLUMNS_COMMENTS_FIELD_FIELD_NAME + " "
            + "FROM"
            + "("
            + "SELECT"
            + "  REMARKS AS " + ClientConstants.F_COLUMNS_COMMENTS_COMMENT_FIELD_NAME + ","
            + "  table_schema AS " + ClientConstants.JDBCCOLS_TABLE_SCHEM + ","
            + "  table_name AS " + ClientConstants.JDBCCOLS_TABLE_NAME + ","
            + "  column_name AS " + ClientConstants.F_COLUMNS_COMMENTS_FIELD_FIELD_NAME + " "
            + "FROM information_schema.columns "
            + "WHERE UPPER(table_schema) = UPPER('%s') AND table_name in (%s) "
            + "ORDER BY table_schema, table_name, ordinal_position"
            + ") column_comments_alias";
    protected static final String SQL_INDEX_KEYS = ""
            + "SELECT"
            + "  TABLE_CAT,"
            + "  " + ClientConstants.JDBCIDX_TABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCIDX_TABLE_NAME + ","
            + "  " + ClientConstants.JDBCIDX_NON_UNIQUE + ","
            + "  " + ClientConstants.JDBCIDX_INDEX_QUALIFIER + ","
            + "  " + ClientConstants.JDBCIDX_INDEX_NAME + ","
            + "  " + ClientConstants.JDBCIDX_TYPE + ","
            + "  " + ClientConstants.JDBCIDX_ORDINAL_POSITION + ","
            + "  " + ClientConstants.JDBCIDX_COLUMN_NAME + ","
            + "  " + ClientConstants.JDBCIDX_ASC_OR_DESC + ","
            + "  CARDINALITY, "
            + "  PAGES, "
            + "  FILTER_CONDITION  "
            + "FROM"
            + "("
            + "SELECT"
            + "  table_catalog AS TABLE_CAT,"
            + "  table_schema AS " + ClientConstants.JDBCIDX_TABLE_SCHEM + ","
            + "  table_name AS " + ClientConstants.JDBCIDX_TABLE_NAME + ","
            + "  CASE WHEN " + ClientConstants.JDBCIDX_NON_UNIQUE + " = TRUE"
            + "     THEN 1 ELSE 0 END AS " + ClientConstants.JDBCIDX_NON_UNIQUE + ","
            + "  NULL AS " + ClientConstants.JDBCIDX_INDEX_QUALIFIER + ","
            + "  index_name as " + ClientConstants.JDBCIDX_INDEX_NAME + ","
            + "  INDEX_TYPE AS " + ClientConstants.JDBCIDX_TYPE + ","
            + "  ORDINAL_POSITION AS " + ClientConstants.JDBCIDX_ORDINAL_POSITION + ","
            + "  column_name AS " + ClientConstants.JDBCIDX_COLUMN_NAME + ","
            + "  ASC_OR_DESC AS " + ClientConstants.JDBCIDX_ASC_OR_DESC + ","
            + "  cardinality AS CARDINALITY,"
            + "  PAGES,"
            + "  FILTER_CONDITION "
            + "FROM INFORMATION_SCHEMA.INDEXES "
            + "WHERE UPPER(table_schema) = UPPER('%s') AND table_name in (%s) "
            + "ORDER BY non_unique, index_name, ORDINAL_POSITION "
            + ") indexes_alias";
    protected static final String SQL_PRIMARY_KEYS = ""
            + "SELECT"
            + "  TABLE_CAT,"
            + "  " + ClientConstants.JDBCPKS_TABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCPKS_TABLE_NAME + ","
            + "  " + ClientConstants.JDBCPKS_COLUMN_NAME + ","
            + "  KEY_SEQ,"
            + "  " + ClientConstants.JDBCPKS_CONSTRAINT_NAME + " "
            + "FROM"
            + "("
            + "SELECT"
            + "  t.table_catalog AS TABLE_CAT,"
            + "  t.table_schema AS " + ClientConstants.JDBCPKS_TABLE_SCHEM + ","
            + "  t.table_name AS " + ClientConstants.JDBCPKS_TABLE_NAME + ","
            + "  t.column_name AS " + ClientConstants.JDBCPKS_COLUMN_NAME + ","
            + "  t.ordinal_position AS KEY_SEQ,"
            + "  t.INDEX_NAME AS " + ClientConstants.JDBCPKS_CONSTRAINT_NAME + " "
            + "FROM  INFORMATION_SCHEMA.INDEXES t "
            + "WHERE"
            + "  t.PRIMARY_KEY = 'TRUE' AND"
            + "  UPPER(t.table_schema) = UPPER('%s') AND t.table_name in (%s) "
            + "ORDER BY t.table_catalog, t.table_schema, t.table_name, t.ordinal_position"
            + ") pkeys_alias";
    protected static final String SQL_FOREIGN_KEYS = ""
            + "SELECT"
            + "  PKTABLE_CAT,"
            + "  " + ClientConstants.JDBCFKS_FKPKTABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCFKS_FKPKTABLE_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKPK_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKPKCOLUMN_NAME + ","
            + "  FKTABLE_CAT,"
            + "  " + ClientConstants.JDBCFKS_FKTABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCFKS_FKTABLE_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FK_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKCOLUMN_NAME + ","
            + "  KEY_SEQ,"
            + "  " + ClientConstants.JDBCFKS_FKUPDATE_RULE + ","
            + "  " + ClientConstants.JDBCFKS_FKDELETE_RULE + ","
            + "  " + ClientConstants.JDBCFKS_FKDEFERRABILITY + " "
            + "FROM"
            + "("
            + "SELECT"
            + "  r.pktable_catalog AS PKTABLE_CAT,"
            + "  r.PKTABLE_SCHEMA AS " + ClientConstants.JDBCFKS_FKPKTABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCFKS_FKPKTABLE_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKPK_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKPKCOLUMN_NAME + ","
            + "  r.fktable_catalog AS FKTABLE_CAT,"
            + "  r.FKTABLE_SCHEMA AS " + ClientConstants.JDBCFKS_FKTABLE_SCHEM + ","
            + "  " + ClientConstants.JDBCFKS_FKTABLE_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FK_NAME + ","
            + "  " + ClientConstants.JDBCFKS_FKCOLUMN_NAME + ","
            + "  ordinal_position AS KEY_SEQ,"
            + "  r.update_rule AS " + ClientConstants.JDBCFKS_FKUPDATE_RULE + ","// 0=CASCADE; 1=RESTRICT | NO ACTION; 2=SET NULL 
            + "  r.delete_rule AS " + ClientConstants.JDBCFKS_FKDELETE_RULE + ","// 0=CASCADE; 1=RESTRICT | NO ACTION; 2=SET NULL
            + "  7 AS " + ClientConstants.JDBCFKS_FKDEFERRABILITY + " "// 5- , 6- , 7- not aplicable
            + "FROM"
            + "  INFORMATION_SCHEMA.CROSS_REFERENCES  r "
            + "WHERE"
            + "  UPPER(r.pktable_schema)  = UPPER('%s') AND r.pktable_name in (%s) "
            + "ORDER BY r.pktable_catalog, r.pktable_schema, r.pktable_name, r.ordinal_position"
            + ") fkeys_alias";
    protected static final String SQL_CREATE_EMPTY_TABLE = ""
            + "CREATE TABLE %s (%s DECIMAL(18,0) NOT NULL PRIMARY KEY)";
    protected static final String SQL_CREATE_TABLE_COMMENT = ""
            + "COMMENT ON TABLE %s IS '%s'";
    protected static final String SQL_CREATE_COLUMN_COMMENT = ""
            + "COMMENT ON COLUMN %s IS '%s'";
    protected static final String SQL_DROP_TABLE = ""
            + "DROP TABLE %s";
    protected static final String SQL_CREATE_INDEX = ""
            + "CREATE %s INDEX %s ON %s (%s)";
    protected static final String SQL_DROP_INDEX = ""
            + "DROP INDEX %s";
    protected static final String SQL_ADD_PK = ""
            + "ALTER TABLE %s ADD %s PRIMARY KEY (%s)";
    protected static final String SQL_DROP_PK = ""
            + "ALTER TABLE %s DROP PRIMARY KEY";
    protected static final String SQL_ADD_FK = ""
            + "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s) %s";
    protected static final String SQL_DROP_FK = ""
            + "ALTER TABLE %s DROP CONSTRAINT %s";
    protected static final String SQL_PARENTS_LIST = ""
            + "WITH RECURSIVE parents(mdent_id, mdent_parent_id) AS "
            + "( "
            + "SELECT m1.mdent_id, m1.mdent_parent_id FROM mtd_entities m1 WHERE m1.mdent_id = %s "
            + "    UNION ALL "
            + "SELECT m2.mdent_id, m2.mdent_parent_id FROM parents p, mtd_entities m2 WHERE m2.mdent_id = p.mdent_parent_id "
            + ") "
            + "SELECT mdent_id, mdent_parent_id FROM parents";
    protected static final String SQL_CHILDREN_LIST = ""
            + "WITH recursive children(mdent_id, mdent_name, mdent_parent_id, mdent_type, mdent_content_txt, mdent_content_txt_size, mdent_content_txt_crc32) AS"
            + "( "
            + "SELECT m1.mdent_id, m1.mdent_name, m1.mdent_parent_id, m1.mdent_type, m1.mdent_content_txt, m1.mdent_content_txt_size, m1.mdent_content_txt_crc32 FROM mtd_entities m1 WHERE m1.mdent_id = :%s "
            + "    union all "
            + "SELECT m2.mdent_id, m2.mdent_name, m2.mdent_parent_id, m2.mdent_type, m2.mdent_content_txt, m2.mdent_content_txt_size, m2.mdent_content_txt_crc32 FROM children c, mtd_entities m2 WHERE c.mdent_id = m2.mdent_parent_id "
            + ") "
            + "SELECT mdent_id, mdent_name, mdent_parent_id, mdent_type, mdent_content_txt, mdent_content_txt_size, mdent_content_txt_crc32 FROM children";
    protected static final String SQL_RENAME_COLUMN = ""
            + "ALTER TABLE %s ALTER COLUMN %s RENAME TO %s";
    protected static final String SQL_CHANGE_COLUMN_TYPE = ""
            + "ALTER TABLE %s ALTER COLUMN %s %s";
    protected static final String SQL_CHANGE_COLUMN_NULLABLE = ""
            + "ALTER TABLE %s ALTER COLUMN %s SET %s NULL";

    public H2SqlDriver() {
        super();
        setWrap("`", "`", new String[]{" "});
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isConstraintsDeferrable() {
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Converter getConverter() {
        return converter;
    }

    /**
     * @inheritDoc
     */
    @Override
    public TypesResolver getTypesResolver() {
        return resolver;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getApplicationSchemaInitResourceName() {
        return "/" + H2SqlDriver.class.getPackage().getName().replace(".", "/") + "/sqlscripts/H2InitSchema.sql";
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Integer> getSupportedJdbcDataTypes() {
        return resolver.getSupportedJdbcDataTypes();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void applyContextToConnection(Connection aConnection, String aSchema) throws Exception {
        if (aSchema != null && !aSchema.isEmpty()) {
            try (Statement stmt = aConnection.createStatement()) {
                stmt.execute(String.format(SET_SCHEMA_CLAUSE, wrapName(aSchema)));
            }
        }
    }

    @Override
    protected String getSql4GetConnectionContext() {
        return GET_SCHEMA_CLAUSE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4TablesEnumeration(String schema4Sql) {
        if (schema4Sql == null) {
            return SQL_ALL_TABLES_VIEWS;
        } else {
            return String.format(SQL_SCHEMA_TABLES_VIEWS, schema4Sql);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4SchemasEnumeration() {
        return SQL_SCHEMAS;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreateSchema(String aSchemaName, String aPassword) {
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            return String.format(CREATE_SCHEMA_CLAUSE, aSchemaName);
        }
        throw new IllegalArgumentException("Schema name is null or empty.");
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4TableColumns(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            String tablesIn = constructIn(aTableNames);
            return String.format(SQL_COLUMNS, aOwnerName, tablesIn.toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4TablePrimaryKeys(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            String tablesIn = constructIn(aTableNames);
            return String.format(SQL_PRIMARY_KEYS, aOwnerName, tablesIn.toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4TableForeignKeys(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            return String.format(SQL_FOREIGN_KEYS, aOwnerName, constructIn(aTableNames).toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4ColumnsComments(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            return String.format(SQL_COLUMNS_COMMENTS, aOwnerName, constructIn(aTableNames).toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4Indexes(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            String tablesIn = constructIn(aTableNames);
            return String.format(SQL_INDEX_KEYS, aOwnerName, tablesIn.toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getSql4CreateColumnComment(String aOwnerName, String aTableName, String aFieldName, String aDescription) {
        String fullName = wrapName(aTableName) + "." + wrapName(aFieldName);
        if (aOwnerName != null && !aOwnerName.isEmpty()) {
            fullName = wrapName(aOwnerName) + "." + fullName;
        }
        if (aDescription == null) {
            aDescription = "";
        }
        return new String[]{String.format(SQL_CREATE_COLUMN_COMMENT, fullName, escapeSingleQuote(aDescription))};
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4TableComments(String aOwnerName, Set<String> aTableNames) {
        if (aTableNames != null && !aTableNames.isEmpty()) {
            return String.format(SQL_TABLE_COMMENTS, aOwnerName, constructIn(aTableNames).toUpperCase());
        } else {
            return String.format(SQL_ALL_OWNER_TABLES_COMMENTS, aOwnerName);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreateTableComment(String aOwnerName, String aTableName, String aDescription) {
        String fullName = wrapName(aTableName);
        if (aOwnerName != null && !aOwnerName.isEmpty()) {
            fullName = wrapName(aOwnerName) + "." + fullName;
        }
        if (aDescription == null) {
            aDescription = "";
        }
        return String.format(SQL_CREATE_TABLE_COMMENT, fullName, escapeSingleQuote(aDescription));
    }

    private String escapeSingleQuote(String str) {
        return str.replaceAll("'", "''"); //NOI18N
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getColumnNameFromCommentsDs(Rowset rs) throws RowsetException {
        if (!rs.isAfterLast() && !rs.isBeforeFirst()) {
            return (String) rs.getObject(rs.getFields().find(ClientConstants.F_COLUMNS_COMMENTS_FIELD_FIELD_NAME));
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getColumnCommentFromCommentsDs(Rowset rs) throws RowsetException {
        if (!rs.isAfterLast() && !rs.isBeforeFirst()) {
            return (String) rs.getObject(rs.getFields().find(ClientConstants.F_COLUMNS_COMMENTS_COMMENT_FIELD_NAME));
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getTableNameFromCommentsDs(Rowset rs) throws RowsetException {
        if (!rs.isAfterLast() && !rs.isBeforeFirst()) {
            return (String) rs.getObject(rs.getFields().find(ClientConstants.F_TABLE_COMMENTS_NAME_FIELD_NAME));
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getTableCommentFromCommentsDs(Rowset rs) throws RowsetException {
        if (!rs.isAfterLast() && !rs.isBeforeFirst()) {
            return (String) rs.getObject(rs.getFields().find(ClientConstants.F_TABLE_COMMENTS_COMMENT_FIELD_NAME));
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4MtdEntitiesParentsList(String aChildParamName) {
        return String.format(SQL_PARENTS_LIST, aChildParamName);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4MtdEntitiesChildrenList(String aParentParamName) {
        return String.format(SQL_CHILDREN_LIST, aParentParamName);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4DropTable(String aSchemaName, String aTableName) {
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            return String.format(SQL_DROP_TABLE, wrapName(aSchemaName) + "." + wrapName(aTableName));
        } else {
            return String.format(SQL_DROP_TABLE, wrapName(aTableName));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4DropIndex(String aSchemaName, String aTableName, String aIndexName) {
        aIndexName = wrapName(aIndexName);
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            aIndexName = wrapName(aSchemaName) + "." + aIndexName;
        }
        return String.format(SQL_DROP_INDEX, wrapName(aIndexName));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4DropFkConstraint(String aSchemaName, ForeignKeySpec aFk) {
        String constraintName = wrapName(aFk.getCName());
        String leftTableFullName = wrapName(aFk.getTable());
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            leftTableFullName = wrapName(aSchemaName) + "." + leftTableFullName;
        }
        return String.format(SQL_DROP_FK, leftTableFullName, constraintName);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreatePkConstraint(String aSchemaName, List<PrimaryKeySpec> listPk) {

        if (listPk != null && listPk.size() > 0) {
            PrimaryKeySpec pk = listPk.get(0);
            String pkTableName = wrapName(pk.getTable());
            String pkName = pk.getCName();
            String pkColumnName = wrapName(pk.getField());
            for (int i = 1; i < listPk.size(); i++) {
                pk = listPk.get(i);
                pkColumnName += ", " + wrapName(pk.getField());
            }
            if (aSchemaName != null && !aSchemaName.isEmpty()) {
                pkTableName = wrapName(aSchemaName) + "." + pkTableName;
            }
            return String.format(SQL_ADD_PK, pkTableName, (pkName.isEmpty() ? "" : "CONSTRAINT " + wrapName(pkName)), pkColumnName);
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4DropPkConstraint(String aSchemaName, PrimaryKeySpec aPk) {
        String pkTableName = wrapName(aPk.getTable());
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            pkTableName = wrapName(aSchemaName) + "." + pkTableName;
        }
        return String.format(SQL_DROP_PK, pkTableName);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreateFkConstraint(String aSchemaName, ForeignKeySpec aFk) {
        List<ForeignKeySpec> fkList = new ArrayList();
        fkList.add(aFk);
        return getSql4CreateFkConstraint(aSchemaName, fkList);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreateFkConstraint(String aSchemaName, List<ForeignKeySpec> listFk) {
        if (listFk != null && listFk.size() > 0) {
            ForeignKeySpec fk = listFk.get(0);
            String fkTableName = wrapName(fk.getTable());
            String fkName = fk.getCName();
            String fkColumnName = wrapName(fk.getField());

            PrimaryKeySpec pk = fk.getReferee();
            String pkSchemaName = pk.getSchema();
            String pkTableName = wrapName(pk.getTable());
            String pkColumnName = wrapName(pk.getField());

            for (int i = 1; i < listFk.size(); i++) {
                fk = listFk.get(i);
                pk = fk.getReferee();
                fkColumnName += ", " + wrapName(fk.getField());
                pkColumnName += ", " + wrapName(pk.getField());
            }

            String fkRule = "";
            switch (fk.getFkUpdateRule()) {
                case CASCADE:
                    fkRule += " ON UPDATE CASCADE";
                    break;
                case NOACTION:
                    fkRule += " ON UPDATE NO ACTION";
                    break;
                case SETDEFAULT:
                    fkRule += " ON UPDATE SET DEFAULT";
                    break;
                case SETNULL:
                    fkRule += " ON UPDATE SET NULL";
                    break;
            }
            switch (fk.getFkDeleteRule()) {
                case CASCADE:
                    fkRule += " ON DELETE CASCADE";
                    break;
                case NOACTION:
                    fkRule += " ON DELETE NO ACTION";
                    break;
                case SETDEFAULT:
                    fkRule += " ON DELETE SET DEFAULT";
                    break;
                case SETNULL:
                    fkRule += " ON DELETE SET NULL";
                    break;
            }
            if (aSchemaName != null && !aSchemaName.isEmpty()) {
                fkTableName = wrapName(aSchemaName) + "." + fkTableName;
            }
            if (pkSchemaName != null && !pkSchemaName.isEmpty()) {
                pkTableName = wrapName(pkSchemaName) + "." + pkTableName;
            }

            return String.format(SQL_ADD_FK, fkTableName, fkName.isEmpty() ? "" : wrapName(fkName), fkColumnName, pkTableName, pkColumnName, fkRule);
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4CreateIndex(String aSchemaName, String aTableName, DbTableIndexSpec aIndex) {
        assert aIndex.getColumns().size() > 0 : "index definition must consist of at least 1 column";

        String tableName = wrapName(aTableName);
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            tableName = wrapName(aSchemaName) + "." + tableName;
        }
        String fieldsList = "";
        for (int i = 0; i < aIndex.getColumns().size(); i++) {
            DbTableIndexColumnSpec column = aIndex.getColumns().get(i);
            fieldsList += wrapName(column.getColumnName());
            if (!column.isAscending()) {
                fieldsList += " DESC";
            }
            if (i != aIndex.getColumns().size() - 1) {
                fieldsList += ", ";
            }
        }
        return String.format(SQL_CREATE_INDEX,
                (aIndex.isUnique() ? "UNIQUE " : "") + (aIndex.isHashed() ? "HASH " : ""),
                wrapName(aIndex.getName()),
                tableName,
                fieldsList);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4EmptyTableCreation(String aSchemaName, String aTableName, String aPkFieldName) {
        String fullName = wrapName(aTableName);
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            fullName = wrapName(aSchemaName) + "." + fullName;
        }
        return String.format(SQL_CREATE_EMPTY_TABLE, fullName, wrapName(aPkFieldName));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String parseException(Exception ex) {
        if (ex != null && ex instanceof SQLException) {
            SQLException sqlEx = (SQLException) ex;
            int errorCode = sqlEx.getErrorCode();
            for (int i = 0; i < h2ErrorCodes.length; i++) {
                if (errorCode == h2ErrorCodes[i]) {
                    return platypusErrorMessages[i];
                }
            }
        }
        return ex.getLocalizedMessage();
    }

    private String getFieldTypeDefinition(Field aField) {
        resolver.resolve2RDBMS(aField);
        String typeName = aField.getTypeInfo().getSqlTypeName().toLowerCase();
        int sqlType = aField.getTypeInfo().getSqlType();
        // field length
        int size = aField.getSize();
        int scale = aField.getScale();

        if (resolver.isScaled(sqlType) && size > 0) {
            typeName += "(" + String.valueOf(size) + "," + String.valueOf(scale) + ")";
        } else {
            if (resolver.isSized(sqlType) && size > 0) {
                typeName += "(" + String.valueOf(size) + ")";
            }
        }
        return typeName;

    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSql4FieldDefinition(Field aField) {
        String fieldDefinition = wrapName(aField.getName()) + " " + getFieldTypeDefinition(aField);

        if (!aField.isNullable()) {
            fieldDefinition += " NOT NULL";
        } else {
            fieldDefinition += " NULL";
        }
        if (aField.isPk()) {
            fieldDefinition += " PRIMARY KEY";
        }
        return fieldDefinition;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getSqls4ModifyingField(String aTableName, Field aOldFieldMd, Field aNewFieldMd) {
        assert aOldFieldMd.getName().toLowerCase().equals(aNewFieldMd.getName().toLowerCase());
        List<String> sql = new ArrayList<>();

        //Change data type
        String lOldTypeName = aOldFieldMd.getTypeInfo().getSqlTypeName();
        if (lOldTypeName == null) {
            lOldTypeName = "";
        }
        String lNewTypeName = aNewFieldMd.getTypeInfo().getSqlTypeName();
        if (lNewTypeName == null) {
            lNewTypeName = "";
        }
        if (aOldFieldMd.getTypeInfo().getSqlType() != aNewFieldMd.getTypeInfo().getSqlType()
                || !lOldTypeName.equalsIgnoreCase(lNewTypeName)
                || aOldFieldMd.getSize() != aNewFieldMd.getSize()
                || aOldFieldMd.getScale() != aNewFieldMd.getScale()) {
            sql.add(String.format(
                    SQL_CHANGE_COLUMN_TYPE,
                    wrapName(aTableName),
                    wrapName(aOldFieldMd.getName()),
                    getFieldTypeDefinition(aNewFieldMd)));
        }

        //Change nullable
        String not = "";
        if (aOldFieldMd.isNullable() != aNewFieldMd.isNullable()) {
            if (!aNewFieldMd.isNullable()) {
                not = "NOT";
            }
            sql.add(String.format(
                    SQL_CHANGE_COLUMN_NULLABLE,
                    wrapName(aTableName),
                    wrapName(aOldFieldMd.getName()),
                    not));
        }

        return sql.toArray(new String[0]);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getSqls4RenamingField(String aTableName, String aOldFieldName, Field aNewFieldMd) {
        String renameSQL = String.format(SQL_RENAME_COLUMN, wrapName(aTableName), wrapName(aOldFieldName), wrapName(aNewFieldMd.getName()));
        return new String[]{renameSQL};
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer getJdbcTypeByRDBMSTypename(String aLowLevelTypeName) {
        return resolver.getJdbcTypeByRDBMSTypename(aLowLevelTypeName);
    }
}