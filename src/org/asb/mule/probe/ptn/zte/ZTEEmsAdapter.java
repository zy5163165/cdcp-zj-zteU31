package org.asb.mule.probe.ptn.zte;

import java.util.List;

import com.alcatelsbell.cdcp.nodefx.*;
import com.alcatelsbell.nms.valueobject.sys.Ems;

import org.asb.mule.probe.framework.entity.DeviceInfo;
import org.asb.mule.probe.framework.service.CorbaSbiService;
import org.asb.mule.probe.framework.service.NbiService;
import org.asb.mule.probe.ptn.zte.nbi.job.DayMigrationJob;
import org.asb.mule.probe.ptn.zte.nbi.job.DayMigrationJob4PTN;
import org.asb.mule.probe.ptn.zte.nbi.job.DeviceJob;
import org.asb.mule.probe.ptn.zte.sbi.service.CorbaService;
import org.asb.mule.probe.ptn.zte.service.ZTEService;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-10
 * Time: 下午8:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ZTEEmsAdapter extends CorbaEmsAdapterTemplate {


    @Override
    public Object doTestEms(NbiService nbiService) {
        return ((ZTEService)nbiService).getCorbaService().isConnectState();
    }

    @Override
    public Object doSyncEms(NbiService nbiService, Ems ems, String _serial) {
        logger.info("doSyncEms:"+ems.getDn());
        ZTEService zteService = (ZTEService)nbiService;
                if (ems.getTag1() == null) ems.setTag1("PTN");
                if (ems.getTag1().equals("SDH") || ems.getTag1().equals("OTN") || ems.getTag1().equals("WDM") ||  ems.getTag1().equals("DWDM")) {
                    DayMigrationJob  job = new DayMigrationJob ();

                    job.setService(zteService);
                    job.setSerial(_serial);
                    job.execute();
                }
                else {
                   logger.error("ZTE with PTN ems? ----------"+ems.getDn());

                    DayMigrationJob4PTN job = new DayMigrationJob4PTN ();

                    job.setService(zteService);
                    job.setSerial(_serial);
                    job.execute();
                }

                return null;
    }

    @Override
    public Object doSyncDevice(NbiService nbiService, String _serial, String devicedn) {
                DeviceJob job = new DeviceJob(devicedn);
                job.setService(nbiService);
                job.setSerial(_serial);
                  job.execute();
        return null;
    }

    @Override
    public CorbaSbiService createCorbaSbiService() {
        return new CorbaService();
    }

    @Override
    public NbiService createNbiService(CorbaSbiService corbaSbiService) {
        ZTEService zteService = new ZTEService();
        zteService.setCorbaService((CorbaService)corbaSbiService);
        return zteService;
    }

    @Override
    public String getType() {
        return "ZTE";
    }
}
