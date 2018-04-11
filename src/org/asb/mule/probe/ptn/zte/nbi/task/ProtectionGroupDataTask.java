package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.ProtectionGroup;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;
import org.asb.mule.probe.framework.service.SqliteConn;


import com.alcatelsbell.nms.valueobject.BObject;
import org.asb.mule.probe.framework.service.SqliteService;

public class ProtectionGroupDataTask extends CommonDataTask {



	public ProtectionGroupDataTask(SqliteConn sqliteConn) {
		 setSqliteConn(sqliteConn);
	}

	@Override
	public Vector<BObject> excute() {
		try {
			List<ProtectionGroup> protectionGroupList = service.retrieveAllProtectionGroupByMe(this.getTask().getObjectName());
			if (protectionGroupList != null) {
				for (ProtectionGroup protectionGroup : protectionGroupList) {
					if (getSqliteConn() != null)
						getSqliteConn().insertBObject(protectionGroup);
					else
						SqliteService.getInstance().insertBObject(protectionGroup);
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
