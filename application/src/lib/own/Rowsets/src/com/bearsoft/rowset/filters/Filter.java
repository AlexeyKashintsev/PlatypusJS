/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.rowset.filters;

import com.bearsoft.rowset.ordering.HashOrderer;
import com.bearsoft.rowset.utils.KeySet;
import com.bearsoft.rowset.Row;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.locators.RowWrap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter class. Performs multi column filtering of rowset.
 * @author mg
 */
public class Filter extends HashOrderer {

    protected List<Row> originalRows;
    protected int originalPos;
    protected boolean filterApplied = false;
    protected KeySet keysetApplied;

    /**
     * The filter's class constructor
     */
    public Filter(Rowset aRowset) {
        super(aRowset);
    }

    /**
     * Returns applied values vector (KeySet) according to feilds indicies vector, setted previously.
     * @return Applied values KeySet.
     * @see #getFields()
     * @see #setFields(java.util.Vector)
     */
    public KeySet getKeysetApplied() {
        return keysetApplied;
    }

    /**
     * Returns whether this filter is applied to it's rowset.
     * @return Whether this filter is applied to it's rowset.
     */
    public boolean isApplied() {
        return filterApplied;
    }

    /**
     * Returns rows vector that was in the rowset before this filter has been applied.
     * @return Rows vector that was in the rowset before this filter has been applied.
     */
    public List<Row> getOriginalRows() {
        return originalRows;
    }

    /**
     * Returns rowset's cursor position was in the rowset before this filter has been applied.
     * @return Rowset's cursor position was in the rowset before this filter has been applied.
     */
    public int getOriginalPos() {
        return originalPos;
    }

    /**
     * Sets rowset's cursor position was in the rowset before this filter has been applied.
     * @param aPos Position is to be setted.
     */
    public void setOriginalPos(int aPos) {
        originalPos = aPos;
    }

    /**
     * Adds a row to hash-ordered structure of row wraps
     * @param aRow Row is to be added.
     * @return True if aRow wass added to the structure, False otherwise.
     * @throws RowsetException
     */
    public boolean add(Row aRow) throws RowsetException {
        KeySet ks = makeKeySet(aRow, fieldsIndicies);
        if (ks != null) {
            List<RowWrap> subset = ordered.get(ks);
            // add to structure
            if (subset == null) {
                subset = new ArrayList<>();
                ordered.put(ks, subset);
            }
            return subset.add(new RowWrap(aRow, -1));
        }
        return false;
    }

    /**
     * Reapplies this filtrer to rowset.
     * @throws RowsetException
     */
    public void refilterRowset() throws RowsetException {
        filterRowset(keysetApplied);
    }

    /**
     * Applies this filter ti the rowset with keys values according to fields (columns) conditions vector already prepared with <code>setFields()</code> or <code>...Constrainting()</code> methods.
     * @param values Values vector.
     * @throws RowsetException
     * @see #beginConstrainting()
     * @see #isConstrainting()
     * @see #addConstraint(int aFieldIndex)
     * @see #removeConstraint(int aFieldIndex)
     * @see #endConstrainting()
     */
    public void filterRowset(Object... values) throws RowsetException {
        if (values != null && values.length > 0) {
            KeySet ks = new KeySet();
            ks.addAll(Arrays.asList(values));
            filterRowset(ks);
        } else {
            throw new RowsetException("bad filtering conditions. Absent or empty");
        }
    }
    private boolean filtering = false;

