package com.eas.client.model.interacting.mixed;

import com.bearsoft.rowset.Rowset;
import com.eas.client.Utils;
import com.eas.client.model.interacting.filtering.FilteringTest;
import com.eas.client.model.store.XmlDom2Model;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.xml.client.XMLParser;

public class MixedCRUDTest extends MixedTest {

	protected ModelState state;
	protected Rowset izmVelRs;
	protected PublishedModule module;
	protected int callCounter;

	protected static final class PublishedModule extends JavaScriptObject {
		protected PublishedModule() {
			super();
		}

		public native void setNaimSiPoVel1RequeriedCounter(int aValue) throws Exception/*-{
			this.naimSiPoVel1RequeriedCounter = aValue;
		}-*/;
	}

	public native PublishedModule publish(MixedCRUDTest aTest) throws Exception/*-{
		var publishedModule = {
			edIzmRequeriedCounter : 0,
			edIzmRequeried : function() {
				publishedModule.edIzmRequeriedCounter++;
				if (publishedModule.edIzmRequeriedCounter == 1) {
					aTest.@com.eas.client.model.interacting.mixed.MixedCRUDTest::validateMixedCrud()();
				} else if (publishedModule.edIzmRequeriedCounter == 2) {
					aTest.@com.eas.client.model.interacting.mixed.MixedCRUDTest::velRsBeforeFirstScrolled()();
				} else if (publishedModule.edIzmRequeriedCounter >= 3) {
					aTest.@com.eas.client.model.interacting.mixed.MixedCRUDTest::velRsNextScrolled()();
				}
			},
			naimSiPoVel1RequeriedCounter : -1000,
			naimSiPoVel1Requeried : function() {
				publishedModule.naimSiPoVel1RequeriedCounter++;
				if (publishedModule.naimSiPoVel1RequeriedCounter == 1) {
					aTest.@com.eas.client.model.interacting.mixed.MixedCRUDTest::naimSiInsertedUpdated()();
				}
			}
		}
		return publishedModule;
	}-*/;

	@Override
	public void validate() throws Exception {
		assertEquals(16, callCounter);
	}
	
	public void testMixedCrud() throws Exception {}
	
	@Override
	protected void setupModel() throws Exception {
		module = publish(this);
		
		model = XmlDom2Model.transform(XMLParser.parse(DATAMODEL_MIXED_RELATIONS), module);
		model.getEntityById(ENTITY_EDINICI_IZMERENIJA_PO_VELICHINE_ID)
				.setOnRequeried(Utils.lookupProperty(module, "edIzmRequeried"));
		model.getEntityById(ENTITY_NAIMENOVANIA_SI_PO_VELICHINE_1_ID)
				.setOnRequeried(Utils.lookupProperty(module, "naimSiPoVel1Requeried"));
		model.publish(module);
		model.setRuntime(true);
	}

	public void validateMixedCrud() throws Exception {
		System.out.println("mixedCrudTest");
		state = new ModelState(model);
		izmVelRs = state.IZMERJAEMIE_VELICHINI.getRowset();
		assertNotNull(izmVelRs);
		izmVelRs.beforeFirst();
		callCounter++;
	}

	public void velRsBeforeFirstScrolled() throws Exception {
		izmVelRs.next();
		callCounter++;
	}

	public void velRsNextScrolled() throws Exception {
		if (!izmVelRs.isAfterLast()) {
			int velPkColIndex = izmVelRs.getFields().find("ID");
			Long velPk = izmVelRs.getLong(velPkColIndex);
			if (velPk.equals(FilteringTest.SILA_EL)) {
				Rowset naimSi = state.NAIMENOVANIE_SI.getRowset();
				assertNotNull(naimSi);

				module.setNaimSiPoVel1RequeriedCounter(0);
				// insert will not change any parameters in related entities
				assertEquals(0, state.NAIMENOVANIA_SI_PO_VELICHINE.getRowset()
						.size());
				naimSi.insert();
				assertEquals(0, state.NAIMENOVANIA_SI_PO_VELICHINE.getRowset()
						.size());
				// insert will change any parameters in related entities by further filtering
				int velColIndex = naimSi.getFields().find("VALUE");
				naimSi.updateObject(velColIndex, FilteringTest.SILA_EL);
				// Check filtering relation
				assertEquals(1, state.NAIMENOVANIA_SI_PO_VELICHINE.getRowset()
						.size());
				return;
			}
			izmVelRs.next();
		}
		callCounter++;
	}

	public void naimSiInsertedUpdated() throws Exception {
		if (!izmVelRs.isAfterLast()) {
			Rowset siRs = state.NAIMENOVANIE_SI.getRowset();
			assertNotNull(siRs);
			int velColIndex = siRs.getFields().find("VALUE");
			if (siRs.getCurrentRow().isInserted()) {
				Object velColValue = siRs.getObject(velColIndex);
				if (velColValue != null)// updated
				{
					module.setNaimSiPoVel1RequeriedCounter(-1000);
					assertEquals(1, state.NAIMENOVANIA_SI_PO_VELICHINE_1
							.getRowset().size());
					siRs.delete();
					izmVelRs.next();
				}
			}
		}
		callCounter++;
	}
}