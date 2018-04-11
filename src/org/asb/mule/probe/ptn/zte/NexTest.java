package org.asb.mule.probe.ptn.zte;

import com.alcatelsbell.cdcp.nodefx.EmsExecutable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.service.NbiService;
import org.asb.mule.probe.ptn.zte.service.ZTEService;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2014/12/25
 * Time: 17:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class NexTest implements EmsExecutable {
    private Log logger = LogFactory.getLog(getClass());

    @Override
    public Object execute(NbiService u2000Service) {
        ZTEService zteService  = (ZTEService)u2000Service;
        List<CTP> ctps = zteService.retrieveAllCtps("EMS:TZ-OTNU31-1-P@ManagedElement:70127685(P)@FTP:/direction=sink/rack=0/shelf=7/slot=24/port=78151693");
        StringBuffer sb = new StringBuffer();
        for (CTP ctp : ctps) {
            sb.append(ctp.getDn()+"\n");
        }
        return sb.toString();

    }
}