    /**
     * Applies this filter ti the rowset with keys values according to fields (columns) conditions vector already prepared with <code>setFields()</code> or <code>...Constrainting()</code> methods.
     * @param values Values <code>KeySet</code>.
     * @throws RowsetException
     * @see #beginConstrainting()
     * @see #isConstrainting()
     * @see #addConstraint(int aFieldIndex)
     * @see #removeConstraint(int aFieldIndex)
     * @see #endConstrainting()
     */
    public void filterRowset(KeySet values) throws RowsetException {
        if (rowset != null) {
            if (!isValid()) {
                validate();
            }
            if (!filtering) {
                filtering = true;
                try {
                    if (values != null) {
                        if (rowset.getRowsetChangeSupport().fireWillFilterEvent()) {
                            if (!caseSensitive) {
                                for (int i = 0; i < values.size(); i++) {
                                    Object keyValue = values.get(i);
                                    if (keyValue != null && keyValue instanceof String) {
                                        values.set(i, ((String) keyValue).toLowerCase());
                                    }
                                }
                            }
                            if (filterApplied) {
                                filterApplied = false;
                            }

                            Filter activeFilter = rowset.getActiveFilter();
                            boolean foreignFilter = activeFilter == null || activeFilter != this;

                            if (foreignFilter && activeFilter != null) {
                                activeFilter.cancelFilter();
                            }

                            if (!filterApplied) {
                                boolean wasBeforeFirst = rowset.isBeforeFirst();
                                boolean wasAfterLast = rowset.isAfterLast();
                                List<Row> subSetRows = new ArrayList<>();
                                List<RowWrap> subSet = ordered.get(values);
                                if (subSet != null) {
                                    int i = 0;
                                    for (RowWrap lrw : subSet) {
                                        assert lrw != null;
                                        subSetRows.add(i++, lrw.getRow());
                                    }
                                }
                                if (foreignFilter) {
                                    originalRows = rowset.getCurrent();
                                    originalPos = rowset.getCursorPos();
                                }
                                rowset.setSubsetAsCurrent(subSetRows);
                                rowset.setActiveFilter(this);
                                if (wasBeforeFirst) {
                                    rowset.beforeFirst();
                                } else if (wasAfterLast) {
                                    rowset.afterLast();
                                } else if (!rowset.isEmpty()) {
                                    rowset.first();
                                }
                                filterApplied = true;
                                keysetApplied = values;
                            }
                            rowset.getRowsetChangeSupport().fireFilteredEvent();
                        }
                    } else {
                        throw new RowsetException(CANT_APPLY_NULL_KEY_SET);
                    }
                } finally {
                    filtering = false;
                }
            }
        } else {
            throw new RowsetException(ROWSET_MISSING);
        }
    }

    /**
     * Cancels this filter from rowset.
     * @throws IllegalStateException
     * @throws RowsetException
     */
    public void cancelFilter() throws RowsetException {
        if (rowset != null) {
            if (!filtering) {
                filtering = true;
                try {
                    if (filterApplied) {
                        if (originalRows != null) {
                            if (rowset.getRowsetChangeSupport().fireWillFilterEvent()) {
                                rowset.setActiveFilter(null);
                                for (int i = originalRows.size() - 1; i >= 0; i--) {
                                    if (originalRows.get(i).isDeleted()) {
                                        originalRows.remove(i);
                                    }
                                }
                                rowset.setSubsetAsCurrent(originalRows);
                                originalRows = null;
                                boolean positioned = rowset.absolute(originalPos);
                                if (!rowset.isEmpty() && !positioned) {
                                    rowset.first();
                                }
                                originalPos = 0;
                                rowset.getRowsetChangeSupport().fireFilteredEvent();
                            }
                        } else {
                            throw new RowsetException(ORIGINAL_ROWS_IS_MISSING);
                        }
                        filterApplied = false;
                        assert !isAnyFilterInstalled();
                    }
                } finally {
                    filtering = false;
                }
            }
        } else {
            throw new RowsetException(ROWSET_MISSING);
        }
    }

    /**
     * Deactivates this filter. It's similar to cancel filter, but it doesn't fire any events and doesn't perform any operations on rowset's set of rows.
     * Such behavior is needed in cases of global changes, inspired by rowset itself, such as refresh of thw whole data.
     */
    public void deactivate() {
        filterApplied = false;
        originalRows = null;
        originalPos = 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void build() throws RowsetException {   // check to avoid filter on filter
        if (!constrainting) {
            if (!isAnyFilterInstalled()) {
                ordered.clear();
                List<Row> rows = getRowsetRows();
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        add(rows.get(i));
                    }
                }
            } else {
                throw new RowsetException(CANT_BUILD_FILTER_ON_FILTER);
            }
        } else {
            throw new RowsetException(CANT_BUILD_FILTER_WHILE_CONSTRAINTING);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void die() {
        super.die();
        originalRows = null;
    }
}