/* Datamodel license.
 * Exclusive rights on this code in any form
 * are belong to it's author. This code was
 * developed for commercial purposes only. 
 * For any questions and any actions with this
 * code in any form you have to contact to it's
 * author.
 * All rights reserved.
 */
package com.eas.client.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bearsoft.rowset.Converter;
import com.bearsoft.rowset.Row;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.RowsetCallbackAdapter;
import com.bearsoft.rowset.dataflow.DelegatingFlowProvider;
import com.bearsoft.rowset.dataflow.TransactionListener;
import com.bearsoft.rowset.dataflow.TransactionListener.Registration;
import com.bearsoft.rowset.events.RowChangeEvent;
import com.bearsoft.rowset.events.RowsetDeleteEvent;
import com.bearsoft.rowset.events.RowsetFilterEvent;
import com.bearsoft.rowset.events.RowsetInsertEvent;
import com.bearsoft.rowset.events.RowsetListener;
import com.bearsoft.rowset.events.RowsetNetErrorEvent;
import com.bearsoft.rowset.events.RowsetRequeryEvent;
import com.bearsoft.rowset.events.RowsetRollbackEvent;
import com.bearsoft.rowset.events.RowsetSaveEvent;
import com.bearsoft.rowset.events.RowsetScrollEvent;
import com.bearsoft.rowset.events.RowsetSortEvent;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.filters.Filter;
import com.bearsoft.rowset.locators.Locator;
import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.Parameter;
import com.bearsoft.rowset.metadata.Parameters;
import com.bearsoft.rowset.sorting.RowsComparator;
import com.bearsoft.rowset.sorting.SortingCriterion;
import com.bearsoft.rowset.utils.IDGenerator;
import com.bearsoft.rowset.utils.KeySet;
import com.bearsoft.rowset.utils.RowsetUtils;
import com.eas.client.Callback;
import com.eas.client.Cancellable;
import com.eas.client.CancellableCallback;
import com.eas.client.CancellableCallbackAdapter;
import com.eas.client.Utils;
import com.eas.client.application.Application;
import com.eas.client.beans.PropertyChangeSupport;
import com.eas.client.form.api.JSEvents;
import com.eas.client.model.Model.ScriptEvent;
import com.eas.client.queries.Query;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;

/**
 * 
 * @author mg
 */
public class Entity implements RowsetListener {

	public static final String QUERY_REQUIRED = "All model entities must have a query";
	protected JavaScriptObject onBeforeChange;
	protected JavaScriptObject onAfterChange;
	protected JavaScriptObject onBeforeScroll;
	protected JavaScriptObject onAfterScroll;
	protected JavaScriptObject onBeforeInsert;
	protected JavaScriptObject onAfterInsert;
	protected JavaScriptObject onBeforeDelete;
	protected JavaScriptObject onAfterDelete;
	protected JavaScriptObject onRequeried;
	protected JavaScriptObject onFiltered;
	// for runtime
	protected List<Integer> filterConstraints = new ArrayList();
	protected Cancellable pending;
	protected Rowset rowset = null;
	protected boolean filteredWhileAjusting = false;
	protected Filter filter = null;
	protected boolean userFiltering = false;
	protected Map<List<Integer>, Locator> userLocators = new HashMap();
	// to preserve relation order
	protected List<Relation> rtInFilterRelations;
	protected int updatingCounter = 0;
	protected String title;
	protected String name; // data source name
	protected String entityId = String.valueOf((long) IDGenerator.genId());
	protected String queryId;
	protected Model model = null;
	protected Query query = null;
	protected Set<Relation> inRelations = new HashSet();
	protected Set<Relation> outRelations = new HashSet();
	protected Fields fields;
	protected PropertyChangeSupport changeSupport;

	public Entity() {
		super();
		changeSupport = new PropertyChangeSupport(this);
	}

	public Entity(Model aModel) {
		this();
		model = aModel;
	}

	public Entity(String aQueryId) {
		this();
		queryId = aQueryId;
	}

	public static native void publish(JavaScriptObject aModule, Entity aEntity) throws Exception/*-{
		var dsName = aEntity.@com.eas.client.model.Entity::getName()();
		if (dsName != undefined && dsName != null && dsName != '') {
			var publishedRowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
			Object.defineProperty(aModule, dsName, {
				get : function() {
					return publishedRowsetFacade;
				}
			});
		}
	}-*/;

