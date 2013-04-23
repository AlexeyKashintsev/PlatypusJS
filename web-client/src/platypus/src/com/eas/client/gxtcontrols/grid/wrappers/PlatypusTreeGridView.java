package com.eas.client.gxtcontrols.grid.wrappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bearsoft.rowset.Row;
import com.eas.client.gxtcontrols.grid.valueproviders.ChangesHost;
import com.eas.client.gxtcontrols.published.PublishedStyle;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnData;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;

public class PlatypusTreeGridView extends TreeGridView<Row> implements PlatypusCellsView {

	protected Map<String, PublishedStyle> renderedCellStyles = new HashMap();

	public PlatypusTreeGridView() {
		super();
	}

	@Override
	public void addCellStyle(int aIndex, int aColumn, PublishedStyle aStyle) {
		if (aStyle != null)
			renderedCellStyles.put(aIndex + "_" + aColumn, aStyle);
	}

	@Override
	public void clearCellsStyles() {
		renderedCellStyles.clear();
	}

	/**
	 * Renders the grid view into safe HTML.
	 * 
	 * @param cs
	 *            the column attributes required for rendering
	 * @param rows
	 *            the data models for the rows to be rendered
	 * @param startRow
	 *            the index of the first row in <code>rows</code>
	 */
	@Override
	protected SafeHtml doRender(List<ColumnData> cs, List<Row> rows, int startRow) {
		int colCount = cm.getColumnCount();
		int last = colCount - 1;

		int[] columnWidths = getColumnWidths();

		// root builder
		SafeHtmlBuilder buf = new SafeHtmlBuilder();

		SafeStyles rowStyles = SafeStylesUtils.fromTrustedString("width: " + getTotalWidth() + "px;");

		String unselectableClass = " " + unselectable;
		String rowAltClass = " " + styles.rowAlt();
		String rowDirtyClass = " " + styles.rowDirty();

		String cellClass = styles.cell();
		String cellInnerClass = styles.cellInner();
		String cellFirstClass = " x-grid-cell-first";
		String cellLastClass = " x-grid-cell-last";
		String cellDirty = " " + styles.cellDirty();

		String rowWrap = styles.rowWrap();
		String rowBody = styles.rowBody();
		String rowBodyRow = styles.rowBodyRow();

		// loop over all rows
		for (int j = 0; j < rows.size(); j++) {
			Row model = rows.get(j);

			ListStore<Row>.Record r = ds.hasRecord(model) ? ds.getRecord(model) : null;

			int rowBodyColSpanCount = colCount;
			if (enableRowBody) {
				for (ColumnConfig<Row, ?> c : cm.getColumns()) {
					if (c instanceof RowExpander) {
						rowBodyColSpanCount--;
					}
				}
			}

			int rowIndex = (j + startRow);

			String rowClasses = styles.row();

			if (!selectable) {
				rowClasses += unselectableClass;
			}
			if (isStripeRows() && ((rowIndex + 1) % 2 == 0)) {
				rowClasses += rowAltClass;
			}

			if (super.isShowDirtyCells() && r != null && r.isDirty()) {
				rowClasses += rowDirtyClass;
			}

			if (viewConfig != null) {
				rowClasses += " " + viewConfig.getRowStyle(model, rowIndex);
			}

			SafeHtmlBuilder trBuilder = new SafeHtmlBuilder();

			// loop each cell per row
			for (int i = 0; i < colCount; i++) {
				SafeHtml rv = getRenderedValue(rowIndex, i, model, r);
				ColumnConfig<Row, ?> columnConfig = cm.getColumn(i);
				ColumnData columnData = cs.get(i);

				String cellClasses = cellClass;
				cellClasses += (i == 0 ? cellFirstClass : (i == last ? cellLastClass : ""));

				if (columnConfig.getColumnTextClassName() != null) {
					cellInnerClass += " " + columnConfig.getColumnTextClassName();
				}

				String id = columnConfig.getColumnClassSuffix();

				if (columnData.getClassNames() != null) {
					cellClasses += " " + columnData.getClassNames();
				}

				if (id != null && !id.equals("")) {
					cellClasses += " x-grid-td-" + id;
				}
				assert ((PlatypusColumnConfig) columnConfig).getValueProvider() instanceof ChangesHost;
				ChangesHost ch = (ChangesHost) ((PlatypusColumnConfig) columnConfig).getValueProvider();
				if (super.isShowDirtyCells() && r != null && columnConfig instanceof PlatypusColumnConfig && ch.isChanged(r.getModel())) {
					cellClasses += cellDirty;
				}

				if (viewConfig != null) {
					cellClasses += " " + viewConfig.getColStyle(model, cm.getValueProvider(i), rowIndex, i);
				}

				SafeStylesBuilder cellStyles = new SafeStylesBuilder();
				cellStyles.append(columnData.getStyles());

				PublishedStyle renderedCellStyle = renderedCellStyles.get(rowIndex + "_" + i);
				if (renderedCellStyle != null) {
					cellStyles.appendTrustedString(renderedCellStyle.toStyled());
				}

				SafeHtml tdContent = null;
				if (enableRowBody && i == 0) {
					tdContent = tpls.tdRowSpan(i, cellClasses, cellStyles.toSafeStyles(), super.getRowBodyRowSpan(), rv);
				} else {
					tdContent = tpls.td(i, cellClasses, cellStyles.toSafeStyles(), cellInnerClass, columnConfig.getColumnTextStyle(), rv);
				}
				trBuilder.append(tdContent);
			}

			if (enableRowBody) {
				String cls = styles.dataTable() + " x-grid-resizer";

				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				sb.append(tpls.tr("", trBuilder.toSafeHtml()));
				sb.appendHtmlConstant("<tr class=" + rowBodyRow + "><td colspan=" + rowBodyColSpanCount + "><div class=" + rowBody + "></div></td></tr>");

				buf.append(tpls.tr(rowClasses, tpls.tdWrap(colCount, "", rowWrap, tpls.table(cls, rowStyles, sb.toSafeHtml(), renderHiddenHeaders(columnWidths)))));

			} else {
				buf.append(tpls.tr(rowClasses, trBuilder.toSafeHtml()));
			}

		}
		renderedCellStyles.clear();
		// end row loop
		return buf.toSafeHtml();
	}
}