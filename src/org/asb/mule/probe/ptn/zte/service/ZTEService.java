package org.asb.mule.probe.ptn.zte.service;

import com.alcatelsbell.cdcp.nodefx.exception.EmsDataIllegalException;
import com.alcatelsbell.cdcp.nodefx.exception.EmsFunctionInvokeException;
import equipment.EquipmentHolder_T;
import equipment.EquipmentOrHolder_T;
import equipment.EquipmentTypeQualifier_T;
import equipment.Equipment_T;
import flowDomain.FlowDomain_T;
import flowDomainFragment.FDFrRoute_THolder;
import flowDomainFragment.FlowDomainFragment_T;
import flowDomainFragment.MatrixFlowDomainFragment_T;
import globaldefs.NameAndStringValue_T;
import globaldefs.NamingAttributesList_THolder;
import globaldefs.ProcessingFailureException;

import java.util.ArrayList;
import java.util.List;

import managedElement.ManagedElement_T;

import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.EquipmentHolder;
import org.asb.mule.probe.framework.entity.FlowDomain;
import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.entity.IPCrossconnection;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.ProtectionGroup;
import org.asb.mule.probe.framework.entity.R_FTP_PTP;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.entity.TrafficTrunk;
import org.asb.mule.probe.framework.entity.TrailNtwProtection;
import org.asb.mule.probe.framework.service.NbiService;
import org.asb.mule.probe.framework.util.CodeTool;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.zte.sbi.mgrhandler.*;
import org.asb.mule.probe.ptn.zte.sbi.service.CorbaService;
import org.asb.mule.probe.ptn.zte.service.mapper.*;

import subnetworkConnection.CrossConnect_T;
import subnetworkConnection.SubnetworkConnection_T;
import subnetworkConnection.TPData_T;
import terminationPoint.TerminationPoint_T;
import topologicalLink.TopologicalLink_T;

public class ZTEService implements NbiService {

	// protected Logger errorlog = ProbeLog.getInstance().getErrorLog();
	// protected Logger sbilog = ProbeLog.getInstance().getSbiLog();

	private CorbaService corbaService;

	private FileLogger sbilog = null;
	private FileLogger errorlog = null;

	private String key;

	public String getServiceName() {
		return corbaService.getEmsName();
	}

	@Override
	public String getEmsName() {
		// TODO Auto-generated method stub
		sbilog = corbaService.getSbilog();
		errorlog = corbaService.getErrorlog();

		return corbaService.getEmsName();
	}

	public void setCorbaService(CorbaService corbaService) {

		this.corbaService = corbaService;
	}

