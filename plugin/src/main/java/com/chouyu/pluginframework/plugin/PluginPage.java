/*
 * File Name: BasePage.java 
 * History:
 * Created by Administrator on 2014-10-14
 */
package com.chouyu.pluginframework.plugin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * <b> 使用范围 插件开发工程
 * <p>
 * 插件程序中每个页面需继承该类
 * 
 * @author Wang xiaoqi
 * @version 1.0
 */
public abstract class PluginPage {

    // ==========================================================================
    // Constants
    // ==========================================================================
    /** 默认模式，多次跳转同一个page可以出现多次 */
    public final static int LAUNCH_MODE_DEFAULT = 0;
    /** 单一模式，多次跳转同一个page只允许出现一次 */
    public final static int LAUNCH_MODE_SINGLE = 1;
    // ==========================================================================
    // Fields
    // ==========================================================================
    private PluginActivityProxy mBasePluginVirtualActivity;
    private Intent mIntent;
    private View mRootView;
    private boolean isInvokeOnResume = false;
    private int mRequestCode = 0;
    private int mResultCode = 0;
    private Intent mResultData;

    private String classUID;
    private int mLaunchMode;

    // ==========================================================================
    // Constructors
    // ==========================================================================
    public PluginPage() {
        classUID = Math.random() + "";
        mLaunchMode = getLaunchMode();
    }

    // ==========================================================================
    // Getters
    // ==========================================================================

    public final View getRootView() {
        if (mRootView != null) {
            return mRootView;
        } else {
            TextView view = new TextView(getActivity());
            view.setText("未添加布局，请通过setPageContentView添加");
            LogPluginUtils.w("未添加布局，请通过setPageContentView添加");
            return view;
        }
    }

    /**
     * 获取外壳Activity
     * 
     * @return {@link Activity}
     */
    public Activity getActivity() {
        return mBasePluginVirtualActivity.mActivity;
    }

    /**
     * 获取外壳ApplicationContext
     * 
     * @return {@link Activity}
     */
    public Context getApplicationContext() {
        return mBasePluginVirtualActivity.mContext;
    }

    /**
     * 获取插件入口
     * 
     * @return 入口需继承{@link com.anzhi.plugin.framework.PluginActivityProxy}
     *         <p>
     *         插件的入口为AndroidMainfest.xml中含有android.intent.action.MAIN 与android.intent.category.LAUNCHER的Activity
     */
    public PluginActivityProxy getMainPluginVirtualActivity() {
        return mBasePluginVirtualActivity;
    }

    /**
     * 获取插件的Resources
     * 
     * @return {@link Activity}
     */
    public Resources getResources() {
        return mBasePluginVirtualActivity.mResources;
    }

    /**
     * 当前page是否finish
     * 
     * @return if true is finished else no finish
     */
    public boolean isPageFinish() {
        return !mBasePluginVirtualActivity.mPageStack.contains(this);
    }

    protected boolean isPageLoadCompleted() {
        return mBasePluginVirtualActivity.mPageStack.contains(this);
    }

