//package com.jxtools.wx.utils;
//
//import android.accessibilityservice.AccessibilityService;
//import android.view.accessibility.AccessibilityEvent;
//import android.view.accessibility.AccessibilityNodeInfo;
//
//import java.text.SimpleDateFormat;
//import java.util.List;
//import java.util.Set;
//
///**
// * @author ZW
// * @description:
// * @date : 2020/11/25 11:13
// */
//public class MessageCaptor extends AccessibilityService {
//
//    final String TAG = "MessageCaptor";
//    final String NameID_qq = "com.tencent.mobileqq:id/title";
//    final String TEXT_WITHDRAW = "撤回了一条消息";
//    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM/dd", Locale.CHINA);
//    List<String> WD_MsgList = XListAdapter.MsgList;
//    List<String> WD_NameList = XListAdapter.NameList;
//    Set<String> QQ_NameList;
//    boolean is_wx;
//    String tempMessage;
//    long ClickTime = 0;
//    long ClickTime2 = 0;
//    long ClickTime3 = 0;
//
//    Handler mHandler;
//    SingleClick singleClick;
//    DoubleClick doubleClick;
//    TrebleClick trebleClick;
//    AddNewMessage addNewMessage;
//    XFile xFile;
//
//    @Override
//    protected void onServiceConnected() {
//        ServerOnConnected = true;
//        xFile = new XFile(this);
//        QQ_NameList = getNameList();
//        mHandler = new Handler();
//    }
//
//    @Override
//    public void onInterrupt() {
//        ServerOnConnected = false;
//    }
//
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//
//        int eventType = event.getEventType();
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//        is_wx = event.getPackageName().equals("com.tencent.mm");
//
//        switch (eventType) {
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                //在屏幕切换时,如果用户是第一次使用app,则推送一条表示成功的通知
//                if (xFile.isShowCheckedNotice())
//                    new XNotification(this).printSuccess();
//                break;
//
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                //顶部通知栏状态改变
//                getNotification(event);
//                break;
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                if (nodeInfo == null)
//                    return;
//
//                //只需在改变类型为文字时执行添加操作
//                //大部分change type为 CONTENT_CHANGE_TYPE_SUBTREE
//                int types = event.getContentChangeTypes();
//                if (types != AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT)
//                    break;
//                CharSequence cs = nodeInfo.getText();
//                if (cs == null)
//                    break;
//
//                Log.w(TAG, "Text Changed : "   cs);
//
//                //判断是不是QQ聊天时其他人发的消息
//                if (isOtherConversation(cs))
//                    break;
//
//                //添加新消息至本地文件
//                addNewMessage = new AddNewMessage();
//                mHandler.post(addNewMessage);
//
//                break;
//            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                //点击事件
//                if (nodeInfo == null)
//                    break;
//                if (nodeInfo.getText() == null)
//                    break;
//                //只有点击了"撤回一条消息"才会继续执行
//                if (!nodeInfo.getText().toString().contains(TEXT_WITHDRAW)) {
//                    //test
////                    new GetNodes();
//                    break;
//                }
//
//                String name = getName();
//
//                //处理点击事件,单击双击等
//                onClick(event, name);
//
//                break;
//
//        }
//    }
