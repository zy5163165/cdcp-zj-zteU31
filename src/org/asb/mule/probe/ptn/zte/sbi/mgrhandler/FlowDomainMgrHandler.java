package org.asb.mule.probe.ptn.zte.sbi.mgrhandler;

import common.CapabilityList_THolder;

import flowDomain.FDIterator_IHolder;
import flowDomain.FDList_THolder;
import flowDomain.FlowDomainMgr_I;
import flowDomain.FlowDomain_T;
import flowDomainFragment.*;
import globaldefs.*;
import org.asb.mule.probe.framework.util.CodeTool;

public class FlowDomainMgrHandler {
    private static FlowDomainMgrHandler instance;

    private static final String head1 = "FlowDomainHandler::";

    public static FlowDomainMgrHandler instance() {
        if (null == instance)
            instance = new FlowDomainMgrHandler();

        return instance;
    }

    private void print(NameAndStringValue_T[] name) {
        for (int j = 0; j < name.length; j++) {
            NameAndStringValue_T nameAndStringValue_t = name[j];
            String value1 = nameAndStringValue_t.value;
            String name1 = nameAndStringValue_t.name;
            System.out.print("name = " + nameAndStringValue_t.name + " ; ");
            System.out.print("value = " + nameAndStringValue_t.value + " | ");

        }
        System.out.println();
    }

    public FlowDomainFragment_T[] retrieveAllFDFrs22(FlowDomainMgr_I fdMgr, short[] connectionRateList, NameAndStringValue_T[] fdName)
            throws ProcessingFailureException {
        // /////////////////////// add by ronnie //////////////////////
        // System.out.println("addbyronnie");
        // FDIterator_IHolder fdIt = new FDIterator_IHolder();
        // FDList_THolder flowDomains = new FDList_THolder();
        // fdMgr.getAllFlowDomains(10, flowDomains, fdIt);
        // FlowDomain_T[] value = flowDomains.value;
        // for (int i = 0; i < value.length; i++) {
        // FlowDomain_T flowDomain_t = value[i];
        // NameAndStringValue_T[] name = flowDomain_t.name;
        // for (int j = 0; j < name.length; j++) {
        // NameAndStringValue_T nameAndStringValue_t = name[j];
        // String value1 = nameAndStringValue_t.value;
        // String name1 = nameAndStringValue_t.name;
        // if (name1.equals("EMS"))
        // nameAndStringValue_t.value = "WRI_1";
        // System.out.println("name1 = " + nameAndStringValue_t.name);
        // System.out.println("value1 = " + nameAndStringValue_t.value);
        //
        //
        // }
        // fdName = name;
        // }
        //
        // System.out.println("addbyronnie end");
        // System.out.println("rate list:");
        // if (connectionRateList != null) {
        // for (int i = 0; i < connectionRateList.length; i++) {
        // short i1 = connectionRateList[i];
        // System.out.println(i+" = " + i1);
        // }
        // }

        // ////////////////////////////////////////////////////////////

        java.util.Vector fdfrsVector = new java.util.Vector();

        FDFrIterator_IHolder fdfrIt = new FDFrIterator_IHolder();
        FDFrList_THolder fdfrList = new FDFrList_THolder();
        System.out.println("invoke getALLFDFrs");
        System.out.println("fdName:");
        for (int i = 0; i < fdName.length; i++) {
            NameAndStringValue_T nameAndStringValue_t = fdName[i];
            System.out.println(nameAndStringValue_t.name + "=" + nameAndStringValue_t.value);
        }
        System.out.println("connectionRateList:" + connectionRateList.length);
        fdMgr.getAllFDFrs(fdName, 10, connectionRateList, fdfrList, fdfrIt);
        System.out.println("fdfrList = " + fdfrList.value.length);
        FDFrList_THolder fdfrList1 = new FDFrList_THolder();
        if (fdfrIt.value != null) {
            fdfrIt.value.next_n(10, fdfrList1);
            System.out.println("fdfrList1 = " + fdfrList1.value.length);
        } else
            System.out.println("fdfrList1 is null");

        for (int i = 0; i < fdfrList.value.length; i++) {
            fdfrsVector.addElement(fdfrList.value[i]);
        }

        if (null != fdfrIt.value) {
            boolean shouldContinue = true;
            while (shouldContinue) {
                shouldContinue = fdfrIt.value.next_n(50, fdfrList);

                for (int i = 0; i < fdfrList.value.length; i++)
                    fdfrsVector.addElement(fdfrList.value[i]);
            }

            try {
                fdfrIt.value.destroy();
            } catch (Throwable ex) {
                // datalog.info("retrieveAllFlowDomainFragments:destory Iterator");
            }
        }

        FlowDomainFragment_T[] fdfrs = new FlowDomainFragment_T[fdfrsVector.size()];
        fdfrsVector.copyInto(fdfrs);

        // for(int i=0;i<fdfrs.length;i++)
        // {
        // datalog.debug("Get fdfrs :"+fdfrs[i].toString());
        // }

        return fdfrs;
    }

