package org.asb.mule.probe.ptn.zte.nbi.task;

import java.util.List;
import java.util.Vector;

import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.nbi.task.CommonDataTask;


import com.alcatelsbell.nms.valueobject.BObject;

public class SectionDataTask extends CommonDataTask {

	public SectionDataTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<BObject> excute() {
		// TODO Auto-generated method stub
		try {
			List<Section> sectionList = service.retrieveAllSections();
			if (sectionList != null && sectionList.size() > 0) {
				nbilog.info("Section : " + sectionList.size());
				for (Section section : sectionList) {
                    getSqliteConn().insertBObject(section);
				}
			}
			return new Vector<BObject>(sectionList);
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
