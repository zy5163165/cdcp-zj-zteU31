package org.asb.mule.probe.ptn.zte.nbi.job;

import com.alcatelsbell.cdcp.nodefx.NEWrapper;
import com.alcatelsbell.cdcp.nodefx.NodeContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.CommandBean;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.EquipmentHolder;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.nbi.job.MigrateCommonJob;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-30
 * Time: 下午11:22
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DeviceJob extends MigrateCommonJob implements CommandBean {

    private String devicedn = null;

    public DeviceJob(String devicedn) {
        this.devicedn = devicedn;
    }

    @Override
    public void execute(JobExecutionContext arg0) {
        // TODO Auto-generated method stub
        NEWrapper neWrapper = new NEWrapper();

        ManagedElement me = service.retrieveManagedElement(devicedn);
        List<EquipmentHolder> holderList = new ArrayList<EquipmentHolder>();
        List<Equipment> cardList = new ArrayList<Equipment>();
        service.retrieveAllEquipmentAndHolders(devicedn, holderList, cardList);
        List<PTP> ptpList = service.retrieveAllPtps(devicedn);

        neWrapper.setMe(me);
        neWrapper.setEquipmentHolders(holderList);
        neWrapper.setEquipments(cardList);
        neWrapper.setPtps(ptpList);

        NodeContext.getNodeContext().deliverEmsJobObject(serial, neWrapper);
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        execute(null);
    }

}
