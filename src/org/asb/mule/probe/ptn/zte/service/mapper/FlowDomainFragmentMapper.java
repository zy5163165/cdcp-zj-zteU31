package org.asb.mule.probe.ptn.zte.service.mapper;

import java.util.HashSet;
import java.util.Set;

import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.CodeTool;

//import org.asb.mule.probe.ptn.fenghuo.service.mapper.*;
import performance.AdministrativeState_T;
import subnetworkConnection.TPData_T;

import com.alcatelsbell.nms.common.SysUtil;

import flowDomainFragment.FlowDomainFragment_T;
import globaldefs.NameAndStringValue_T;

public class FlowDomainFragmentMapper extends  CommonMapper

{
	private static String CHANNELIDLIST = "ChannelIdList";

	private static FlowDomainFragmentMapper instance;

	public static FlowDomainFragmentMapper instance() {
		if (instance == null) {
			instance = new FlowDomainFragmentMapper();
		}
		return instance;
	}
	protected String getChannelIdList(NameAndStringValue_T[] info) {
		for (NameAndStringValue_T nv : info) {
			if (nv.name.trim().equals(CHANNELIDLIST)) {
				return nv.value;
			}
		}
		return "";
	}

	public FlowDomainFragment convertFlowDomainFragment(FlowDomainFragment_T vendorEntity) {
		FlowDomainFragment tt = new FlowDomainFragment();

		tt.setDn(nv2dn(vendorEntity.name));
		// tt.setTag1(nv2dn(vendorEntity.name));
		tt.setParentDn(getChannelIdList(vendorEntity.additionalInfo));
		tt.setTag2(vendorEntity.name[2].value);
		tt.setEmsName(vendorEntity.name[0].value);
		tt.setNativeEMSName(CodeTool.isoToGbk(vendorEntity.nativeEMSName));
		tt.setUserLabel(CodeTool.isoToGbk(vendorEntity.userLabel));
		/**
		 * 
		 private String multipointServiceAttr;
		 */
		tt.setNetworkAccessDomain(vendorEntity.networkAccessDomain);
		tt.setFdfrType(vendorEntity.fdfrType);
		tt.setDirection(mapperConnectionDirection(vendorEntity.direction));
		tt.setTransmissionParams(mapperTransmissionPara(vendorEntity.transmissionParams));
		tt.setFlexible(vendorEntity.flexible);
		tt.setAdministrativeState(mapperAdministrativeState(vendorEntity.administrativeState));
		tt.setFdfrState(mapperActiveState(vendorEntity.fdfrState));
		if (vendorEntity.aEnd.length > 0 && vendorEntity.aEnd[0] != null) {
			tt.setaEnd(nvs2dn(vendorEntity.aEnd));
			tt.setaPtp(getPtpdn(nvs2dn(vendorEntity.aEnd)));
		//	tt.setaEndTrans(mapperTransmissionParas(vendorEntity.aEnd[0].transmissionParams));
			tt.setaNE(getPtpdn(nvs2dn(vendorEntity.aEnd)));
		}

		if (vendorEntity.zEnd.length > 0 && vendorEntity.zEnd[0] != null) {
			tt.setzEnd(nvs2dn(vendorEntity.zEnd));
			tt.setzPtp(getPtpdn(nvs2dn(vendorEntity.zEnd)));
		//	tt.setzEndtrans(mapperTransmissionParas(vendorEntity.zEnd[0].transmissionParams));
			tt.setzNE(getPtpdn(nvs2dn(vendorEntity.zEnd)));
		}
		tt.setAdditionalInfo(mapperAdditionalInfo(vendorEntity.additionalInfo));
		for (NameAndStringValue_T nv : vendorEntity.additionalInfo) {
			if (nv.name.trim().equals("ProtectionGroupType")) {
				tt.setTag1(nv.value);
				break;
			}
		}
//		if (vendorEntity.fdfrType.equals("FDFRT_MULTIPOINT")) {
//			StringBuffer sb = new StringBuffer();
//			for (TPData_T tp : vendorEntity.aEnd) {
//				String vlanid = "";
//				for (NameAndStringValue_T trans : tp.transmissionParams[0].transmissionParams) {
//					if (trans.name.trim().equals("VLANID")) {
//						vlanid = trans.value;
//						break;
//					}
//				}
//				sb.append(Constant.listSplit);
//				sb.append(vlanid);
//			}
//			tt.setTag3(sb.substring(2));
//		}

		return tt;
	}

	protected String mapperAdministrativeState(AdministrativeState_T administrativeState) {
		String value = "";
		switch (administrativeState.value()) {
			case AdministrativeState_T._AS_Locked:
				value = "AS_Locked";
				break;
			case AdministrativeState_T._AS_Unlocked:
				value = "AS_Unlocked";
				break;

		}
		return value;
	}

	private String getPtpdn(String ptpdn) {
		if (ptpdn != null && ptpdn.contains("||")) {
			String[] ptps = ptpdn.split("\\|\\|");
			Set<String> set = new HashSet<String>();
			for (String ptp : ptps) {
				set.add(ptp);
			}
			StringBuilder buff = new StringBuilder();
			for (String ptp : set) {
				buff.append("||");
				buff.append(ptp);
			}
			return buff.substring(2);
		}
		return ptpdn;
	}
}