    protected boolean isInvokeOnResume() {
        return isInvokeOnResume;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Intent getResultData() {
        return mResultData;
    }

    public boolean hideLastPage() {
        return true;
    }
    
    /**
     * PluginPage默认背景色
     * @return 默认值为0xFFFFFFFF
     */
    public int getPageDefaultBackgroundColor() {
        return 0xFFFFFFFF;
    }
    
    public boolean isPauseState() {
        return !isInvokeOnResume;
    }
    
    public boolean canFinish() {
        return true;
    }
    // ==========================================================================
    // Setters
    // ==========================================================================

    protected void setVirtualActivity(PluginActivityProxy activity) {
        mBasePluginVirtualActivity = activity;
    }

    protected void setIntent(Intent intent) {
        mIntent = intent;
    }

    /**
     * 给当前Page添加布局, 同Activity setContentView
     * 
     * @param view
     */
    public final void setPageContentView(View view) {
        mRootView = view;
    }

    /**
     * 给当前Page添加布局, 同Activity setContentView
     * 
     * @param layoutId
     *            LayoutID
     */
    public final void setPageContentView(int layoutId) {
        mRootView = mBasePluginVirtualActivity.mLayoutInflater.inflate(
                mBasePluginVirtualActivity.mResources.getLayout(layoutId), null);
    }

    protected void setInvokeOnResume(boolean value) {
        isInvokeOnResume = value;
    }

    /**
     * 同Activity onKeyDown
     * 
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 同Activity onKeyUp
     * 
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    public void setResultCode(int resultCode) {
        mResultCode = resultCode;
    }

    public void setResultCode(int resultCode, Intent intent) {
        mResultCode = resultCode;
        mResultData = intent;
    }

    public void setResultData(Intent intent) {
        mResultData = intent;
    }

    // ==========================================================================
    // Methods
    // ==========================================================================
    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    /**
     * 页面跳转
     * 
     * @bundle 传递参数
     */
    public void startPage(Class<?> c, Intent intent) {
        mBasePluginVirtualActivity.startPage(c, intent, 0, false);
    }

    /**
     * page切换
     * 
     * @param pageClass
     *            要跳转的pageclass
     * @param bundle
     *            参数
     * @param finishAllOther
     *            是否关闭其它page，只保留跳转后的page
     */
    public void startPage(Class<?> pageClass, Intent intent, boolean finishAllOther) {
        mBasePluginVirtualActivity.startPage(pageClass, intent, 0, finishAllOther);
    }

    /**
     * page切换
     * 
     * @param pageClass
     *            要跳转的pageclass
     * @param finishAllOther
     *            是否关闭其它page，只保留跳转后的page
     */
    public void startPage(Class<?> pageClass, boolean finishAllOther) {
        mBasePluginVirtualActivity.startPage(pageClass, null, 0, finishAllOther);
    }

    /**
     * page切换
     * 
     * @param pageClass
     *            要跳转的pageclass
     */
    public void startPage(Class<?> c) {
        mBasePluginVirtualActivity.startPage(c, null, 0, false);
    }

    /**
     * page切换
     * 
     * @param pageClass
     *            要跳转的pageclass
     * @param bundle
     *            参数
     * @param requestCode
     *            当新的page被finish后，接入参数的page会执行onPageResult
     */
    public void startPageForResult(Class<?> pageClass, Intent intent, int requestCode) {
        if (requestCode > 0) {
            mBasePluginVirtualActivity.startPage(pageClass, intent, requestCode, false);
        } else {
            throw new RuntimeException("startPageForResult must request >= 0");
        }
    }

    /**
     * page切换
     * 
     * @param pageClass
     *            要跳转的pageclass
     * @param requestCode
     *            当新的page被finish后，接入参数的page会执行onPageResult
     */
    public void startPageForResult(Class<?> pageClass, int requestCode) {
        if (requestCode > 0) {
            mBasePluginVirtualActivity.startPage(pageClass, null, requestCode, false);
        } else {
            throw new RuntimeException("startPageForResult must request >= 0");
        }
    }

    /**
     * 关闭当前page
     */
    public void finishPage() {
        mBasePluginVirtualActivity.finishPage(this);
    }

    /**
     * 关闭所有page，并关闭外壳Activity
     */
    public void finishAllPage() {
        mBasePluginVirtualActivity.finishAllPage(false);
    }
    
    /**
     * 关闭所有page，并关闭外壳Activity
     */
    public void finishAllPage(boolean force) {
        mBasePluginVirtualActivity.finishAllPage(force);
    }
//
//    /**
//     * 关闭某个page
//     * 
//     * @param page
//     *            要关闭的page
//     */
//    public void finishPage(PluginPage page) {
//        mBasePluginVirtualActivity.finishPage(page);
//    }

    /**
     * 获取插件String资源
     * 
     * @param resId
     *            资源ID
     * @return String
     */
    public String getString(int resId) {
        return mBasePluginVirtualActivity.getString(resId);
    }

    /**
     * 获取插件String资源
     * 
     * @param resId
     *            资源ID
     * @param formatArgs
     *            复合参数
     * @return String
     */
    public String getString(int resId, String... formatArgs) {
        return mBasePluginVirtualActivity.getString(resId, formatArgs);
    }

    /**
     * 获取插件raw资源
     * 
     * @param id
     *            资源ID
     * @return InputStream
     */
    public InputStream getRaw(int id) {
        return mBasePluginVirtualActivity.getRaw(id);
    }

    /**
     * 获取插件Drawable资源
     * 
     * @param resId
     *            资源id
     * @return Drawable
     */
    public Drawable getDrawable(int resId) {
        return mBasePluginVirtualActivity.getDrawable(resId);
    }

    /**
     * 获取插件的Layout布局
     * 
     * @param layoutId
     *            布局ID
     * @return View
     */
    public View inflate(int layoutId) {
        return mBasePluginVirtualActivity.inflate(layoutId);
    }

    /**
     * 获取资源色值
     * 
     * @param resId
     *            资源id
     * @return 色值
     */
    public int getColor(int resId) {
        return mBasePluginVirtualActivity.getColor(resId);
    }

    /**
     * 获取主题资源状态色值表
     * 
     * @param resId
     *            资源id
     * @return 状态色值表
     */
    public ColorStateList getColorStateList(int resId) {
        return mBasePluginVirtualActivity.getColorStateList(resId);
    }

    /**
     * 获取资源像素值
     * 
     * @param resId
     *            资源id
     * @return 像素值
     */
    public int getDimensionPixel(int resId) {
        return mBasePluginVirtualActivity.getDimensionPixel(resId);
    }

    public int dip2px(float dipValue) {
        return mBasePluginVirtualActivity.dip2px(dipValue);
    }

    public int px2dip(float pxValue) {
        return mBasePluginVirtualActivity.px2dip(pxValue);
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        return mBasePluginVirtualActivity.postDelayed(r, delayMillis);
    }

    public boolean post(Runnable r) {
        return mBasePluginVirtualActivity.post(r);
    }

    public void removeCallbacks(Runnable r) {
    	mBasePluginVirtualActivity.removeCallbacks(r);
    }
    
    public void removeCallbacks(Runnable r, Object obj) {
    	mBasePluginVirtualActivity.removeCallbacks(r, obj);
    }
    
    /**
     * 收起软键盘
     */
    public void closeInput() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
    }
//
//    /**
//     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
//     * 
//     * @param what
//     */
//    public void sendMessage(int what) {
//        mBasePluginVirtualActivity.sendMessage(what);
//    }
//
//    /**
//     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
//     * 
//     * @param what
//     * @param obj
//     */
//    public void sendMessage(int what, Object obj) {
//        mBasePluginVirtualActivity.sendMessage(what, obj);
//    }
//
//    /**
//     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
//     * 
//     * @param what
//     * @param arg1
//     * @param arg2
//     */
//    public void sendMessage(int what, int arg1, int arg2) {
//        mBasePluginVirtualActivity.sendMessage(what, arg1, arg2);
//    }
//
//    /**
//     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
//     * 
//     * @param what
//     * @param arg1
//     * @param arg2
//     * @param obj
//     */
//    public void sendMessage(int what, int arg1, int arg2, Object obj) {
//        mBasePluginVirtualActivity.sendMessage(what, arg1, arg2, obj);
//    }

