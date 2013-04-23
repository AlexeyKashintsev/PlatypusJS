/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.metadata;

import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.exceptions.InvalidColIndexException;
import com.bearsoft.rowset.exceptions.InvalidCursorPositionException;
import com.bearsoft.rowset.metadata.*;
import com.bearsoft.rowset.metadata.ForeignKeySpec.ForeignKeyRule;
import com.eas.client.*;
import com.eas.client.metadata.DbTableIndexColumnSpec;
import com.eas.client.metadata.DbTableIndexSpec;
import com.eas.client.queries.SqlCompiledQuery;
import com.eas.client.settings.DbConnectionSettings;
import com.eas.client.settings.EasSettings;
import com.eas.client.sqldrivers.OracleSqlDriver;
import com.eas.client.sqldrivers.SqlDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author vy
 */
public class MetadataSynchronizer {

    // command line switches
    public static final String CMD_SWITCHS_PREFIX = "-";
    public static final String URLFROM_CMD_SWITCH = "urlFrom";
    public static final String SCHEMAFROM_CMD_SWITCH = "schemaFrom";
    public static final String USERFROM_CMD_SWITCH = "userFrom";
    public static final String PASSWORDFROM_CMD_SWITCH = "passwordFrom";
    public static final String URLTO_CMD_SWITCH = "urlTo";
    public static final String USERTO_CMD_SWITCH = "userTo";
    public static final String SCHEMATO_CMD_SWITCH = "schemaTo";
    public static final String PASSWORDTO_CMD_SWITCH = "passwordTo";
    public static final String FILEXML_CMD_SWITCH = "file";
    public static final String LOGLEVEL_CMD_SWITCH = "logLevel";
    public static final String NOEXECUTE_CMD_SWITCH = "noExecute";
    public static final String LOGCODEPAGE_CMD_SWITCH = "logEncoding";
    public static final String NODROPTABLES_CMD_SWITCH = "noDropTables";
    public static final String LISTTABLES_CMD_SWITCH = "listTables";
    public static final String LOG_CMD_SWITCH = "";
    public static final String LOGPATH_CMD_SWITCH = "logPath";
    // variables for parameters
    private String urlFrom;
    private String userFrom;
    private String schemaFrom;
    private String passwordFrom;
    private String urlTo;
    private String userTo;
    private String schemaTo;
    private String passwordTo;
    private String fileXml;
    private Level logLevel = Level.INFO;
    private String logEncoding = "UTF-8";
    private boolean noExecute = false;
    private boolean noDropTables = false;
    private String logPath = "";
    private Set<String> listTables = new HashSet<>();
    private final String messageUsage = "\n\nRequired parameters are not defined!\n"
            + "Usage: \n"
            + "-urlFrom <value> -schemaFrom <value> -nameFrom <value> -passwordFrom <value> "
            + "-urlTo <value> -schemaTo <value> -nameTo <value> -passwordTo <value> \nor\n"
            + "-urlFrom <value> -schemaFrom <value> -nameFrom <value> -passwordFrom <value> "
            + "-file <value> \nor\n"
            + "-urlTo <value> -schemaTo <value> -nameTo <value> -passwordTo <value> "
            + "-file <value> \n\n"
            + "Add parameter: -logLevel <value> to set logging level.\n"
            + "Add parameter: -logEncoding  <value> to set loggers encoding.\n"
            + "To enable 'no execute' mode (sql generated, but not executed) add parameter: -noExecute.\n"
            + "To disable 'drop tables' commands add parameter: -noDropTables.\n"
            + "To set list tables for synchronize metadata add parameter: -listTables <values>.\n"
            + "To set path for output log-files add parameter: -logPath <values>.\n";
    public static final String SCHEMA_TAG_NAME = "schema";
    public static final String TABLES_TAG_NAME = "tables";
    public static final String TABLE_TAG_NAME = "table";
    public static final String COLUMNS_TAG_NAME = "columns";
    public static final String COLUMN_TAG_NAME = "column";
    public static final String INDEXES_TAG_NAME = "indexes";
    public static final String INDEX_TAG_NAME = "index";
    public static final String INDCOLUMN_TAG_NAME = "indColumn";
    public static final String FKEYS_TAG_NAME = "foreignKeys";
    public static final String FKEY_TAG_NAME = "foreignKey";
    public static final String FKEYCOLUMN_TAG_NAME = "fkColumn";
    public static final String PKEY_TAG_NAME = "primaryKey";
    public static final String PKEYCOLUMN_TAG_NAME = "pkColumn";
    public static final String DATABASEDIALECT_ATTR_NAME = "databaseDialect";
    public static final String NAME_ATTR_NAME = "name";
    public static final String DESCRIPTION_ATTR_NAME = "description";
    public static final String TYPE_ATTR_NAME = "type";
    public static final String TYPENAME_ATTR_NAME = "typeName";
    public static final String JAVACLASS_ATTR_NAME = "javaType";
    public static final String SIZE_ATTR_NAME = "size";
    public static final String SCALE_ATTR_NAME = "scale";
    public static final String PRECISION_ATTR_NAME = "precision";
    public static final String SIGNED_ATTR_NAME = "signed";
    public static final String NULLABLE_ATTR_NAME = "nullable";
    public static final String ISPK_ATTR_NAME = "isPk";
    public static final String POSITION_ATTR_NAME = "position";
    public static final String ASCENDING_ATTR_NAME = "ascending";
    public static final String FKNAME_ATTR_NAME = "fkName";
    public static final String FKSCHEMA_ATTR_NAME = "fkSchema";
    public static final String FKTABLE_ATTR_NAME = "fkTable";
    public static final String FKFIELD_ATTR_NAME = "fkField";
    public static final String FKDEFERRABLE_ATTR_NAME = "fkDeferrable";
    public static final String FKDELETERULE_ATTR_NAME = "fkDeleteRule";
    public static final String FKUPDATERULE_ATTR_NAME = "fkUpdateRule";
    public static final String PKNAME_ATTR_NAME = "pkName";
    public static final String PKSCHEMA_ATTR_NAME = "pkSchema";
    public static final String PKTABLE_ATTR_NAME = "pkTable";
    public static final String PKFIELD_ATTR_NAME = "pkField";
    public static final String CLUSTERED_ATTR_NAME = "clustered";
    public static final String HASHED_ATTR_NAME = "hashed";
    public static final String UNIQUE_ATTR_NAME = "unique";
    // all for loggers
    private static final String SQL_LOGGER_NAME = "sql";
    private static final String ERROR_LOGGER_NAME = "error";
    private static final String INFO_LOGGER_NAME = "info";
    private Logger sqlLogger;
    private Logger errorLogger;
    private Logger infoLogger;
    private static final Logger mdsLogger = Logger.getLogger(MetadataSynchronizer.class.getName());  
    private static final Logger mergerLogger = Logger.getLogger(MetadataMerger.class.getName());
    private static Level mdsLoggerLevel;
    private static  Level mergerLoggerLevel;
    private static  Level sqlLoggerLevel;
    private static  Level errorLoggerLevel;
    private static  Level infoLoggerLevel;
    private static  Boolean mdsLoggerUseParentHandlers;
    private static  Boolean mergerLoggerUseParentHandlers;
    private static  Boolean sqlLoggerUseParentHandlers;
    private static  Boolean errorLoggerUseParentHandlers;
    private static  Boolean infoLoggerUseParentHandlers;

    /**
     * Syncronize metadata between two schemas database
     */
    public MetadataSynchronizer() {
    }

    /**
     * Reads database meta data to the file.
     *
     * @param aClient Platypus Database client
     * @param aFileXmlPath Destination file
     * @param anOut PrintWriter for logging
     * @throws Exception
     */
    public static void readMetadataSnapshot(DbClient aClient, String aFileXmlPath, PrintWriter anOut) throws Exception {
        MetadataSynchronizer mds = new MetadataSynchronizer();
        mds.initDefaultLoggers((anOut == null?null:new PrintWriterHandler(anOut)), Level.INFO, false);
        try {
            mds.serializeMetadata(mds.readDBStructure(aClient), aFileXmlPath);
        } finally {
            mds.clearDefaultLoggers();
        }
    }

