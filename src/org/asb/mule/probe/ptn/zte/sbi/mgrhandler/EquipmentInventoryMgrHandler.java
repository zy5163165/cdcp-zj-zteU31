package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import java.util.Vector;

import equipment.EquipmentHolder_T;
import equipment.EquipmentInventoryMgr_I;
import equipment.EquipmentOrHolderIterator_IHolder;
import equipment.EquipmentOrHolderList_THolder;
import equipment.EquipmentOrHolder_T;
import equipment.Equipment_T;
import globaldefs.NameAndStringValue_T;
import globaldefs.ProcessingFailureException;

public class EquipmentInventoryMgrHandler {
	private static EquipmentInventoryMgrHandler instance;

	public static EquipmentInventoryMgrHandler instance() {
		if (null == instance) {
			instance = new EquipmentInventoryMgrHandler();
		}
		return instance;
	}

	public EquipmentOrHolder_T[] retrieveAllEquipmentAndHolders(EquipmentInventoryMgr_I equipmentInventoryMgr, NameAndStringValue_T[] containerName)
			throws ProcessingFailureException {
		EquipmentOrHolderList_THolder emseqList = new EquipmentOrHolderList_THolder();

		equipmentInventoryMgr.getAllEquipment(containerName, emseqList);

		return emseqList.value;

	}


	public EquipmentOrHolder_T[] retrieveContainedEquipments(EquipmentInventoryMgr_I equipmentInventoryMgr, NameAndStringValue_T[] containerName)
			throws ProcessingFailureException {
		EquipmentOrHolderList_THolder emseqList = new EquipmentOrHolderList_THolder();

		equipmentInventoryMgr.getContainedEquipment(containerName, emseqList);

		return emseqList.value;

	}

	private EquipmentInventoryMgrHandler() {
	}


	public Equipment_T[] retrieveAllEquipments(EquipmentInventoryMgr_I equipmentInventoryMgr, NameAndStringValue_T[] containerName)
			throws ProcessingFailureException {
		EquipmentOrHolderList_THolder emseqList = new EquipmentOrHolderList_THolder();
		EquipmentOrHolderIterator_IHolder emseqIt = new EquipmentOrHolderIterator_IHolder();

		// test
		//int len = containerName.length;
		//for (int i = 0; i < len; i++)
		//	datalog.debug("name: " + containerName[i].name + "=" + containerName[i].value);
		// test end

		equipmentInventoryMgr.getAllEquipment(containerName, emseqList);

		//datalog.debug("retrieveAllEquipments: emseqList.value.length=" + emseqList.value.length);

		java.util.Vector emseqVector = new java.util.Vector();

		for (int i = 0; i < emseqList.value.length; i++) {
			if (emseqList.value[i].discriminator().equals(equipment.EquipmentTypeQualifier_T.EQT))
				emseqVector.addElement(emseqList.value[i].equip());
		}

		if (null != emseqIt.value) {

			boolean shouldContinue = true;

			try {
				emseqIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		Equipment_T[] equipments = new Equipment_T[emseqVector.size()];
		emseqVector.copyInto(equipments);

		return equipments;

	}

	public EquipmentHolder_T[] retrieveAllEquipmentHolders(EquipmentInventoryMgr_I equipmentInventoryMgr, NameAndStringValue_T[] containerName)
			throws ProcessingFailureException {
		EquipmentOrHolderList_THolder emseqList = new EquipmentOrHolderList_THolder();
		EquipmentOrHolderIterator_IHolder emseqIt = new EquipmentOrHolderIterator_IHolder();
		int howmany = 50;
		// test
		//int len = containerName.length;
		//for (int i = 0; i < len; i++)
		//	datalog.debug("name: " + containerName[i].name + "=" + containerName[i].value);
		// test end

		equipmentInventoryMgr.getAllEquipment(containerName, emseqList);

		//datalog.debug("retrieveAllEquipmentHolders: emseqList.value.length=" + emseqList.value.length);

		java.util.Vector emseqVector = new java.util.Vector();

		for (int i = 0; i < emseqList.value.length; i++) {
			if (emseqList.value[i].discriminator().equals(equipment.EquipmentTypeQualifier_T.EQT_HOLDER))
				emseqVector.addElement(emseqList.value[i].holder());
		}

		if (null != emseqIt.value) {

			boolean shouldContinue = true;
			while (shouldContinue) {
				shouldContinue = emseqIt.value.next_n(howmany, emseqList);

				for (int i = 0; i < emseqList.value.length; i++) {
					if (emseqList.value[i].discriminator().equals(equipment.EquipmentTypeQualifier_T.EQT_HOLDER))
						emseqVector.addElement(emseqList.value[i].holder());
				}

			}
			try {
				emseqIt.value.destroy();
			} catch (Throwable ex) {

			}
		}

		EquipmentHolder_T[] equipmentHolders = new EquipmentHolder_T[emseqVector.size()];
		emseqVector.copyInto(equipmentHolders);

		return equipmentHolders;

	}

}
