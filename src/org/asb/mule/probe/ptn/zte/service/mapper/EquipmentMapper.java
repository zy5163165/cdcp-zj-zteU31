package org.asb.mule.probe.ptn.zte.service.mapper;

import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.util.CodeTool;

import equipment.Equipment_T;
import equipment.ServiceState_T;

public class EquipmentMapper extends CommonMapper

{
	private static EquipmentMapper instance;

	public static EquipmentMapper instance() {
		if (instance == null) {
			instance = new EquipmentMapper();
		}
		return instance;
	}

	public Equipment convertEquipment(Equipment_T vendorEntity, String parentDn)

	{
		Equipment ne = new Equipment();

		// ne.setDn(vendorEntity.name[0].value + Constant.dnSplit + vendorEntity.name[1].value + Constant.dnSplit + vendorEntity.name[2].value +
		// Constant.dnSplit
		// + vendorEntity.name[3].value);
		ne.setDn(nv2dn(vendorEntity.name));
		// ne.setParentDn(mapperParentDnNameAndStringValue(vendorEntity.name));
		ne.setParentDn(parentDn);
		ne.setEmsName(vendorEntity.name[0].value);
		ne.setUserLabel(CodeTool.isoToGbk(vendorEntity.userLabel));
		ne.setExpectedEquipmentObjectType(vendorEntity.expectedBoardType);
		ne.setInstalledEquipmentObjectType(vendorEntity.installedBoardType);
		ne.setInstalledPartNumber(vendorEntity.installedBoardType);
		ne.setServiceState(mapperServiceState(vendorEntity.serviceState));
		ne.setAdditionalInfo(mapperAdditionalInfo(vendorEntity.additionalInfo));

		return ne;
	}

	public String mapperServiceState(ServiceState_T state) {
		switch (state.value()) {
		case 0:
			return "IN_SERVICE";
		case 1:
			return "OUT_OF_SERVICE";
		case 2:
			return "OUT_OF_SERVICE_BY_MAINTENANCE";
		case 3:
			return "SERV_NA";
		}
		return "";
	}

}
