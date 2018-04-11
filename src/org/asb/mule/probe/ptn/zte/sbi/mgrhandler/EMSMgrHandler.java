package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import globaldefs.*;
import topologicalLink.*;
import emsMgr.*;
import multiLayerSubnetwork.*;

import java.util.Vector;

/**
 * Handler class for using EMSMgr .
 */
public class EMSMgrHandler {
	private static EMSMgrHandler instance;

	public static EMSMgrHandler instance() {
		if (null == instance)
			instance = new EMSMgrHandler();

		return instance;
	}

	public MultiLayerSubnetwork_T[] retrieveAllTopLevelSubnetworks(EMSMgr_I emsMgr) throws ProcessingFailureException {

		SubnetworkList_THolder emsSubNEList = new SubnetworkList_THolder();

		emsMgr.getAllTopLevelSubnetworks(emsSubNEList);
		return emsSubNEList.value;
	}

	public TopologicalLink_T[] retrieveAllTopLevelTopologicalLinks(EMSMgr_I emsMgr) throws ProcessingFailureException {

		TopologicalLinkList_THolder emsTopLinkNEList = new TopologicalLinkList_THolder();
		emsMgr.getAllTopLevelTopologicalLinks(emsTopLinkNEList);

		return emsTopLinkNEList.value;
	}

	private EMSMgrHandler() {
	}

}
