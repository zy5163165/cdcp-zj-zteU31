package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import globaldefs.NameAndStringValue_T;
import globaldefs.ProcessingFailureException;
import multiLayerSubnetwork.MultiLayerSubnetworkMgr_I;
import subnetworkConnection.CrossConnect_T;
import subnetworkConnection.Route_THolder;
import subnetworkConnection.SNCIterator_IHolder;
import subnetworkConnection.SubnetworkConnectionList_THolder;
import subnetworkConnection.SubnetworkConnection_T;
import topologicalLink.TopologicalLinkList_THolder;
import topologicalLink.TopologicalLink_T;

public class SubnetworkMgrHandler {
	private static SubnetworkMgrHandler instance;

	public static SubnetworkMgrHandler instance() {
		if (null == instance)
			instance = new SubnetworkMgrHandler();

		return instance;
	}

	// private Object retrieveAllInternalTopologicalLinks;

	public TopologicalLink_T[] retrieveAllTopologicalLinks(MultiLayerSubnetworkMgr_I subnetworkMgr, NameAndStringValue_T[] subnetworkName)
			throws ProcessingFailureException {
		TopologicalLinkList_THolder tpLinkList = new TopologicalLinkList_THolder();

		subnetworkMgr.getAllTopologicalLinks(subnetworkName, tpLinkList);

		return tpLinkList.value;
	}

	public SubnetworkConnection_T[] retrieveAllSNCs(MultiLayerSubnetworkMgr_I subnetworkMgr, NameAndStringValue_T[] subnetworkName, short[] layerRateList)
			throws ProcessingFailureException {

		SubnetworkConnectionList_THolder sncList = new SubnetworkConnectionList_THolder();
		SNCIterator_IHolder sncIt = new SNCIterator_IHolder();

		java.util.Vector emsSNCVector = new java.util.Vector();

		subnetworkMgr.getAllSubnetworkConnections(subnetworkName, layerRateList, 50, sncList, sncIt);

		for (int i = 0; i < sncList.value.length; i++) {
			emsSNCVector.addElement(sncList.value[i]);
		}

		if (null != sncIt.value) {
			boolean shouldContinue = true;
			while (shouldContinue) {
				shouldContinue = sncIt.value.next_n(50, sncList);

				for (int i = 0; i < sncList.value.length; i++)
					emsSNCVector.addElement(sncList.value[i]);
			}

			try {
				sncIt.value.destroy();
			} catch (Throwable ex) {
				// datalog.info("destory Iterator");
			}
		}

		SubnetworkConnection_T[] sncs = new SubnetworkConnection_T[emsSNCVector.size()];
		emsSNCVector.copyInto(sncs);

		return sncs;
	}

	public CrossConnect_T[] retrieveRoute(MultiLayerSubnetworkMgr_I subnetworkMgr, NameAndStringValue_T[] sncName, boolean includeHigherOrderCCs)
			throws ProcessingFailureException {

		Route_THolder ccList = new Route_THolder();

		subnetworkMgr.getRoute(sncName, includeHigherOrderCCs, ccList);

		return ccList.value;
	}



}
