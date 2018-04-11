package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import globaldefs.NameAndStringValue_T;
import globaldefs.ProcessingFailureException;
import managedElement.ManagedElementIterator_IHolder;
import managedElement.ManagedElementList_THolder;
import managedElement.ManagedElement_T;
import managedElement.ManagedElement_THolder;
import managedElementManager.ManagedElementMgr_I;
import mstpcommon.MSTPCommon_I;
import subnetworkConnection.CCIterator_IHolder;
import subnetworkConnection.CrossConnectList_THolder;
import subnetworkConnection.CrossConnect_T;
import subnetworkConnection.TPDataList_THolder;
import subnetworkConnection.TPData_T;
import terminationPoint.TerminationPointIterator_IHolder;
import terminationPoint.TerminationPointList_THolder;
import terminationPoint.TerminationPoint_T;

/**
 * Handler class for using ManagedElementMgr_I object.
 */
public class ManagedElementMgrHandler {
	private static ManagedElementMgrHandler instance;

	public static ManagedElementMgrHandler instance() {
		if (null == instance)
			instance = new ManagedElementMgrHandler();
		return instance;
	}

	/**
	 * Retrieve all managed elements using the given mgr.
	 * 
	 * @param mgr
	 *            mgr from which managed elements retrieved.
	 * @return ManagedElement_T[]
	 */
	public ManagedElement_T[] retrieveAllManagedElements(ManagedElementMgr_I mgr) throws globaldefs.ProcessingFailureException {
		int how_many = 500;

		java.util.Vector mes = new java.util.Vector();
		ManagedElementList_THolder meList = new ManagedElementList_THolder();
		ManagedElementIterator_IHolder meIt = new ManagedElementIterator_IHolder();

		mgr.getAllManagedElementsByIterator(how_many, meList, meIt);
		for (int i = 0; i < meList.value.length; i++) {
			mes.addElement(meList.value[i]);
		}

		if (meIt.value != null) {
			boolean hasMore;
			do {
				hasMore = meIt.value.next_n(how_many, meList);

				for (int i = 0; i < meList.value.length; i++) {
					mes.addElement(meList.value[i]);
				}
			} while (hasMore);

			try {
				meIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		ManagedElement_T[] result = new ManagedElement_T[mes.size()];
		mes.copyInto(result);
		return result;
	}

	/**
	 * Retrieve one managed elements using the given meName.
	 * 
	 * @param mgr
	 *            ,neName mgr from which managed elements retrieved.
	 * @return ManagedElement_T
	 */
	public ManagedElement_T retrieveManagedElement(ManagedElementMgr_I mgr, globaldefs.NameAndStringValue_T[] vendorMeName)
			throws globaldefs.ProcessingFailureException {
		ManagedElement_THolder me = new ManagedElement_THolder();

		mgr.getManagedElement(vendorMeName, me);

		return me.value;
	}

	public TerminationPoint_T[] retrieveAllPTPs(ManagedElementMgr_I mgr, NameAndStringValue_T[] managedElementName, short[] tpLayerRateList,
			short[] connectionLayerRateList) throws ProcessingFailureException {

		TerminationPointList_THolder tpList = new TerminationPointList_THolder();

		mgr.getAllPTPs(managedElementName, tpLayerRateList, connectionLayerRateList, tpList);

		return tpList.value;
	}

	public TerminationPoint_T[] retrieveContainedInUseTPs(ManagedElementMgr_I mgr, NameAndStringValue_T[] tpName, short[] layerRateList)
			throws ProcessingFailureException {
		int how_many = 50;

		java.util.Vector tps = new java.util.Vector();
		TerminationPointList_THolder tpList = new TerminationPointList_THolder();
		TerminationPointIterator_IHolder tpIt = new TerminationPointIterator_IHolder();

		mgr.getContainedInUseTPs(tpName, layerRateList, how_many, tpList, tpIt);

		for (int i = 0; i < tpList.value.length; i++) {
			tps.addElement(tpList.value[i]);
		}

		if (tpIt.value != null) {
			boolean hasMore;
			do {
				hasMore = tpIt.value.next_n(how_many, tpList);
				for (int i = 0; i < tpList.value.length; i++) {
					tps.addElement(tpList.value[i]);
				}
			} while (hasMore);
			try {
				tpIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		TerminationPoint_T result[] = new TerminationPoint_T[tps.size()];
		tps.copyInto(result);

		return result;
	}

	public TerminationPoint_T[] retrieveContainedPotentialTPs(ManagedElementMgr_I mgr, NameAndStringValue_T[] tpName, short[] layerRateList)
			throws ProcessingFailureException {
		int how_many = 50;

		java.util.Vector tps = new java.util.Vector();
		TerminationPointList_THolder tpList = new TerminationPointList_THolder();
		TerminationPointIterator_IHolder tpIt = new TerminationPointIterator_IHolder();

		try {
			mgr.getContainedPotentialTPs(tpName, layerRateList, how_many, tpList, tpIt);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ProcessingFailureException();
		}
		for (int i = 0; i < tpList.value.length; i++) {
			tps.addElement(tpList.value[i]);
		}

		if (tpIt.value != null) {
			boolean hasMore;
			do {
				hasMore = tpIt.value.next_n(how_many, tpList);
				for (int i = 0; i < tpList.value.length; i++) {
					tps.addElement(tpList.value[i]);
				}
			} while (hasMore);
			try {
				tpIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		TerminationPoint_T result[] = new TerminationPoint_T[tps.size()];
		tps.copyInto(result);

		return result;
	}

	public TerminationPoint_T[] retrieveContainedCurrentTPs(ManagedElementMgr_I mgr, NameAndStringValue_T[] tpName, short[] layerRateList)
			throws ProcessingFailureException {
		int how_many = 50;

		java.util.Vector tps = new java.util.Vector();
		TerminationPointList_THolder tpList = new TerminationPointList_THolder();
		TerminationPointIterator_IHolder tpIt = new TerminationPointIterator_IHolder();

		try {
			mgr.getContainedCurrentTPs(tpName, layerRateList, how_many, tpList, tpIt);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ProcessingFailureException();
		}
		for (int i = 0; i < tpList.value.length; i++) {
			tps.addElement(tpList.value[i]);
		}

		if (tpIt.value != null) {
			boolean hasMore;
			do {
				hasMore = tpIt.value.next_n(how_many, tpList);

				for (int i = 0; i < tpList.value.length; i++) {
					tps.addElement(tpList.value[i]);
				}
			} while (hasMore);

			try {
				tpIt.value.destroy();
			} catch (Throwable ex) {

			}
		}
		TerminationPoint_T result[] = new TerminationPoint_T[tps.size()];
		tps.copyInto(result);

		return result;
	}

	public CrossConnect_T[] retrieveAllCrossConnections(ManagedElementMgr_I mgr, NameAndStringValue_T[] meName, short[] connectionRateList)
			throws ProcessingFailureException {
		int how_many = 50;

		java.util.Vector ccs = new java.util.Vector();
		CrossConnectList_THolder ccList = new CrossConnectList_THolder();
		CCIterator_IHolder ccIt = new CCIterator_IHolder();
		mgr.getAllCrossConnections(meName, connectionRateList, how_many, ccList, ccIt);
		for (int i = 0; i < ccList.value.length; i++) {
			ccs.addElement(ccList.value[i]);
		}

		if (ccIt.value != null) {
			boolean hasMore;
			do {
				hasMore = ccIt.value.next_n(how_many, ccList);

				for (int i = 0; i < ccList.value.length; i++) {
					ccs.addElement(ccList.value[i]);
				}
			} while (hasMore);

			try {
				ccIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		CrossConnect_T result[] = new CrossConnect_T[ccs.size()];
		ccs.copyInto(result);

		return result;
	}
	
	/**
	 * Retrieve all referenced tps using the given mgr.
	 * 
	 * @param mgr
	 *            mgr from which managed elements retrieved.
	 * @return ManagedElement_T[]
	 */
	public TPData_T[] retrieveAllPtpsByFtp(MSTPCommon_I mstpCom, NameAndStringValue_T[] ftpName) throws globaldefs.ProcessingFailureException {

		java.util.Vector mes = new java.util.Vector();
		TPDataList_THolder tpList = new TPDataList_THolder();

		mstpCom.getFTPMembers(ftpName, tpList);
		for (int i = 0; i < tpList.value.length; i++) {
			mes.addElement(tpList.value[i]);
		}

		TPData_T[] result = new TPData_T[mes.size()];
		mes.copyInto(result);

		return result;
	}

	private ManagedElementMgrHandler() {
	}

}
