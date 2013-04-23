/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.threetier;

import com.bearsoft.rowset.Row;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.exceptions.AlreadyExistSerializerException;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.serial.BinaryRowsetWriter;
import com.bearsoft.rowset.serial.custom.CompactLobsSerializer;
import com.eas.client.SQLUtils;
import com.vividsolutions.jts.geom.Geometry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mg
 */
public class PlatypusRowsetWriter extends BinaryRowsetWriter {

    protected boolean preProcessFields;
    protected boolean writeOnlyChanged;

    public PlatypusRowsetWriter(boolean aPreProcessFields, boolean aWriteOnlyChanged) {
        super();
        try {
            preProcessFields = aPreProcessFields;
            writeOnlyChanged = aWriteOnlyChanged;
            CompactLobsSerializer ser = new CompactLobsSerializer();
            addSerializer(DataTypeInfo.CLOB, ser);
            addSerializer(DataTypeInfo.NCLOB, ser);
            addSerializer(DataTypeInfo.BLOB, ser);
            addSerializer(DataTypeInfo.BINARY, ser);
            addSerializer(DataTypeInfo.VARBINARY, ser);
            addSerializer(DataTypeInfo.LONGVARBINARY, ser);
            addSerializer(new DataTypeInfo(java.sql.Types.STRUCT, "MDSYS.SDO_GEOMETRY", Geometry.class.getName()), new JtsGeometrySerializer());
            addSerializer(new DataTypeInfo(java.sql.Types.STRUCT, "MDSYS.SDO_GEOMETRY", Object.class.getName()), new JtsGeometrySerializer());
        } catch (AlreadyExistSerializerException ex) {
            Logger.getLogger(PlatypusRowsetWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PlatypusRowsetWriter(boolean aPreProcessFields) {
        this(aPreProcessFields, false);
    }

    public PlatypusRowsetWriter(){
        this(false, false);
    }

    @Override
    public void writeFields(Fields aFields, ByteArrayOutputStream aOut) throws IOException {
        if (preProcessFields) {
            aFields = aFields.copy();
            SQLUtils.processFieldsPreClient(aFields);
        }
        super.writeFields(aFields, aOut);
    }

    @Override
    protected ByteArrayOutputStream writeData(Rowset aRowset) throws RowsetException, IOException {
        return super.writeData(aRowset);
    }

    @Override
    protected boolean needToWriteRow(Row aRow) {
        return !writeOnlyChanged || aRow.isDeleted() || aRow.isInserted() || aRow.isUpdated();
    }
}