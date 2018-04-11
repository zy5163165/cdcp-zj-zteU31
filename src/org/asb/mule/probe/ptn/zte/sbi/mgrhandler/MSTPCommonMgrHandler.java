package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import globaldefs.NameAndStringValue_T;
import mstpcommon.MSTPCommon_I;
import subnetworkConnection.TPDataList_THolder;
import subnetworkConnection.TPData_T;

/**
 * Handler class for using MSTPCommon_I object.
 */
public class MSTPCommonMgrHandler {
	private static MSTPCommonMgrHandler instance;

	public static MSTPCommonMgrHandler instance() {
		if (null == instance)
			instance = new MSTPCommonMgrHandler();
		return instance;
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

	private MSTPCommonMgrHandler() {
	}

}
