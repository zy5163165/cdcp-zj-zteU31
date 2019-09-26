package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.EquipmentHolder;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.R_FTP_PTP;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;


import com.alcatelsbell.nms.valueobject.BObject;

public class PhysicalDataTask extends CommonDataTask {
	public Vector<BObject> excute() {
		try {
			// OTN网管沿用老方法采集ctp，PTN网管改为通过网元采集ctp。
			String mod = "OTN";
			if ("ZJ-ZTE-1-PTN".equals(service.getEmsName())) {
				mod = "PTN";
			}
			
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
			
			if ("PTN".equals(mod)) {
				// 这里修改了ctp的采集方法，不通过ptp采集，改为通过网元采集。
				List<CTP> ctpNeList = service.retrieveAllCtps(this.getTask().getObjectName());
				String size = String.valueOf(ctpNeList != null?ctpNeList.size():0);
				nbilog.info("PhysicalDataTask.ctpNe size = " + size);
				if (ctpNeList != null && ctpNeList.size() > 0) {
					for (CTP ctp : ctpNeList) {
	                    getSqliteConn().insertBObject(ctp);
					}
				}
			}

			if ("OTN".equals(mod)) {
				String size = String.valueOf(ptpList != null?ptpList.size():0);
				nbilog.info("PhysicalDataTask.ptpNe size = " + size);
			}
			
			if (ptpList != null && ptpList.size() > 0) {
				for (PTP ptp : ptpList) {
                    getSqliteConn().insertBObject(ptp);
				}
				
				if ("OTN".equals(mod)) {
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
				
			}
			
			if ("PTN".equals(mod)) {
				List<R_FTP_PTP> ftpList = service.retrieveAllPTPsByFtp(this.getTask().getObjectName());
				nbilog.info("PhysicalDataTask.ftpList size = " + ftpList.size());
				if (ftpList != null && ftpList.size() > 0) {
					for (R_FTP_PTP ftp : ftpList) {
						try {
							getSqliteConn().insertBObject(ftp);
						} catch (Exception e) {
							nbilog.error("PhysicalDataTask.ftpList.excute Exception:", e);
						}
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
