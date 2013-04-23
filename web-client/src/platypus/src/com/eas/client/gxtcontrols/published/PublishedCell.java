package com.eas.client.gxtcontrols.published;

import com.google.gwt.core.client.JavaScriptObject;
import com.sencha.gxt.core.client.dom.XElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;

public final class PublishedCell extends JavaScriptObject {
	protected PublishedCell() {
	}

	public final native String getDisplay()/*-{
		return this.display != null ? (this.display + '') : null;
	}-*/;

	public final native PublishedStyle getStyle()/*-{
		return this.style;
	}-*/;

	public final void styleToElement(XElement aElement) {
		if (aElement != null && getStyle() != null) {
			Style eStyle = aElement.getStyle();
			PublishedStyle pStyle = getStyle();
			if (pStyle.getBackground() != null)
				eStyle.setBackgroundColor(pStyle.getBackground().toStyled());
			if (pStyle.getForeground() != null) {
				eStyle.setColor(pStyle.getForeground().toStyled());
			}
			if (pStyle.getFont() != null) {
				eStyle.setFontSize(pStyle.getFont().getSize(), Unit.PT);
				eStyle.setFontStyle(pStyle.getFont().isItalic() ? FontStyle.ITALIC : FontStyle.NORMAL);
				eStyle.setFontWeight(pStyle.getFont().isBold() ? FontWeight.BOLD : FontWeight.NORMAL);
			}
		}
	}
}