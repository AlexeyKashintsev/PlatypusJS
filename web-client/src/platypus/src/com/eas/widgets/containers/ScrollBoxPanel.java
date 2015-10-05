/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.widgets.containers;

import com.eas.ui.XElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mg
 */
public class ScrollBoxPanel extends ScrollPanel implements IndexedPanel {

    public enum ScrollPolicy {

        ALLWAYS,
        NEVER,
        AUTO;
    }
    protected ScrollPolicy horizontalScrollPolicy = ScrollPolicy.AUTO;
    protected ScrollPolicy verticalScrollPolicy = ScrollPolicy.AUTO;

    public ScrollBoxPanel() {
        super();
		getElement().<XElement>cast().addResizingTransitionEnd(this);
    }

    public ScrollBoxPanel(Widget child) {
        super(child);
		getElement().<XElement>cast().addResizingTransitionEnd(this);
    }

    public ScrollPolicy getHorizontalScrollPolicy() {
        return horizontalScrollPolicy;
    }

    public void setHorizontalScrollPolicy(ScrollPolicy aValue) {
        horizontalScrollPolicy = aValue;
        if (isAttached()) {
            applyHorizontalScrollPolicy();
        }
    }

    public ScrollPolicy getVerticalScrollPolicy() {
        return verticalScrollPolicy;
    }

    public void setVerticalScrollPolicy(ScrollPolicy aValue) {
        verticalScrollPolicy = aValue;
        if (isAttached()) {
            applyVerticalScrollPolicy();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        applyHorizontalScrollPolicy();
        applyVerticalScrollPolicy();
    }

    private void applyVerticalScrollPolicy() {
        Style.Overflow value = Style.Overflow.AUTO;
        if (verticalScrollPolicy == ScrollPolicy.ALLWAYS) {
            value = Style.Overflow.SCROLL;
        } else if (verticalScrollPolicy == ScrollPolicy.NEVER) {
            value = Style.Overflow.HIDDEN;
        }
        getScrollableElement().getStyle().setOverflowY(value);
    }

    private void applyHorizontalScrollPolicy() {
        Style.Overflow value = Style.Overflow.AUTO;
        if (horizontalScrollPolicy == ScrollPolicy.ALLWAYS) {
            value = Style.Overflow.SCROLL;
        } else if (horizontalScrollPolicy == ScrollPolicy.NEVER) {
            value = Style.Overflow.HIDDEN;
        }
        getScrollableElement().getStyle().setOverflowX(value);
    }

	@Override
    public Widget getWidget(int index) {
	    return index == 0 ? getWidget() : null;
    }

	@Override
    public int getWidgetCount() {
	    return getWidget() == null ? 0 : 1;
    }

	@Override
    public int getWidgetIndex(Widget child) {
		return getWidget() == child ? 0 : -1;
    }

	@Override
    public boolean remove(int index) {
	    boolean res = index == 0 && getWidget() != null ? true : false;
	    if(res){
	    	setWidget(null);
	    }
	    return res;
    }
}