    public void showToastSafe(final CharSequence text) {
        showToastSafe(text, 0);
    }

    public void showToastSafe(final int resId, final int duration) {
        mBasePluginVirtualActivity.showToastSafe(resId, duration);
    }

    public void showToastSafe(final View view, final int duration) {
        mBasePluginVirtualActivity.showToastSafe(view, duration);
    }

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param text
     *            Toast内容
     * @param duration
     *            Toast的持续时间
     */
    public void showToastSafe(final CharSequence text, final int duration) {
        mBasePluginVirtualActivity.showToastSafe(text, duration);
    }

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param resId
     *            Toast内容的资源id
     * @param duration
     *            Toast的持续时间
     * @param formatArgs
     *            Toast内容进行文字替换所使用的参数
     */
    public void showToastSafe(final int resId, final int duration, final Object... formatArgs) {
        mBasePluginVirtualActivity.showToastSafe(resId, duration, formatArgs);
    }

    /**
     * 对dismissDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param dialog
     *            Dialog实例
     */
    public void dismissDialogSafe(final Dialog dialog) {
        mBasePluginVirtualActivity.dismissDialogSafe(dialog);
    }

    /**
     * 对showDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param dialog
     *            Dialog实例
     */
    public void showDialogSafe(final Dialog dialog) {
        mBasePluginVirtualActivity.showDialogSafe(dialog);
    }

