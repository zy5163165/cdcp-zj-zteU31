package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.entity.TopoNode;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;

import org.asb.mule.probe.ptn.zte.service.ZTEService;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;

public class ManagedElementDataTask extends CommonDataTask {

	public Vector<BObject> excute() {
		Vector<BObject> neVec = new Vector<BObject>();
		try {
			List<ManagedElement> neList = service.retrieveAllManagedElements();
			if (neList != null && neList.size() > 0) {
				nbilog.info("ManagedElement : " + neList.size());
				for (ManagedElement ne : neList) {
					getSqliteConn().insertBObject(ne);
					neVec.add(ne);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return neVec;

	}

	@Override
	public void insertDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

}
