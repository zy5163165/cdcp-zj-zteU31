package org.asb.mule.probe.ptn.zte.nbi.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.asb.mule.probe.framework.CommandBean;
import org.asb.mule.probe.framework.entity.CRD;
import org.asb.mule.probe.framework.entity.EDS_PTN;
import org.asb.mule.probe.framework.entity.EPG;
import org.asb.mule.probe.framework.entity.EPU;
import org.asb.mule.probe.framework.entity.EQH;
import org.asb.mule.probe.framework.entity.NEL;
import org.asb.mule.probe.framework.entity.OMC;
import org.asb.mule.probe.framework.entity.PGU;
import org.asb.mule.probe.framework.entity.PRT;
import org.asb.mule.probe.framework.entity.PTG;
import org.asb.mule.probe.framework.entity.SBN;
import org.asb.mule.probe.framework.entity.SNN;
import org.asb.mule.probe.framework.entity.TPL;
import org.asb.mule.probe.framework.entity.spn.BRD;
import org.asb.mule.probe.framework.entity.spn.ESI;
import org.asb.mule.probe.framework.entity.spn.ESP;
import org.asb.mule.probe.framework.entity.spn.ETH;
import org.asb.mule.probe.framework.entity.spn.ETP;
import org.asb.mule.probe.framework.entity.spn.IGL;
import org.asb.mule.probe.framework.entity.spn.IGT;
import org.asb.mule.probe.framework.entity.spn.L3I;
import org.asb.mule.probe.framework.entity.spn.L3P;
import org.asb.mule.probe.framework.entity.spn.L3T;
import org.asb.mule.probe.framework.entity.spn.LBS;
import org.asb.mule.probe.framework.entity.spn.MCB;
import org.asb.mule.probe.framework.entity.spn.MCL;
import org.asb.mule.probe.framework.entity.spn.MCP;
import org.asb.mule.probe.framework.entity.spn.MCS;
import org.asb.mule.probe.framework.entity.spn.MGB;
import org.asb.mule.probe.framework.entity.spn.MGP;
import org.asb.mule.probe.framework.entity.spn.MPI;
import org.asb.mule.probe.framework.entity.spn.MTL;
import org.asb.mule.probe.framework.entity.spn.MTR;
import org.asb.mule.probe.framework.entity.spn.NWS;
import org.asb.mule.probe.framework.entity.spn.PRB;
import org.asb.mule.probe.framework.entity.spn.PSW;
import org.asb.mule.probe.framework.entity.spn.PWP;
import org.asb.mule.probe.framework.entity.spn.PWT;
import org.asb.mule.probe.framework.entity.spn.SRR;
import org.asb.mule.probe.framework.entity.spn.SRT;
import org.asb.mule.probe.framework.entity.spn.STT;
import org.asb.mule.probe.framework.entity.spn.TDM;
import org.asb.mule.probe.framework.entity.spn.TNL;
import org.asb.mule.probe.framework.entity.spn.TPB;
import org.asb.mule.probe.framework.entity.spn.TPI;
import org.asb.mule.probe.framework.nbi.job.MigrateCommonJob;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.service.SqliteConn;
import org.asb.mule.probe.framework.util.CodeTool;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.zte.service.ZTEService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.quartz.JobExecutionContext;

import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.FtpUtil;
import com.alcatelsbell.cdcp.nodefx.MessageUtil;
import com.alcatelsbell.cdcp.nodefx.NodeContext;
import com.alcatelsbell.nms.common.Detect;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.valueobject.BObject;

/**
 * 中兴SPN，OMC接口
 * Author: Zongyu
 * Date: 2020
 * zongyu@shsinceretech.com
 * 
 * 针对集团要求的新的socket接口开发
 * 该java类主要做三件事：
 * 1. 从ftp服务器上下载xml文件的gz压缩包
 * 2. 解压压缩包得到xml文件
 * 3. 把xml文件转换成sqlite的db文件
 * 
 */
public class DayMigrationJob4SPN  extends MigrateCommonJob implements CommandBean {