	public static native JavaScriptObject publishEntityFacade(Entity aEntity) throws Exception/*-{

		function getRowset() {
			return aEntity.@com.eas.client.model.Entity::getRowset()();
		}
		
		function propsToArray(aObj)
		{
			var linearProps = [];
			for(var pName in aObj)
			{
				if(isNaN(pName) && pName != 'length')
				{
					linearProps.push(pName);
					linearProps.push(aObj[pName]);
				}
			}
			return linearProps;
		}
		if(aEntity != null)
		{
			var published = aEntity.@com.eas.client.model.Entity::getPublished()();
			if(published == null)
			{
				published = {
					// array mutator methods
					pop : function()
					{
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						var deleted = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(size);
						rowset.@com.bearsoft.rowset.Rowset::deleteAt(I)(size);
						return @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(deleted, aEntity);
					},
					shift : function()
					{
						var rowset = getRowset();
						var deleted = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(1);
						rowset.@com.bearsoft.rowset.Rowset::deleteAt(I)(1);
						return @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(deleted, aEntity);
					},
					push : function()
					{
						var rowset = getRowset();
						for(var i=0;i<arguments.length;i++)
						{
							var cSize = rowset.@com.bearsoft.rowset.Rowset::size()();
							var propsAsArray = propsToArray(arguments[i]);
							aEntity.@com.eas.client.model.Entity::insertAt(ILcom/google/gwt/core/client/JavaScriptObject;)(cSize+1, propsAsArray);
						}
						return rowset.@com.bearsoft.rowset.Rowset::size()();
					},
					unshift : function()
					{
						var rowset = getRowset();
						for(var i=arguments.length-1;i>=0;i--)
						{
							var propsAsArray = propsToArray(arguments[i]);
							aEntity.@com.eas.client.model.Entity::insertAt(ILcom/google/gwt/core/client/JavaScriptObject;)(1, propsAsArray);
						}
						return rowset.@com.bearsoft.rowset.Rowset::size()();
					},
					reverse : function()
					{
						var rowset = getRowset();
						rowset.@com.bearsoft.rowset.Rowset::reverse()();
					},
					splice : function()
					{
						if(arguments.length > 0)
						{
							var rowset = getRowset();
							var size = rowset.@com.bearsoft.rowset.Rowset::size()();
							var startAt = arguments[0];
							if(startAt < 0)
								startAt = size+startAt;
							if(startAt < 0)
								throw "Bad first argument 'index'. It should be less than or equal array's length by absolute value"; 
							var howMany = arguments.length > 1 ? arguments[1] : size;
							if(howMany < 0)
								throw "Bad second argument 'howMany'. It should greater or equal to zero"; 
							var toAdd = [];
							if(arguments.length > 2)
							{
								for(var ai=2; ai<arguments.length; ai++)
									toAdd.push(arguments[ai]);
							}
							var removed = [];
							while(startAt < size && removed.length < howMany)
							{
								var deleted = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(startAt+1);
								rowset.@com.bearsoft.rowset.Rowset::deleteAt(I)(startAt+1);
								var deletedFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(deleted, aEntity);
								removed.push(deletedFacade);
								size = rowset.@com.bearsoft.rowset.Rowset::size()();
							}
							for(var l=arguments.length-1;l>=2;l--)
							{						
								var propsAsArray = propsToArray(arguments[l]);
								aEntity.@com.eas.client.model.Entity::insertAt(ILcom/google/gwt/core/client/JavaScriptObject;)(startAt+1, propsAsArray);
							}
							return removed;
						}else
							throw "Bad arguments. There are must at least one argument";
					},
					sort : function(aComparator) {
						if(aComparator != null && aComparator != undefined)
						{
							if(aComparator.call != undefined)
							{
								aEntity.@com.eas.client.model.Entity::sort(Lcom/google/gwt/core/client/JavaScriptObject;)(aComparator);
							}else
								aEntity.@com.eas.client.model.Entity::sort(Lcom/bearsoft/rowset/sorting/RowsComparator;)(aComparator);
						}else
							throw "A comparing function or comparator object must be specified."; 
					},
				    // array accessor methods
				    concat : function()
				    {
				    	var i;
				    	var concated = [];
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						for(i=0;i<size;i++)
						{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
			    			concated.push(rowFacade);
						}
				    	for(i=0;i<arguments.length;i++)
				    	{
				    		if(Array.isArray(arguments[i]))
				    		{
				    			for(var l=0;l<arguments[i].length;l++)
				    			{
				    				concated.push(arguments[i][l]);
				    			}
				    		}else
				    		{ 
				    			concated.push(arguments[i]);
				    		}
				    	}
				    	return concated;
				    },
				    join : function(aSeparator)
				    {
				    	var joined = [];
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						for(var i=0;i<size;i++)
						{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							var sElement = "{";
							for(var l=0;l<rowFacade.length;l++)
							{
								if(l > 0)
									sElement += ", ";
								sElement += rowFacade.md[l].name + ":" + rowFacade[l];
							} 
							sElement += "}";
			    			joined.push(sElement);
						}
						return joined.join(aSeparator);
				    },
				    slice : function(startAt, endAt)
				    {
				    	var sliced = [];
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						if(startAt < 0)
							startAt = size+startAt;
						if(startAt < 0)
							throw "Bad first argument 'begin'. It should be less than or equal array's length by absolute value";
					 	if(endAt == undefined)
					 		endAt = size-1; 
						if(endAt < 0)
							endAt = size+endAt;
						if(endAt < 0)
							throw "Bad second argument 'end'. It should be less than or equal array's length by absolute value";
					 		
						for(var i=startAt;i<=endAt;i++)
						{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							sliced.push(rowFacade);
						}
						return sliced;
				    },
				    toString : function()
				    {
				    	var joined = [];
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						for(var i=0;i<size;i++)
						{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							var sElement = "{";
							for(var l=0;l<rowFacade.length;l++)
							{
								if(l > 0)
									sElement += ", ";
								sElement += rowFacade.md[l].name + ":" + rowFacade[l];
							} 
							sElement += "}";
			    			joined.push(sElement);
						}
						return "["+joined.join(",\n")+"]";
				    },
				    indexOf : function(aObj)
				    {
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
				    		if(rowFacade == aObj)
				    			return i; 
				    	}
				    	return -1;
				    },
				    lastIndexOf : function(aObj)
				    {
						var rowset = getRowset();
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=size-1;i>=0;i--)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
				    		if(rowFacade == aObj)
				    			return i; 
				    	}
				    	return -1;
				    },
				    // array iteration methods
				    filter : function(callback, thisObj){
				    	var filtered = [];
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							if(callback.call(thisObj, rowFacade, i, rowsetFacade))
								filtered.push(rowFacade);
				    	}
				    	return filtered;
				    },
				    forEach : function(callback, thisObj){
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							callback.call(thisObj, rowFacade, i, rowsetFacade);
				    	}
				    },
				    every : function(callback, thisObj){
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowsetFacade.length;
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							if(!callback.call(thisObj, rowFacade, i, rowsetFacade))
								return false;
				    	}
				    	return true;
				    },
				    map : function(callback, thisObj){
				    	var mapped = [];
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							mapped.push(callback.call(thisObj, rowFacade, i, rowsetFacade));
				    	}
				    	return mapped;
				    },
				    some : function(callback, thisObj){
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
				    	for(var i=0;i<size;i++)
				    	{
							var row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row, aEntity);
							if(callback.call(thisObj, rowFacade, i, rowsetFacade))
								return true;
				    	}
				    	return false;
				    },
				    reduce : function(callback, initialValue){
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						var startAt;
						var previousValue;
						if(initialValue == undefined)
						{
							startAt = 1;
							var _row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(1);
							previousValue = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(_row, aEntity);
							
						}else
						{
							startAt = 0;
							previousValue = initialValue;
						}
				    	for(var i=startAt;i<size;i++)
				    	{
							var row1 = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade1 = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row1, aEntity);
							previousValue = callback.call(null, previousValue, rowFacade1, i, rowsetFacade);
				    	}
				    	return previousValue;
				    },
				    reduceRight : function(callback, initialValue){
						var rowset = getRowset();
						var rowsetFacade = @com.eas.client.model.Entity::publishEntityFacade(Lcom/eas/client/model/Entity;)(aEntity);
						var size = rowset.@com.bearsoft.rowset.Rowset::size()();
						var startAt;
						var previousValue;
						if(initialValue == undefined)
						{
							startAt = size-2;
							var _row = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(size);
							previousValue = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(_row, aEntity);
							
						}else
						{
							startAt = size-1;
							previousValue = initialValue;
						}
				    	for(var i=startAt;i>=0;i--)
				    	{
							var row1 = rowset.@com.bearsoft.rowset.Rowset::getRow(I)(i+1);
							var rowFacade1 = @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(row1, aEntity);
							previousValue = callback.call(null, previousValue, rowFacade1, i, rowsetFacade);
				    	}
				    	return previousValue;
				    },
					// properties
					getQueryId : function() {
						return aEntity.@com.eas.client.model.Entity::getQueryId()();
					},
					isModified : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::isModified()();
						else
							return false;
					},
					isEmpty : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::isEmpty()();
						else
							return true;
					},
					isInserting : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::isInserting()();
						else
							return false;
					},
					getSize : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::size()();
						else
							return 0;
					},
					getRowIndex : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::getCursorPos()();
						else
							return -1;
					},
					setRowIndex : function(aRowIndex) {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::absolute(I)(aRowIndex);
					},
					getSubstitute : function() {
						return aEntity.@com.eas.client.model.Entity::getSubstitute()();
					},
					setSubstitute : function(aSubstitute) {
						aEntity.@com.eas.client.model.Entity::setSubstitute(Lcom/google/gwt/core/client/JavaScriptObject;)(aSubstitute);
					},
					// cursor interface 
					scrollTo : function(aRow) {
						return aEntity.@com.eas.client.model.Entity::scrollTo(Lcom/google/gwt/core/client/JavaScriptObject;)(aRow);
					},
					beforeFirst : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::beforeFirst()();
						else
							return false;
					},
					afterLast : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::afterLast()();
						else
							return false;
					},
					bof : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::isBeforeFirst()();
						else
							return false;
					},
					eof : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::isAfterLast()();
						else
							return false;
					},
					first : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::first()();
						else
							return false;
					},
					next : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::next()();
						else
							return false;
					},
					prev : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::previous()();
						else
							return false;
					},
					last : function() {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::last()();
						else
							return false;
					},
					pos : function(aIndex) {
						var rowset = getRowset();
						if(rowset != null)
							return rowset.@com.bearsoft.rowset.Rowset::absolute(I)(aIndex);
						else
							return false;
					},
					getRow : function(aIndex) {
						var rowset = getRowset();
						if(rowset != null)
							return @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(rowset.@com.bearsoft.rowset.Rowset::getRow(I)(aIndex), aEntity);
						else
							return null;
					},
					// find interface
					find : function() {
						return aEntity.@com.eas.client.model.Entity::find(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					findById : function(aValue) {
						return aEntity.@com.eas.client.model.Entity::findById(Ljava/lang/Object;)($wnd.boxAsJava(aValue));
					},
					// relations interface
					beginUpdate : function() {
						aEntity.@com.eas.client.model.Entity::beginUpdate()();
					},
					endUpdate : function() {
						aEntity.@com.eas.client.model.Entity::endUpdate()();
					},
					// 
					execute : function() {
						aEntity.@com.eas.client.model.Entity::execute(Lcom/eas/client/CancellableCallback;Lcom/eas/client/Callback;)(null, null);
					},
					executeChildrenOnly : function() {
						aEntity.@com.eas.client.model.Entity::executeChildren()();
					},
					requery : function(aCallback) {
						aEntity.@com.eas.client.model.Entity::refresh(Lcom/eas/client/CancellableCallback;Lcom/eas/client/Callback;)(null, null);
					},
					requeryChildrenOnly : function() {
						aEntity.@com.eas.client.model.Entity::refreshChildren()();
					},
					// processing interface
					createLocator : function() {
						return aEntity.@com.eas.client.model.Entity::createLocator(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					createFilter : function() {
						return aEntity.@com.eas.client.model.Entity::createFilter(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					createSorting : function() {
						return aEntity.@com.eas.client.model.Entity::createSorting(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					// modify interface
					getObject : function(aColIndex) {
						var rowset = getRowset();
						if(rowset != null)
						{
							var rValue = $wnd.boxAsJs(rowset.@com.bearsoft.rowset.Rowset::getJsObject(I)(aColIndex));
							if(rValue == null)
							{
								var s = aEntity.@com.eas.client.model.Entity::getSubstitute()();
								if(s != null)
									rValue = s.getObject(aColIndex);
							}
							return rValue;
						}else
							return null;
					},
					updateObject : function(aColIndex, aValue) {
						var rowset = getRowset();
						if(rowset != null)
							rowset.@com.bearsoft.rowset.Rowset::updateJsObject(ILjava/lang/Object;)(aColIndex, $wnd.boxAsJava(aValue));
					},
					insert : function() {
						aEntity.@com.eas.client.model.Entity::insert(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					deleteAll : function() {
						var rowset = getRowset();
						if(rowset != null)
							rowset.@com.bearsoft.rowset.Rowset::deleteAll()();
					},
					deleteRow : function(aRowIndex) {
						var rowset = getRowset();
						if(rowset != null)
						{
							if(aRowIndex != undefined)
								rowset.@com.bearsoft.rowset.Rowset::deleteAt(I)(aRowIndex);
							else
								rowset.@com.bearsoft.rowset.Rowset::delete()();
						}
					},
					unwrap : function() {
						return aEntity;
					}
				};			
				// properties
				Object.defineProperty(published, "queryId",    { get : function(){ return published.getQueryId()}});
				Object.defineProperty(published, "modified",   { get : function(){ return published.isModified()}});
				Object.defineProperty(published, "empty",      { get : function(){ return published.isEmpty()}});
				Object.defineProperty(published, "inserting",  { get : function(){ return published.isInserting()}});
				Object.defineProperty(published, "size",       { get : function(){ return published.getSize()}});
				Object.defineProperty(published, "length",     { get : function(){ return published.getSize()}});
				Object.defineProperty(published, "rowIndex",   { get : function(){ return published.getRowIndex()}});
				Object.defineProperty(published, "substitute", { get : function(){ return published.getSubstitute()}, set : function(aValue){ published.setSubstitute(aValue)}});
				
				Object.defineProperty(published, "md",         { get : function(){ return @com.eas.client.model.Entity::publishFieldsFacade(Lcom/bearsoft/rowset/metadata/Fields;Lcom/eas/client/model/Entity;)(aEntity.@com.eas.client.model.Entity::getFields()(), aEntity) }});
				// default row dynamic properties interface
				for(var i=0;i<published.md.length;i++)
				{
					(function(){
						var _i = i;
						Object.defineProperty(published, published.md[_i].name,
						{
							 get : function(){ return published.getObject(_i+1); },
							 set : function(aValue){ published.updateObject(_i+1, aValue); }
						});
					})();
				}
				// params
				var nativeQuery = aEntity.@com.eas.client.model.Entity::getQuery()();
				if(nativeQuery != null)// Parameters entity has no query
				{
					var nativeParams = nativeQuery.@com.eas.client.queries.Query::getParameters()();
					var publishedParams = {};  
					Object.defineProperty(publishedParams, "md", { get : function(){ return @com.eas.client.model.Entity::publishFieldsFacade(Lcom/bearsoft/rowset/metadata/Fields;Lcom/eas/client/model/Entity;)(nativeParams, aEntity); }});
					Object.defineProperty(publishedParams, "length", { get : function(){ return publishedParams.md.length; }});
					for(var i=0;i<publishedParams.md.length;i++)
					{
						(function(){
							var _i = i;
							var propDesc = {
								 get : function(){ return publishedParams.md[_i].value; },
								 set : function(aValue){ publishedParams.md[_i].value = aValue; }
							};
							Object.defineProperty(publishedParams, publishedParams.md[_i].name, propDesc);
							Object.defineProperty(publishedParams, (_i+""), propDesc);
						})();
					}
					
					Object.defineProperty(published, "params", {
						get : function(){
							return publishedParams;
						}
					});
				}
				// events
				Object.defineProperty(published, "willChange", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnBeforeChange()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnBeforeChange(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "willDelete", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnBeforeDelete()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnBeforeDelete(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "willInsert", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnBeforeInsert()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnBeforeInsert(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "willScroll", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnBeforeScroll()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnBeforeScroll(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onChanged", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnAfterChange()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnAfterChange(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onDeleted", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnAfterDelete()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnAfterDelete(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onFiltered", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnFiltered()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnFiltered(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onInserted", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnAfterInsert()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnAfterInsert(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onRequeried", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnRequeried()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnRequeried(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				Object.defineProperty(published, "onScrolled", {
					get : function()
					{
						return aEntity.@com.eas.client.model.Entity::getOnAfterScroll()();
					},
					set : function(aValue)
					{
						aEntity.@com.eas.client.model.Entity::setOnAfterScroll(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
					}
				});
				aEntity.@com.eas.client.model.Entity::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		}else
			return null;
	}-*/;

