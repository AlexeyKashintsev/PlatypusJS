package com.eas.client.model.interacting.mixed;

import com.bearsoft.rowset.Rowset;
import com.eas.client.Utils;
import com.eas.client.model.EntityDataListener;
import com.eas.client.model.store.XmlDom2Model;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.xml.client.XMLParser;

public class MixedModelStructureTest extends MixedTest {

	protected ModelState state;
	protected Rowset izmVel;
	protected Rowset naimSi;
	protected int callCounter;

	public native JavaScriptObject publish(MixedModelStructureTest aTest) throws Exception/*-{
		var publishedModule = {
			edIzmRequeriedCounter : 0,
			edIzmRequeried : function() {
				publishedModule.edIzmRequeriedCounter++;
				if (publishedModule.edIzmRequeriedCounter == 1) {
					aTest.@com.eas.client.model.interacting.mixed.MixedModelStructureTest::validateMixedModelStructure()();
				} else if (publishedModule.edIzmRequeriedCounter == 2) {
					aTest.@com.eas.client.model.interacting.mixed.MixedModelStructureTest::izmVelBeforeFirstScrolled()();
				} else if (publishedModule.edIzmRequeriedCounter >= 3) {
					aTest.@com.eas.client.model.interacting.mixed.MixedModelStructureTest::izmVelNextScrolled()();
				}
			},
			naimSiPoVel1Requeried : function() {
				aTest.@com.eas.client.model.interacting.mixed.MixedModelStructureTest::naimSiPoVel1Requeried()();
			}
		}
		return publishedModule;
	}-*/;

	@Override
	protected int getTimeout() {
		return 60 * 1000;
	}

	@Override
	public void validate() throws Exception {
		assertEquals(19, callCounter);
	}

	public void testMixedModelStructure() throws Exception {
	}

	@Override
	protected void setupModel() throws Exception {
		JavaScriptObject module = publish(this);

		model = XmlDom2Model.transform(XMLParser.parse(DATAMODEL_MIXED_RELATIONS), module);
		model.getEntityById(ENTITY_EDINICI_IZMERENIJA_PO_VELICHINE_ID).setOnRequeried(Utils.lookupProperty(module, "edIzmRequeried"));
		model.getEntityById(ENTITY_NAIMENOVANIA_SI_PO_VELICHINE_1_ID).setOnRequeried(Utils.lookupProperty(module, "naimSiPoVel1Requeried"));
		model.publish(module);
		model.setRuntime(true);
	}

	public void validateMixedModelStructure() throws Exception {
		System.out.println("mixedModelStructureTest");
		state = new ModelState(model);

		EntityDataListener dataListener1 = new EntityDataListener();
		state.EDINICI_IZMERENIJA_PO_VELICHINE.getRowset().addRowsetListener(dataListener1);

		EntityDataListener dataListener2 = new EntityDataListener();
		state.EDINICI_IZMERENIJA_PO_VELICHINE_1.getRowset().addRowsetListener(dataListener2);

		EntityDataListener dataListener3 = new EntityDataListener();
		state.NAIMENOVANIA_SI_PO_VELICHINE.getRowset().addRowsetListener(dataListener3);

		EntityDataListener dataListener4 = new EntityDataListener();
		state.NAIMENOVANIA_SI_PO_VELICHINE_1.getRowset().addRowsetListener(dataListener4);

		izmVel = state.IZMERJAEMIE_VELICHINI.getRowset();
		naimSi = state.NAIMENOVANIE_SI.getRowset();
		assertNotNull(izmVel);
		assertNotNull(naimSi);
		izmVel.beforeFirst();
		callCounter++;
	}

	public void izmVelBeforeFirstScrolled() throws Exception {
		izmVel.next();
		callCounter++;
	}

	protected boolean naimSiUnderProcess;

	public void izmVelNextScrolled() throws Exception {
		if (!izmVel.isAfterLast()) {
			int velPkColIndex = izmVel.getFields().find("ID");
			int velColIndex = naimSi.getFields().find("VALUE");
			Long vel1 = izmVel.getLong(velPkColIndex);
			naimSi.beforeFirst();
			while (naimSi.next()) {
				Long vel2 = naimSi.getLong(velColIndex);
				if ((vel1 == null && vel2 == null) || (vel1 != null && vel1.equals(vel2))) {
					naimSiUnderProcess = true;
					return;// check if edIzmpoVel1 will be requeried.
				}
			}
			izmVel.next();
		}
		callCounter++;
	}

	public void naimSiPoVel1Requeried() throws Exception {
		if (naimSiUnderProcess) {
			naimSiUnderProcess = false;
			izmVel.next();
		}
		callCounter++;
	}
}