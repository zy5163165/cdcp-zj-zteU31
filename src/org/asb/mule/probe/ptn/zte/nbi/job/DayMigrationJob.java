package org.asb.mule.probe.ptn.zte.nbi.job;

import java.io.File;
import java.util.*;

import com.alcatelsbell.cdcp.domain.SummaryUtil;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.MessageUtil;
import com.alcatelsbell.cdcp.nodefx.NodeContext;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.apache.log4j.Logger;
import org.asb.mule.probe.framework.CommandBean;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.EDS_PTN;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.nbi.job.MigrateCommonJob;
import org.asb.mule.probe.framework.nbi.task.DataTask;
import org.asb.mule.probe.framework.nbi.task.TaskPoolExecutor;
import org.asb.mule.probe.framework.service.SqliteConn;

import org.asb.mule.probe.framework.util.CodeTool;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.zte.nbi.task.*;
import org.asb.mule.probe.ptn.zte.service.ZTEService;
import org.quartz.JobExecutionContext;

import com.alcatelsbell.cdcp.nodefx.FtpUtil;
import com.alcatelsbell.nms.valueobject.BObject;

public class DayMigrationJob extends MigrateCommonJob implements CommandBean {

    private FileLogger nbilog = null;
    private String name = "";
    private SqliteConn sqliteConn = null;
    @Override
    public void execute(JobExecutionContext arg0) {
        Date startTime = new Date();
        nbilog = ((ZTEService) service).getCorbaService().getNbilog();
        //
        if (!service.getConnectState()) {
            nbilog.error(">>>EMS is disconnect.");
            NodeContext.getNodeContext().getLogger().error(service.getEmsName()+" connected failed !!!!!");
            return;
        }

//        String ptp = "EMS:TZ-OTNU31-1-P@ManagedElement:70127740(P)@PTP:/direction=src/rack=0/shelf=1/slot=14/port=26427393";
//        String ftp = "EMS:TZ-OTNU31-1-P@ManagedElement:70127582(P)@FTP:/direction=src/rack=0/shelf=7/slot=52/port=78200856";
//        service.getEmsName();
//        queryCTP(ptp);
//        queryCTP(ftp);

        nbilog.info("Start for task : " + serial);
        nbilog.info("Start to migrate all data from ems: " + service.getEmsName());
        String dbName = getJobName() + ".db";
        nbilog.info("db: " + dbName);
        // name = "";// set empty to create new db instance
        try {
            // 0. set new db for new task.
            sqliteConn = new SqliteConn();
            sqliteConn.setDataPath(dbName);
            sqliteConn.init();
            // 1.ne
            nbilog.info("ManagedElementDataTask : ");

            ManagedElementDataTask neTask = new ManagedElementDataTask();

            neTask.CreateTask(service, getJobName(), service.getEmsName(), nbilog);
            neTask.setSqliteConn(sqliteConn);
            Vector<BObject> neList = neTask.excute();


            nbilog.info("SectionDataTask: ");
            SectionDataTask sectionTask = new SectionDataTask();
            sectionTask.setSqliteConn(sqliteConn);
            sectionTask.CreateTask(service, getJobName(), null, nbilog);
            sectionTask.excute();

            nbilog.info("PhysicalDataTask CrossConnectionDataTask: ");
            int ps = 5;
        //    if (service.getEmsName().equals("WZ-OTNU31-1-P"))
      //          ps = 1;
            TaskPoolExecutor executor = new TaskPoolExecutor(ps);
            for (BObject ne : neList) {
                PhysicalDataTask phyTask = new PhysicalDataTask();
                phyTask.CreateTask(service, getJobName(), ne.getDn(), nbilog);
                phyTask.setSqliteConn(sqliteConn);
                executor.executeTask(phyTask);

                CrossConnectionDataTask ccTask = new CrossConnectionDataTask();
                ccTask.CreateTask(service, getJobName(), ne.getDn(), nbilog);
                ccTask.setSqliteConn(sqliteConn);
                executor.executeTask(ccTask);
            }
            nbilog.info("PhysicalDataTask CrossConnectionDataTask: waitingForAllFinish.");
            executor.waitingForAllFinish();
            nbilog.info("PhysicalDataTask CrossConnectionDataTask: waitingForInsertBObject.");
            sqliteConn.waitingForInsertBObject();



            nbilog.info("SNCDataTask: ");
            SNCDataTask ttTask = new SNCDataTask();
            ttTask.CreateTask(service, getJobName(), null, nbilog);
            ttTask.setSqliteConn(sqliteConn);
            Vector<BObject> ttVector = ttTask.excute();

            nbilog.info("SNCAndCCAndSectionDataTask: ");
            ps = 6;
      //      if (service.getEmsName().equals("WZ-OTNU31-1-P"))
       //         ps = 1;
            TaskPoolExecutor executor2 = new TaskPoolExecutor(ps);
            for (BObject snc : ttVector) {
                SNCAndCCAndSectionDataTask task = new SNCAndCCAndSectionDataTask();
                task.CreateTask(service, getJobName(), snc.getDn(), nbilog);
                task.setSqliteConn(sqliteConn);
                executor2.executeTask(task);
            }
            nbilog.info("SNCAndCCAndSectionDataTask: waitingForAllFinish.");
            executor2.waitingForAllFinish();
            nbilog.info("SNCAndCCAndSectionDataTask: waitingForInsertBObject.");
            sqliteConn.waitingForInsertBObject();

            sqliteConn.waitingForInsertBObject();

            queryCount();

            // clear
            if (neList != null) {
                neList.clear();
            }
            if (ttVector != null) {
                ttVector.clear();
            }
            nbilog.info("End to migrate all data from ems.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            nbilog.error("DayMigrationJob.execute Exception:", e);
        }


        // ftp
        try {
            MessageUtil.sendSBIMessage(serial, "ftpFile", 85);
            // String localfile="2013-09-16-170120-QZ-U2000-1-P-DayMigration.db";
            FtpInfo ftpInfo = FtpUtil.uploadFile("SDH", "ZTE", service.getEmsName(), new File(dbName));

            nbilog.info("uploaded");
            EDS_PTN eds = SummaryUtil.geyEDS(serial,sqliteConn,service.getEmsName(),dbName);
            nbilog.info("eds");
            try {
                sqliteConn.release();
                nbilog.info("release");
            } catch (Exception e) {
                nbilog.error(e, e);
            }
            eds.setStartTime(startTime);
            nbilog.info("sendSBIFinishMessage");
            MessageUtil.sendSBIFinishMessage(ftpInfo, serial, eds);
            nbilog.info("sendSBIFinishMessage success");
        } catch (Exception e) {
            nbilog.error("DayMigrationJob.ftp Exception:", e);
            try {
                MessageUtil.sendSBIFailedMessage("FTP ERROR.", serial);
            } catch (Exception e1) {
                nbilog.error("DayMigrationJob.Message Exception:", e);
            }
        }
        try {
            File file = new File(dbName);
            file.delete();
            MessageUtil.sendSBIMessage(serial, "End", 90);
        } catch (Exception e) {
            nbilog.error("DayMigrationJob.Message Exception:", e);
        }

        nbilog.info("End of task : " + serial);
        // message

    }

    private void queryCount() {
        Logger logger = NodeContext.getNodeContext().getLogger();
        synchronized (logger) {
            logger.info("===========================  ["+service.getEmsName()+"]"+getJobName()+" =========================================================");
            try {
                JPASupport jpaSupport = sqliteConn.getJpaSupport();
                HashMap<String,String> sqls = new HashMap<String, String>();
                sqls.put("NE:","SELECT count(ne.dn)     FROM  ManagedElement ne ");
                sqls.put("slot:","SELECT count(slot.dn)       FROM  EquipmentHolder slot WHERE slot.holderType='slot' ");
                sqls.put("subslot:", "SELECT count(subslot.dn)    FROM  EquipmentHolder subslot WHERE subslot.holderType='sub_slot' ");
                sqls.put("card:","SELECT count(card.dn)       FROM  Equipment card ");
                sqls.put("ptp:","SELECT count(ptp.dn)        FROM  PTP ptp WHERE dn like '%PTP%' ");
                sqls.put("ftp:","SELECT count(ftp.dn)        FROM  PTP ftp WHERE dn like '%FTP%' ");
                sqls.put("ctp:","SELECT count(id)        FROM  CTP ");
                sqls.put("crossconnect:","SELECT count(id)        FROM  CrossConnect ");
                sqls.put("subnetworkconnection:","SELECT count(id) FROM SubnetworkConnection ");
                sqls.put("section:","SELECT count(id) FROM Section ");
                sqls.put("R_TrafficTrunk_CC_Section:","SELECT count(id) FROM R_TrafficTrunk_CC_Section ");

                Set<String> keySet = sqls.keySet();
                for (String key : keySet) {
                    String sql = sqls.get(key);
                    List list = JPAUtil.getInstance().queryQL(jpaSupport, sql);
                    int count = ((Long) list.get(0)).intValue();
                    nbilog.info(key+" "+count);
                    logger.info(key+" "+count);
                }


                // jpaSupport.end();
            } catch (Exception e) {
                nbilog.error(e,e);
            }
            logger.info("===============================================================================================================");
        }

    }
    private void executeTask(TaskPoolExecutor executor, DataTask task) {
        executor.executeTask(task);
    }

    /**
     * store tp and sectionDn relation key:ptpDn value:sectionDn
     *
     * @param sectionList
     * @return
     */
    private HashMap getSectionByTp(Vector<BObject> sectionList) {
        HashMap map = new HashMap<String, Section>();
        for (BObject section : sectionList) {
            if (section instanceof Section) {
                map.put(((Section) section).getaEndTP(), section);
                map.put(((Section) section).getzEndTP(), section);
            }
        }
        return map;
    }

    /**
     * define job name ,as unique id for migration job. It can be used in failed
     * job to migrate ems data from ems.
     *
     * @return
     */
    private String getJobName() {
        if (name.trim().length() == 0) {
            // name = CodeTool.getDatetime()+"-"+service.getEmsName()+"-DayMigration";
            name = service.getEmsName().contains("/") ? service.getEmsName().replace("/", "-") : service.getEmsName();
            name = CodeTool.getDatetimeStr() + "-" + name + "-DayMigration";
        }
        return name;
    }

    @Override
    public void execute() {

        execute(null);
    }

    private void queryCTP(String ptpDn) {
        List<CTP> ctps = service.retrieveAllCtps(ptpDn);
        nbilog.info("ptp="+ptpDn);
        nbilog.info("ctp size = "+(ctps == null ? null:ctps.size()));
        if (ctps != null) {
            for (CTP ctp : ctps) {
                nbilog.info("ctp="+ctp.getDn());
            }
        }

    }
}