	public static native void publishRows(JavaScriptObject aPublished) throws Exception/*-{
		for ( var j = 0; j < aPublished.length; j++) {
			aPublished[(j + "")] = aPublished.getRow(j + 1);
		}
	}-*/;

	public static native JavaScriptObject publishRowFacade(Row aRow, Entity aEntity) throws Exception/*-{
		if(aRow != null)
		{
			var published = aRow.@com.bearsoft.rowset.Row::getPublished()();
			if(published == null)
			{
				published = {
					getColumnObject : function(aFieldName) {
						return $wnd.boxAsJs(aRow.@com.bearsoft.rowset.Row::getFieldObject(Ljava/lang/String;)(aFieldName));
					},
					setColumnObject : function(aFieldName, aValue) {
						aRow.@com.bearsoft.rowset.Row::setFieldObject(Ljava/lang/String;Ljava/lang/Object;)(aFieldName, $wnd.boxAsJava(aValue));
					},
					unwrap : function() {
						return aRow;
					} 
				};
				Object.defineProperty(published, "md", { get : function(){ return @com.eas.client.model.Entity::publishFieldsFacade(Lcom/bearsoft/rowset/metadata/Fields;Lcom/eas/client/model/Entity;)(aRow.@com.bearsoft.rowset.Row::getFields()(), aEntity); }});
				Object.defineProperty(published, "length", { get : function(){ return published.md.length; }});
				for(var i=0;i<published.md.length;i++)
				{
					(function(){
						var _i = i;
						var propDesc = {
							 get : function(){ return published.getColumnObject(published.md[_i].name); },
							 set : function(aValue){ published.setColumnObject(published.md[_i].name, aValue); }
						};
						Object.defineProperty(published, published.md[_i].name, propDesc);
						Object.defineProperty(published, (_i+""),     propDesc);
					})();
				}
				aRow.@com.bearsoft.rowset.Row::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		}else
			return null;
	}-*/;

	public static native JavaScriptObject publishFieldsFacade(Fields aFields, Entity aEntity) throws Exception/*-{
		if(aFields != null)
		{
			var published = aFields.@com.bearsoft.rowset.metadata.Fields::getPublished()();
			if(published == null)
			{
				published = {
					getFieldsCount : function() {
						return aFields.@com.bearsoft.rowset.metadata.Fields::getFieldsCount()();
					},
					isEmpty : function() {
						return aFields.@com.bearsoft.rowset.metadata.Fields::isEmpty()();
					},
					get : function(aFieldIndex) {
						return @com.eas.client.model.Entity::publishFieldFacade(Lcom/bearsoft/rowset/metadata/Field;)(aFields.@com.bearsoft.rowset.metadata.Fields::get(I)(aFieldIndex));
					},
					getTableDescription : function() {
						return aFields.@com.bearsoft.rowset.metadata.Fields::getTableDescription()();
					},
					unwrap : function()
					{
						return aFields;
					}
				};
				
				Object.defineProperty(published, "empty", { get : function(){ return published.isEmpty()}});
				Object.defineProperty(published, "tableDescription", { get : function(){ return published.getTableDescription()}});
				Object.defineProperty(published, "length", { get : function(){ return published.getFieldsCount()}});
				
				for(var i = 0; i < published.length; i++)
				{
					(function(){
						var _i = i;
						Object.defineProperty(published, (_i+""), { get : function(){ return published.get(_i+1) }});
						Object.defineProperty(published, published.get(_i+1).name, { get : function(){ return published.get(_i+1) }});
					})();
				}
				aFields.@com.bearsoft.rowset.metadata.Fields::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		}else
			return null;
	}-*/;

