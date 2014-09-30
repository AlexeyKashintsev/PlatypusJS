/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.proto.dom;

import com.eas.proto.ProtoReader;
import com.eas.proto.ProtoReaderException;
import com.eas.proto.ProtoUtil;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

/**
 *
 * @author pk
 */
class ProtoLeafNode implements ProtoNode {

    private final int nodeTag;
    private final byte[] data;
    private final int offset;
    private final int size;

    ProtoLeafNode(int tag, byte[] data, int dataOffset, int size) {
        if (data.length < dataOffset + size) {
            throw new IllegalArgumentException("Buffer overflow");
        }
        this.nodeTag = tag;
        this.data = data;
        this.offset = dataOffset;
        this.size = size;
    }

    @Override
    public int getNodeTag() {
        return nodeTag;
    }

    @Override
    public byte getByte() throws ProtoReaderException {
        if (size == 1) {
            return data[offset];
        } else {
            throw new ProtoReaderException("Wrong size " + size + ", expected 1");
        }
    }

    @Override
    public boolean getBoolean() throws ProtoReaderException {
        if (size == 1) {
            return data[offset] == 1;
        } else {
            throw new ProtoReaderException("Wrong size " + size + ", expected 1");
        }
    }

    @Override
    public Date getDate() throws ProtoReaderException {
        return new Date(getLong());
    }

    @Override
    public double getDouble() throws ProtoReaderException {
        return Double.longBitsToDouble(getLong());
    }

    @Override
    public float getFloat() throws ProtoReaderException {
        return Float.intBitsToFloat(getInt());
    }

    @Override
    public long getEntityID() throws ProtoReaderException {
        return getLong();
    }

    public short getShort() throws ProtoReaderException {
        if (size == 2) {
            return (short) (((data[offset + 0] & 0xff) << 8) + (data[offset + 1] & 0xff));
        } else {
            throw new ProtoReaderException("Wrong size " + size + ", expected 2");
        }
    }

    @Override
    public int getInt() throws ProtoReaderException {
        if (size == 4) {
            return (((data[offset + 0] & 0xff) << 24) + ((data[offset + 1] & 0xff) << 16) + ((data[offset + 2] & 0xff) << 8) + ((data[offset + 3] & 0xff)));
        } else {
            throw new ProtoReaderException("Wrong size " + size + ", expected 4");
        }
    }

    @Override
    public long getLong() throws ProtoReaderException {
        if (size == 8) {
            return (((long) (data[offset + 0] & 0xff) << 56)
                    + ((long) (data[offset + 1] & 0xff) << 48)
                    + ((long) (data[offset + 2] & 0xff) << 40)
                    + ((long) (data[offset + 3] & 0xff) << 32)
                    + ((long) (data[offset + 4] & 0xff) << 24)
                    + ((data[offset + 5] & 0xff) << 16)
                    + ((data[offset + 6] & 0xff) << 8)
                    + ((data[offset + 7] & 0xff)));
        } else {
            throw new ProtoReaderException("Wrong size " + size + ", expected 8");
        }
    }

    @Override
    public String getString() {
        try {
            // This constrcutor is chosen because it doesn't copy input byte array and
            // starts to decode it into string immidiatly. So, performance of decoding is pretty and nice.
            // If anybody has dicovered the reason of unnessasary copy made by
            // folowing constructor: new String(byte[] adata, int offset, int size, Charset aCharset),
            // talk to mg bout this!
            String val = new String(data, offset, size, ProtoUtil.CHARSET_4_STRING_SERIALIZATION_NAME);
            return val;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ProtoLeafNode.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    @Override
    public BigDecimal getBigDecimal() {
        byte[] unscaled = new byte[size - Integer.SIZE / Byte.SIZE];
        System.arraycopy(data, offset, unscaled, 0, unscaled.length);
        int scaleVal = (((data[offset + unscaled.length + 0] & 0xff) << 24) + ((data[offset + unscaled.length + 1] & 0xff) << 16) + ((data[offset + unscaled.length + 2] & 0xff) << 8) + ((data[offset + unscaled.length + 3] & 0xff)));
        BigInteger unscaledVal = new BigInteger(unscaled);
        BigDecimal val = new BigDecimal(unscaledVal, scaleVal);
        return val;
    }

    @Override
    public Object getJDBCCompatible(int sqlType) throws ProtoReaderException {
        if (size == 0) {
            return null;
        }
        switch (sqlType) {
            case java.sql.Types.BIGINT:
                return BigInteger.valueOf(getLong());
            case java.sql.Types.FLOAT:
                return getFloat();
            case java.sql.Types.DOUBLE:
            case java.sql.Types.REAL:
                return getDouble();
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                return getBigDecimal();
            case java.sql.Types.TINYINT:
            case java.sql.Types.INTEGER:
                return getInt();
            case java.sql.Types.SMALLINT:
                return getShort();
            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
                return getInt() != 0;
            case java.sql.Types.VARBINARY:
            case java.sql.Types.BINARY:
            case java.sql.Types.LONGVARBINARY: {
                byte[] val = new byte[size];
                System.arraycopy(data, offset, val, 0, size);
                return val;
            }
            case java.sql.Types.BLOB: {
                byte[] val = new byte[size];
                System.arraycopy(data, offset, val, 0, size);
                SerialBlob blob;
                try {
                    blob = new SerialBlob(val);
                } catch (SerialException ex) {
                    Logger.getLogger(ProtoReader.class.getName()).log(Level.SEVERE, null, ex);
                    throw new ProtoReaderException(ex.getMessage());
                } catch (SQLException ex) {
                    Logger.getLogger(ProtoReader.class.getName()).log(Level.SEVERE, null, ex);
                    throw new ProtoReaderException(ex.getMessage());
                }
                return blob;
            }
            case java.sql.Types.CLOB: {
                char[] val = getString().toCharArray();
                SerialClob clob;
                try {
                    clob = new SerialClob(val);
                } catch (SerialException ex) {
                    Logger.getLogger(ProtoReader.class.getName()).log(Level.SEVERE, null, ex);
                    throw new ProtoReaderException(ex.getMessage());
                } catch (SQLException ex) {
                    Logger.getLogger(ProtoReader.class.getName()).log(Level.SEVERE, null, ex);
                    throw new ProtoReaderException(ex.getMessage());
                }
                return clob;
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.LONGNVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NCLOB:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.SQLXML:
                return getString();
            case java.sql.Types.DATE:
                return new java.sql.Date(getDate().getTime());
            case java.sql.Types.TIME:
                return new java.sql.Time(getDate().getTime());
            case java.sql.Types.TIMESTAMP:
                return new java.sql.Timestamp(getDate().getTime());
            default:
                throw new ProtoReaderException("Unsupported SQL type " + sqlType);
        }
    }

    @Override
    public ProtoNode getChild(int tag) {
        throw new UnsupportedOperationException("Unsupported for a leaf node");
    }

    @Override
    public List<ProtoNode> getChildren(int tag) {
        throw new UnsupportedOperationException("Unsupported for entity ref node");
    }

    @Override
    public Iterator<ProtoNode> iterator() {
        throw new UnsupportedOperationException("Unsupported for a leaf node");
    }

    @Override
    public boolean containsChild(int tag) {
        throw new UnsupportedOperationException("Unsupported for a leaf node");
    }

    @Override
    public ProtoNodeType getNodeType() {
        return ProtoNodeType.LEAF;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }
}
