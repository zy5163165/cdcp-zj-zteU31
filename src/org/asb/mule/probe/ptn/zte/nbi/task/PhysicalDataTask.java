package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.EquipmentHolder;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;


import com.alcatelsbell.nms.valueobject.BObject;

public class PhysicalDataTask extends CommonDataTask {
	public Vector<BObject> excute() {
		try {
			List<EquipmentHolder> holderList = new ArrayList<EquipmentHolder>();
			List<Equipment> cardList = new ArrayList<Equipment>();
			service.retrieveAllEquipmentAndHolders(getTask().getObjectName(), holderList, cardList);

			List<PTP> ptpList = service.retrieveAllPtps(this.getTask().getObjectName());
			if (holderList != null && holderList.size() > 0) {
				for (EquipmentHolder holder : holderList) {
					getSqliteConn().insertBObject(holder);
				}
			}

			if (cardList != null && cardList.size() > 0) {
				for (Equipment card : cardList) {
					getSqliteConn().insertBObject(card);
				}
			}

			if (ptpList != null && ptpList.size() > 0) {
				for (PTP ptp : ptpList) {
                    getSqliteConn().insertBObject(ptp);
				}
				for (PTP ptp : ptpList) {
					try {
						List<CTP> ctpList = service.retrieveAllCtps(ptp.getDn());
						if (ctpList != null && ctpList.size() > 0) {
							for (CTP ctp : ctpList) {
                                getSqliteConn().insertBObject(ctp);
							}
						}
					} catch (Exception e) {
						nbilog.error("PhysicalDataTask.excute Exception:", e);
					}
				}
			}
			// List<CrossConnect> ipccList = service.retrieveAllCrossConnects(this.getTask().getObjectName());
			// if (ipccList != null) {
			// for (CrossConnect ipcc : ipccList) {
			// SqliteService.getInstance().insertBObject(ipcc);
			// }
			// }
		} catch (Exception e) {
			nbilog.error("PhysicalDataTask.excute Exception:", e);
		}
		return null;

	}

	@Override
	public void deleteDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDate(Vector<BObject> dataList) {
		// TODO Auto-generated method stub

	}

}