    /**
     * Applies meta data from file to the database.
     *
     * @param aClient Platypus Database client
     * @param aFileXmlPath Destination file
     * @param aLogPath output path for logging
     * @param anOut PrintWriter for logging
     * @throws Exception
     */
    public static void applyMetadataSnapshot(DbClient aClient, String aFileXmlPath, String aLogPath, PrintWriter anOut) throws Exception {
        MetadataSynchronizer mds = new MetadataSynchronizer();
        mds.initDefaultLoggers((anOut == null?null:new PrintWriterHandler(anOut)), Level.INFO, false);
        mds.setLogPath(aLogPath);

        mds.initSqlLogger(mds.logPath + "sql.log", mds.logEncoding, Level.INFO, false, new LogFormatter());
        mds.initErrorLogger(mds.logPath + "error.log", mds.logEncoding, Level.INFO, false, new LogFormatter());
        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, String.format("logPath is '%s'", mds.logPath));

        try {
            MetadataMerger metadataMerger = new MetadataMerger(aClient, mds.readDBStructureFromFile(aFileXmlPath), mds.readDBStructure(aClient), false, true, new HashSet<String>());
            metadataMerger.setSqlLogger(mds.sqlLogger);
            metadataMerger.setErrorLogger(mds.errorLogger);
            metadataMerger.run();
        } finally {
            mds.clearSqlLogger();
            mds.clearErrorLogger();
            mds.clearDefaultLoggers();
        }
    }

    /**
     * Metadata serialization
     *
     * @param dbStructure database structure
     * @param aFileXml filename for serialization
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    private void serializeMetadata(DBStructure dbStructure, String aFileXml) throws ParserConfigurationException, TransformerConfigurationException, FileNotFoundException, TransformerException {
        if (dbStructure != null && aFileXml != null && !aFileXml.isEmpty()) {
            Document doc = createDocument(dbStructure);
            File f = new File(aFileXml);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(f)));
            Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, String.format("Document saved in '%s'", aFileXml));
        }
    }

    /**
     * Create connection to DataBase
     *
     * @param aUrl url connection
     * @param aSchema schema connection
     * @param aUser database user
     * @param aPassword password for user database
     * @return connection to database
     * @throws Exception
     */
    private DbClient createClient(String aUrl, String aSchema, String aUser, String aPassword) throws Exception {
        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, String.format("Start creating connection to schema %s", aSchema));
        EasSettings settings = EasSettings.createInstance(aUrl);
        if (settings instanceof DbConnectionSettings) {
            if (aUser == null || aUser.isEmpty() || aPassword == null || aPassword.isEmpty() || aSchema == null || aSchema.isEmpty()) {
                throw new Exception(" May be bad db connection settings (url, dbuser, dbpassword, dbschema).");
            }
            settings.getInfo().put(ClientConstants.DB_CONNECTION_USER_PROP_NAME, aUser);
            settings.getInfo().put(ClientConstants.DB_CONNECTION_PASSWORD_PROP_NAME, aPassword);
            settings.getInfo().put(ClientConstants.DB_CONNECTION_SCHEMA_PROP_NAME, aSchema);
            ((DbConnectionSettings) settings).setInitSchema(false);
        }
        settings.setUrl(aUrl);
        Client lclient = ClientFactory.getInstance(settings);
        assert lclient instanceof DbClient;
        DbClient client = (DbClient) lclient;
        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, String.format("Connect to schema %s created", (client == null ? "not " : "")));
        return client;
    }

    /**
     * Runner for MetadataSyncronizer
     *
     * @throws Exception
     */
    public void run() throws Exception {
        boolean emptyFrom = (urlFrom == null || urlFrom.isEmpty() || schemaFrom == null || schemaFrom.isEmpty()
                || userFrom == null || userFrom.isEmpty() || passwordFrom == null);
        boolean emptyTo = (urlTo == null || urlTo.isEmpty() || schemaTo == null || schemaTo.isEmpty()
                || userTo == null || userTo.isEmpty() || passwordTo == null);
        boolean emptyXml = (fileXml == null || fileXml.isEmpty());

        if (emptyFrom) {
            if (emptyTo || emptyXml) {
                throw new IllegalArgumentException(messageUsage);
            }
        } else {
            if (emptyTo && emptyXml) {
                throw new IllegalArgumentException(messageUsage);
            }
        }

        DBStructure srcDBStructure = null;
        if (!emptyFrom) {
            DbClient client1 = createClient(urlFrom, schemaFrom, userFrom, passwordFrom);
            srcDBStructure = readDBStructure(client1);
            if (!emptyXml) {
                serializeMetadata(srcDBStructure, getFileXml());
            }
            if (client1 != null) {
                client1.shutdown();
            }
        }
        if (srcDBStructure == null && !emptyXml) {
            srcDBStructure = readDBStructureFromFile(getFileXml());
        }
        
        if (!emptyTo && srcDBStructure != null) {
            DbClient client2 = createClient(urlTo, schemaTo, userTo, passwordTo);
            MetadataMerger metadataMerger = new MetadataMerger(client2, srcDBStructure, readDBStructure(client2), isNoExecute(), isNoDropTables(), listTables);
            metadataMerger.setSqlLogger(sqlLogger);
            metadataMerger.setErrorLogger(errorLogger);
            metadataMerger.run();
            if (client2 != null) {
                client2.shutdown();
            }

            // re-read structure destination for compare with source
            client2 = createClient(urlTo, schemaTo, userTo, passwordTo);
            if (infoLogger != null && client2 != null) {
                MetadataUtils.printCompareMetadata(srcDBStructure, readDBStructure(client2), infoLogger);
            }
            if (client2 != null) {
                client2.shutdown();
            }
        }
    }

    /**
     * Create structure metadata from connection
     *
     * @param aClient connection to database
     * @return structure metadata
     * @throws Exception
     */
    private DBStructure readDBStructure(DbClient aClient) throws Exception {

        int MAX_TABLES_IN_SQLS = 100;    // set max count fetched  from database descriptions for primary keys and indexes

        Map<String, TableStructure> mdStructure = null;
        String dbDialect = null;

        DbMetadataCache mdCache;

        // for default logger       
        int cntTables = 0;
        int cntFields = 0;
        int cntIndexes = 0;
        int cntIndexesF = 0;
        int cntPKs = 0;
        int cntFKs = 0;

        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, "Start reading structure metadata from connection");

        if (aClient != null) {
            dbDialect = aClient.getConnectionDialect(null);
            mdStructure = new HashMap<>();
            mdCache = aClient.getDbMetadataCache(null);
            SqlDriver driver = mdCache.getConnectionDriver();
            String dbSchema = mdCache.getConnectionSchema();
            // search all tables
            String sql4Tables = driver.getSql4TablesEnumeration(dbSchema);
            SqlCompiledQuery query = new SqlCompiledQuery(aClient, null, sql4Tables);
            Rowset rowsetTablesList = query.executeQuery();

            Fields fieldsTable = rowsetTablesList.getFields();

            if (rowsetTablesList.first()) {
                List<String> tableNamesList = new ArrayList<>();
                int tableColIndex = fieldsTable.find(ClientConstants.JDBCCOLS_TABLE_NAME);
                int tableTypeColIndex = fieldsTable.find(ClientConstants.JDBCPKS_TABLE_TYPE_FIELD_NAME);
                do {
                    // each table 
                    String tableType = null;
                    if (tableTypeColIndex > 0) {
                        tableType = rowsetTablesList.getString(tableTypeColIndex);
                    }
                    if (tableType == null || tableType.equalsIgnoreCase(ClientConstants.JDBCPKS_TABLE_TYPE_TABLE)) {
                        String tableName = rowsetTablesList.getString(tableColIndex);
                        String tableNameUpper = tableName.toUpperCase();

                        TableStructure tblStructure = new TableStructure();
                        tblStructure.setTableName(tableName);

                        // get fields for table
                        Fields fields = mdCache.getTableMetadata(tableName);
                        tblStructure.setTableFields(fields);

                        cntFields = cntFields + fields.getFieldsCount();

                        // comment on table
                        tblStructure.setTableDescription(fields.getTableDescription());

                        // for primary keys and indexes
                        tableNamesList.add(tableName);

                        mdStructure.put(tableNameUpper, tblStructure);
                    }
                } while (rowsetTablesList.next());

                // get indexes and primary keys
                int begPos = 0;
                int endPos = 0;
                int maxPos = tableNamesList.size();

                do {
                    endPos = endPos + MAX_TABLES_IN_SQLS;
                    HashSet<String> tablesSet = new HashSet<>();
                    if (endPos > maxPos) {
                        endPos = maxPos;
                    }
                    tablesSet.addAll(tableNamesList.subList(begPos, endPos));
                    begPos = begPos + MAX_TABLES_IN_SQLS;

                    // indexes
                    String sql4Indexes = driver.getSql4Indexes(dbSchema, tablesSet);
                    SqlCompiledQuery queryIndexes = new SqlCompiledQuery(aClient, null, sql4Indexes);
                    Rowset rowsetIndexes = queryIndexes.executeQuery();
                    cntIndexesF = cntIndexesF + addIndexFromRowset(mdStructure, rowsetIndexes);

                    // sort all columns in index
                    Iterator<String> names = tablesSet.iterator();

                    while (names.hasNext()) {
                        String name = names.next();
                        TableStructure tableStructure = mdStructure.get(name.toUpperCase());
                        Map<String, DbTableIndexSpec> tableIndexSpecs = tableStructure.getTableIndexSpecs();
                        if (tableIndexSpecs != null) {
                            for (String nameSpec : tableIndexSpecs.keySet()) {
                                tableIndexSpecs.get(nameSpec).sortColumns();
                                cntIndexes++;
                            }
                        }
                    }

                    //pk
                    String sqlPK = driver.getSql4TablePrimaryKeys(dbSchema, tablesSet);
                    SqlCompiledQuery queryPK = new SqlCompiledQuery(aClient, null, sqlPK);
                    Rowset rowsetPK = queryPK.executeQuery();

                    cntPKs = cntPKs + addPKeysFromRowset(mdStructure, rowsetPK);

                    //fk
                    String sqlFK = driver.getSql4TableForeignKeys(dbSchema, tablesSet);
                    SqlCompiledQuery queryFK = new SqlCompiledQuery(aClient, null, sqlFK);
                    Rowset rowsetFK = queryFK.executeQuery();

                    cntFKs = cntFKs + addFKeysFromRowset(mdStructure, rowsetFK);
                } while (endPos < maxPos);
            }

            // set all maps uppername -> name
            Iterator<TableStructure> tablesStructure = mdStructure.values().iterator();
            while (tablesStructure.hasNext()) {
                tablesStructure.next().makeMapNamesToUpper();
            }

            cntTables = mdStructure.size();
            StringBuilder sb = new StringBuilder();
            sb.append("Read structure from schema ").append(dbSchema);
            sb.append("\n   tables: ").append(cntTables);
            sb.append("\n   fields: ").append(cntFields);
            sb.append("\n   indexes: ").append(cntIndexes);
            sb.append("\n   indexed columns: ").append(cntIndexesF);
            sb.append("\n   primary keys: ").append(cntPKs);
            sb.append("\n   foreign keys: ").append(cntFKs);
            sb.append("\n");

            Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, sb.toString());
        }
        return new DBStructure(mdStructure,dbDialect);
    }

    /**
     * Create structure metadata from connection
     *
     * @param aUrl url connection
     * @param aSchema schema connection
     * @param aUser database user
     * @param aPassword password for user database
     * @return structure metadata
     * @throws Exception
     */
    public DBStructure readDBStructure(String aUrl, String aSchema, String aUser, String aPassword) throws Exception {
        DbClient client = createClient(aUrl,aSchema,aUser,aPassword);        
        DBStructure structure = readDBStructure(client);
        client.shutdown();
        return structure;
    }
    
    /**
     * Create document from structure metadata database
     *
     * @param aDBStructure structure metadata database
     * @throws ParserConfigurationException
     */
    private Document createDocument(DBStructure aDBStructure) throws ParserConfigurationException {
        assert aDBStructure != null;
        Map<String, TableStructure> tablesStructure = aDBStructure.getTablesStructure();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DbMetadataCache mdCache = null;

        // for default logger
        int cntTables = 0;
        int cntFields = 0;
        int cntIndexes = 0;
        int cntIndexesF = 0;
        int cntPKs = 0;
        int cntFKs = 0;

        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, "Start creating document from metadata");
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
	doc.setXmlStandalone(true);
        // root element
        Element rootElement = doc.createElement(SCHEMA_TAG_NAME);
        rootElement.setAttribute(DATABASEDIALECT_ATTR_NAME, aDBStructure.getDatabaseDialect());
        //rootElement.setAttribute(NAME_ATTR_NAME, dbschema);
        doc.appendChild(rootElement);

        // tables
        Element tablesElement = doc.createElement(TABLES_TAG_NAME);
        rootElement.appendChild(tablesElement);

        // for all tables
        for (String tableName : tablesStructure.keySet()) {
            // each table
            TableStructure tblStructure =tablesStructure.get(tableName);
            cntTables++;

            Element tableElement = doc.createElement(TABLE_TAG_NAME);
            tablesElement.appendChild(tableElement);

            tableElement.setAttribute(NAME_ATTR_NAME, tableName);
            tableElement.setAttribute(DESCRIPTION_ATTR_NAME, tblStructure.getTableDescription());

            // all columns
            Element fieldsElement = doc.createElement(COLUMNS_TAG_NAME);
            tableElement.appendChild(fieldsElement);

            Fields tableFields = tblStructure.getTableFields();
            int fieldsCount = tableFields.getFieldsCount();
            for (int j = 1; j <= fieldsCount; j++) {
                // each column
                Field field = tableFields.get(j);

                Element fieldElement = doc.createElement(COLUMN_TAG_NAME);
                fieldsElement.appendChild(fieldElement);

                fieldElement.setAttribute(NAME_ATTR_NAME, field.getName());
                fieldElement.setAttribute(DESCRIPTION_ATTR_NAME, field.getDescription());
                fieldElement.setAttribute(TYPE_ATTR_NAME, String.valueOf(field.getTypeInfo().getSqlType()));
                fieldElement.setAttribute(TYPENAME_ATTR_NAME, field.getTypeInfo().getSqlTypeName());
                fieldElement.setAttribute(JAVACLASS_ATTR_NAME, field.getTypeInfo().getJavaClassName());
                fieldElement.setAttribute(SIZE_ATTR_NAME, String.valueOf(field.getSize()));
                fieldElement.setAttribute(SCALE_ATTR_NAME, String.valueOf(field.getScale()));
                fieldElement.setAttribute(PRECISION_ATTR_NAME, String.valueOf(field.getPrecision()));
                fieldElement.setAttribute(SIGNED_ATTR_NAME, String.valueOf(field.isSigned()));
                fieldElement.setAttribute(NULLABLE_ATTR_NAME, String.valueOf(field.isNullable()));
                fieldElement.setAttribute(ISPK_ATTR_NAME, String.valueOf(field.isPk()));
            }
            cntFields = cntFields + fieldsCount;

            // all indexes
            Element indexesElement = doc.createElement(INDEXES_TAG_NAME);
            tableElement.appendChild(indexesElement);

            Map<String, DbTableIndexSpec> indexes = tblStructure.getTableIndexSpecs();

            if (indexes != null) {
                for (DbTableIndexSpec index : indexes.values()) {
                    Element indexElement = doc.createElement(INDEX_TAG_NAME);
                    indexesElement.appendChild(indexElement);
                    List<DbTableIndexColumnSpec> columns = index.getColumns();

                    indexElement.setAttribute(NAME_ATTR_NAME, index.getName());
                    indexElement.setAttribute(CLUSTERED_ATTR_NAME, String.valueOf(index.isClustered()));
                    indexElement.setAttribute(HASHED_ATTR_NAME, String.valueOf(index.isHashed()));
                    indexElement.setAttribute(UNIQUE_ATTR_NAME, String.valueOf(index.isUnique()));

                    for (DbTableIndexColumnSpec column : columns) {
                        // each indexed column
                        Element fieldElement = doc.createElement(INDCOLUMN_TAG_NAME);
                        indexElement.appendChild(fieldElement);

                        fieldElement.setAttribute(NAME_ATTR_NAME, column.getColumnName());
                        fieldElement.setAttribute(POSITION_ATTR_NAME, String.valueOf(column.getOrdinalPosition()));
                        fieldElement.setAttribute(ASCENDING_ATTR_NAME, String.valueOf(column.isAscending()));
                        cntIndexesF++;
                    }
                }
                cntIndexes = cntIndexes + indexes.size();
            }

            // all PrimaryKeys
            List<PrimaryKeySpec> tablePKeySpecs = tblStructure.getTablePKeySpecs();
            if (tablePKeySpecs != null && tablePKeySpecs.size() > 0) {
                Element pKeyElement = doc.createElement(PKEY_TAG_NAME);
                tableElement.appendChild(pKeyElement);

                String pKeyCName = tblStructure.getPKeyCName();
                pKeyElement.setAttribute(NAME_ATTR_NAME, pKeyCName);

                for (PrimaryKeySpec pkeySpec : tblStructure.getTablePKeySpecs()) {
                    Element pKeyColumnElement = doc.createElement(PKEYCOLUMN_TAG_NAME);
                    pKeyElement.appendChild(pKeyColumnElement);

                    pKeyColumnElement.setAttribute(PKNAME_ATTR_NAME, pKeyCName);
                    pKeyColumnElement.setAttribute(PKSCHEMA_ATTR_NAME, pkeySpec.getSchema());
                    pKeyColumnElement.setAttribute(PKTABLE_ATTR_NAME, pkeySpec.getTable());
                    pKeyColumnElement.setAttribute(PKFIELD_ATTR_NAME, pkeySpec.getField());

                    cntPKs++;
                }
            }

            // all ForeignKeys
            Map<String, List<ForeignKeySpec>> tableFKeySpecs = tblStructure.getTableFKeySpecs();
            if (tableFKeySpecs != null) {
                Element fKeysElement = doc.createElement(FKEYS_TAG_NAME);
                tableElement.appendChild(fKeysElement);

                // each fkey spec
                for (String fKeyCName : tableFKeySpecs.keySet()) {
                    Element fKeyElement = doc.createElement(FKEY_TAG_NAME);
                    fKeysElement.appendChild(fKeyElement);

                    fKeyElement.setAttribute(NAME_ATTR_NAME, fKeyCName);

                    for (ForeignKeySpec fkSpec : tableFKeySpecs.get(fKeyCName)) {
                        Element fKeyColumnElement = doc.createElement(FKEYCOLUMN_TAG_NAME);
                        fKeyElement.appendChild(fKeyColumnElement);

                        fKeyColumnElement.setAttribute(FKNAME_ATTR_NAME, fkSpec.getCName());
                        fKeyColumnElement.setAttribute(FKSCHEMA_ATTR_NAME, fkSpec.getSchema());
                        fKeyColumnElement.setAttribute(FKTABLE_ATTR_NAME, fkSpec.getTable());
                        fKeyColumnElement.setAttribute(FKFIELD_ATTR_NAME, fkSpec.getField());
                        fKeyColumnElement.setAttribute(FKDEFERRABLE_ATTR_NAME, String.valueOf(fkSpec.getFkDeferrable()));
                        fKeyColumnElement.setAttribute(FKDELETERULE_ATTR_NAME, String.valueOf(fkSpec.getFkDeleteRule()));
                        fKeyColumnElement.setAttribute(FKUPDATERULE_ATTR_NAME, String.valueOf(fkSpec.getFkUpdateRule()));
                        fKeyColumnElement.setAttribute(PKNAME_ATTR_NAME, fkSpec.getReferee().getCName());
                        fKeyColumnElement.setAttribute(PKSCHEMA_ATTR_NAME, fkSpec.getReferee().getSchema());
                        fKeyColumnElement.setAttribute(PKTABLE_ATTR_NAME, fkSpec.getReferee().getTable());
                        fKeyColumnElement.setAttribute(PKFIELD_ATTR_NAME, fkSpec.getReferee().getField());

                        cntFKs++;
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Document created");
        sb.append("\n   tables = ").append(cntTables);
        sb.append("\n   fields = ").append(cntFields);
        sb.append("\n   indexes = ").append(cntIndexes);
        sb.append("\n   indexed columns = ").append(cntIndexesF);
        sb.append("\n   primary keys = ").append(cntPKs);
        sb.append("\n   foreign keys = ").append(cntFKs);
        sb.append("\n");

        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, sb.toString());
        return doc;
    }

    /**
     * create structure metadata from document
     *
     * @param aFileXml file with description structure database
     * @return structure metadata
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public DBStructure readDBStructureFromFile(String aFileXml) throws ParserConfigurationException, SAXException, IOException {
        Map<String, TableStructure> tables = null;
        String dialect = null;

        // for default logger
        int cntTables = 0;
        int cntFields = 0;
        int cntIndexes = 0;
        int cntIndexesF = 0;
        int cntPKs = 0;
        int cntFKs = 0;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(new File(aFileXml));

        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, String.format("Start creating structure metadata from document '%s'",aFileXml));

        if (doc != null) {
            Element root = doc.getDocumentElement();            
            dialect = root.getAttribute(DATABASEDIALECT_ATTR_NAME);
            tables = new HashMap<>();
            NodeList tablesNodeList = doc.getElementsByTagName(TABLE_TAG_NAME);

            for (int i = 0; i < tablesNodeList.getLength(); i++) {
                if (tablesNodeList.item(i) instanceof Element) {
                    // each table
                    TableStructure tableStructure = new TableStructure();

                    Element tableElement = (Element) tablesNodeList.item(i);
                    String tableName = String.valueOf(tableElement.getAttribute(NAME_ATTR_NAME));
                    String tableNameUpper = tableName.toUpperCase();

                    String tableDescription = String.valueOf(tableElement.getAttribute(DESCRIPTION_ATTR_NAME));
                    tableStructure.setTableDescription(tableDescription);
                    tableStructure.setTableName(tableName);

                    // fields
                    NodeList fieldsNodeList = tableElement.getElementsByTagName(COLUMN_TAG_NAME);
                    Fields fields = new Fields();

                    for (int j = 0; j < fieldsNodeList.getLength(); j++) {
                        if (fieldsNodeList.item(j) instanceof Element) {
                            // each field
                            Element fieldElement = (Element) fieldsNodeList.item(j);

                            Field fld = new Field();

                            fld.setTableName(tableName);
                            fld.setName(fieldElement.getAttribute(NAME_ATTR_NAME));

                            fld.setDescription(fieldElement.getAttribute(DESCRIPTION_ATTR_NAME));

                            DataTypeInfo info = new DataTypeInfo();
                            String attribute = fieldElement.getAttribute(TYPE_ATTR_NAME);
                            info.setSqlType(Integer.valueOf(fieldElement.getAttribute(TYPE_ATTR_NAME)));
                            info.setSqlTypeName(fieldElement.getAttribute(TYPENAME_ATTR_NAME));
                            info.setJavaClassName(fieldElement.getAttribute(JAVACLASS_ATTR_NAME));
                            fld.setTypeInfo(info);

                            fld.setSize(Integer.valueOf(fieldElement.getAttribute(SIZE_ATTR_NAME)));
                            fld.setScale(Integer.valueOf(fieldElement.getAttribute(SCALE_ATTR_NAME)));
                            fld.setPrecision(Integer.valueOf(fieldElement.getAttribute(PRECISION_ATTR_NAME)));
                            fld.setSigned(Boolean.valueOf(fieldElement.getAttribute(SIGNED_ATTR_NAME)));
                            fld.setNullable(Boolean.valueOf(fieldElement.getAttribute(NULLABLE_ATTR_NAME)));
                            fld.setPk(Boolean.valueOf(fieldElement.getAttribute(ISPK_ATTR_NAME)));
                            fields.add(fld);

                            cntFields++;
                        }
                        tableStructure.setTableFields(fields);
                    }

                    // indexes
                    NodeList indexesNodeList = tableElement.getElementsByTagName(INDEX_TAG_NAME);

                    Map<String, DbTableIndexSpec> indexes = new HashMap<>();

                    for (int j = 0; j < indexesNodeList.getLength(); j++) {
                        if (indexesNodeList.item(j) instanceof Element) {
                            // each index
                            Element indexElement = (Element) indexesNodeList.item(j);
                            DbTableIndexSpec dbTableIndexSpec = new DbTableIndexSpec();

                            String indexName = indexElement.getAttribute(NAME_ATTR_NAME);

                            dbTableIndexSpec.setName(indexName);
                            dbTableIndexSpec.setClustered(Boolean.valueOf(indexElement.getAttribute(CLUSTERED_ATTR_NAME)));
                            dbTableIndexSpec.setHashed(Boolean.valueOf(indexElement.getAttribute(HASHED_ATTR_NAME)));
                            dbTableIndexSpec.setUnique(Boolean.valueOf(indexElement.getAttribute(UNIQUE_ATTR_NAME)));

                            // indexed columns
                            NodeList indColumnNodeList = indexElement.getElementsByTagName(INDCOLUMN_TAG_NAME);

                            for (int k = 0; k < indColumnNodeList.getLength(); k++) {
                                if (indColumnNodeList.item(k) instanceof Element) {
                                    // each indexed column
                                    Element indColumnElement = (Element) indColumnNodeList.item(k);

                                    String indColumnName = indColumnElement.getAttribute(NAME_ATTR_NAME);
                                    Boolean indColumnAsc = Boolean.valueOf(indColumnElement.getAttribute(ASCENDING_ATTR_NAME));
                                    DbTableIndexColumnSpec dbTableIndexColumnSpec = new DbTableIndexColumnSpec(indColumnName, indColumnAsc);

                                    dbTableIndexColumnSpec.setOrdinalPosition(Integer.valueOf(indColumnElement.getAttribute(POSITION_ATTR_NAME)));
                                    dbTableIndexSpec.addColumn(dbTableIndexColumnSpec);
                                }
                            }
                            indexes.put(indexName, dbTableIndexSpec);

                            cntIndexes++;
                            cntIndexesF = cntIndexesF + dbTableIndexSpec.getColumns().size();
                        }
                        tableStructure.setTableIndexSpecs(indexes);
                    }

                    // Primary Keys
                    ArrayList<PrimaryKeySpec> tablePKeySpecs = new ArrayList<>();
                    NodeList pKeyNodeList = tableElement.getElementsByTagName(PKEY_TAG_NAME);
                    for (int j = 0; j < pKeyNodeList.getLength(); j++) {
                        if (pKeyNodeList.item(j) instanceof Element) {
                            Element pKeyElement = (Element) pKeyNodeList.item(j);

                            tableStructure.setPKeyCName(pKeyElement.getAttribute(NAME_ATTR_NAME));

                            // all columns
                            NodeList pkColumnNodeList = tableElement.getElementsByTagName(PKEYCOLUMN_TAG_NAME);
                            for (int k = 0; k < pkColumnNodeList.getLength(); k++) {
                                if (pkColumnNodeList.item(k) instanceof Element) {
                                    Element pkColumnElement = (Element) pkColumnNodeList.item(k);

                                    String pkName = pkColumnElement.getAttribute(PKNAME_ATTR_NAME);
                                    String pkShema = pkColumnElement.getAttribute(PKSCHEMA_ATTR_NAME);
                                    String pkTable = pkColumnElement.getAttribute(PKTABLE_ATTR_NAME);
                                    String pkField = pkColumnElement.getAttribute(PKFIELD_ATTR_NAME);

                                    tablePKeySpecs.add(new PrimaryKeySpec(pkShema, pkTable, pkField, pkName));

                                    cntPKs++;
                                }
                            }
                        }
                    }
                    tableStructure.setTablePKeySpecs(tablePKeySpecs);

                    // Foreign Keys
                    Map<String, List<ForeignKeySpec>> tableFKeySpecs = new HashMap<>();
                    NodeList fKeyNodeList = tableElement.getElementsByTagName(FKEY_TAG_NAME);
                    for (int j = 0; j < fKeyNodeList.getLength(); j++) {

                        if (fKeyNodeList.item(j) instanceof Element) {
                            Element fKeyElement = (Element) fKeyNodeList.item(j);
                            String CName = fKeyElement.getAttribute(NAME_ATTR_NAME);

                            //all columns
                            ArrayList<ForeignKeySpec> fkeySpecs = new ArrayList<>();
                            NodeList fkColumnNodeList = fKeyElement.getElementsByTagName(FKEYCOLUMN_TAG_NAME);
                            for (int k = 0; k < fkColumnNodeList.getLength(); k++) {
                                if (fkColumnNodeList.item(k) instanceof Element) {
                                    Element fkColumnElement = (Element) fkColumnNodeList.item(k);

                                    String fkName = fkColumnElement.getAttribute(FKNAME_ATTR_NAME);
                                    String fkShema = fkColumnElement.getAttribute(FKSCHEMA_ATTR_NAME);
                                    String fkTable = fkColumnElement.getAttribute(FKTABLE_ATTR_NAME);
                                    String fkField = fkColumnElement.getAttribute(FKFIELD_ATTR_NAME);

                                    String pkName = fkColumnElement.getAttribute(PKNAME_ATTR_NAME);
                                    String pkShema = fkColumnElement.getAttribute(PKSCHEMA_ATTR_NAME);
                                    String pkTable = fkColumnElement.getAttribute(PKTABLE_ATTR_NAME);
                                    String pkField = fkColumnElement.getAttribute(PKFIELD_ATTR_NAME);

                                    // ????  in Oracle only DELETERULE                                   
                                    String attributeFkUpdate = fkColumnElement.getAttribute(FKUPDATERULE_ATTR_NAME);
                                    ForeignKeyRule updateFkRule = (attributeFkUpdate != null && !attributeFkUpdate.equalsIgnoreCase("null") ? ForeignKeyRule.valueOf(attributeFkUpdate) : null);

                                    ForeignKeySpec fkSpec = new ForeignKeySpec(fkShema, fkTable, fkField, fkName,
                                            updateFkRule,
                                            ForeignKeyRule.valueOf(fkColumnElement.getAttribute(FKDELETERULE_ATTR_NAME)),
                                            Boolean.valueOf(fkColumnElement.getAttribute(FKDEFERRABLE_ATTR_NAME)),
                                            pkShema, pkTable, pkField, pkName);

                                    fkeySpecs.add(fkSpec);
                                    tableFKeySpecs.put(CName, fkeySpecs);

                                    Field field = fields.get(fkField);
                                    field.setFk(fkSpec);

                                    cntFKs++;
                                }
                            }
                        }
                    }
                    tableStructure.setTableFKeySpecs(tableFKeySpecs);
                    tableStructure.makeMapNamesToUpper();
                    tables.put(tableNameUpper, tableStructure);
                    cntTables++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Structure ").append(tables == null ? "not " : "").append("created");
        sb.append("\n   tables = ").append(cntTables);
        sb.append("\n   fields = ").append(cntFields);
        sb.append("\n   indexes = ").append(cntIndexes);
        sb.append("\n   indexed columns = ").append(cntIndexesF);
        sb.append("\n   primary keys = ").append(cntPKs);
        sb.append("\n   foreign keys = ").append(cntFKs);
        sb.append("\n");

        Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.INFO, sb.toString());
        return new DBStructure(tables,dialect);
    }

    /**
     * Parsing arguments from command line
     *
     * @param args arguments from command line
     * @throws Exception
     */
    private void parseArgs(String[] args) throws Exception {
        int i = 0;
        while (i < args.length) {
            // section From   
            if ((CMD_SWITCHS_PREFIX + URLFROM_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setUrlFrom(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("urlFrom syntax: -urlFrom <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + SCHEMAFROM_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setSchemaFrom(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("schemaFrom syntax: -schemaFrom <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + USERFROM_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setUserFrom(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("userFrom syntax: -userFrom <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + PASSWORDFROM_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setPasswordFrom(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("passwordFrom syntax: -passwordFrom <value>");
                }
            } // section To
            else if ((CMD_SWITCHS_PREFIX + URLTO_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setUrlTo(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("urlTo syntax: -urlTo <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + SCHEMATO_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setSchemaTo(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("schemaTo syntax: -schemaTo <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + USERTO_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setUserTo(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("userTo syntax: -userTo <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + PASSWORDTO_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setPasswordTo(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("passwordTo syntax: -passwordTo <value>");
                }
            } // section fileXML
            else if ((CMD_SWITCHS_PREFIX + FILEXML_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setFileXml(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("fileXml syntax: -fileXml <value>");
                }
            } // section Logger
            else if ((CMD_SWITCHS_PREFIX + LOGLEVEL_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    logLevel = (Level.parse(args[i + 1].toUpperCase()));
                    i += 1;
                } else {
                    throw new IllegalArgumentException("loggers level syntax: -logLevel <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + LOGCODEPAGE_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    logEncoding = args[i + 1];
                    i += 1;
                } else {
                    throw new IllegalArgumentException("loggers encoding  syntax: -logEncodinge <value>");
                }
            } else if ((CMD_SWITCHS_PREFIX + LOGPATH_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    setLogPath(args[i + 1]);
                    i += 1;
                } else {
                    throw new IllegalArgumentException("loggers files path syntax: -logPath <value>");
                }
            } // section execute
            else if ((CMD_SWITCHS_PREFIX + NOEXECUTE_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                setNoExecute(true);
            } else if ((CMD_SWITCHS_PREFIX + NODROPTABLES_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                setNoDropTables(true);
            } else if ((CMD_SWITCHS_PREFIX + LISTTABLES_CMD_SWITCH).equalsIgnoreCase(args[i])) {
                if (i < args.length - 1) {
                    parseTablesList(args[i+1],",");
                    i += 1;
                } else {
                    throw new IllegalArgumentException("to set list tables syntax: -listTables tableName1,tableName2,...");
                }
            } else {
                throw new IllegalArgumentException("unknown argument: " + args[i]);
            }
            i++;
        }
    }

    /**
     * add to metadata description for indexes from database
     *
     * @param aMDStructure structure metadata
     * @param aRowset rowset with description indexes from database
     * @return count fetched indexes
     * @throws InvalidColIndexException
     * @throws InvalidCursorPositionException
     */
    private int addIndexFromRowset(Map<String, TableStructure> aMDStructure, Rowset aRowset) throws InvalidColIndexException, InvalidCursorPositionException {
        int cnt = 0;
        if (aRowset.first()) {
            Fields fields = aRowset.getFields();
            int nCol_Idx_TableName = fields.find(ClientConstants.JDBCIDX_TABLE_NAME);
            int nCol_Idx_Name = fields.find(ClientConstants.JDBCIDX_INDEX_NAME);
            int nCol_Idx_Non_Uni = fields.find(ClientConstants.JDBCIDX_NON_UNIQUE);
            int nCol_Idx_Type = fields.find(ClientConstants.JDBCIDX_TYPE);
            int nCol_Idx_ColumnName = fields.find(ClientConstants.JDBCIDX_COLUMN_NAME);
            int nCol_Idx_Asc = fields.find(ClientConstants.JDBCIDX_ASC_OR_DESC);
            int nCol_Idx_OrdinalPosition = fields.find(ClientConstants.JDBCIDX_ORDINAL_POSITION);

            do {
                String tableName = aRowset.getString(nCol_Idx_TableName);
                String tableNameUpper = tableName.toUpperCase();
                TableStructure tableStructure = aMDStructure.get(tableNameUpper);
                Map<String, DbTableIndexSpec> tableIndexSpecs = tableStructure.getTableIndexSpecs();
                if (tableIndexSpecs == null) {
                    tableIndexSpecs = new HashMap<>();
                }
                DbTableIndexSpec idxSpec = null;
                String idxName = "";

                Object oIdxName = aRowset.getObject(nCol_Idx_Name);
                if (oIdxName != null && oIdxName instanceof String) {
                    idxName = (String) oIdxName;

                    idxSpec = tableIndexSpecs.get(idxName);
                    if (idxSpec == null) {
                        idxSpec = new DbTableIndexSpec();
                        idxSpec.setName(idxName);
                    }
                }

                Object oNonUnique = aRowset.getObject(nCol_Idx_Non_Uni);
                if (oNonUnique != null) {
                    boolean isUnique = false;
                    if (oNonUnique instanceof Number) {
                        isUnique = !(((Number) oNonUnique).intValue() != 0);
                    }
                    idxSpec.setUnique(isUnique);
                }
                Object oType = aRowset.getObject(nCol_Idx_Type);
                if (oType != null) {
                    if (oType instanceof Number) {
                        short type = ((Number) oType).shortValue();
                        idxSpec.setClustered(false);
                        idxSpec.setHashed(false);
                        switch (type) {
                            case DatabaseMetaData.tableIndexClustered:
                                idxSpec.setClustered(true);
                                break;
                            case DatabaseMetaData.tableIndexHashed:
                                idxSpec.setHashed(true);
                                break;
                            case DatabaseMetaData.tableIndexStatistic:
                                break;
                            case DatabaseMetaData.tableIndexOther:
                                break;
                        }
                    }
                }
                Object oColumnName = aRowset.getObject(nCol_Idx_ColumnName);
                if (oColumnName != null && oColumnName instanceof String) {
                    String sColumnName = (String) oColumnName;
                    DbTableIndexColumnSpec column = idxSpec.getColumn(sColumnName);
                    if (column == null) {
                        column = new DbTableIndexColumnSpec(sColumnName, true);
                        idxSpec.addColumn(column);
                    }
                    Object oAsc = aRowset.getObject(nCol_Idx_Asc);
                    if (oAsc != null && oAsc instanceof String) {
                        String sAsc = (String) oAsc;
                        column.setAscending(sAsc.toLowerCase().equals("a"));
                    }
                    Object oPosition = aRowset.getObject(nCol_Idx_OrdinalPosition);
                    if (oPosition != null && oPosition instanceof Number) {
                        column.setOrdinalPosition((int) ((Number) oPosition).shortValue());
                    }
                }
                tableIndexSpecs.put(idxName, idxSpec);
                tableStructure.setTableIndexSpecs(tableIndexSpecs);
                cnt++;
            } while (aRowset.next());
        }
        return cnt;
    }

    /**
     * add to metadata description for primary keys from database
     *
     * @param aMDStructure structure metadata
     * @param aRowset rowset with description primary keys from database
     * @return count fetched primary keys
     * @throws InvalidColIndexException
     * @throws InvalidCursorPositionException
     */
    private int addPKeysFromRowset(Map<String, TableStructure> aMDStructure, Rowset aRowset) throws InvalidColIndexException, InvalidCursorPositionException {
        int cnt = 0;

        if (aRowset.first()) {
            Fields fieldsPK = aRowset.getFields();
            int pkNameColIndex = fieldsPK.find(ClientConstants.JDBCPKS_CONSTRAINT_NAME);
            int pkSchemaColIndex = fieldsPK.find(ClientConstants.JDBCPKS_TABLE_SCHEM);
            int pkTableColIndex = fieldsPK.find(ClientConstants.JDBCPKS_TABLE_NAME);
            int pkFieldColIndex = fieldsPK.find(ClientConstants.JDBCPKS_COLUMN_NAME);

            do {
                String pkSchema = aRowset.getString(pkSchemaColIndex);
                String pkTable = aRowset.getString(pkTableColIndex);
                String pkField = aRowset.getString(pkFieldColIndex);
                String pkCName = aRowset.getString(pkNameColIndex);

                String tableNameUpper = pkTable.toUpperCase();

                PrimaryKeySpec pkSpec = new PrimaryKeySpec();
                pkSpec.setSchema(pkSchema);
                pkSpec.setTable(pkTable);
                pkSpec.setField(pkField);
                pkSpec.setCName(pkCName);

                TableStructure tblStructure = aMDStructure.get(tableNameUpper);
                tblStructure.setPKeyCName(pkCName);
                List<PrimaryKeySpec> tablePKeySpecs = tblStructure.getTablePKeySpecs();
                if (tablePKeySpecs == null) {
                    tablePKeySpecs = new ArrayList<>();
                }
                tablePKeySpecs.add(pkSpec);
                tblStructure.setTablePKeySpecs(tablePKeySpecs);

                cnt++;
            } while (aRowset.next());
        }
        return cnt;
    }

    /** !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * add to metadata description for primary keys from database
     *
     * @param aMDStructure structure metadata
     * @param aRowset rowset with description primary keys from database
     * @return count fetched primary keys
     * @throws InvalidColIndexException
     * @throws InvalidCursorPositionException
     */
    private int addFKeysFromRowset(Map<String, TableStructure> aMDStructure, Rowset aRowset) throws InvalidColIndexException, InvalidCursorPositionException {
        int cnt = 0;

        if (aRowset.first()) {
            Fields fieldsFK = aRowset.getFields();
            int refSchemaColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKPKTABLE_SCHEM);
            int refTableColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKPKTABLE_NAME);
            int refPKeyNameColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKPK_NAME);
            int refColumnColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKPKCOLUMN_NAME);
            
            int schemaColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKTABLE_SCHEM);
            int tableColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKTABLE_NAME);
            int fKeyNameColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FK_NAME);
            int columnColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKCOLUMN_NAME);
            //????            fieldsFK.find("KEY_SEQ");
            int updateRuleColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKUPDATE_RULE);
            int deleteRuleColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKDELETE_RULE);
            int deferrabilityColIndex = fieldsFK.find(ClientConstants.JDBCFKS_FKDEFERRABILITY);
            
            

            do {
                String refSchemaName = aRowset.getString(refSchemaColIndex);
                String refTableName = aRowset.getString(refTableColIndex);
                String refPKeyName = aRowset.getString(refPKeyNameColIndex);
                String refColumnName = aRowset.getString(refColumnColIndex);

                String schemaName = aRowset.getString(schemaColIndex);
                String tableName = aRowset.getString(tableColIndex);
                String fKeyName = aRowset.getString(fKeyNameColIndex);
                String columnName = aRowset.getString(columnColIndex);
                
                Short updateRule = aRowset.getShort(updateRuleColIndex);
                Short deleteRule = aRowset.getShort(deleteRuleColIndex);
                Short deferrability = aRowset.getShort(deferrabilityColIndex);

                String tableNameUpper = tableName.toUpperCase();
                
                TableStructure tblStructure = aMDStructure.get(tableNameUpper);
                Map<String, List<ForeignKeySpec>> allFKeySpecs = tblStructure.getTableFKeySpecs();
                if (allFKeySpecs == null) {
                    allFKeySpecs = new HashMap();
                }
                List<ForeignKeySpec> fKeySpecs = allFKeySpecs.get(fKeyName);
                if (fKeySpecs == null) {
                    fKeySpecs = new ArrayList();
                }
                ForeignKeySpec fKeySpec = new ForeignKeySpec();               
                //fKeySpec.set
                fKeySpec.setSchema(schemaName);
                fKeySpec.setTable(tableName);
                fKeySpec.setField(columnName);
                fKeySpec.setCName(fKeyName);
                fKeySpec.setReferee(new PrimaryKeySpec(refSchemaName,refTableName,refColumnName,refPKeyName));
//                fKeySpec.setFkDeleteRule(ForeignKeySpec.ForeignKeyRule.valueOf(deleteRule != null ? deleteRule : 0));
//                fKeySpec.setFkUpdateRule(ForeignKeySpec.ForeignKeyRule.valueOf(updateRule != null ? updateRule : 0));
//???????                
                fKeySpec.setFkDeleteRule(deleteRule != null ? ForeignKeySpec.ForeignKeyRule.valueOf(deleteRule) : null);
                fKeySpec.setFkUpdateRule(updateRule != null ? ForeignKeySpec.ForeignKeyRule.valueOf(updateRule) : null);
                fKeySpec.setFkDeferrable(deferrability != null ? deferrability == 5 ? true : false : false);
                
                fKeySpecs.add(fKeySpec);
                allFKeySpecs.put(fKeyName, fKeySpecs);
                tblStructure.setTableFKeySpecs(allFKeySpecs);
                cnt++;
            } while (aRowset.next());
        }
        return cnt;
        
        
//                        Map<String, List<ForeignKeySpec>> tableFKeySpecs = tblStructure.getTableFKeySpecs();
//                        if (tableFKeySpecs == null) {
//                            tableFKeySpecs = new HashMap<>();
//                        }
//                        if (foreingKeys != null) {
//                            for (Field f : foreingKeys) {
//                                ForeignKeySpec fk = f.getFk();
//                                String cName = fk.getCName();
//                                List<ForeignKeySpec> fkList = tableFKeySpecs.get(cName);
//                                if (fkList == null) {
//                                    fkList = new ArrayList<>();
//                                }
//                                // !!! ONLY for Oracle                                    
//                                if (driver instanceof OracleSqlDriver) {
//                                    fk.setFkUpdateRule(null);
//                                }
//
//                                fkList.add(fk);
//                                tableFKeySpecs.put(cName, fkList);
//                            }
//                        }
//
//                        tblStructure.setTableFKeySpecs(tableFKeySpecs);
//
//                        cntFKs = cntFKs + foreingKeys.size();
        
        
    }

    private void setLogPath(String aLogPath) {
        if (aLogPath != null) {
            logPath = aLogPath;
            if (!logPath.isEmpty()) {
                String separator = System.getProperty("file.separator");
                if (!logPath.endsWith(separator)) {
                    logPath += separator;
                }
            }
        }
    }

    /**
     * @return the noExecute
     */
    public boolean isNoExecute() {
        return noExecute;
    }

    /**
     * @param noExecute the noExecute to set
     */
    public void setNoExecute(boolean aNoExecute) {
        noExecute = aNoExecute;
    }

    /**
     * @return the urlFrom
     */
    public String getUrlFrom() {
        return urlFrom;
    }

    /**
     * @param aUrlFrom the urlFrom to set
     */
    public void setUrlFrom(String aUrlFrom) {
        urlFrom = aUrlFrom;
    }

    /**
     * @return the userFrom
     */
    public String getUserFrom() {
        return userFrom;
    }

    /**
     * @param aUserFrom the userFrom to set
     */
    public void setUserFrom(String aUserFrom) {
        userFrom = aUserFrom;
    }

    /**
     * @return the schemaFrom
     */
    public String getSchemaFrom() {
        return schemaFrom;
    }

    /**
     * @param aSchemaFrom the schemaFrom to set
     */
    public void setSchemaFrom(String aSchemaFrom) {
        schemaFrom = aSchemaFrom;
    }

    /**
     * @param aPasswordFrom the passwordFrom to set
     */
    public void setPasswordFrom(String aPasswordFrom) {
        passwordFrom = aPasswordFrom;
    }

    /**
     * @return the urlTo
     */
    public String getUrlTo() {
        return urlTo;
    }

    /**
     * @param aUrlTo the urlTo to set
     */
    public void setUrlTo(String aUrlTo) {
        urlTo = aUrlTo;
    }

    /**
     * @return the userTo
     */
    public String getUserTo() {
        return userTo;
    }

    /**
     * @param userTo the userTo to set
     */
    public void setUserTo(String aUserTo) {
        userTo = aUserTo;
    }

    /**
     * @return the schemaTo
     */
    public String getSchemaTo() {
        return schemaTo;
    }

    /**
     * @param aSchemaTo the schemaTo to set
     */
    public void setSchemaTo(String aSchemaTo) {
        schemaTo = aSchemaTo;
    }

    /**
     * @param aPasswordTo the passwordTo to set
     */
    public void setPasswordTo(String aPasswordTo) {
        passwordTo = aPasswordTo;
    }

    /**
     * @return the fileXml
     */
    public String getFileXml() {
        return fileXml;
    }

    /**
     * @param aFileName the fileXml to set
     */
    public void setFileXml(String aFileName) {
        fileXml = aFileName;
    }

    /**
     * @return the noDropTables
     */
    public boolean isNoDropTables() {
        return noDropTables;
    }

    /**
     * @param noDropTables the noDropTables to set
     */
    public void setNoDropTables(boolean noDropTables) {
        this.noDropTables = noDropTables;
    }

    public void initSqlLogger(Handler aHandler, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        sqlLogger = Logger.getLogger(SQL_LOGGER_NAME);
        if (sqlLoggerLevel == null ) {
            sqlLoggerLevel = sqlLogger.getLevel();
        }
        if (sqlLoggerUseParentHandlers == null) {
            sqlLoggerUseParentHandlers = sqlLogger.getUseParentHandlers();
        }
        initLogger(sqlLogger, aHandler, aLevel, isUseParentHandlers);
    }

    public void initSqlLogger(String aFileName, String aEncoding, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        initSqlLogger(createFileHandler(aFileName, aEncoding, aFormatter), aLevel, isUseParentHandlers, aFormatter);
    }

    public void clearSqlLogger() {
        clearLogger(sqlLogger, sqlLoggerLevel, sqlLoggerUseParentHandlers);
    }

    public void initErrorLogger(Handler aHandler, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        errorLogger = Logger.getLogger(ERROR_LOGGER_NAME);
        if (errorLoggerLevel == null) {
            errorLoggerLevel = errorLogger.getLevel();
        } 
        if (errorLoggerUseParentHandlers == null) {
            errorLoggerUseParentHandlers = errorLogger.getUseParentHandlers();
        }       
        initLogger(errorLogger, aHandler, aLevel, isUseParentHandlers);
    }
    
    public void initErrorLogger(String aFileName, String aEncoding, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        initErrorLogger(createFileHandler(aFileName, aEncoding, aFormatter), aLevel, isUseParentHandlers, aFormatter);
    }

    public void clearErrorLogger() {
        clearLogger(errorLogger, errorLoggerLevel, errorLoggerUseParentHandlers);
    }

    public void initInfoLogger(Handler aHandler, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        infoLogger = Logger.getLogger(INFO_LOGGER_NAME);
        if (infoLoggerLevel == null) {
            infoLoggerLevel = infoLogger.getLevel();
        }
        if (infoLoggerUseParentHandlers == null) {
            infoLoggerUseParentHandlers = infoLogger.getUseParentHandlers();
        }        
        initLogger(infoLogger, aHandler, aLevel, isUseParentHandlers);
    }

    public void initInfoLogger(String aFileName, String aEncoding, Level aLevel, boolean isUseParentHandlers, Formatter aFormatter) {
        initInfoLogger(createFileHandler(aFileName, aEncoding, aFormatter), aLevel, isUseParentHandlers, aFormatter);
    }

    public void clearInfoLogger() {
        clearLogger(infoLogger, infoLoggerLevel, infoLoggerUseParentHandlers);
    }

    public void initDefaultLoggers(Handler aLogHandler, Level aLevel, boolean isUseParentHandlers) {
        if (mdsLoggerLevel == null) {
            mdsLoggerLevel = Logger.getLogger(MetadataSynchronizer.class.getName()).getLevel();
        }
        if (mdsLoggerUseParentHandlers == null) {
            mdsLoggerUseParentHandlers = Logger.getLogger(MetadataSynchronizer.class.getName()).getUseParentHandlers();
        }        
        initLogger(Logger.getLogger(MetadataSynchronizer.class.getName()), aLogHandler, aLevel, isUseParentHandlers);
        if (mergerLoggerLevel == null) {
            mergerLoggerLevel = Logger.getLogger(MetadataMerger.class.getName()).getLevel();
        }
        if (mergerLoggerUseParentHandlers == null) {
            mergerLoggerUseParentHandlers = Logger.getLogger(MetadataMerger.class.getName()).getUseParentHandlers();
        }        
        initLogger(Logger.getLogger(MetadataMerger.class.getName()), aLogHandler, aLevel, isUseParentHandlers);
    }

    public void clearDefaultLoggers() {
        clearLogger(Logger.getLogger(MetadataSynchronizer.class.getName()), mdsLoggerLevel, mdsLoggerUseParentHandlers);
        clearLogger(Logger.getLogger(MetadataMerger.class.getName()), mergerLoggerLevel, mergerLoggerUseParentHandlers);
    }

     private void initLogger(Logger aLogger, Handler aHandler, Level aLevel, boolean isUseParentHandlers) {    
        assert aLogger != null;
        if (aHandler != null) {
            aLogger.addHandler(aHandler);
        }
        if (aLevel != null) {
            aLogger.setLevel(aLevel);
        }
        aLogger.setUseParentHandlers(isUseParentHandlers);
    }

    private void clearLogger(Logger aLogger,  Level aLevel, Boolean isUseParentHandlers) {
        if (aLogger != null) {
            for (Handler handler : aLogger.getHandlers()) {
                handler.close();
                aLogger.removeHandler(handler);
            }
            if (aLevel != null) {
                aLogger.setLevel(aLevel);
            }
            if (isUseParentHandlers != null) {
                aLogger.setUseParentHandlers(isUseParentHandlers);
            }
        }
    }

    private FileHandler createFileHandler(String aFileName, String aLogEncoding, Formatter aLogFormatter) {
        try {
            FileHandler handler = new FileHandler(aFileName);
            handler.setEncoding(aLogEncoding);
            if (aLogFormatter != null) {
                handler.setFormatter(aLogFormatter);
            }
            return handler;
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MetadataSynchronizer mds = null;
        try {
            mds = new MetadataSynchronizer();
            mds.initDefaultLoggers(null, Level.INFO, true);
            mds.parseArgs(args);
            mds.initSqlLogger(mds.logPath + "sql.log", mds.logEncoding, mds.logLevel, false, new LogFormatter());
            mds.initErrorLogger(mds.logPath + "error.log", mds.logEncoding, mds.logLevel, false, new LogFormatter());
            mds.initInfoLogger(mds.logPath + "info.log", mds.logEncoding, mds.logLevel, false, new LogFormatter());
            mds.run();
        } catch (Exception ex) {
            Logger.getLogger(MetadataSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (mds != null) {
                mds.clearSqlLogger();
                mds.clearErrorLogger();
                mds.clearInfoLogger();
                mds.clearDefaultLoggers();
            }
        }
    }

    public void parseTablesList(String aValue, String aDelimiter) {
        if (aValue != null && !aValue.isEmpty()) {
            listTables.clear();
            StringTokenizer st = new StringTokenizer(aValue, aDelimiter, false);
            while (st.hasMoreTokens()) {
                String tableName = st.nextToken();
                if (tableName != null && !tableName.isEmpty()) {
                    listTables.add(tableName.trim().toUpperCase());
                }
            }
        }
    }
}