    public FlowDomainFragment_T[] retrieveAllFDFrs(FlowDomainMgr_I fdMgr, short[] connectionRateList, NameAndStringValue_T[] fdName)
            throws ProcessingFailureException {
        // System.out.println("fdName111:");
        // for (int i = 0; i < fdName.length; i++) {
        // NameAndStringValue_T nameAndStringValue_t = fdName[i];
        // System.out.println(nameAndStringValue_t.name + "=" + nameAndStringValue_t.value);
        // }
        java.util.Vector fdfrsVector = new java.util.Vector();

        FDFrExIterator_IHolder fdfrIt = new FDFrExIterator_IHolder();
        FDFrExList_THolder fdfrList = new FDFrExList_THolder();
        fdMgr.getAllFDFrExs(fdName, 50, connectionRateList, fdfrList, fdfrIt);

        for (int i = 0; i < fdfrList.value.length; i++) {
            fdfrsVector.addElement(fdfrList.value[i]);
        }

        if (null != fdfrIt.value) {
            boolean shouldContinue = true;
            while (shouldContinue) {
                shouldContinue = fdfrIt.value.next_n(50, fdfrList);
                // shouldContinue = false; // only for debug
                for (int i = 0; i < fdfrList.value.length; i++)
                    fdfrsVector.addElement(fdfrList.value[i]);
            }

            try {
                fdfrIt.value.destroy();
            } catch (Throwable ex) {
                // datalog.info("retrieveAllFlowDomainFragments:destory Iterator");
            }
        }

        FlowDomainFragment_T[] fdfrs = new FlowDomainFragment_T[fdfrsVector.size()];
        fdfrsVector.copyInto(fdfrs);

        // for (int i = 0; i < 10; i++) {
        // //datalog.debug("Get fdfrs :" + fdfrs[i].toString());
        // FDFrRoute_THolder route = new FDFrRoute_THolder();
        // try {
        // print(fdfrs[i].name);
        // fdMgr.getFDFrRoute(fdfrs[i].name, route);
        // MatrixFlowDomainFragment_T[] value = route.value;
        // if (value != null && value.length > 0) {
        // datalog.debug("route size :" + value.length);
        // System.out.println("route size :" + value.length);
        // }
        // } catch (ProcessingFailureException e) {
        //
        // System.out.println("reterive failure-"+ CodeTool.isoToGbk(e.errorReason));
        // continue;
        // }
        //
        //
        //
        // }

        return fdfrs;
    }

    public FlowDomain_T[] retrieveAllFlowDomains(FlowDomainMgr_I fdMgr) throws ProcessingFailureException {

        java.util.Vector fdVector = new java.util.Vector();

        FDList_THolder fdH = new FDList_THolder();
        FDIterator_IHolder fdI = new FDIterator_IHolder();

        fdMgr.getAllFlowDomains(50, fdH, fdI);

        for (int i = 0; i < fdH.value.length; i++) {
            fdVector.addElement(fdH.value[i]);
        }

        if (null != fdI.value) {
            boolean shouldContinue = true;
            while (shouldContinue) {
                shouldContinue = fdI.value.next_n(50, fdH);

                for (int i = 0; i < fdH.value.length; i++)
                    fdVector.addElement(fdH.value[i]);
            }

            try {
                fdI.value.destroy();
            } catch (Throwable ex) {
                // datalog.info("retrieveAllFlowDomains:destory Iterator");
            }
        }

        FlowDomain_T[] fds = new FlowDomain_T[fdVector.size()];
        fdVector.copyInto(fds);

        // for (int i = 0; i < fds.length; i++) {
        // datalog.debug("Get flowdomains :" + fds[i].toString());
        // }

        return fds;
    }

}