	public static native JavaScriptObject publishFieldFacade(Field aField) throws Exception/*-{
		if(aField != null)
		{
			var published = aField.@com.bearsoft.rowset.metadata.Field::getPublished()();
			if (published == null) {
				published = {
					getName : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::getName()();
					},
					getDescription : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::getDescription()();
					},
					getSize : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::getSize()();
					},
					isPk : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::isPk()();
					},
					setPk : function(aValue) {
						aField.@com.bearsoft.rowset.metadata.Field::setPk(Z)(aValue);
					},
					isStrong4Insert : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::isStrong4Insert()();
					},
					setStrong4Insert : function(aValue) {
						aField.@com.bearsoft.rowset.metadata.Field::setStrong4Insert(Z)(aValue);
					},
					isNullable : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::isNullable()();
					},
					isReadonly : function() {
						return aField.@com.bearsoft.rowset.metadata.Field::isReadonly()();
					},
					unwrap : function() {
						return aField;
					}
				};
				Object.defineProperty(published, "name", {
					get : function() {
						return published.getName();
					}
				});
				Object.defineProperty(published, "description", {
					get : function() {
						return published.getDescription();
					}
				});
				Object.defineProperty(published, "size", {
					get : function() {
						return published.getSize();
					}
				});
				Object.defineProperty(published, "pk", {
					get : function() {
						return published.isPk();
					},
					set : function(aValue) {
						published.setPk(aValue);
					}
				});
				Object.defineProperty(published, "strong4Insert", {
					get : function() {
						return published.isStrong4Insert();
					},
					set : function(aValue) {
						published.setStrong4Insert(aValue);
					}
				});
				Object.defineProperty(published, "nullable", {
					get : function() {
						return published.isNullable();
					}
				});
				Object.defineProperty(published, "readonly", {
					get : function() {
						return published.isReadonly();
					}
				});
				if(@com.eas.client.model.Entity::isParameter(Lcom/bearsoft/rowset/metadata/Field;)(aField))
				{
					Object.defineProperty(published, "modified", {
						get : function() {
							return aField.@com.bearsoft.rowset.metadata.Parameter::isModified()();
						}
					});
					Object.defineProperty(published, "value", {
						get : function() {
							return $wnd.boxAsJs(aField.@com.bearsoft.rowset.metadata.Parameter::getJsValue()());
						},
						set : function(aValue) {
							aField.@com.bearsoft.rowset.metadata.Parameter::setJsValue(Ljava/lang/Object;)($wnd.boxAsJava(aValue));
						}
					});
				}
				aField.@com.bearsoft.rowset.metadata.Field::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		} else
			return null;
	}-*/;

	public static native JavaScriptObject publishLocatorFacade(Locator loc, Entity aEntity) throws Exception/*-{
		if(loc != null)
		{
			var published = loc.@com.bearsoft.rowset.locators.Locator::getPublished()();
			if(published == null)
			{
				published = {
					first : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::first()();
					},
					last : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::last()();
					},
					next : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::next()();
					},
					prev : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::previous()();
					},
					isBeforeFirst : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::isBeforeFirst()();
					},
					isAfterLast : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::isAfterLast()();
					},
					find : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::find(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					getRow : function(aIndex) {
						return @com.eas.client.model.Entity::publishRowFacade(Lcom/bearsoft/rowset/Row;Lcom/eas/client/model/Entity;)(loc.@com.bearsoft.rowset.locators.Locator::getRow(I)(aIndex), aEntity);
					},
					getSize : function() {
						return loc.@com.bearsoft.rowset.locators.Locator::getSize()();
					},
					unwrap : function() {
						return loc;
					}
				}
				loc.@com.bearsoft.rowset.locators.Locator::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		} else
			return null;
	}-*/;

	public static native JavaScriptObject publishFilterFacade(Filter aFilter, Entity aEntity) throws Exception/*-{
		if (aFilter != null) {
			var published = aFilter.@com.bearsoft.rowset.filters.Filter::getPublished()();
			if (published == null) {
				published = {
					apply : function() {
						aEntity.@com.eas.client.model.Entity::setUserFiltering(Z)(true);
						aFilter.@com.bearsoft.rowset.filters.Filter::apply(Lcom/google/gwt/core/client/JavaScriptObject;)(arguments);
					},
					isApplied : function() {
						return aFilter.@com.bearsoft.rowset.filters.Filter::isApplied()();
					},
					cancel : function() {
						aFilter.@com.bearsoft.rowset.filters.Filter::cancelFilter()();
					},
					unwrap : function() {
						return aFilter;
					}
				}
				aFilter.@com.bearsoft.rowset.filters.Filter::setPublished(Lcom/google/gwt/core/client/JavaScriptObject;)(published);
			}
			return published;
		} else
			return null;
	}-*/;

	public PropertyChangeSupport getChangeSupport() {
		return changeSupport;
	}

