package org.asb.mule.probe.ptn.zte.service.mapper;

import managedElement.CommunicationState_T;

import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.CodeTool;

public class ManagedElementMapper extends CommonMapper

{
	private static ManagedElementMapper instance;

	public static ManagedElementMapper instance() {
		if (instance == null) {
			instance = new ManagedElementMapper();
		}
		return instance;
	}
	public ManagedElement convertManagedElement(managedElement.ManagedElement_T vendorEntity)

	{
		ManagedElement ne = new ManagedElement();

		// ne.setDn(vendorEntity.name[0].value + Constant.dnSplit + vendorEntity.name[1].value);
		ne.setDn(nv2dn(vendorEntity.name));
		// ne.setParentDn(mapperParentDnNameAndStringValue(vendorEntity.name));
		ne.setParentDn(vendorEntity.name[0].name + Constant.namevalueSplit + vendorEntity.name[0].value);
		ne.setEmsName(vendorEntity.name[0].value);
		// try {
		// FileOutputStream fos = new FileOutputStream("bs");
		// fos.write(vendorEntity.nativeEMSName.getBytes("ISO-8859-1"));
		// fos.flush();
		// fos.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		ne.setNeVersion(vendorEntity.hardwareVersion);
		ne.setOwner(vendorEntity.softwareVersion);
		ne.setNativeEMSName(CodeTool.isoToGbk(vendorEntity.nativeEMSName));
		ne.setUserLabel(CodeTool.isoToGbk(vendorEntity.userLabel));
		ne.setLocation(vendorEntity.location);
		ne.setProductName(CodeTool.isoToGbk(vendorEntity.productName));
		ne.setCommunicationState(mapperCommunicationState(vendorEntity.communicationState));
		ne.setAdditionalInfo(mapperAdditionalInfo(vendorEntity.additionalInfo));

		return ne;
	}

	private String mapperCommunicationState(CommunicationState_T state) {
		switch (state.value()) {
		case CommunicationState_T._CS_AVAILABLE:
			return "CS_AVAILABLE";
		case CommunicationState_T._CS_UNAVAILABLE:
			return "CS_UNAVAILABLE";

		}
		return "";
	}

}