	public CorbaService getCorbaService() {
		return corbaService;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	// 1.
	public List<ManagedElement> retrieveAllManagedElements() {
		ManagedElement_T[] vendorNeList = null;
		List<ManagedElement> neList = new ArrayList();
		try {
			vendorNeList = ManagedElementMgrHandler.instance().retrieveAllManagedElements(corbaService.getNmsSession().getManagedElementMgr());
		} catch (ProcessingFailureException e) {
			corbaService.handleException(new EmsFunctionInvokeException("retrieveAllManagedElements",e));
			errorlog.error("retrieveAllManagedElements ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			corbaService.handleException(new EmsFunctionInvokeException("retrieveAllManagedElements",e));
			errorlog.error("retrieveAllManagedElements CORBA.SystemException: " + e.getMessage(), e);
		}
		if (vendorNeList == null || vendorNeList.length == 0)
			corbaService.handleException(new EmsDataIllegalException("Managedelement",null," size = 0 "));

		if (vendorNeList != null && vendorNeList.length > 0) {
			corbaService.handleExceptionRecover(EmsDataIllegalException.EXCEPTION_CODE+"Managedelement");
			corbaService.handleExceptionRecover(EmsFunctionInvokeException.EXCEPTION_CODE+"retrieveAllManagedElements");

			for (ManagedElement_T vendorNe : vendorNeList) {
				try {
					ManagedElement ne = ManagedElementMapper.instance().convertManagedElement(vendorNe);
					neList.add(ne);
				} catch (Exception e) {
					errorlog.error("retrieveAllManagedElements convertException: ", e);
				}
			}
		}
		sbilog.info("retrieveAllManagedElements : " + neList.size());
		return neList;
	}

	/**
	 * 2.3.����Ҫ�ֿ�ȡ��
	 * 
	 * @return
	 */
	public void retrieveAllEquipmentAndHolders(String neName, List<EquipmentHolder> equipmentHolderList, List<Equipment> equipmentList) {
		EquipmentOrHolder_T[] equipmentOrHolderList = null;
		NameAndStringValue_T[] neDn = VendorDNFactory.createCommonDN(neName);
		try {
			equipmentOrHolderList = EquipmentInventoryMgrHandler.instance().retrieveAllEquipmentAndHolders(
					corbaService.getNmsSession().getEquipmentInventoryMgr(), neDn);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllEquipmentAndHolders ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
			// once again
			try {
				Thread.sleep(60000L);
			} catch (InterruptedException e2) {
				errorlog.error("retrieveAllEquipmentAndHolders1 InterruptedException: ", e2);
			}
			try {
				equipmentOrHolderList = EquipmentInventoryMgrHandler.instance().retrieveAllEquipmentAndHolders(
						corbaService.getNmsSession().getEquipmentInventoryMgr(), neDn);
			} catch (ProcessingFailureException e1) {
				errorlog.error("retrieveAllEquipmentAndHolders1 ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
			} catch (org.omg.CORBA.SystemException e1) {
				errorlog.error("retrieveAllEquipmentAndHolders1 CORBA.SystemException: " + e.getMessage(), e1);
			}
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllEquipmentAndHolders CORBA.SystemException: " + e.getMessage(), e);
		}
		if (equipmentOrHolderList != null) {
			for (EquipmentOrHolder_T equipmentOrHolder : equipmentOrHolderList) {
				try {
					if (equipmentOrHolder.discriminator().equals(EquipmentTypeQualifier_T.EQT_HOLDER)) {
						EquipmentHolder holder = EquipmentHolderMapper.instance().convertEquipmentHolder(equipmentOrHolder.holder(), neName);
						equipmentHolderList.add(holder);
					} else if (equipmentOrHolder.discriminator().equals(EquipmentTypeQualifier_T.EQT)) {
						Equipment card = EquipmentMapper.instance().convertEquipment(equipmentOrHolder.equip(), neName);
						equipmentList.add(card);
					}
				} catch (Exception e) {
					errorlog.error("retrieveAllEquipmentAndHolders convertException: ", e);
				}
			}
		}
		sbilog.info("retrieveAllEquipmentAndHolders EquipmentHolders: " + equipmentHolderList.size());
		sbilog.info("retrieveAllEquipmentAndHolders Equipments: " + equipmentList.size());

	}

	/**
	 * 4.
	 * 
	 * @return
	 */
	public List<PTP> retrieveAllPtps(String neName) {
		TerminationPoint_T[] vendorPtpList = null;
		List<PTP> ptpList = new ArrayList<PTP>();
		try {
			NameAndStringValue_T[] neDn = VendorDNFactory.createCommonDN(neName);
			short[] tpLayerRateList = new short[0];
			short[] connectionLayerRateList = new short[0];
			vendorPtpList = ManagedElementMgrHandler.instance().retrieveAllPTPs(corbaService.getNmsSession().getManagedElementMgr(), neDn, tpLayerRateList,
					connectionLayerRateList);
		} catch (ProcessingFailureException e)  {
			errorlog.error("retrieveAllPtps ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllPtps CORBA.SystemException: " + e.getMessage(), e);
		}
		if (vendorPtpList != null) {
			for (TerminationPoint_T vendorPtp : vendorPtpList) {
				try {
					PTP ptp = PtpMapper.instance().convertPtp(vendorPtp, neName);
					ptpList.add(ptp);
				} catch (Exception e) {
					errorlog.error("retrieveAllPtps convertException: ", e);
				}
			}

		}
		sbilog.info("retrieveAllPtps : " + ptpList.size());
		return ptpList;
	}

	/**
	 * 5.
	 * 
	 * @return
	 */

	public List<CTP> retrieveAllCtps(String ptpName) {
		TerminationPoint_T[] vendorCtpList = null;
		List<CTP> ctpList = new ArrayList();
		try {
			NameAndStringValue_T[] ptpDn = VendorDNFactory.createCommonDN(ptpName);
//			vendorCtpList = ManagedElementMgrHandler.instance().retrieveContainedPotentialTPs(corbaService.getNmsSession().getManagedElementMgr(), ptpDn,
//					new short[0]);
            vendorCtpList = ManagedElementMgrHandler.instance().retrieveContainedCurrentTPs(corbaService.getNmsSession().getManagedElementMgr(), ptpDn,
                    new short[0]);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllCtps ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllCtps CORBA.SystemException: " + e.getMessage(), e);
			sbilog.error("retrieveAllCrossConnects CORBA.SystemException: " + e.getMessage(), e);
		}
		if (vendorCtpList != null) {
			for (TerminationPoint_T vendorctp : vendorCtpList) {
				try {
					CTP ctp = CtpMapper.instance().convertCtp(vendorctp, ptpName);
					ctpList.add(ctp);
				} catch (Exception e) {
					errorlog.error("retrieveAllCtps convertException: ", e);
				}
			}
		}

	//	sbilog.info("retrieveAllCtps : " + (ctpList == null ? "null":ctpList.size()));
		return ctpList;
	}

	/**
	 * 6.
	 * 
	 * @return
	 */

	public List<CrossConnect> retrieveAllCrossConnects(String neName) {
		CrossConnect_T[] vendorIPCrossconnectionList = null;
		List<CrossConnect> ccList = new ArrayList<CrossConnect>();
		try {
			NameAndStringValue_T[] neDn = VendorDNFactory.createCommonDN(neName);
			short[] layer = new short[0];
			vendorIPCrossconnectionList = ManagedElementMgrHandler.instance().retrieveAllCrossConnections(corbaService.getNmsSession().getManagedElementMgr(),
					neDn, layer);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllCrossConnects ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllCrossConnects CORBA.SystemException: " + e.getMessage(), e);
			sbilog.error("retrieveAllCrossConnects CORBA.SystemException: " + e.getMessage(), e);
		}
		if (vendorIPCrossconnectionList != null) {
			for (CrossConnect_T vendorIPCc : vendorIPCrossconnectionList) {
				try {
					CrossConnect ipCC = IPCrossconnectionMapper.instance().convertCrossConnection(vendorIPCc, neName);
					ccList.add(ipCC);
				} catch (Exception e) {
					errorlog.error("retrieveAllCrossConnects convertException: ", e);
				}
			}

		}
		sbilog.info("retrieveAllCrossConnects : " + ccList.size());
		return ccList;
	}
    private String toStr(NameAndStringValue_T[] nvs) {
        StringBuffer sb = new StringBuffer();
        if (nvs != null) {
            for (NameAndStringValue_T nv : nvs) {
                sb.append(nv.name).append(":").append(nv.value).append("@");
            }
        }
        return sb.toString();
    }
    public List<Section> retrieveAllSections() {
        List<Section> sectionList = new ArrayList<Section>();
        NamingAttributesList_THolder namingAttributesList_tHolder = new NamingAttributesList_THolder();
        try {
            corbaService.getNmsSession().getEmsMgr().getAllTopLevelSubnetworkNames(namingAttributesList_tHolder);
        } catch (ProcessingFailureException e) {
            errorlog.error(e, e);
        }
        NameAndStringValue_T[][] subnetDns = namingAttributesList_tHolder.value;
        if (subnetDns != null) {
            for (NameAndStringValue_T[] subnetDn : subnetDns) {
                TopologicalLink_T[] vendorSectionList = null;

                 try {
                    vendorSectionList = SubnetworkMgrHandler.instance()
                            .retrieveAllTopologicalLinks(corbaService.getNmsSession().getMultiLayerSubnetworkMgr(), subnetDn);

                } catch (ProcessingFailureException e) {
                    errorlog.error("retrieveAllSections ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
                } catch (org.omg.CORBA.SystemException e) {
                    errorlog.error("retrieveAllSections CORBA.SystemException: " + e.getMessage(), e);
                }
                sbilog.info("vendorSectionList="+(vendorSectionList == null ? null:vendorSectionList.length)+" subnet="+toStr(subnetDn));

                if (vendorSectionList != null) {

                    for (TopologicalLink_T vendorSection : vendorSectionList) {
                        try {
                            Section section = SectionMapper.instance().convertSection(vendorSection, subnetDn);
                            sectionList.add(section);
                        } catch (Exception e) {
                            errorlog.error("retrieveAllSections convertException: ", e);
                        }
                    }
                }

            }
        }

        TopologicalLink_T[] topLevelTopological = null;
        try {
            topLevelTopological = EMSMgrHandler.instance().retrieveAllTopLevelTopologicalLinks(corbaService.getNmsSession().getEmsMgr());
        } catch (ProcessingFailureException e) {
            errorlog.error("retrieveAllSections ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
        } catch (org.omg.CORBA.SystemException e) {
            errorlog.error("retrieveAllSections CORBA.SystemException: " + e.getMessage(), e);
        }
        sbilog.info("topLevelTopological : " + topLevelTopological.length);
        if (topLevelTopological != null) {
            for (TopologicalLink_T vendorSection : topLevelTopological) {
                try {
                    Section section = SectionMapper.instance().convertSection(vendorSection, null);
                    sectionList.add(section);
                } catch (Exception e) {
                    errorlog.error("retrieveAllSections convertException: ", e);
                }
            }
        }
        sbilog.info("retrieveAllSections : " + sectionList.size());
        return sectionList;

    }
	/**
	 * 7.
	 * 
	 * @return
	 */
	public List<Section> retrieveAllSectionsOld() {




        TopologicalLink_T[] vendorSectionList = null;
		List<Section> sectionList = new ArrayList<Section>();
		NameAndStringValue_T[] subnetDn = VendorDNFactory.createSubnetworkDN(corbaService.getEmsDn(), "1");
		try {
			vendorSectionList = SubnetworkMgrHandler.instance()
					.retrieveAllTopologicalLinks(corbaService.getNmsSession().getMultiLayerSubnetworkMgr(), subnetDn);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllSections ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllSections CORBA.SystemException: " + e.getMessage(), e);
		}
		TopologicalLink_T[] topLevelTopological = null;
		try {
			topLevelTopological = EMSMgrHandler.instance().retrieveAllTopLevelTopologicalLinks(corbaService.getNmsSession().getEmsMgr());
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllSections ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllSections CORBA.SystemException: " + e.getMessage(), e);
		}
		sbilog.info("topLevelTopological : " + topLevelTopological.length);
		if (vendorSectionList != null) {
			for (TopologicalLink_T vendorSection : vendorSectionList) {
				try {
					Section section = SectionMapper.instance().convertSection(vendorSection, subnetDn);
					sectionList.add(section);
				} catch (Exception e) {
					errorlog.error("retrieveAllSections convertException: ", e);
				}
			}
		}

        if (topLevelTopological != null) {
            for (TopologicalLink_T vendorSection : topLevelTopological) {
                try {
                    Section section = SectionMapper.instance().convertSection(vendorSection, null);
                    sectionList.add(section);
                } catch (Exception e) {
                    errorlog.error("retrieveAllSections convertException: ", e);
                }
            }
        }
		sbilog.info("retrieveAllSections : " + sectionList.size());
		return sectionList;
	}

	@Override
	public boolean connect() {
		return corbaService.connect();

	}

	@Override
	public boolean disconnect() {
		return corbaService.disconnect();
	}

	@Override
	public boolean getConnectState() {
		// TODO Auto-generated method stub
		return corbaService.isConnectState();
	}

	@Override
	public String getLastestDayMigrationJobName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean ping() {

		return corbaService.getNmsSession().isEmsSessionOK();
	}

	@Override
	public List<FlowDomain> retrieveAllFlowDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SubnetworkConnection> retrieveAllSNCs() {
		List<SubnetworkConnection> sncList = new ArrayList<SubnetworkConnection>();
		SubnetworkConnection_T[] sncs = null;
		NameAndStringValue_T[] subnetworkName = VendorDNFactory.createSubnetworkDN(corbaService.getEmsDn(), "1");
		try {
			sncs = SubnetworkMgrHandler.instance().retrieveAllSNCs(corbaService.getNmsSession().getMultiLayerSubnetworkMgr(), subnetworkName, new short[0]);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllSNCs ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllSNCs CORBA.SystemException: " + e.getMessage(), e);
		}
		if (sncs != null) {
			for (SubnetworkConnection_T snc : sncs) {
				try {
					sncList.add(SubnetworkConnectionMapper.instance().convertSNC(snc));
				} catch (Exception e) {
					errorlog.error("retrieveAllSNCs convertException: ", e);
				}
			}
		}
		sbilog.info("retrieveAllSNCs : " + sncList.size());
		return sncList;
	}

	@Override
	public List<IPCrossconnection> retrieveAllCrossconnections(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EquipmentHolder> retrieveAllEquipmentHolders(String neName) {
		EquipmentHolder_T[] vendorHolderList = null;
		List<EquipmentHolder> holderList = new ArrayList();
		try {

			// String[] neNameList = neName.split(Constant.dnSplit);
			NameAndStringValue_T[] neDn = VendorDNFactory.createCommonDN(neName);

			vendorHolderList = EquipmentInventoryMgrHandler.instance().retrieveAllEquipmentHolders(corbaService.getNmsSession().getEquipmentInventoryMgr(),
					neDn);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllEquipmentHolders ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		}
		if (vendorHolderList != null) {
			for (EquipmentHolder_T vendorHolder : vendorHolderList) {
				try {
					EquipmentHolder card = EquipmentHolderMapper.instance().convertEquipmentHolder(vendorHolder, neName);
					holderList.add(card);
				} catch (Exception e) {
					errorlog.error("retrieveAllEquipmentHolders convertException: ", e);
				}

			}
		}
		sbilog.info("retrieveAllEquipmentHolders : " + holderList.size());
		return holderList;
	}

	@Override
	public List<Equipment> retrieveAllEquipments(String neName) {
		Equipment_T[] vendorCardList = null;
		List<Equipment> cardList = new ArrayList<Equipment>();
		try {
			// String[] neNameList = neName.split(Constant.dnSplit);
			NameAndStringValue_T[] neDn = VendorDNFactory.createCommonDN(neName);

			vendorCardList = EquipmentInventoryMgrHandler.instance().retrieveAllEquipments(corbaService.getNmsSession().getEquipmentInventoryMgr(), neDn);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllEquipments ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		}
		if (vendorCardList != null) {
			for (Equipment_T vendorCard : vendorCardList) {
				try {
					Equipment card = EquipmentMapper.instance().convertEquipment(vendorCard, neName);
					cardList.add(card);
				} catch (Exception e) {
					errorlog.error("retrieveAllEquipments convertException: ", e);
				}

			}
		}
		sbilog.info("retrieveAllEquipments : " + cardList.size());
		return cardList;
	}

	@Override
	public List<FlowDomainFragment> retrieveAllFdrs() {
		List<FlowDomainFragment> fdrList = new ArrayList<FlowDomainFragment>();
		FlowDomain_T[] fds = null;
		try {
			fds = FlowDomainMgrHandler.instance().retrieveAllFlowDomains(corbaService.getNmsSession().getFlowDomainMgr());
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllFdrs.retrieveAllFlowDomains ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllFdrs.retrieveAllFlowDomains CORBA.SystemException: " + e.getMessage(), e);
		}
		if (fds != null) {
			for (FlowDomain_T fd : fds) {
				short[] rates = new short[] { 309, 1500, 96, 5 };
				for (short rate : rates) {
					FlowDomainFragment_T[] fdfrs = null;
					try {
						short[] layer = new short[] { rate };
						fdfrs = FlowDomainMgrHandler.instance().retrieveAllFDFrs(corbaService.getNmsSession().getFlowDomainMgr(), layer, fd.name);
					} catch (ProcessingFailureException e) {
						errorlog.error("retrieveAllFdrs ProcessingFailureException: rate=[ " + rate + " ] : " + CodeTool.isoToGbk(e.errorReason), e);
					} catch (org.omg.CORBA.SystemException e) {
						errorlog.error("retrieveAllFdrs CORBA.SystemException: " + e.getMessage(), e);
					}
					if (fdfrs != null) {
						sbilog.info("AllFdfrs " + rate + " COUNTS: " + fdfrs.length);
						for (FlowDomainFragment_T vendorFdr : fdfrs) {
							try {
								FlowDomainFragment fdr = FlowDomainFragmentMapper.instance().convertFlowDomainFragment(vendorFdr);
								fdr.setRate(String.valueOf(rate));
								fdrList.add(fdr);
							} catch (Exception e) {
								errorlog.error("retrieveAllFdrs convertException: " + vendorFdr, e);
							}
						}
					}
				}
			}
		}
		sbilog.info("AllTrafficTrunk COUNTS: " + fdrList.size());
		return fdrList;
	}

	@Override
	public List<R_FTP_PTP> retrieveAllPTPsByFtp(String ftpName) {
		// TODO Auto-generated method stub
		sbilog.info("retrieveAllPTPsByFtp : " + ftpName);
		
		TPData_T[] vendorTpList = null;
		List<R_FTP_PTP> ptpList = new ArrayList();
		try {

			// String[] ftpDn = ftpName.split(Constant.dnSplit);
			NameAndStringValue_T[] dn = VendorDNFactory.createFtpDN(ftpName);
			vendorTpList = ManagedElementMgrHandler.instance().retrieveAllPtpsByFtp(corbaService.getNmsSession().getMSTPCommonMgr(), dn);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveAllPTPsByFtp ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveAllPTPsByFtp CORBA.SystemException: " + e.getMessage(), e);
		}
		if (vendorTpList != null) {
			
			R_FTP_PTP ptp = PtpMapper.instance().convertPtpFtpRelation(vendorTpList[0], ftpName);
			ptpList.add(ptp);
			
//			for (TPData_T vptp : vendorTpList) {
//				try {
//					R_FTP_PTP ptp = PtpMapper.instance().convertPtpFtpRelation(vptp, ftpName);
//					ptpList.add(ptp);
//				} catch (Exception e) {
//					errorlog.error("retrieveAllPTPsByFtp convertException: ", e);
//				}
//			}
			
		}
		sbilog.info("retrieveAllPTPsByFtp.size : " + ptpList.size());
		
		return ptpList;
	}

	@Override
	public List<ProtectionGroup> retrieveAllProtectionGroupByMe(String meName) {
		return null;
	}

	@Override
	public List<TrafficTrunk> retrieveAllTrafficTrunk() {
		// TODO Auto-generated method stub
		return null;
	}
	public List retrieveRouteAndTopologicalLinks1(String sncName) {

		List ccList = new ArrayList();
		List sectionList = new ArrayList();

		this.retrieveRouteAndTopologicalLinks(sncName,ccList,sectionList);
		List result = new ArrayList();
		result.addAll(ccList);
		result.addAll(sectionList);
		return result;
	}

	public void retrieveRouteAndTopologicalLinks(String sncName, List<CrossConnect> ccList, List<Section> sectionList) {
		System.out.println("sbilog = " + sbilog);
		sbilog.info("retrieveRouteAndTopologicalLinks:"+sncName);
		subnetworkConnection.Route_THolder normalRoute = new subnetworkConnection.Route_THolder();
		topologicalLink.TopologicalLinkList_THolder topologicalLinkList = new topologicalLink.TopologicalLinkList_THolder();
		String[] sncdns = sncName.split("@");
		NameAndStringValue_T[] vendorSncName = VendorDNFactory.createSNCDN(sncdns[0].substring(4), sncdns[1].substring(21), sncdns[2].substring(21));
		try {
			corbaService.getNmsSession().getMultiLayerSubnetworkMgr().getRouteAndTopologicalLinks(vendorSncName, normalRoute, topologicalLinkList);
		} catch (ProcessingFailureException e) {
			errorlog.error("retrieveRouteAndTopologicalLinks ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error("retrieveRouteAndTopologicalLinks CORBA.SystemException: " + e.getMessage(), e);
		} catch (Throwable e) {
			errorlog.error("retrieveRouteAndTopologicalLinks ERROR: " + e.getMessage(), e);
		}
		if (normalRoute.value != null) {
			sbilog.info("normalroute="+normalRoute.value.length);
			for (subnetworkConnection.CrossConnect_T cc : normalRoute.value) {
				try {
					ccList.add(IPCrossconnectionMapper.instance().convertCrossConnection(cc, sncName));
				} catch (Exception e) {
					errorlog.error("retrieveRouteAndTopologicalLinks convertException: ", e);
				}
			}
		}
		if (topologicalLinkList.value != null) {
			sbilog.info("topologicalLinkList="+topologicalLinkList.value.length);
			for (topologicalLink.TopologicalLink_T section : topologicalLinkList.value) {
				try {
					sectionList.add(SectionMapper.instance().convertSection(section, vendorSncName));
				} catch (Exception e) {
					errorlog.error("retrieveRouteAndTopologicalLinks convertException: ", e);
				}
			}
		}
		sbilog.info("retrieveRouteAndTopologicalLinks ccList: " + ccList.size());
		sbilog.info("retrieveRouteAndTopologicalLinks sectionList: " + sectionList.size());

	}

	@Override
	public List<IPCrossconnection> retrieveRoute(String trafficTrunkName) {
		List<IPCrossconnection> IPCrossconnectionList = new ArrayList<IPCrossconnection>();
		MatrixFlowDomainFragment_T[] routes = null;
		NameAndStringValue_T[] ttname = VendorDNFactory.createCommonDN(trafficTrunkName);
		FDFrRoute_THolder routeHolder = new FDFrRoute_THolder();
		long start = System.currentTimeMillis();
		sbilog.debug("retrieveRoute :start " + trafficTrunkName);
		try {
			corbaService.getNmsSession().getFlowDomainMgr().getFDFrRoute(ttname, routeHolder);
			routes = routeHolder.value;
		} catch (ProcessingFailureException e) {
			errorlog.error(trafficTrunkName + " retrieveRoute ProcessingFailureException: " + CodeTool.isoToGbk(e.errorReason), e);
		} catch (org.omg.CORBA.SystemException e) {
			errorlog.error(trafficTrunkName + " retrieveRoute CORBA.SystemException: " + e.getMessage(), e);
		}
		sbilog.debug("retrieveRoute :end " + trafficTrunkName);
		long end = System.currentTimeMillis();
		long sub = end - start;
		sbilog.info("retrieveRoute : " + sub + "ms Tunnel: " + trafficTrunkName);
		if (sub > 60000) {
			sbilog.info("retrieveRoute1 : " + sub + "ms Tunnel: " + trafficTrunkName);
		}
		// if (routes == null || routes.length == 0) {
		// sbilog.info("retrieveRoute1 :0 Tunnel: " + trafficTrunkName);
		// try {
		// Thread.sleep(600000L);
		// } catch (InterruptedException e2) {
		// errorlog.error("retrieveRoute2 InterruptedException: ", e2);
		// }
		// try {
		// corbaService.getNmsSession().getExtendFlowDomainMgr().getExFDFrRoute(ttname, routeHolder);
		// routes = routeHolder.value;
		// } catch (ProcessingFailureException e1) {
		// errorlog.error(trafficTrunkName + " retrieveRoute2 ProcessingFailureException: " + CodeTool.isoToGbk(e1.errorReason), e1);
		// } catch (org.omg.CORBA.SystemException e1) {
		// errorlog.error(trafficTrunkName + " retrieveRoute2 CORBA.SystemException: " + e1.getMessage(), e1);
		// }
		// }
		if (routes != null) {
			for (MatrixFlowDomainFragment_T route : routes) {
				try {
					IPCrossconnection ipCC = IPCrossconnectionMapper.instance().convertIPCrossConnection(route, trafficTrunkName);
					IPCrossconnectionList.add(ipCC);
				} catch (Exception e) {
					errorlog.error("retrieveRoute convertException: \nTunnel=" + trafficTrunkName + " \nroute=" + route, e);
				}
			}
		}
		sbilog.info("retrieveRoute : " + IPCrossconnectionList.size() + " Tunnel: " + trafficTrunkName);
		return IPCrossconnectionList;
	}
	@Override
	public List<TrailNtwProtection> retrieveAllTrailNtwProtections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ManagedElement retrieveManagedElement(String neName) {
		// TODO Auto-generated method stub
		return null;
	}

}
