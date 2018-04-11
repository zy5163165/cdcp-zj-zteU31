package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.IPCrossconnection;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;


import com.alcatelsbell.nms.valueobject.BObject;

public class CrossConnectionDataTask extends CommonDataTask {

	public CrossConnectionDataTask() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public Vector<BObject> excute() {
		try {
			List<CrossConnect> ipccList = service.retrieveAllCrossConnects(this.getTask().getObjectName());
			if (ipccList != null) {
				for (CrossConnect ipcc : ipccList) {
                    getSqliteConn().insertBObject(ipcc);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void insertDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

}
