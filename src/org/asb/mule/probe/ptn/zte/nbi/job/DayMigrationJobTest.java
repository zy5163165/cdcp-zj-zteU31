package org.asb.mule.probe.ptn.zte.nbi.job;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.FtpUtil;
import org.asb.mule.probe.framework.CommandBean;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.nbi.job.MigrateCommonJob;
import org.asb.mule.probe.framework.nbi.task.DataTask;
import org.asb.mule.probe.framework.nbi.task.TaskPoolExecutor;
import org.asb.mule.probe.framework.service.SqliteConn;

import org.asb.mule.probe.framework.util.CodeTool;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.zte.nbi.task.CrossConnectionDataTask;
import org.asb.mule.probe.ptn.zte.nbi.task.ManagedElementDataTask;
import org.asb.mule.probe.ptn.zte.nbi.task.PhysicalDataTask;
import org.asb.mule.probe.ptn.zte.nbi.task.SNCAndCCAndSectionDataTask;
import org.asb.mule.probe.ptn.zte.nbi.task.SNCDataTask;
import org.asb.mule.probe.ptn.zte.nbi.task.SectionDataTask;
import org.asb.mule.probe.ptn.zte.service.ZTEService;
import org.quartz.JobExecutionContext;

import com.alcatelsbell.nms.valueobject.BObject;

public class DayMigrationJobTest extends MigrateCommonJob implements CommandBean {

    private FileLogger nbilog = null;
    private String name = "";
    private SqliteConn sqliteConn = null;

    @Override
    public void execute(JobExecutionContext arg0) {
        nbilog = ((ZTEService) service).getCorbaService().getNbilog();
        //
        if (!service.getConnectState()) {
            nbilog.error(">>>EMS is disconnect.");
            return;
        }

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
            neTask.setSqliteConn(sqliteConn);
            neTask.CreateTask(service, getJobName(), service.getEmsName(), nbilog);
            Vector<BObject> neList = neTask.excute();

            nbilog.info("PhysicalDataTask CrossConnectionDataTask: ");
            TaskPoolExecutor executor = new TaskPoolExecutor(5);
            for (BObject ne : neList) {
                PhysicalDataTask phyTask = new PhysicalDataTask();
                phyTask.CreateTask(service, getJobName(), ne.getDn(), nbilog);
                phyTask.setSqliteConn(sqliteConn);
                executor.executeTask(phyTask);

                CrossConnectionDataTask ccTask = new CrossConnectionDataTask();
                ccTask.setSqliteConn(sqliteConn);
                ccTask.CreateTask(service, getJobName(), ne.getDn(), nbilog);
                executor.executeTask(ccTask);
            }
            nbilog.info("PhysicalDataTask CrossConnectionDataTask: waitingForAllFinish.");
            executor.waitingForAllFinish();
            nbilog.info("PhysicalDataTask CrossConnectionDataTask: waitingForInsertBObject.");
            sqliteConn.waitingForInsertBObject();

            nbilog.info("SectionDataTask: ");
            SectionDataTask sectionTask = new SectionDataTask();
            sectionTask.setSqliteConn(sqliteConn);
            sectionTask.CreateTask(service, getJobName(), null, nbilog);
            sectionTask.excute();

            nbilog.info("SNCDataTask: ");
            SNCDataTask ttTask = new SNCDataTask();
            ttTask.setSqliteConn(sqliteConn);
            ttTask.CreateTask(service, getJobName(), null, nbilog);
            Vector<BObject> ttVector = ttTask.excute();

            nbilog.info("SNCAndCCAndSectionDataTask: ");
            TaskPoolExecutor executor2 = new TaskPoolExecutor(6);
            for (BObject snc : ttVector) {
                SNCAndCCAndSectionDataTask task = new SNCAndCCAndSectionDataTask();
                task.setSqliteConn(sqliteConn);
                task.CreateTask(service, getJobName(), snc.getDn(), nbilog);
                executor2.executeTask(task);
            }
            nbilog.info("SNCAndCCAndSectionDataTask: waitingForAllFinish.");
            executor2.waitingForAllFinish();
            nbilog.info("SNCAndCCAndSectionDataTask: waitingForInsertBObject.");
            sqliteConn.waitingForInsertBObject();

            sqliteConn.waitingForInsertBObject();
            sqliteConn.release();
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

        nbilog.info("Uploading file...");
        try {
            FtpInfo ftpInfo = FtpUtil.uploadFile("SDH", "ZTE", service.getEmsName(), new File(dbName));
            nbilog.info("Uploading file to :"+ftpInfo);
        } catch (Exception e) {
            nbilog.error(e, e);
        }
        nbilog.info("End of task : " + serial);
        try {
            Thread.sleep(5000l);
        } catch (InterruptedException e) {

        }



        nbilog.info("System Exit");
        System.exit(0);
        // message

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

}