    private FileLogger nbilog = null;
    private String name = "";
    private SqliteConn sqliteConn = null;

    private long t1 = System.currentTimeMillis();
    @Override
    public void execute(JobExecutionContext arg0) {
        nbilog = ((ZTEService) service).getCorbaService().getNbilog();
        Date startTime = new Date();
        EDS_PTN eds = null;
//        if (!service.getConnectState()) {
//            nbilog.error(service.getEmsName()+"  >>>EMS is disconnect.");
//            NodeContext.getNodeContext().getLogger().error(service.getEmsName()+"  >>>EMS is disconnect.");
//            return;
//        }
        String emsDn = service.getEmsName();
        nbilog.info("中兴SPN网管采集：DayMigrationJob4SPN");
        nbilog.info("Start for task : " + serial);
        nbilog.info("Start to migrate all data from ems: " + emsDn);

        String dir = SysProperty.getString("cdcp.node.db.dir", "");
        if (!dir.isEmpty() && !(dir.endsWith("/") || dir.endsWith("\\"))) dir += File.pathSeparator;
        if (!dir.isEmpty() && new File(dir).isDirectory()) new File(dir).mkdirs();
        String dbName = dir +getJobName() + ".db";

        nbilog.info("db: " + dbName);
        try {
//          String date = StringUtils.substringBetween(serial, "@", "-");
        	String date = new SimpleDateFormat("YYYYMMdd").format(System.currentTimeMillis());
        	
        	nbilog.info("emsdn: " + emsDn);
//        	Ems ems = (Ems) JpaServerUtil.getInstance().findObjectByDN(Ems.class, emsDn);
//        	String tag2 = ems.getTag2();
        	String info = SysProperty.getString("cdcp.emsinfo."+emsDn, "");
        	nbilog.info("additionalInfo: " + info);
        	Map<String, String> map = transMapValue(info);
        	String ipAddress = map.get("ipAddress");//"10.76.135.226";
        	String ftpPath = map.get("ftpPath") + date + "/";//"ZJ/CS/FH/ZJ-FH-1-OTN/CM/" + date + "/";
        	String user = map.get("user");//"admin";
        	String passwd = map.get("passwd");//"vislecaina@123";
        	String amStamp = map.get("amStamp") != null ? map.get("amStamp") : "00";
        	String pmStamp = map.get("pmStamp") != null ? map.get("pmStamp") : "12";
        	
        	String gzPath = "/home/emsptn/ftpDownload/gz/" + emsDn + "/" + date + "/"; // gz压缩包路径
        	String xmlPath = "/home/emsptn/ftpDownload/xml/" + emsDn + "/" + date; // xml文件路径
        	
        	SimpleDateFormat df = new SimpleDateFormat("HH");
			String timeStamp = date + amStamp;
			if (Integer.parseInt(df.format(System.currentTimeMillis())) > 12) {
				// 过了12点，属于下午的采集
				timeStamp = date + pmStamp;
				xmlPath = xmlPath + "-pm/";
			} else {
				// 没过12点，属于上午的采集
				xmlPath = xmlPath + "-am/";
			}
        	
        	nbilog.info("date: " + date);
        	nbilog.info("ipAddress: " + ipAddress);
        	nbilog.info("ftpPath: " + ftpPath);
        	nbilog.info("user: " + user);
        	nbilog.info("passwd: " + passwd);
        	nbilog.info("gzPath: " + gzPath);
        	nbilog.info("xmlPath: " + xmlPath);
        	nbilog.info("timeStamp: " + timeStamp);
        	
        	clearPath(gzPath, timeStamp);
        	clearPath(xmlPath, timeStamp);
        	
        	nbilog.info("downloadFromFtp : start...");
        	downloadFromFtp(ipAddress, user, passwd, ftpPath, gzPath, timeStamp); // 从ftp下载压缩包
        	
        	nbilog.info("decompressionGz : start...");
        	File file = new File(gzPath);
    		File[] tempFile = file.listFiles();
    		boolean isGz = false;
    		if (tempFile != null && tempFile.length > 0 && tempFile[0].getName().endsWith(".gz")) {
    			isGz = true;
    		}
    		if (isGz) {
    			nbilog.info("decompressionGz : gz...");
    			decomGz(gzPath, xmlPath, timeStamp); // 解压压缩包gz得到xml文件
    		} else {
    			nbilog.info("decompressionGz : zip...");
    			decomZip(gzPath, xmlPath, timeStamp); // 解压压缩包zip得到xml文件
    		}
        	
        	nbilog.info("transXml : start...");
    		transXml(xmlPath, dbName); // 解析xml文件
    		
    		nbilog.info("transXml : end...");
            
            queryCount();
//            eds = SummaryUtil.geyEDS(serial, sqliteConn, emsDn, dbName);
            sqliteConn.release();
            nbilog.info("End to migrate all data from ems.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	nbilog.error(e, e);
            e.printStackTrace();
        }

        // message
        nbilog.info("Uploading file...");
        try {
            FtpInfo ftpInfo = FtpUtil.uploadFile("SDH", "FH", emsDn, new File(dbName));
            ftpInfo.getAttributes().put("logical",""+logical);
            nbilog.info("Uploading file to :"+ftpInfo);
            if (eds != null) {
                eds.setStartTime(startTime);
                HashMap map = new HashMap();
                map.put("logical",logical);
                eds.setUserObject(map);
            }

            MessageUtil.sendSBIFinishMessage(ftpInfo, serial, eds);
        } catch (Exception e) {
            nbilog.error(e, e);
        }

        try {
            File file = new File(dbName);
            file.delete();
            MessageUtil.sendSBIMessage(serial, "End", 90);
        } catch (Exception e) {
            nbilog.error("DayMigrationJob.Message Exception:", e);
        }

        nbilog.info("End of task : " + serial);
        try {
            Thread.sleep(5000l);
        } catch (InterruptedException e) {

        }

    }
    
	private void downloadFromFtp(String ipAddress, String user, String passwd, String ftpPath, String gzPath, String timeStamp) {
		// ftp服务器登录凭证
		int port = 21;
		FTPClient ftp = null;
		try {
			// ftp的数据下载
			ftp = new FTPClient();
			ftp.connect(ipAddress, port);
			ftp.login(user, passwd);
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			// 设置linux环境
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
			ftp.configure(conf);

			// 判断是否连接成功
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.out.println("FTP server refused connection.");
				return;
			}

			// 设置访问被动模式
			ftp.setRemoteVerificationEnabled(false);
			ftp.enterLocalPassiveMode();

			// 检索ftp目录下所有的文件，利用时间字符串进行过滤
			boolean dir = ftp.changeWorkingDirectory(ftpPath);
			nbilog.info("dir: " + dir);
			if (dir) {
				FTPFile[] fs = ftp.listFiles();
				for (FTPFile f : fs) {
					if(f.getName().indexOf(timeStamp)>0) {
						nbilog.info(f.getName() + "  ftpDownload下载xml成功");
						File localFile = new File(gzPath + f.getName());
						OutputStream ios = new FileOutputStream(localFile);
						ftp.retrieveFile(f.getName(), ios);
						ios.close();
					}
				}
			}
		} catch (Exception e) {
			nbilog.error(e, e);
			e.printStackTrace();
			System.out.println(new Date() + "  ftp下载xml文件发生错误");
		} finally {
			if (ftp != null) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					nbilog.error(ioe, ioe);
				}
			}
		}

	}
	
	private void decomGz(String gzPath, String xmlPath, String timeStamp) {
        try {
        	File file = new File(gzPath);
    		File[] tempFile = file.listFiles();
    		for (int i = 0; i < tempFile.length; i++) {
    			if (StringUtils.contains(tempFile[i].getName(), timeStamp)) {
    				String sourcedir = gzPath + tempFile[i].getName();
    				String ouputfile = xmlPath + tempFile[i].getName();
    				ouputfile = ouputfile.substring(0,ouputfile.lastIndexOf('.'));
    				
    				//建立gzip压缩文件输入流 
                    FileInputStream fin = new FileInputStream(sourcedir);   
                    //建立gzip解压工作流
                    GZIPInputStream gzin = new GZIPInputStream(fin);   
                    //建立解压文件输出流  
                    FileOutputStream fout = new FileOutputStream(ouputfile);   
                    
                    int num;
                    byte[] buf=new byte[1024];

                    while ((num = gzin.read(buf,0,buf.length)) != -1)
                    {   
                        fout.write(buf,0,num);   
                    }

                    gzin.close();   
                    fout.close();   
                    fin.close();   
    			}
    		}
        } catch (Exception ex){  
        	nbilog.error(ex, ex);
            System.err.println(ex.toString());  
        }  
        return;
    }
    
	private static final int buffer = 2048;
    private void decomZip(String zipPath, String xmlPath, String timeStamp) {
    	InputStream is = null; 
	    FileOutputStream fos = null; 
	    BufferedOutputStream bos = null;
	    ZipFile zipFile = null;
        try {
        	File file = new File(zipPath);
    		File[] tempFile = file.listFiles();
    		for (int i = 0; i < tempFile.length; i++) {
    			if (StringUtils.contains(tempFile[i].getName(), timeStamp)) {
    				String sourcedir = zipPath + tempFile[i].getName();
    				zipFile = new ZipFile(sourcedir, "gbk"); //解决中文乱码问题 
    			    Enumeration<?> entries = zipFile.getEntries();
    			    int count = -1;
    			    while(entries.hasMoreElements()) {
    			    	byte buf[] = new byte[buffer]; 
    			        ZipEntry entry = (ZipEntry)entries.nextElement(); 
    			        String filename = entry.getName(); 
    			        String ouputFile = xmlPath + filename;
    			        file = new File(ouputFile);
    			        file.createNewFile(); //创建文件 
    			        is = zipFile.getInputStream(entry); 
    			        fos = new FileOutputStream(file); 
    			        bos = new BufferedOutputStream(fos, buffer); 
    			        while((count = is.read(buf)) > -1) 
    			        { 
    			          bos.write(buf, 0, count); 
    			        } 
    			        bos.flush(); 
    			        bos.close(); 
    			        fos.close(); 
    			        is.close(); 
    			    }
    				
    			    zipFile.close();
    			}
    		}
        } catch (Exception ex){  
        	nbilog.error(ex, ex);
            System.err.println(ex.toString());  
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				nbilog.error(e, e);
	            System.err.println(e.toString());  
			}
		} 
        return;
    	
    }
    
    /**
     * 文件路径初始化
     * @param file
     */
    private void createPath(File file) {
    	File fileParent = file.getParentFile();
    	if(!fileParent.exists()){
    		fileParent.mkdirs();
    	}
    }
    /**
     * 文件夹初始化
     * @param path
     * @param timeStamp
     */
	private void clearPath(String path, String timeStamp) {
		File file = new File(path);
		if (file.exists()) {
			// 有文件夹，删除下面的目标文件
			String[] fileList = file.list();
			for (int i = 0; i < fileList.length; i++) {
				if (StringUtils.contains(fileList[i], timeStamp)) {
					File del = new File(path + fileList[i]);
					del.delete();
				}
			}

		} else {
			// 无文件夹，创建路径
			file.mkdirs();
		}

	}

    private void queryCount() {
        Logger logger = NodeContext.getNodeContext().getLogger();
        synchronized (logger) {
            logger.info("");
            long t2 = (System.currentTimeMillis() - t1) / (3600000l);
            logger.info("=========================== "+t2+"Hours ["+service.getEmsName()+"]"+getJobName()+" =========================================================");
            try {
                JPASupport jpaSupport = sqliteConn.getJpaSupport();
                HashMap<String,String> sqls = new HashMap<String, String>();
                sqls.put("OMC:","SELECT count(rmuid) FROM OMC ");
                sqls.put("NEL:","SELECT count(rmuid) FROM NEL ");
                sqls.put("EQH:","SELECT count(rmuid) FROM EQH ");
                sqls.put("CRD:","SELECT count(rmuid) FROM CRD ");
                sqls.put("PRT:","SELECT count(rmuid) FROM PRT ");
                sqls.put("PRB:","SELECT count(rmuid) FROM PRB ");
                sqls.put("TNL:","SELECT count(rmuid) FROM TNL ");
                sqls.put("LBS:","SELECT count(rmuid) FROM LBS ");
                sqls.put("TPI:","SELECT count(rmuid) FROM TPI ");
                sqls.put("TPB:","SELECT count(rmuid) FROM TPB ");
                sqls.put("MPI:","SELECT count(rmuid) FROM MPI ");
                sqls.put("MTR:","SELECT count(rmuid) FROM MTR ");
                sqls.put("MTL:","SELECT count(rmuid) FROM MTL ");
                sqls.put("PSW:","SELECT count(rmuid) FROM PSW ");
                sqls.put("PWP:","SELECT count(rmuid) FROM PWP ");
                sqls.put("PWT:","SELECT count(rmuid) FROM PWT ");
                sqls.put("ETH:","SELECT count(rmuid) FROM ETH ");
                sqls.put("ESP:","SELECT count(rmuid) FROM ESP ");
                sqls.put("ESI:","SELECT count(rmuid) FROM ESI ");
                sqls.put("TDM:","SELECT count(rmuid) FROM TDM ");
                sqls.put("ETP:","SELECT count(rmuid) FROM ETP ");
                sqls.put("BRD:","SELECT count(rmuid) FROM BRD ");
                sqls.put("L3I:","SELECT count(rmuid) FROM L3I ");
                sqls.put("L3P:","SELECT count(rmuid) FROM L3P ");
                sqls.put("L3T:","SELECT count(rmuid) FROM L3T ");
                sqls.put("TPL:","SELECT count(rmuid) FROM TPL ");
                sqls.put("SBN:","SELECT count(rmuid) FROM SBN ");
                sqls.put("SNN:","SELECT count(rmuid) FROM SNN ");
                sqls.put("EPG:","SELECT count(rmuid) FROM EPG ");
                sqls.put("EPU:","SELECT count(rmuid) FROM EPU ");
                sqls.put("PTG:","SELECT count(rmuid) FROM PTG ");
                sqls.put("PGU:","SELECT count(rmuid) FROM PGU ");
                sqls.put("STT:","SELECT count(rmuid) FROM STT ");
                sqls.put("SRT:","SELECT count(rmuid) FROM SRT ");
                sqls.put("SRR:","SELECT count(rmuid) FROM SRR ");
                sqls.put("IGT:","SELECT count(rmuid) FROM IGT ");
                sqls.put("IGL:","SELECT count(rmuid) FROM IGL ");
                sqls.put("NWS:","SELECT count(rmuid) FROM NWS ");
                sqls.put("MGP:","SELECT count(rmuid) FROM MGP ");
                sqls.put("MGB:","SELECT count(rmuid) FROM MGB ");
                sqls.put("MCL:","SELECT count(rmuid) FROM MCL ");
                sqls.put("MCS:","SELECT count(rmuid) FROM MCS ");
                sqls.put("MCP:","SELECT count(rmuid) FROM MCP ");
                sqls.put("MCB:","SELECT count(rmuid) FROM MCB ");
                
                sqls.put("ptp:","SELECT count(rmuid) FROM PRT prt WHERE prt.physicalOrLogical='ptp' ");
                sqls.put("ftp:","SELECT count(rmuid) FROM PRT prt WHERE prt.physicalOrLogical='ftp' ");
                

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
            logger.info("");
        }

    }

    /**
     * define job name ,as unique id for migration job.
     * It can be used in failed job to migrate ems data from ems.
     *
     * @return
     */
    private String getJobName() {
        if (name.trim().length() == 0) {
            // name = CodeTool.getDatetime()+"-"+service.getEmsName()+"-DayMigration";

            name = CodeTool.getDatetimeStr() + "-" + service.getEmsName() + "-DayMigration";
        }
        return name;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        execute(null);
    }
    
	/**
	 * xml文件转db文件
	 */
	public void transXml(String path, String dbName) {
		
		HashMap<String,Object> map = getObjectMap();
		sqliteConn = new SqliteConn();
		sqliteConn.setDataPath(dbName);
		sqliteConn.init();
		try {
			// read xml
			// "D:\\xml\\CM-OTN-CRD-A1-V1.0.0-20180705120024.xml"
			for (String key : map.keySet()) {
//				String filePath = path + "CM-OTN-" + key + "-A1-V1.0.0-20180705120024.xml"; // 写死的拼装文件名
				try {
					String fileName = getFileName(key, path); // xml文件名模糊匹配
					if (!Detect.notEmpty(fileName)) {
						if ("MGP".equals(key)) {
							nbilog.error("MGP替换为FEG");
							fileName = getFileName("FEG", path);
						}
						if ("MGB".equals(key)) {
							nbilog.error("MGB替换为FGB");
							fileName = getFileName("FGB", path);
						}
						if ("MCL".equals(key)) {
							nbilog.error("MCL替换为FEC");
							fileName = getFileName("FEC", path);
						}
						if ("MCS".equals(key)) {
							nbilog.error("MCS替换为FES");
							fileName = getFileName("FES", path);
						}
						if ("MCP".equals(key)) {
							nbilog.error("MCP替换为FCP");
							fileName = getFileName("FCP", path);
						}
						if ("MCB".equals(key)) {
							nbilog.error("MCB替换为FCB");
							fileName = getFileName("FCB", path);
						}
					}
					if (!Detect.notEmpty(fileName)) {
						nbilog.error("实体："+key+"的xml文件未找到，请检查！");
						continue;
					}
					String filePath = path + fileName;
					
					File inputXml = new File(filePath);
					SAXReader saxReader = new SAXReader();
					Document document = saxReader.read(inputXml);
					List<BObject> objs = getObject(document, map.get(key).getClass());
					for (BObject obj : objs) {
						sqliteConn.insertBObject(obj);
					}
					sqliteConn.waitingForInsertBObject();
				} catch (Exception e) {
					nbilog.error(key+"解析报错："+e, e);
					e.printStackTrace();
				}
			}
			
			sqliteConn.waitingForInsertBObject();
			
			System.out.println("trans finished...");
			
		} catch (Exception e) {
			nbilog.error(e, e);
			e.printStackTrace();
		}
		
	}

	/**
	 * xml文档Document转对象
	 *
	 * @param document
	 * @param clazz
	 * @return
	 */
	public List<BObject> getObject(Document document, Class<?> clazz) {
		List<BObject> objs = new ArrayList<BObject>();
		BObject obj = null;
		Element users = document.getRootElement();
		HashMap<String,String> fieldNames = new HashMap<String,String>();
		
		try {
			
            // 获取实体类的所有属性，返回Field数组
            Field[] fields = clazz.getDeclaredFields();
            
            for (Iterator<?> i = users.elementIterator(); i.hasNext();) { // 外层循环
    			Element user = (Element) i.next();
    			for (Iterator<?> j = user.elementIterator(); j.hasNext();) {
    				Element node = (Element) j.next();
    				if ("FieldName".equals(node.getName())) { // 属性名列表
    					for (Iterator<?> k = node.elementIterator(); k.hasNext();) {
    						Element fieldName = (Element) k.next();
    						System.out.println(fieldName.attribute("i").getValue() + ":" + fieldName.getText());
    						fieldNames.put(fieldName.attribute("i").getValue(), fieldName.getText());
    					}
    				} else if ("FieldValue".equals(node.getName())) { // 属性值列表
    					for (Iterator<?> k = node.elementIterator(); k.hasNext();) {
    						obj = (BObject) clazz.newInstance();//创建对象
    						
    						Element fieldValue = (Element) k.next();
    						String rmUIDname = fieldValue.attribute(0).getName();//获取属性名
			                String rmUIDvalue = fieldValue.attribute(0).getValue();//获取属性值
    						setObjectValue(obj, rmUIDname, rmUIDvalue, fieldNames, fields);
    						for (Iterator<?> l = fieldValue.elementIterator(); l.hasNext();) {
    							Element object = (Element) l.next();
    							
    							String propertyname = fieldNames.get(object.attribute("i").getValue());//获取属性名
    			                String propertyvalue = object.getText();//获取属性值
    			                
    			                setObjectValue(obj, propertyname, propertyvalue, fieldNames, fields);
    						}
    						
    						objs.add(obj);
    					}
    				} else { // 其他xml头内容
    					System.out.println(node.getName() + ":" + node.getText());
    				}

    			}
    			System.out.println();
    		}
            
            
		} catch (Exception e) {
			nbilog.error(e, e);
            e.printStackTrace();
        }
		
		return objs;
	}
	
	/**
	 * 
	 * object对象设值
	 * 
	 * @param obj
	 * @param propertyname
	 * @param propertyvalue
	 * @param fieldNames
	 * @param fields
	 */
	public void setObjectValue(Object obj, String propertyname, String propertyvalue, HashMap<String,String> fieldNames, Field[] fields) {
		
//        System.out.println(propertyname + ":" + propertyvalue);

        for (Field field1 : fields) {
            if (field1.getName().equalsIgnoreCase(propertyname)) { // 判断实体类中是否有该属性
            	try {
            		//(首字母大写) 若首字母本来就大写，则不变
            		String upperPropertyname = field1.getName().substring(0, 1).toUpperCase() + field1.getName().substring(1);
            		char[] chars = field1.getName().toCharArray();
            		if (Character.isUpperCase(chars[1])) {
            			upperPropertyname = field1.getName();
            		}
            		
            		Method m = obj.getClass().getMethod("set" + upperPropertyname, String.class);
            		m.invoke(obj, propertyvalue);
            		
            		break;
            	} catch (Exception e) {
            		nbilog.error(e, e);
                    e.printStackTrace();
                }
            }
        }
		
	}
	
	public HashMap<String,Object> getObjectMap() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		OMC omc = new OMC();
		NEL nel = new NEL();
		EQH eqh = new EQH();
		CRD crd = new CRD();
		PRT prt = new PRT();
		PRB prb = new PRB();
		TNL tnl = new TNL();
		LBS lbs = new LBS();
		TPI tpi = new TPI();
		TPB tpb = new TPB();
		MPI mpi = new MPI();
		MTR mtr = new MTR();
		MTL mtl = new MTL();
		PSW psw = new PSW();
		PWP pwp = new PWP();
		PWT pwt = new PWT();
		ETH eth = new ETH();
		ESP esp = new ESP();
		ESI esi = new ESI();
		TDM tdm = new TDM();
		ETP etp = new ETP();
		BRD brd = new BRD();
		L3I l3i = new L3I();
		L3P l3p = new L3P();
		L3T l3t = new L3T();
		TPL tpl = new TPL();
		SBN sbn = new SBN();
		SNN snn = new SNN();
		EPG epg = new EPG();
		EPU epu = new EPU();
		PTG ptg = new PTG();
		PGU pgu = new PGU();
		STT stt = new STT();
		SRT srt = new SRT();
		SRR srr = new SRR();
		IGT igt = new IGT();
		IGL igl = new IGL();
		NWS nws = new NWS();
		MGP mgp = new MGP();
		MGB mgb = new MGB();
		MCL mcl = new MCL();
		MCS mcs = new MCS();
		MCP mcp = new MCP();
		MCB mcb = new MCB();

		
		map.put("OMC", omc);
		map.put("NEL", nel);
		map.put("EQH", eqh);
		map.put("CRD", crd);
		map.put("PRT", prt);
		map.put("PRB", prb);
		map.put("TNL", tnl);
		map.put("LBS", lbs);
		map.put("TPI", tpi);
		map.put("TPB", tpb);
		map.put("MPI", mpi);
		map.put("MTR", mtr);
		map.put("MTL", mtl);
		map.put("PSW", psw);
		map.put("PWP", pwp);
		map.put("PWT", pwt);
		map.put("ETH", eth);
		map.put("ESP", esp);
		map.put("ESI", esi);
		map.put("TDM", tdm);
		map.put("ETP", etp);
		map.put("BRD", brd);
		map.put("L3I", l3i);
		map.put("L3P", l3p);
		map.put("L3T", l3t);
		map.put("TPL", tpl);
		map.put("SBN", sbn);
		map.put("SNN", snn);
		map.put("EPG", epg);
		map.put("EPU", epu);
		map.put("PTG", ptg);
		map.put("PGU", pgu);
		map.put("STT", stt);
		map.put("SRT", srt);
		map.put("SRR", srr);
		map.put("IGT", igt);
		map.put("IGL", igl);
		map.put("NWS", nws);
		map.put("MGP", mgp);
		map.put("MGB", mgb);
		map.put("MCL", mcl);
		map.put("MCS", mcs);
		map.put("MCP", mcp);
		map.put("MCB", mcb);

		
		return map;
	}
	
	/**
	 * 根据实体模糊匹配得到xml文件名
	 * 
	 * @param key
	 * @param path
	 * @return
	 */
	public String getFileName(String key, String path) {
		String fileName = "";
		File file = new File(path);
		File[] tempFile = file.listFiles();
		for (int i = 0; i < tempFile.length; i++) {
			if (StringUtils.contains(tempFile[i].getName(), key)) {
				fileName = tempFile[i].getName();
				break;
			}
		}

		return fileName;
	}
	
	/**
	 * 解析s_ems.additionalinfo中的ftp服务器信息
	 */
	public static HashMap<String,String> transMapValue(String value) {
		HashMap<String,String> map = new HashMap<String,String>();
		if (value != null) {
			String[] pairs = value.split(";");
			for (String pair : pairs) {
				if (pair.contains("||")) {
					String[] split = pair.split(Constant.listSplitReg);
					if (split != null && split.length == 2) {
						map.put(split[0], split[1]);
					}
				}
			}
		}
		return map;
	}
	
	public static void main(String[] args) {

		String emsDn = "ZJ-UME-1-SPN";
		StringUtils.split(emsDn, "-");
		ArrayUtils.indexOf(StringUtils.split(emsDn, "-"), "OTN");
		String date = "20180726";
		long t1 = System.currentTimeMillis();
		
		String gzPath = "D:\\20200423\\zip\\"; // gz压缩包路径
    	String xmlPath = "D:\\20200423\\xml\\"; // xml文件路径
    	String timeStamp = "2020042315";
		
    	DayMigrationJob4SPN job = new DayMigrationJob4SPN();
		job.decomZip(gzPath, xmlPath, timeStamp);
		
		String dbName = "D:\\20180726.db";
		job.sqliteConn = new SqliteConn();
		job.sqliteConn.setDataPath(dbName);
		job.sqliteConn.init();
    	
    	SimpleDateFormat df = new SimpleDateFormat("HH");
		
		if (Integer.parseInt(df.format(System.currentTimeMillis())) > 12) {
			// 过了12点，属于下午的采集
			timeStamp = date + "1200";
			xmlPath = xmlPath + "-pm\\";
		} else {
			// 没过12点，属于上午的采集
			xmlPath = xmlPath + "-am\\";
		}
		
		File file = new File(dbName);
		file.delete();
		
		job.clearPath(xmlPath, timeStamp);
		job.decomZip(gzPath, xmlPath, timeStamp);
		job.transXml(xmlPath, dbName);
		
		job.sqliteConn.release();

		long t2 = System.currentTimeMillis();
		long t = (t2 - t1) / (3600000l);
		String unit = "Hours";
		if (t == 0) {
			t = (t2 - t1) / (60000l);
			unit = "Minutes";
			if (t == 0) {
				t = (t2 - t1) / (1000l);
				unit = "Seconds";
			}
		}
		System.out.println("================== "+t+unit+" ["+emsDn+"] =====================");
		
	}
	

}