    /**
     * 运行模式，{@link com.anzhi.plugin.framework.PluginPage#LAUNCH_MODE_DEFAULT} or {@link com.anzhi.plugin.framework.PluginPage#LAUNCH_MODE_SINGLE}
     * 
     * @return default return {@link com.anzhi.plugin.framework.PluginPage#LAUNCH_MODE_DEFAULT}
     */
    public int getLaunchMode() {
        return LAUNCH_MODE_DEFAULT;
    }

    public void onAttachedToWindow() {

    }
    
    public void onWindowFocusChanged(boolean hasFocus) {
        
    }

    /**
     * 同Activity onActivityResult
     * 
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onPageResult(int requestCode, int resultCode, Intent data) {

    }
    
    public void onPageRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    public boolean dispatchKeyEvent(KeyEvent ev) {
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    /**
     * 同Activity onStart
     */
    public void onPageStart() {

    }

    /**
     * 同Activity onStop
     */
    public void onPageStop() {

    }

    /**
     * 同Activity onResume
     */
    public void onPageResume() {

    }

    /**
     * 同Activity onPause
     */
    public void onPagePause() {

    }

    /**
     * 同Activity onDestory
     */
    public void onPageDestory() {

    }

    /**
     * 同Activity onCreate
     * 
     * @param intent
     */
    public void onPageCreate(Intent intent) {

    }

    /**
     * 同Activity onNewIntent
     * 
     * @param intent
     */
    public void onPageNewIntent(Intent intent) {

    }

    /**
     * 同Activity onConfigurationChanged
     * 
     * @param newConfig
     */
    public void onConfigurationChanged(Configuration newConfig) {

    }

//    public void onHandleMessage(Message msg) {
//
//    }

    /**
     * 获取外壳传递进来的Parcelable类型数据
     * 
     * <pre>
     * demo
     * <p> @BySDKLibInvoke
     * private void setStudentToPlugin(Parcelable inItem) {
     *      StudentInfo studentInfo = new StudentInfo();
     *      createParcelableItem(studentInfo, inItem)
     * }
     * </pre>
     * 
     * @param inItem
     *            外壳传递进来的数据
     * @param outItem
     *            要转换输出的Parcelable数据
     * 
     * @return Parcelable
     */
    public Parcelable createParceableItem(Class<?> outClass, Parcelable inItem) {
        return mBasePluginVirtualActivity.createParceableItem(outClass, inItem);
    }
    
    public List<Parcelable> createParceableItems(Class<?> outClass, List<Parcelable> inItems) {
        return mBasePluginVirtualActivity.createParceableItems(outClass, inItems);
    }

    /**
     * 调用外壳程序的方法
     * 
     * <pre>
     * DEMO<p>
     * int state = invoke("login", new Class[]{String.class, String.class} ,new Object[]{user, pass});
     * </pre>
     * 
     * @param methodName
     *            方法名 parameterTypes
     * @param parameterTypes
     *            参数类型
     * @param params
     *            具体参数
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String methodName, Class<?>[] parameterTypes, Object[] params) {
        return mBasePluginVirtualActivity.invoke(methodName, parameterTypes, params);
    }
    
    public <T> T invoke(Object receiver, String methodName, Class<?>[] parameterTypes, Object[] params) {
        return mBasePluginVirtualActivity.invoke(receiver, methodName, parameterTypes, params);
    }

    public Method getMethod(Class<?> obj, String methodName, Class<?>[] parameterTypes) {
        return mBasePluginVirtualActivity.getMethod(obj, methodName, parameterTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (mLaunchMode == LAUNCH_MODE_SINGLE) {
            if (o != null && o instanceof PluginPage) {
                return this.getClass().getName().equals(o.getClass().getName());
            }
        }
        if (o != null && o instanceof PluginPage) {
            PluginPage temp = (PluginPage) o;
            return (this.getClass().getName() + "_" + classUID).equals(temp.getClass().getName() + "_" + temp.classUID);
        }
        return false;
    }
    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}
