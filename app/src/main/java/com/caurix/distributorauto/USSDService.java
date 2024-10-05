package com.caurix.distributorauto;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class USSDService extends AccessibilityService {

    public static boolean isDone = true;

    private static int counter = 0;
    public String value;
    public String buttonSend = "Send";
    public String buttonOk = "Ok";

    private String TAG = "USSDService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG,String.valueOf(isDone));
        if(isDone)
            return;

        Log.d(TAG,"onAccessibilityEvent, counter: " + String.valueOf(counter));

        AccessibilityNodeInfo accessibilityNodeInfo;
        accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            Log.d(TAG,"nodeinfo null1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG,"interrupted");
            }
            accessibilityNodeInfo = getRootInActiveWindow();
        }

        if (accessibilityNodeInfo == null) {
            Log.d(TAG,"nodeinfo null2");
            return;
        } else {
            Log.d(TAG,"nodeinfo not null2");
        }
        if (InSMSReceiverDistributor.Data6 == null) {
            if (counter ==  5) {
                List<AccessibilityNodeInfo> nodeInfoListButtonOk =
                        accessibilityNodeInfo.findAccessibilityNodeInfosByText("Ok");

                if (!nodeInfoListButtonOk.isEmpty()){
                    Log.d(TAG,"nodeInfoListOK not null");
                    nodeInfoListButtonOk.get(nodeInfoListButtonOk.size() -1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isDone = true;
                    counter = 0;
                    return;
                }
            }
        } else {
            if (counter ==  6) {
                List<AccessibilityNodeInfo> nodeInfoListButtonOk =
                        accessibilityNodeInfo.findAccessibilityNodeInfosByText("Ok");

                if (!nodeInfoListButtonOk.isEmpty()){
                    Log.d(TAG,"nodeInfoListOK not null");
                    nodeInfoListButtonOk.get(nodeInfoListButtonOk.size() -1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isDone = true;
                    counter = 0;
                    return;
                }
            }

            if(counter == 3){
//                List<AccessibilityNodeInfo> nodeInfoListDepot =
//                        accessibilityNodeInfo.findAccessibilityNodeInfosByText("dépot");
//                if (nodeInfoListDepot.isEmpty()){
//                    Log.d(TAG,"nodeInfoListDepot not null");
//                    counter += 2;
//                }


                for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                    if(accessibilityNodeInfo.getChild(i).getClassName().equals("android.widget.TextView")){
                        Toast.makeText(this, "found " + accessibilityNodeInfo.getChild(i).getText(), Toast.LENGTH_SHORT).show();
                        if(accessibilityNodeInfo.getChild(i).getText().toString().contains("peut")){
                            counter += 2;
                        }
                    }
                }
            }
        }


        switch (counter) {
            case 0:
                value = InSMSReceiverDistributor.Data1;
                break;
            case 1:
                value = InSMSReceiverDistributor.Data2;
                break;
            case 2:
                value = InSMSReceiverDistributor.Data3;
                break;
            case 3:
                value = InSMSReceiverDistributor.Data4;
                break;
            case 4:
                value = InSMSReceiverDistributor.Data5;
                break;
            case 5:
                value = InSMSReceiverDistributor.Data6;
                break;
            default:
                value = "1";
        }

        List<AccessibilityNodeInfo> nodeInfoList =
                accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.android.phone:id/input_field");

        if (nodeInfoList.isEmpty()){
            Log.d(TAG,"nodeInfoList null");
            return;
        }

        for (AccessibilityNodeInfo i: nodeInfoList) {
            if (i.getClassName() != null) {

                if(i.getText() != null) {
                    Log.d(TAG,"Text: " + i.getText());
                    return;
                } else {
                    Log.d(TAG,"Text: " + i.getText());
                }

                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
                i.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG,"interrupted");
                }
            } else
                Log.d(TAG,"Class name is null");
        }


        List<AccessibilityNodeInfo> nodeInfoListButtonSend =
                accessibilityNodeInfo.findAccessibilityNodeInfosByText(buttonSend);

        if (nodeInfoListButtonSend.isEmpty()){
            Log.d(TAG,"nodeInfoList null");
            return;
        }

        nodeInfoListButtonSend.get(nodeInfoListButtonSend.size() -1).performAction(AccessibilityNodeInfo.ACTION_CLICK);

        counter++;

        if (InSMSReceiverDistributor.Data6 == null) {
            if(counter == 6) {
                isDone = true;
                counter = 0;
            }
        } else {
            if(counter == 7) {
                isDone = true;
                counter = 0;
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG,"onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.packageNames = new String[]{"com.android.phone", "com.example.myapplication"};
        setServiceInfo(info);
    }
}