	public Fields getFields() {
		if (fields == null) {
			try {
				assert query != null : "Query must present";
				if (query != null) {
					fields = query.getFields();
					if (fields == null) {
						fields = getFactFields();
					}
					assert fields != null;
				}
			} catch (Exception ex) {
				fields = null;
				Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (fields != null)
			fields.setOwner(this);
		return fields;
	}

	public void clearFields() {
		if (fields != null)
			fields.setOwner(null);
		fields = null;
		setQuery(null);
	}

	public Model getModel() {
		return model;
	}

	public void regenerateId() {
		entityId = String.valueOf(IDGenerator.genId());
	}

	public void setModel(Model aValue) {
		model = aValue;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String aValue) {
		entityId = aValue;
	}

	public String getTitle() {
		String ltitle = title;
		if (ltitle == null || ltitle.isEmpty()) {
			try {
				Query lquery = getQuery();
				ltitle = lquery.getTitle();
			} catch (Exception ex) {
				Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
				ltitle = "";
			}
			setTitle(ltitle);
		}
		return ltitle;
	}

	public void setTitle(String aValue) {
		title = aValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String aValue) {
		name = aValue;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String aValue) {
		if (queryId == null ? aValue != null : !queryId.equals(aValue)) {
			setQuery(null);
			rowset = null;
			filter = null;
		}
		queryId = aValue;
	}

	public Query getQuery() throws Exception {
		assert query != null : "Query must present";
		return query;
	}

	public void setQuery(Query aValue) {
		if (query != null)
			query.getParameters().setOwner(null);
		query = aValue;
		if (query != null)
			query.getParameters().setOwner(this);
	}

	public boolean removeOutRelation(Relation aRelation) {
		return outRelations.remove(aRelation);
	}

	public boolean removeInRelation(Relation aRelation) {
		return inRelations.remove(aRelation);
	}

	public boolean addOutRelation(Relation aRelation) {
		return outRelations.add(aRelation);
	}

	public boolean addInRelation(Relation aRelation) {
		return inRelations.add(aRelation);
	}

	public Set<Relation> getInRelations() {
		return inRelations;
	}

	public Set<Relation> getOutRelations() {
		return outRelations;
	}

	public Set<Relation> getInOutRelations() {
		Set<Relation> lInOutRelations = new HashSet();
		lInOutRelations.addAll(inRelations);
		lInOutRelations.addAll(outRelations);
		return lInOutRelations;
	}

	protected boolean isTagValid(String aTagName) {
		return true;
	}

	protected Cancellable achieveOrRefreshRowset(final CancellableCallback onSuccess, final Callback<String> onFailure) throws Exception {
		if (query != null) {
			if (rowset == null) {
				// The first time we obtain a rowset...
				final Entity rowsetListener = this;
				return query.execute(new RowsetCallbackAdapter() {

					@Override
					public void doWork(Rowset aRowset) throws Exception {
						rowset = aRowset;
						rowset.currentToOriginal();
						rowset.setFlowProvider(new DelegatingFlowProvider(rowset.getFlowProvider()) {
							public java.util.List<com.bearsoft.rowset.changes.Change> getChangeLog() {
								return model.getChangeLog();
							};

							@Override
							public Registration addTransactionListener(TransactionListener aListener) {
								return model.addTransactionListener(aListener);
							}
						});
						rowset.first();
						rowset.addRowsetListener(rowsetListener);
						changeSupport.firePropertyChange("rowset", null, rowset);
						rowset.getRowsetChangeSupport().fireRequeriedEvent();
						onSuccess.run();
					}
				}, new Callback<String>() {
					@Override
					public void run(String aResult) throws Exception {
						changeSupport.firePropertyChange("rowsetError", null, aResult);
						if (onFailure != null)
							onFailure.run(aResult);
					}

					@Override
					public void cancel() {
					}
				});
			} else {
				return rowset.refresh(query.getParameters(), onSuccess, onFailure);
			}
		}
		return null;
	}

	public void validateQuery() throws Exception {
		if (query == null) {
			setQuery(Application.getAppQuery(queryId));
		}
	}

	public Entity copy() throws Exception {
		assert model != null : "Entities can't exist without a model";
		Entity copied = new Entity(model);
		assign(copied);
		return copied;
	}

	public JavaScriptObject getOnAfterChange() {
		return onAfterChange;
	}

	public JavaScriptObject getOnAfterDelete() {
		return onAfterDelete;
	}

	public JavaScriptObject getOnAfterInsert() {
		return onAfterInsert;
	}

	public JavaScriptObject getOnAfterScroll() {
		return onAfterScroll;
	}

	public JavaScriptObject getOnBeforeChange() {
		return onBeforeChange;
	}

	public JavaScriptObject getOnBeforeDelete() {
		return onBeforeDelete;
	}

	public JavaScriptObject getOnBeforeInsert() {
		return onBeforeInsert;
	}

	public JavaScriptObject getOnBeforeScroll() {
		return onBeforeScroll;
	}

	public JavaScriptObject getOnFiltered() {
		return onFiltered;
	}

	public JavaScriptObject getOnRequeried() {
		return onRequeried;
	}

	public void setOnAfterChange(JavaScriptObject aValue) {
		onAfterChange = aValue;
	}

	public void setOnAfterDelete(JavaScriptObject aValue) {
		onAfterDelete = aValue;
	}

	public void setOnAfterInsert(JavaScriptObject aValue) {
		onAfterInsert = aValue;
	}

	public void setOnAfterScroll(JavaScriptObject aValue) {
		onAfterScroll = aValue;
	}

	public void setOnBeforeChange(JavaScriptObject aValue) {
		onBeforeChange = aValue;
	}

	public void setOnBeforeDelete(JavaScriptObject aValue) {
		onBeforeDelete = aValue;
	}

	public void setOnBeforeInsert(JavaScriptObject aValue) {
		onBeforeInsert = aValue;
	}

	public void setOnBeforeScroll(JavaScriptObject aValue) {
		onBeforeScroll = aValue;
	}

	public void setOnFiltered(JavaScriptObject aValue) {
		onFiltered = aValue;
	}

	public void setOnRequeried(JavaScriptObject aValue) {
		onRequeried = aValue;
	}

	/*
	 * private void silentFirst() throws InvalidCursorPositionException {
	 * rowset.removeRowsetListener(this); try { rowset.first(); } finally {
	 * rowset.addRowsetListener(this); } }
	 */

	public void beginUpdate() {
		updatingCounter++;
	}

	public void endUpdate() throws Exception {
		assert updatingCounter > 0;
		updatingCounter--;
		if (updatingCounter == 0) {
			internalExecuteChildren(false);
		}
	}

	public boolean isPending() {
		return pending != null;
	}

	public boolean isRowsetPresent() {
		return rowset != null;
	}

	protected Fields getFactFields() throws Exception {
		if (rowset != null) {
			return rowset.getFields();
		}
		return null;
	}

	public void refresh(final CancellableCallback onSuccess, Callback<String> onFailure) throws Exception {
		if (model != null && model.isRuntime()) {
			internalExecute(true, onSuccess, onFailure);
			/*
			 * internalExecute(true, new CancellableCallbackAdapter() {
			 * 
			 * @Override public void doWork() throws Exception {
			 * onSuccess.run(); internalExecuteChildren(true); } });
			 */
		}
	}

	public void execute(final CancellableCallback onSuccess, Callback<String> onFailure) throws Exception {
		if (model != null && model.isRuntime()) {
			internalExecute(false, onSuccess, onFailure);
			/*
			 * internalExecute(false, new CancellableCallbackAdapter() {
			 * 
			 * @Override public void doWork() throws Exception {
			 * onSuccess.run(); internalExecuteChildren(false); } });
			 */
		}
	}

	protected void internalExecute(boolean refresh, final CancellableCallback onSuccess, Callback<String> onFailure) throws Exception {
		if (model != null && model.isRuntime()) {
			assert query != null : QUERY_REQUIRED;
			// try to select any data only within non-dml queries
			// platypus dml queries are:
			// - insert, update, delete queries;
			// - stored procedures, witch changes data.
			if (!query.isDml()) {
				if (pending != null)
					pending.cancel();
				if (refresh) {
					uninstallUserFiltering();
				}
				// There might be entities - parameters values sources, with no
				// data in theirs rowsets,
				// so we can't bind query parameters to proper values. In the
				// such case we initialize
				// parameters values with RowsetUtils.UNDEFINED_SQL_VALUE
				final Map<String, Object> oldParamValues = new HashMap();
				for (int i = 1; i <= query.getParameters().getParametersCount(); i++) {
					Parameter p = query.getParameters().get(i);
					oldParamValues.put(p.getName(), p.getValue());
				}
				boolean parametersBinded = bindQueryParameters();
				if (rowset == null || parametersBinded || refresh) {
					// if we have no rowset yet or query parameters values have
					// been changed ...
					// or we are forced to refresh the data.
					// re-query ...
					uninstallUserFiltering();
					final Cancellable lexecuting = achieveOrRefreshRowset(new CancellableCallbackAdapter() {

						@Override
						public void doWork() throws Exception {
							assert rowset != null;
							filterRowset();
							pending = null;
							model.pumpEvents();
							if (onSuccess != null)
								onSuccess.run();
						}
					}, onFailure);
					pending = new Cancellable() {

						@Override
						public void cancel() {
							for (int i = 1; i <= query.getParameters().getParametersCount(); i++) {
								Parameter p = query.getParameters().get(i);
								p.setValue(oldParamValues.get(p.getName()));
								p.setModified(false);
							}
							lexecuting.cancel();
						}
					};
				} else {
					// There might be a case of only rowset filtering
					assert rowset != null;
					filterRowset();
					if (pending != null) {
						pending = null;
						model.pumpEvents();
					}
				}
			}
		}
	}

	protected void uninstallUserFiltering() throws RowsetException {
		if (userFiltering && rowset != null && rowset.getActiveFilter() != null) {
			rowset.getActiveFilter().cancelFilter();
		}
		userFiltering = false;
	}

	public void refreshChildren() throws Exception {
		internalExecuteChildren(true);
	}

	protected void executeChildren() throws Exception {
		internalExecuteChildren(false);
	}

	protected void internalExecuteChildren(boolean refresh) throws Exception {
		if (updatingCounter == 0) {
			Set<Relation> rels = getOutRelations();
			if (rels != null) {
				Set<Entity> toExecute = new HashSet();
				for (Relation outRel : rels) {
					if (outRel != null) {
						Entity ent = outRel.getRightEntity();
						if (ent != null) {
							toExecute.add(ent);
						}
					}
				}
				model.executeEntities(refresh, toExecute);
			}
		}
	}

	protected void internalExecuteChildren(boolean refresh, int aOnlyFieldIndex) throws Exception {
		if (updatingCounter == 0) {
			Set<Relation> rels = getOutRelations();
			if (rels != null) {
				String onlyFieldName = getFields().get(aOnlyFieldIndex).getName();
				Set<Entity> toExecute = new HashSet();
				for (Relation outRel : rels) {
					if (outRel != null) {
						Entity ent = outRel.getRightEntity();
						if (ent != null && outRel.isLeftField() && outRel.getLeftField().equalsIgnoreCase(onlyFieldName)) {
							toExecute.add(ent);
						}
					}
				}
				model.executeEntities(refresh, toExecute);
			}
		}
	}

	public void setRowset(Rowset aRowset) {
		rowset = aRowset;
	}

	public boolean isUserFiltering() {
		return userFiltering;
	}

	public void setUserFiltering(boolean aUserFiltering) throws Exception {
		boolean oldUserFiltering = userFiltering;
		userFiltering = aUserFiltering;
		if (oldUserFiltering != userFiltering) {
			if (rowset.getActiveFilter() != null) {
				rowset.getActiveFilter().cancelFilter();
			}
			execute(new CancellableCallbackAdapter() {

				@Override
				public void doWork() throws Exception {
					// no op
				}
			}, null);
		}
	}

	protected boolean isFilterable() throws Exception {
		return rowset != null && !userFiltering && rtInFilterRelations != null && !rtInFilterRelations.isEmpty();
	}

	public boolean bindQueryParameters() throws Exception {
		Query selfQuery = getQuery();
		if (selfQuery != null) {
			Parameters selfParameters = selfQuery.getParameters();
			boolean parametersModified = false;
			Set<Relation> inRels = getInRelations();
			if (inRels != null && !inRels.isEmpty()) {
				for (Relation relation : inRels) {
					if (relation != null && relation.isRightParameter()) {
						Entity leftEntity = relation.getLeftEntity();
						if (leftEntity != null) {
							Object pValue = null;
							if (relation.isLeftField()) {
								Rowset leftRowset = leftEntity.getRowset();
								if (leftRowset != null && !leftRowset.isEmpty() && !leftRowset.isBeforeFirst() && !leftRowset.isAfterLast()) {
									try {
										pValue = leftRowset.getObject(leftRowset.getFields().find(relation.getLeftField()));
									} catch (Exception ex) {
										pValue = RowsetUtils.UNDEFINED_SQL_VALUE;
										Logger.getLogger(Entity.class.getName()).log(Level.SEVERE,
										        "while assigning parameter:" + relation.getRightParameter() + " in entity: " + getTitle() + " [" + String.valueOf(getEntityId()) + "]", ex);
									}
								} else {
									pValue = RowsetUtils.UNDEFINED_SQL_VALUE;
								}
							} else {
								Query leftQuery = leftEntity.getQuery();
								assert leftQuery != null : "Left query must present (Relation points to query, but query is absent)";
								Parameters leftParams = leftQuery.getParameters();
								assert leftParams != null : "Parameters of left query must present (Relation points to query parameter, but query parameters are absent)";
								Parameter leftParameter = leftParams.get(relation.getLeftParameter());
								if (leftParameter != null) {
									pValue = leftParameter.getValue();
									if (pValue == null) {
										pValue = leftParameter.getDefaultValue();
									}
								} else {
									Logger.getLogger(Entity.class.getName()).log(
									        Level.SEVERE,
									        "Parameter of left query must present (Relation points to query parameter " + relation.getRightParameter() + " in entity: " + getTitle() + " ["
									                + String.valueOf(getEntityId()) + "], but query parameter with specified name is absent)");
								}
							}
							Parameter selfPm = selfParameters.get(relation.getRightParameter());
							if (selfPm != null) {
								Object selfValue = selfPm.getValue();
								if ((selfValue == null && pValue != null) || (selfValue != null && !selfValue.equals(pValue))) {
									selfPm.setValue(pValue);
								}
							}
						} else {
							Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, "Relation had no left entity");
						}
					}
				}
			}
			for (int i = 1; i <= selfParameters.getFieldsCount(); i++) {
				Parameter param = (Parameter) selfParameters.get(i);
				if (param.isModified()) {
					parametersModified = true;
					param.setModified(false);
				}
			}
			return parametersModified;
		}
		return false;
	}

	protected void validateInFilterRelations() {
		// never build yet, so build it ...
		if (rtInFilterRelations == null) {
			rtInFilterRelations = new ArrayList();
			assert rowset != null;
			Set<Relation> inRels = getInRelations();
			if (inRels != null) {
				for (Relation rel : inRels) {
					if (rel != null && rel.isRightField()) {
						rtInFilterRelations.add(rel);
					}
				}
			}
		}
	}

	protected void validateFilter() throws RowsetException {
		assert rtInFilterRelations != null;
		assert rowset != null;
		if (filter == null && !rtInFilterRelations.isEmpty()) {
			List<String> constraints = new ArrayList();
			// enumerate filtering relations ...
			for (Relation rel : rtInFilterRelations) {
				assert rel != null && rel.isRightField();
				constraints.add(rel.getRightField());
			}
			if (!constraints.isEmpty()) {
				filter = rowset.createFilter();
				filter.beginConstrainting();
				try {
					Fields rFields = rowset.getFields();
					for (String fieldName : constraints) {
						filter.addConstraint(rFields.find(fieldName));
					}
				} finally {
					filter.endConstrainting();
				}
				filter.build();
			}
		}
	}

	public boolean filterRowset() throws Exception {
		validateInFilterRelations();
		if (isFilterable()) {
			validateFilter();
			return applyFilter();
		} else {
			return false;
		}
	}

	public boolean applyFilter() throws Exception {
		assert !userFiltering : "Can't apply own filter while user filtering";
		assert rowset != null : "Bad requery -> filter chain";
		KeySet filterKeySet = new KeySet();
		if (!rtInFilterRelations.isEmpty()) {
			for (Relation rel : rtInFilterRelations) {
				// relation must be filtering relation ...
				assert rel != null && rel.isRightField();
				Entity leftEntity = rel.getLeftEntity();
				assert leftEntity != null;
				Object fValue = null;
				if (rel.isLeftField()) {
					Rowset leftRowset = leftEntity.getRowset();
					if (leftRowset != null) {
						if (!leftRowset.isEmpty()) {
							if (!leftRowset.isBeforeFirst() && !leftRowset.isAfterLast()) {
								fValue = leftRowset.getObject(leftRowset.getFields().find(rel.getLeftField()));
							} else {
								fValue = RowsetUtils.UNDEFINED_SQL_VALUE;
								Logger.getLogger(Entity.class.getName()).log(
								        Level.FINE,
								        "Failed to achieve value for filtering field:" + rel.getRightField() + " in entity: " + getTitle() + " [" + String.valueOf(getEntityId())
								                + "]. The source rowset has bad position (before first or after last).");
							}
						} else {
							fValue = RowsetUtils.UNDEFINED_SQL_VALUE;
							Logger.getLogger(Entity.class.getName()).log(
							        Level.FINE,
							        "Failed to achieve value for filtering field:" + rel.getRightField() + " in entity: " + getTitle() + " [" + String.valueOf(getEntityId())
							                + "]. The source rowset has no any rows.");
						}
					} else {
						fValue = RowsetUtils.UNDEFINED_SQL_VALUE;
						Logger.getLogger(Entity.class.getName()).log(
						        Level.FINE,
						        "Failed to achieve value for filtering field:" + rel.getRightField() + " in entity: " + getTitle() + " [" + String.valueOf(getEntityId())
						                + "]. The source rowset is absent.");
					}
				} else {
					Query leftQuery = leftEntity.getQuery();
					assert leftQuery != null : "Left query must present (Relation points to query, but query is absent)";
					Parameters leftParams = leftQuery.getParameters();
					assert leftParams != null : "Parameters of left query must present (Relation points to query parameter, but query parameters are absent)";
					Parameter leftParameter = leftParams.get(rel.getLeftParameter());
					if (leftParameter != null) {
						fValue = leftParameter.getValue();
						if (fValue == null) {
							fValue = leftParameter.getDefaultValue();
						}
					} else {
						Logger.getLogger(Entity.class.getName()).log(Level.SEVERE,
						        "Parameter of left query must present (Relation points to query parameter " + rel.getLeftParameter() + ", but query parameter with specified name is absent)");
					}
				}
				Field fieldOfValue = rowset.getFields().get(rel.getRightField());
				filterKeySet.add(Converter.convert2RowsetCompatible(fValue, fieldOfValue.getTypeInfo()));
			}
		}
		if (filter != null && !filter.isEmpty() && (filter != rowset.getActiveFilter() || !filter.getKeysetApplied().equals(filterKeySet))) {
			filter.filterRowset(filterKeySet);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean willScroll(RowsetScrollEvent aEvent) {
		assert aEvent.getRowset() == rowset;
		if (model.isAjusting()) {
			model.addSavedRowIndex(this, aEvent.getOldRowIndex());
		} else {
			// call script method
			Boolean sRes = null;
			try {
				sRes = Utils.executeScriptEventBoolean(jsPublished, onBeforeScroll, JSEvents.publishCursorPositionWillChangeEvent(jsPublished, aEvent.getNewRowIndex()));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			if (sRes != null) {
				return sRes;
			}
		}
		return true;
	}

	@Override
	public void rowsetScrolled(RowsetScrollEvent aEvent) {
		assert pending == null;
		Rowset eventRowset = aEvent.getRowset();
		assert eventRowset == rowset;
		if (aEvent.getNewRowIndex() >= 0 && aEvent.getNewRowIndex() <= eventRowset.size() + 1) {
			try {
				internalExecuteChildren(false);
				if (!model.isAjusting()) {
					// call script method
					Utils.executeScriptEventVoid(jsPublished, onAfterScroll, JSEvents.publishCursorPositionChangedEvent(jsPublished, aEvent.getOldRowIndex(), aEvent.getNewRowIndex()));
				}
			} catch (Exception ex) {
				Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public boolean willChangeRow(RowChangeEvent aEvent) {
		boolean assertres = model.isAjusting();
		assert !assertres;
		Fields fmdv = getFields();
		if (fmdv != null) {
			Field fmd = fmdv.get(aEvent.getFieldIndex());
			if (fmd != null) {
				// call script method
				Boolean sRes = null;
				try {
					JavaScriptObject publishedRow = publishRowFacade(aEvent.getChangedRow(), this);
					sRes = Utils.executeScriptEventBoolean(jsPublished, onBeforeChange,
					        JSEvents.publishEntityInstanceChangeEvent(publishedRow, publishFieldFacade(fmd), Utils.toJs(aEvent.getOldValue()), Utils.toJs(aEvent.getNewValue())));
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
				if (sRes != null) {
					return sRes;
				}
			}
		}
		return true;
	}

	@Override
	public void rowChanged(RowChangeEvent aEvent) {
		try {
			boolean assertres = model.isAjusting();
			assert !assertres;
			internalExecuteChildren(false, aEvent.getFieldIndex());
			Fields fmdv = getFields();
			if (fmdv != null) {
				Field fmd = fmdv.get(aEvent.getFieldIndex());
				if (fmd != null) {
					// call script method
					JavaScriptObject publishedRow = publishRowFacade(aEvent.getChangedRow(), this);
					Utils.executeScriptEventVoid(jsPublished, onAfterChange,
					        JSEvents.publishEntityInstanceChangeEvent(publishedRow, publishFieldFacade(fmd), Utils.toJs(aEvent.getOldValue()), Utils.toJs(aEvent.getNewValue())));
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public boolean willInsertRow(RowsetInsertEvent aEvent) {
		// call script method
		assert !model.isAjusting();
		Boolean sRes = null;
		try {
			JavaScriptObject publishedRow = publishRowFacade(aEvent.getRow(), this);
			sRes = Utils.executeScriptEventBoolean(jsPublished, onBeforeInsert, JSEvents.publishEntityInstanceInsertEvent(jsPublished, publishedRow));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		if (sRes != null) {
			return sRes;
		}
		return true;
	}

	@Override
	public boolean willDeleteRow(RowsetDeleteEvent aEvent) {
		// call script method
		assert !model.isAjusting();
		Boolean sRes = null;
		try {
			JavaScriptObject publishedRow = publishRowFacade(aEvent.getRow(), this);
			sRes = Utils.executeScriptEventBoolean(jsPublished, onBeforeDelete, JSEvents.publishEntityInstanceDeleteEvent(jsPublished, publishedRow));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		if (sRes != null) {
			return sRes;
		}
		return true;
	}

	@Override
	public void rowInserted(RowsetInsertEvent aEvent) {
		try {
			boolean assertres = model.isAjusting();
			assert !assertres;
			if (jsPublished != null)
				publishRows(jsPublished);
			internalExecuteChildren(false);
			// call script method
			JavaScriptObject publishedRow = publishRowFacade(aEvent.getRow(), this);
			Utils.executeScriptEventVoid(jsPublished, onAfterInsert, JSEvents.publishEntityInstanceInsertEvent(jsPublished, publishedRow));
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void rowDeleted(RowsetDeleteEvent aEvent) {
		try {
			boolean assertres = model.isAjusting();
			assert !assertres;
			if (jsPublished != null)
				publishRows(jsPublished);
			internalExecuteChildren(false);
			// call script method
			JavaScriptObject publishedRow = publishRowFacade(aEvent.getRow(), this);
			Utils.executeScriptEventVoid(jsPublished, onAfterDelete, JSEvents.publishEntityInstanceDeleteEvent(jsPublished, publishedRow));
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void rowsetSorted(RowsetSortEvent event) {
		try {
			assert pending == null;
			internalExecuteChildren(false);
			// call script method
			JavaScriptObject publishedEvent = JSEvents.publishScriptSourcedEvent(jsPublished);
			if (!model.isAjusting()) {
				Utils.executeScriptEventVoid(jsPublished, onFiltered, publishedEvent);
			}
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void rowsetRequeried(RowsetRequeryEvent event) {
		try {
			assert pending != null;
			if (jsPublished != null)
				publishRows(jsPublished);
			JavaScriptObject publishedEvent = JSEvents.publishScriptSourcedEvent(jsPublished);
			if (!model.isAjusting()) {
				if (model.isPending())
					model.enqueueEvent(new ScriptEvent(jsPublished, this, onRequeried, publishedEvent));
				else
					Utils.executeScriptEventVoid(jsPublished, onRequeried, publishedEvent);
			}
			internalExecuteChildren(false);
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void rowsetNetError(RowsetNetErrorEvent event) {
	}

	@Override
	public void rowsetFiltered(RowsetFilterEvent event) {
		try {
			if (jsPublished != null)
				publishRows(jsPublished);
			if ((!rowset.isBeforeFirst() && !rowset.isAfterLast()) || !rowset.first())
				internalExecuteChildren(false);
			// call script method
			JavaScriptObject publishedEvent = JSEvents.publishScriptSourcedEvent(jsPublished);
			if (!model.isAjusting()) {
				if (model.isPending())
					model.enqueueEvent(new ScriptEvent(jsPublished, this, onFiltered, publishedEvent));
				else
					Utils.executeScriptEventVoid(jsPublished, onFiltered, publishedEvent);
			}
		} catch (Exception ex) {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void rowsetSaved(RowsetSaveEvent event) {
	}

	@Override
	public void rowsetRolledback(RowsetRollbackEvent event) {
	}

	@Override
	public boolean willFilter(RowsetFilterEvent event) {
		return true;
	}

	@Override
	public boolean willRequery(RowsetRequeryEvent event) {
		return true;
	}

	@Override
	public void beforeRequery(RowsetRequeryEvent event) {
	}

	@Override
	public boolean willSort(RowsetSortEvent event) {
		return true;
	}

	protected void assign(Entity appTarget) throws Exception {
		appTarget.setEntityId(entityId);
		appTarget.setQueryId(queryId);
		appTarget.setTitle(getTitle());
		appTarget.setName(getName());
		appTarget.setOnAfterChange(onAfterChange);
		appTarget.setOnAfterDelete(onAfterDelete);
		appTarget.setOnAfterInsert(onAfterInsert);
		appTarget.setOnAfterScroll(onAfterScroll);
		appTarget.setOnFiltered(onFiltered);
		appTarget.setOnRequeried(onRequeried);
		appTarget.setOnBeforeChange(onBeforeChange);
		appTarget.setOnBeforeDelete(onBeforeDelete);
		appTarget.setOnBeforeInsert(onBeforeInsert);
		appTarget.setOnBeforeScroll(onBeforeScroll);
	}

	public void accept(ModelVisitor visitor) {
		visitor.visit(this);
	}

	public Rowset getRowset() {
		return rowset;
	}

	private Locator checkUserLocator(List<Integer> constraints) throws IllegalStateException {
		Locator loc = userLocators.get(constraints);
		if (loc == null) {
			Rowset lrowset = getRowset();
			loc = lrowset.createLocator();
			loc.beginConstrainting();
			try {
				for (int colIdx : constraints) {
					loc.addConstraint(colIdx);
				}
			} finally {
				loc.endConstrainting();
			}
			userLocators.put(constraints, loc);
		}
		return loc;
	}

	// Scriptable rowset interface

	protected JavaScriptObject jsSubstitute;

	public JavaScriptObject getSubstitute() {
		return jsSubstitute;
	}

	public void setSubstitute(JavaScriptObject aSubstitute) {
		jsSubstitute = aSubstitute;
	}

	protected native Row unwrapRow(JavaScriptObject aRowFacade) throws Exception/*-{
		return aRowFacade.unwrap();
	}-*/;

	public boolean scrollTo(JavaScriptObject aRowFacade) throws Exception {
		Row row = unwrapRow(aRowFacade);
		return scrollTo(row);
	}

	public boolean scrollTo(Row aRow) throws Exception {
		if (rowset != null) {
			List<Integer> pkIndices = rowset.getFields().getPrimaryKeysIndicies();
			if (!pkIndices.isEmpty()) {
				Locator loc = checkUserLocator(pkIndices);
				Object[] keyValues = new Object[pkIndices.size()];
				for (int i = 0; i < keyValues.length; i++) {
					assert pkIndices.get(i) != null : "Primary keys indices must non null integers";
					int colIndex = pkIndices.get(i);
					keyValues[i] = aRow.getColumnObject(colIndex);
				}
				if (loc.find(keyValues)) {
					return loc.first();
				} else
					return false;
			} else
				throw new IllegalArgumentException("Scrolling possible only for rows with primary keys specified");
		} else
			return false;
	}

	/**
	 * Finds a single row and wraps it in appropriate js object.
	 * 
	 * @param aValue
	 *            Search key value.
	 * @return Wrapped row if it have been found and null otherwise.
	 */
	public JavaScriptObject findById(Object aValue) throws Exception {
		List<Integer> pkIndicies = getFields().getPrimaryKeysIndicies();
		if (pkIndicies.size() == 1) {
			Object keyValue = Utils.toJava(aValue);
			keyValue = Converter.convert2RowsetCompatible(keyValue, getFields().get(pkIndicies.get(0)).getTypeInfo());
			Locator loc = checkUserLocator(pkIndicies);
			if (loc.find(new Object[] { keyValue }))
				return publishRowFacade(loc.getRow(0), this);
			else
				return null;
		} else
			throw new IllegalArgumentException("There are must be only one primary key field to be searched on.");
	}

	public static final String BAD_FIND_AGRUMENTS_MSG = "Bad find agruments";
	public static final String BAD_FIND_ARGUMENT_MSG = "Argument at index %d must be a rowset's field.";

	/**
	 * Finds set of rows and wraps it in appropriate js objects.
	 * 
	 * @param aValues
	 *            Search key fields and key values.
	 * @return js array of wrapped rows.
	 * @throws RowsetException
	 * @throws IllegalStateException
	 */
	public JavaScriptObject find(JavaScriptObject aValues) throws Exception {
		JsArrayMixed values = aValues.<JsArrayMixed> cast();
		JsArray<JavaScriptObject> arFound = JavaScriptObject.createArray().<JsArray<JavaScriptObject>> cast();
		if (values.length() > 0 && values.length() % 2 == 0) {
			List<Integer> constraints = new ArrayList();
			List<Object> keyValues = new ArrayList();
			for (int i = 0; i < values.length(); i += 2) {
				int colIndex = 0;
				DataTypeInfo typeInfo = null;
				try {
					JavaScriptObject jsField = values.getObject(i);
					Field field = RowsetUtils.unwrapField(jsField);
					colIndex = getFields().find(field.getName());
					typeInfo = field.getTypeInfo();
				} catch (Exception ex) {
					colIndex = Double.valueOf(values.getNumber(i)).intValue();
					typeInfo = getFields().get(colIndex).getTypeInfo();
				}
				// field col index
				constraints.add(colIndex);
				// correponding value
				Object keyValue = RowsetUtils.extractValueFromJsArray(values, i + 1);
				keyValues.add(Converter.convert2RowsetCompatible(keyValue, typeInfo));
			}
			Locator loc = checkUserLocator(constraints);
			if (loc.find(keyValues.toArray())) {
				for (int i = 0; i < loc.getSize(); i++) {
					arFound.push(publishRowFacade(loc.getRow(i), this));
				}
			}
		} else {
			Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, BAD_FIND_AGRUMENTS_MSG);
		}
		return arFound;
	}

	public JavaScriptObject createLocator(JavaScriptObject aConstraints) throws Exception {
		JsArrayMixed constraints = aConstraints.<JsArrayMixed> cast();
		Locator loc = new Locator(getRowset());
		loc.beginConstrainting();
		try {
			for (int i = 0; i < constraints.length(); i++) {
				JavaScriptObject jsConstraint = constraints.getObject(i);
				Field field = RowsetUtils.unwrapField(jsConstraint);
				loc.addConstraint(getFields().find(field.getName()));
			}
		} finally {
			loc.endConstrainting();
		}
		return publishLocatorFacade(loc, this);
	}

	public JavaScriptObject createFilter(JavaScriptObject aConstraints) throws Exception {
		JsArrayMixed constraints = aConstraints.<JsArrayMixed> cast();
		Filter filter = new Filter(getRowset());
		filter.beginConstrainting();
		try {
			for (int i = 0; i < constraints.length(); i++) {
				JavaScriptObject jsConstraint = constraints.getObject(i);
				Field field = RowsetUtils.unwrapField(jsConstraint);
				filter.addConstraint(getFields().find(field.getName()));
			}
		} finally {
			filter.endConstrainting();
		}
		return publishFilterFacade(filter, this);
	}

	public RowsComparator createSorting(JavaScriptObject aConstraints) {
		JsArrayMixed constraints = aConstraints.<JsArrayMixed> cast();
		List<SortingCriterion> criteria = new ArrayList();
		for (int i = 0; i < constraints.length(); i += 2) {
			JavaScriptObject fieldValue = constraints.getObject(i);
			int colIndex = 0;
			// field
			try {
				// instance
				Field field = RowsetUtils.unwrapField(fieldValue);
				colIndex = getFields().find(field.getName());
			} catch (Exception ex) {
				// colIndex
				colIndex = Double.valueOf(constraints.getNumber(i)).intValue();
			}
			// sorting direction
			boolean direction = true;
			if (i < constraints.length() - 1)
				direction = constraints.getBoolean(i + 1);
			criteria.add(new SortingCriterion(colIndex, direction));
		}
		RowsComparator comparator = new RowsComparator(criteria);
		return comparator;
	}

	public void sort(RowsComparator aComparator) throws Exception {
		RowsComparator comparator = (RowsComparator) aComparator;
		getRowset().sort(comparator);
	}

	public static boolean isParameter(Field aField) {
		return aField instanceof Parameter;
	}

	protected native static int invokeComparatorFunc(JavaScriptObject aComparatorFun, JavaScriptObject row1, JavaScriptObject row2) throws Exception/*-{
		return aComparatorFun(row1, row2);
	}-*/;

	public void sort(final JavaScriptObject aComparatorFunc) throws Exception {
		Comparator<Row> comparator = new Comparator<Row>() {

			@Override
			public int compare(Row row1, Row row2) {
				// Row row1 = (Row)arg0;
				// Row row2 = (Row)arg1;
				try {
					return invokeComparatorFunc(aComparatorFunc, publishRowFacade(row1, Entity.this), publishRowFacade(row2, Entity.this));
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

		};
		getRowset().sort(comparator);
	}

	public void insert(JavaScriptObject aValues) throws Exception {
		JsArrayMixed fieldsValues = aValues.<JsArrayMixed> cast();
		Object[] initingValues = new Object[fieldsValues.length()];
		for (int i = 0; i < initingValues.length; i += 2) {
			JavaScriptObject fieldValue = fieldsValues.getObject(i);
			// field
			try {
				initingValues[i] = RowsetUtils.unwrapField(fieldValue);
			} catch (Exception ex) {
				initingValues[i] = fieldsValues.getNumber(i);
			}
			// value
			initingValues[i + 1] = RowsetUtils.extractValueFromJsArray(fieldsValues, i + 1);
		}
		getRowset().insert(initingValues);
	}

	/**
	 * 
	 * @param aValues
	 *            JavaScript array containing duplets of field name and field
	 *            value.
	 * @param aIndex
	 *            Index new row will be inserted at. 1-based.
	 */
	public void insertAt(int aIndex, JavaScriptObject aValues) throws Exception {
		JsArrayMixed fieldsValues = aValues.<JsArrayMixed> cast();
		List<Object> initingValues = new ArrayList();
		for (int i = 0; i < fieldsValues.length(); i += 2) {
			// field
			String fieldName = fieldsValues.getString(i);
			Field field = getFields().get(fieldName);
			if (field != null) {
				initingValues.add(field);
				// value
				initingValues.add(RowsetUtils.extractValueFromJsArray(fieldsValues, i + 1));
			}
		}
		getRowset().insertAt(aIndex, initingValues.toArray());
	}

	protected JavaScriptObject jsPublished;

	public void setPublished(JavaScriptObject aPublished) {
		jsPublished = aPublished;
	}

	public JavaScriptObject getPublished() {
		return jsPublished;
	}
}