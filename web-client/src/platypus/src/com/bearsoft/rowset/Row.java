package com.bearsoft.rowset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bearsoft.rowset.exceptions.InvalidColIndexException;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.eas.client.Utils;
import com.eas.client.beans.PropertyChangeEvent;
import com.eas.client.beans.PropertyChangeListener;
import com.eas.client.beans.PropertyChangeSupport;
import com.eas.client.beans.VetoableChangeListener;
import com.eas.client.beans.VetoableChangeSupport;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * This class serves as the values holder.
 * 
 * @author mg
 */
public class Row {

	protected Fields fields;
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);
	protected VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(
			this);
	protected Set<Integer> updated = new HashSet();
	protected boolean deleted = false;
	protected boolean inserted = false;
	protected List<Object> originalValues = new ArrayList();
	protected List<Object> currentValues = new ArrayList();

	/**
	 * Row's POJO-like constructor.
	 */
	public Row() {
		super();
	}

	/**
	 * Constructs the row with column count equals to colCount values vectors
	 * allocated.
	 * 
	 * @param colCount
	 *            - column count that you whant to be in this row.
	 */
	public Row(Fields aFields) {
		super();
		setFields(aFields);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		try {
			boolean pks = false;
			int hashCode = 1;
			for (int i = 1; i <= fields.getFieldsCount(); i++) {
				Field field = fields.get(i);
				if (field.isPk()) {
					pks = true;
					Object obj = getColumnObject(i);
					hashCode = 31 * hashCode
							+ (obj == null ? 0 : obj.hashCode());
				}
			}
			if (pks) {
				return hashCode;
			} else {
				return super.hashCode();
			}
		} catch (InvalidColIndexException ex) {
			return super.hashCode();
		}
	}

	/**
	 * Fires vetoable change events, without sending reverting corresponding
	 * events.
	 * 
	 * @param event
	 *            An event to fire.
	 * @return True if no one of listeners has vetoed the change. False
	 *         otherwise.
	 */
	protected boolean checkChange(PropertyChangeEvent event) {
		try {
			VetoableChangeListener[] vls = vetoableChangeSupport
					.getVetoableChangeListeners();
			for (VetoableChangeListener vl : vls) {
				vetoableChangeSupport.removeVetoableChangeListener(vl);
			}
			try {
				for (VetoableChangeListener vl : vls) {
					vl.vetoableChange(event);
				}
			} finally {
				for (VetoableChangeListener vl : vls) {
					vetoableChangeSupport.addVetoableChangeListener(vl);
				}
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public Fields getFields() {
		return fields;
	}

	public final void setFields(Fields aFields) {
		fields = aFields;
		originalValues.clear();
		currentValues.clear();
		for (int i = 0; i < fields.getFieldsCount(); i++) {
			originalValues.add(i, null);
			currentValues.add(i, null);
		}
	}

	public PropertyChangeSupport getChangeSupport() {
		return propertyChangeSupport;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	public void addVetoableChangeListener(VetoableChangeListener l) {
		vetoableChangeSupport.addVetoableChangeListener(l);
	}

	public void removeVetoableChangeListener(VetoableChangeListener l) {
		vetoableChangeSupport.removeVetoableChangeListener(l);
	}

	/**
	 * Returns whether the row is updated at whole or partially. The updated
	 * flag is <code>true</code> if <code>setColumnObject()</code> or
	 * <code>setUpdated()</code> methods have been invoked between invocations
	 * of <code>currentToOriginal()</code> or <code>originalToCurrent()</code>
	 * methods.
	 * 
	 * @return Whether the row is updated.
	 * @see #setColumnObject(int, Object)
	 */
	public boolean isUpdated() {
		return !updated.isEmpty();
	}

	/**
	 * Sets updated flags for all columns.
	 */
	public void setUpdated() {
		for (int i = 1; i <= fields.getFieldsCount(); i++) {
			updated.add(i);
		}
	}

	/**
	 * Clears the updated flags for all colmns.
	 */
	public void clearUpdated() {
		updated.clear();
	}

	/**
	 * Returns whether the row is deleted. The deleted flag is <code>true</code>
	 * if <code>setDeleted()</code> method has been invoked.
	 * 
	 * @return Whether the row is deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Sets the deleted flag.
	 */
	public void setDeleted() {
		deleted = true;
	}

	/**
	 * Clears the deleted flag.
	 */
	public void clearDeleted() {
		deleted = false;
	}

	/**
	 * Returns whether the row is inserted. The inserted flag is
	 * <code>true</code> if <code>setInserted()</code> method has been invoked.
	 * 
	 * @return Whether the row is inserted.
	 */
	public boolean isInserted() {
		return inserted;
	}

	/**
	 * Sets the inserted flag.
	 */
	public void setInserted() {
		inserted = true;
	}

	/**
	 * Clears the inserted flag.
	 */
	public void clearInserted() {
		inserted = false;
	}

	/**
	 * Applies current values to this row. After this method has been invoked,
	 * the current values become original values. Also it clears updated flag.
	 * 
	 * @see #setUpdated()
	 * @see #isUpdated()
	 * @see #clearUpdated()
	 * @see #originalToCurrent()
	 */
	public void currentToOriginal() {
		for (int i = 0; i < getColumnCount(); i++) {
			originalValues.set(i, currentValues.get(i));
		}
		clearUpdated();
	}

	/**
	 * Cancels current values in this row. After this method has been invoked,
	 * the original values become current values. Also it clears updated flag.
	 * 
	 * @see #setUpdated()
	 * @see #isUpdated()
	 * @see #clearUpdated()
	 * @see #currentToOriginal()
	 */
	public void originalToCurrent() {
		for (int i = 0; i < getColumnCount(); i++) {
			currentValues.set(i, originalValues.get(i));
		}
		clearUpdated();
	}

	/**
	 * Returns the columns count of this row.
	 * 
	 * @return The columns count of this row.
	 * @see #Row(int colCount)
	 */
	public int getColumnCount() {
		assert currentValues.size() == originalValues.size();
		assert currentValues.size() == fields.getFieldsCount();
		return fields.getFieldsCount();
	}

	public void setFieldObject(String aFieldName, Object aFieldValue)
			throws Exception {
		int colIndex = fields.find(aFieldName);
		setColumnObject(colIndex, Utils.toJava(aFieldValue));
	}

	/**
	 * Sets current value of particular column.
	 * 
	 * @param aColIndex
	 *            ordinal position of the column. It's value lies within the
	 *            range of [1: <code>getColumnCount()</code>].
	 * @param aValue
	 *            value that you whant to be setted to the column as the current
	 *            column value.
	 * @throws InvalidColIndexException
	 *             if colIndex < 1 or colIndex > <code>getColumnCount()</code>
	 */
	public void setColumnObject(int aColIndex, Object aValue)
			throws RowsetException {
		if (aColIndex >= 1 && aColIndex <= getColumnCount()) {
			Field field = fields.get(aColIndex);
			aValue = Converter.convert2RowsetCompatible(aValue,
					field.getTypeInfo());
			if (!smartEquals(getColumnObject(aColIndex), aValue)) {
				Object oldColValue = currentValues.get(aColIndex - 1);
				PropertyChangeEvent event = new PropertyChangeEvent(this,
						field.getName(), oldColValue, aValue);
				event.setPropagationId(aColIndex);
				if (checkChange(event)) {
					currentValues.set(aColIndex - 1, aValue);
					updated.add(aColIndex);
					propertyChangeSupport.firePropertyChange(event);
				}
			}
		} else {
			if (aColIndex < 1) {
				throw new InvalidColIndexException("colIndex < 1");
			}
			if (aColIndex > getColumnCount()) {
				throw new InvalidColIndexException(
						"colIndex > getColumnCount()");
			}
		}
	}

	public Object getFieldObject(String aFieldName) throws Exception {
		int colIndex = fields.find(aFieldName);
		return Utils.toJs(getColumnObject(colIndex));
	}

	/**
	 * Returns current column value.
	 * 
	 * @param colIndex
	 *            ordinal position of the column. It's value lies within the
	 *            range of [1: <code>getColumnCount()</code>].
	 * @return The column's current value.
	 * @throws InvalidColIndexException
	 *             if colIndex < 1 or colIndex > <code>getColumnCount()</code>
	 */
	public Object getColumnObject(int colIndex) throws InvalidColIndexException {
		if (colIndex >= 1 && colIndex <= getColumnCount()) {
			return currentValues.get(colIndex - 1);
		} else {
			if (colIndex < 1) {
				throw new InvalidColIndexException("colIndex < 1");
			} else if (colIndex > getColumnCount()) {
				throw new InvalidColIndexException(
						"colIndex > getColumnCount()");
			} else {
				throw new InvalidColIndexException("unexpected");
			}
		}
	}

	/**
	 * Returns original column value.
	 * 
	 * @param colIndex
	 *            ordinal position of the column. It's value lies within the
	 *            range of [1: <code>getColumnCount()</code>].
	 * @return The column's original value.
	 * @throws InvalidColIndexException
	 *             if colIndex < 1 or colIndex > <code>getColumnCount()</code>
	 */
	public Object getOriginalColumnObject(int colIndex)
			throws InvalidColIndexException {
		if (colIndex >= 1 && colIndex <= getColumnCount()) {
			return originalValues.get(colIndex - 1);
		} else {
			if (colIndex < 1) {
				throw new InvalidColIndexException("colIndex < 1");
			} else if (colIndex > getColumnCount()) {
				throw new InvalidColIndexException(
						"colIndex > getColumnCount()");
			} else {
				throw new InvalidColIndexException("unexpected");
			}
		}
	}

	/**
	 * Returns whether particular column is updated.
	 * 
	 * @param aColIndex
	 *            ordinal position of the column. Index is 1-based.
	 * @return Whether aprticular column is updated.
	 * @see #setColumnObject(int, Object)
	 * @see #getUpdatedColumns()
	 * @see #isUpdated()
	 * @see #clearUpdated()
	 * @see #setUpdated()
	 * @see #setColumnUpdated(int)
	 */
	public boolean isColumnUpdated(int aColIndex) {
		return updated.contains(aColIndex);
	}

	/**
	 * Sets particular column updated state.
	 * 
	 * @param colIndex
	 *            ordinal position of the column. Index is 1-based.
	 * @see #setColumnObject(int, Object)
	 * @see #getUpdatedColumns()
	 * @see #isUpdated()
	 * @see #clearUpdated()
	 * @see #setUpdated()
	 * @see #isColumnUpdated(int)
	 */
	public void setColumnUpdated(int colIndex) {
		updated.add(colIndex);
	}

	/**
	 * Returns an updated columns indicies set.
	 * 
	 * @return An updated columns indicies set.
	 * @see #setColumnObject(int, Object)
	 * @see #isColumnUpdated(int)
	 * @see #isUpdated()
	 * @see #clearUpdated()
	 * @see #setUpdated()
	 */
	public Set<Integer> getUpdatedColumns() {
		return updated;
	}

	/**
	 * Returns an array of original values of this row.
	 * 
	 * @return An array of original values of this row.
	 * @see #getCurrentValues()
	 * @see #originalToCurrent()
	 * @see #currentToOriginal()
	 */
	public Object[] getOriginalValues() {
		return originalValues.toArray();
	}

	/**
	 * Returns an array of current values of this row.
	 * 
	 * @return An array of current values of this row.
	 * @see #getOriginalValues()
	 * @see #originalToCurrent()
	 * @see #currentToOriginal()
	 */
	public Object[] getCurrentValues() {
		return currentValues.toArray();
	}

	/**
	 * Returns an array of current values of primary-key fields of this row.
	 * 
	 * @return An array of current values of primary-key fields of this row.
	 */
	public Object[] getPKValues() {
		Object[] lcurrentValues = getCurrentValues();
		List<Integer> pkIndicies = fields.getPrimaryKeysIndicies();
		List<Object> pkValues = new ArrayList();
		for (Integer pkIdx : pkIndicies) {
			assert pkIdx >= 1 && pkIdx <= lcurrentValues.length;
			pkValues.add(lcurrentValues[pkIdx - 1]);
		}
		return pkValues.toArray();
	}

	/**
	 * Returns an internal representation of row's data. It's not recomended to
	 * add or remove any elements from the list returned. Furthermore, if you
	 * have changed some elements of the list, than you must set internal
	 * updated flags in some way.
	 * 
	 * @return Internal row's data values list.
	 */
	public List<Object> getInternalCurrentValues() {
		return currentValues;
	}

	/**
	 * Tests if two values are equal to each other. It casts both values to big
	 * decimal format if they are numbers.
	 * 
	 * @param aFirstValue
	 *            First value to be compared.
	 * @param aOtherValue
	 *            Second value to be compared.
	 * @return If two values are equal to each other.
	 */
	public static boolean smartEquals(Object aFirstValue, Object aOtherValue) {
		if (aFirstValue != null) {
			if (aFirstValue instanceof Number && aOtherValue instanceof Number) {
				Double bd1 = ((Number) aFirstValue).doubleValue();
				Double bd2 = ((Number) aOtherValue).doubleValue();
				return bd1.compareTo(bd2) == 0;
			} else {
				return aFirstValue.equals(aOtherValue);
			}
		} else {
			return aOtherValue == null;
		}
	}

	protected JavaScriptObject jsPublished;

	public void setPublished(JavaScriptObject aPublished) {
		jsPublished = aPublished;
	}

	public JavaScriptObject getPublished() {
		return jsPublished;
	}
}