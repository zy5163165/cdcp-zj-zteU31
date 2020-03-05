package org.asb.mule.probe.ptn.zte.service.mapper;

import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.R_FTP_PTP;
import org.asb.mule.probe.framework.util.CodeTool;

import subnetworkConnection.TPData_T;
import terminationPoint.TPProtectionAssociation_T;
import terminationPoint.TerminationMode_T;
import terminationPoint.TerminationPoint_T;

public class PtpMapper extends CommonMapper

{
	private static PtpMapper instance;

	public static PtpMapper instance() {
		if (instance == null) {
			instance = new PtpMapper();
		}
		return instance;
	}

	public PTP convertPtp(TerminationPoint_T vendorEntity, String parentDn) {

		PTP ptp = new PTP();

		// ptp.setDn(mapperPtpDn(vendorEntity.name));
		ptp.setDn(nv2dn(vendorEntity.name));
		// ptp.setParentDn(mapperParentDnNameAndStringValue(vendorEntity.name));
		ptp.setParentDn(parentDn);
		ptp.setEmsName(vendorEntity.name[0].value);

		ptp.setUserLabel(CodeTool.isoToGbk(vendorEntity.userLabel));

		ptp.setConnectionState(mapperConnectionState(vendorEntity.connectionState));
		ptp.setDirection(mapperDirection(vendorEntity.direction));
		ptp.setEdgePoint(vendorEntity.edgePoint);
		ptp.setTpMappingMode(mapperTpMappingMode(vendorEntity.tpMappingMode));
		ptp.setRate(mapperRates(vendorEntity.transmissionParams));
		ptp.setTpProtectionAssociation(mapperProtectionAssiciation(vendorEntity.tpProtectionAssociation));
		ptp.setTransmissionParams(mapperTransmissionParas(vendorEntity.transmissionParams));

		ptp.setAdditionalInfo(CodeTool.isoToGbk(mapperAnyAdditionalInfo(vendorEntity.additionalInfo)));

		return ptp;
	}

	public R_FTP_PTP convertPtpFtpRelation(TPData_T vptp, String ftpName) {

		R_FTP_PTP ptp = new R_FTP_PTP();

		ptp.setFtpDn(ftpName);
		ptp.setPtpDn(nv2dn(vptp.tpName));
		ptp.setTransmissionParams(mapperTransmissionParas(vptp.transmissionParams));
		ptp.setRate(mapperRates(vptp.transmissionParams));
		ptp.setTpMappingMode(mapperTpMappingMode(vptp.tpMappingMode));

		return ptp;
	}

	private String mapperProtectionAssiciation(TPProtectionAssociation_T tpProtectionAssociation) {
		String pmode = "";
		switch (tpProtectionAssociation.value()) {
		case terminationPoint.TPProtectionAssociation_T._TPPA_NA:
			pmode = "TPPA_NA";
			break;
		case terminationPoint.TPProtectionAssociation_T._TPPA_PSR_RELATED:
			pmode = "TPPA_PSR_RELATED";
			break;

		}
		return pmode;
	}

	private String mapperTpMappingMode(TerminationMode_T tpMappingMode) {
		String mode = "";
		switch (tpMappingMode.value()) {
		case terminationPoint.TerminationMode_T._TM_NA:
			mode = "D_NA";
			break;
		case terminationPoint.TerminationMode_T._TM_NEITHER_TERMINATED_NOR_AVAILABLE_FOR_MAPPING:
			mode = "TM_NEITHER_TERMINATED_NOR_AVAILABLE_FOR_MAPPING";
			break;
		case terminationPoint.TerminationMode_T._TM_TERMINATED_AND_AVAILABLE_FOR_MAPPING:
			mode = "TM_TERMINATED_AND_AVAILABLE_FOR_MAPPING";
			break;

		}
		return mode;
	}

}
