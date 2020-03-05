package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.R_FTP_PTP;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;
import org.asb.mule.probe.framework.service.SqliteConn;

import com.alcatelsbell.nms.valueobject.BObject;

public class FTPAndPTPDataTask extends CommonDataTask {

    public FTPAndPTPDataTask(SqliteConn sqliteConn) {
        this.setSqliteConn(sqliteConn);
        // TODO Auto-generated constructor stub

    }
	public Vector<BObject> excute() {
		// TODO Auto-generated method stub
		// Vector<BObject> neVec = new Vector<BObject>();

		List<R_FTP_PTP> resultList = service.retrieveAllPTPsByFtp(this.getTask().getObjectName());
		//System.out.println("retrieveAllPTPsByFtp:" + getTask().getObjectName() + " size = " + (resultList == null ? null : resultList.size()));

		try {
			// JPASupport sqliteJPASupport = getSqliteConn()
			// .getJpaSupport();
			// sqliteJPASupport.begin();
			if (resultList != null && resultList.size() > 0) {
				for (R_FTP_PTP item : resultList) {
					// JPAUtil.getInstance().saveObject(sqliteJPASupport, -1, item);
					getSqliteConn().insertBObject(item);
					// neVec.add(item);
				}
			}
			// getSqliteConn().getJpaSupport().end();
			// getSqliteConn().getJpaSupport().release();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			// getSqliteConn().getJpaSupport().release();
			// return neVec;
			return null;
		}
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
