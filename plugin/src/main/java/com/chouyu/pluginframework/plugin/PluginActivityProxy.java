/*
 * File Name: BasePluginActivity.java
 * History:
 * Created by Administrator on 2014-7-28
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * <b> 使用范围 插件开发工程
 * <p>
 * 插件程序入口需继承该类
 *
 * @author Wang xiaoqi
 * @version 1.0
 */
public abstract class PluginActivityProxy {
    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Fields
    // ==========================================================================
    protected Activity mActivity;
    protected Context mContext;
    protected Object mInvokeInterfaceObj;
    protected Handler mHandler;
    protected Resources mResources;
    protected LayoutInflater mLayoutInflater;
    protected int mUIThreadId;
    protected FrameLayout mRootView;
    protected Stack<PluginPage> mPageStack;
    protected int mPreKeyCode = -1;
    protected PluginPage mCurPage;

    // ==========================================================================
    // Constructors
    // ==========================================================================

    // ==========================================================================
    // Getters
    // ==========================================================================

    /**
     * 获取栈中所有PluginPage
     *
     * @return
     */
    public final Stack<PluginPage> getPageStack() {
        return mPageStack;
    }

    @BySDKLibInvoke
    private View getRootView() {
        return mRootView;
    }

    /**
     * 获取外壳Activity
     *
     * @return
     */
    public Activity getActivity() {
        return mActivity;
    }

    public int getUIThread() {
        return mUIThreadId;
    }

    /**
     * 获取外壳applicationContext
     *
     * @return Context
     */
    public Context getApplicationContext() {
        return mContext;
    }

    @BySDKLibInvoke
    private Object getPluginInvokeInterface() {
        if (mCurPage != null) {
            return mCurPage;
        }
        return this;
    }

    /**
     * 获取当前栈顶的PluginPage
     *
     * @return
     */
    public PluginPage getCurrentPage() {
        return mCurPage;
    }

    public Resources getResources() {
        return mResources;
    }

    /**
     * 获取插件String资源
     *
     * @param resId 资源ID
     * @return String
     */
    public String getString(int resId) {
        return mResources.getString(resId);
    }

    /**
     * 获取插件String资源
     *
     * @param resId      资源ID
     * @param formatArgs 复合参数
     * @return String
     */
    public String getString(int resId, String... formatArgs) {
        return mResources.getString(resId, formatArgs);
    }

    /**
     * 获取插件raw资源
     *
     * @param id 资源ID
     * @return InputStream
     */
    public InputStream getRaw(int id) {
        return mResources.openRawResource(id);
    }

    /**
     * 获取插件Drawable资源
     *
     * @param resId 资源id
     * @return Drawable
     */
    public Drawable getDrawable(int resId) {
        return mResources.getDrawable(resId);
    }

    /**
     * 获取插件的Layout布局
     *
     * @param layoutId 布局ID
     * @return View
     */
    public View inflate(int layoutId) {
        return mLayoutInflater.inflate(mResources.getLayout(layoutId), null);
    }

    /**
     * 获取资源色值
     *
     * @param resId 资源id
     * @return 色值
     */
    public int getColor(int resId) {
        return mResources.getColor(resId);
    }

    /**
     * 获取主题资源状态色值表
     *
     * @param resId 资源id
     * @return 状态色值表
     */
    public ColorStateList getColorStateList(int resId) {
        return mResources.getColorStateList(resId);
    }

    /**
     * 获取资源像素值
     *
     * @param resId 资源id
     * @return 像素值
     */
    public int getDimensionPixel(int resId) {
        return mResources.getDimensionPixelSize(resId);
    }

    public int dip2px(float dipValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public int px2dip(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

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
     * @param outCls  生成的数据类型
     * @param inItem  外壳传递进来的数据
     * @param outItem 要转换输出的Parcelable数据
     * @return Parcelable
     */
    public Parcelable createParceableItem(Class<?> outCls, Parcelable inItem) {
        try {
            Parcel parcel = Parcel.obtain();
            inItem.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            parcel.setDataPosition(0);
            Field field = outCls.getField("CREATOR");
            field.setAccessible(true);
            Creator<?> creator = (Creator<?>) field.get(outCls);
            return (Parcelable) creator.createFromParcel(parcel);
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
        return null;
    }

    /**
     * 获取外壳传递进来的Parcelable类型数据
     *
     * @param outCls  生成的数据类型
     * @param inItems 数据源
     * @return
     */
    public List<Parcelable> createParceableItems(Class<?> outCls, List<Parcelable> inItems) {
        List<Parcelable> list = null;
        if (inItems != null && inItems.size() > 0) {
            list = new ArrayList<Parcelable>();
            for (int i = 0; i < inItems.size(); i++) {
                Parcelable p = createParceableItem(outCls, inItems.get(i));
                if (p != null) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    // ==========================================================================
    // Setters
    // ==========================================================================
    @BySDKLibInvoke
    private void setActivity(Activity activity) {
        mActivity = activity;
        if (mActivity != null) {
            mContext = mActivity.getApplicationContext();
            mLayoutInflater = activity.getLayoutInflater();
        } else {// 资源释放
            mContext = null;
            mLayoutInflater = null;
        }
        LogPluginUtils.i("setActivity " + activity);
    }

    @BySDKLibInvoke
    public void setApplicationContext(Context context) {
        mContext = context;
        LogPluginUtils.i("setApplicationContext " + context);
    }

    @BySDKLibInvoke
    private void setHandler(Handler handler) {
        mHandler = handler;
        LogPluginUtils.i("setHandler " + handler);
    }

    @BySDKLibInvoke
    private void setResources(Resources resources) {
        mResources = resources;
        LogPluginUtils.i("setResources " + resources);
    }

    @BySDKLibInvoke
    private void setInvokeInterface(Object obj) {
        mInvokeInterfaceObj = obj;
        LogPluginUtils.i("setInvokeInterface " + obj);
    }

    // ==========================================================================
    // Methods
    // ==========================================================================

    @BySDKLibInvoke
    public void createMainView(Activity activity) {
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
        mLayoutInflater = activity.getLayoutInflater();
        mHandler = new Handler(mActivity.getMainLooper());
        mUIThreadId = Process.myTid();
        mPageStack = new Stack<PluginPage>();
        mRootView = new FrameLayout(activity);
        LogPluginUtils.i("plugin createMainView");
        startPage(getMainPageClass(mActivity.getIntent()), mActivity.getIntent(), 0, false);
    }

    @BySDKLibInvoke
    public void onCreate(Bundle savedInstanceState) {
        LogPluginUtils.i("plugin onCreate");
        createMainView(mActivity);
    }

    @BySDKLibInvoke
    public void onNewIntent(Intent intent) {
        mUIThreadId = Process.myTid();
        Class<?> pageCls = getMainPageClass(intent);
        if (pageCls != null) {
            startPage(getMainPageClass(intent), intent, 0, false);
            return;
        }
        LogPluginUtils.i("plugin onNewIntent curPage= " + mCurPage);
        if (mCurPage != null) {
            mCurPage.onPageNewIntent(intent);
        }
    }

    @BySDKLibInvoke
    private void setContentView(View v) {
        mActivity.setContentView(v);
        LogPluginUtils.i("plugin setContentView v= " + v);
    }

    @BySDKLibInvoke
    private void setContentView(int resId) {
        mActivity.setContentView(resId);
        LogPluginUtils.i("plugin setContentView resId= " + resId);
    }

    @BySDKLibInvoke
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogPluginUtils.i("plugin onActivityResult curPage= " + mCurPage + ",requestCode=" + requestCode
                + ",resultCode=" + resultCode + ",data=" + data);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onPageResult(requestCode, resultCode, data);
        }
    }

    @BySDKLibInvoke
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        LogPluginUtils.i("plugin onRequestPermissionsResult curPage= " + mCurPage + ",requestCode=" + requestCode);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onPageRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @BySDKLibInvoke
    public void onResume() {
        LogPluginUtils.i("plugin onResume curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted() && !mCurPage.isInvokeOnResume()) {
            mCurPage.onPageResume();
            mCurPage.setInvokeOnResume(true);
        }
    }

    @BySDKLibInvoke
    public void onPause() {
        LogPluginUtils.i("plugin onPause curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted() && mCurPage.isInvokeOnResume()) {
            mCurPage.onPagePause();
            mCurPage.setInvokeOnResume(false);
        }
    }

    @BySDKLibInvoke
    public void onStart() {
        LogPluginUtils.i("plugin onStart curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted() && mCurPage.isInvokeOnResume()) {
            mCurPage.onPageStart();
        }
    }

    @BySDKLibInvoke
    public void onStop() {
        LogPluginUtils.i("plugin onStop curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted() && mCurPage.isInvokeOnResume()) {
            mCurPage.onPageStop();
        }
    }

    @BySDKLibInvoke
    public void onDestroy() {
        LogPluginUtils.i("plugin onDestroy curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onPageDestory();
        }
    }

    @BySDKLibInvoke
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            return mCurPage.dispatchTouchEvent(ev);
        }
        return false;
    }

    @BySDKLibInvoke
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            return mCurPage.dispatchKeyEvent(ev);
        }
        return false;
    }

    @BySDKLibInvoke
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogPluginUtils.i("plugin onKeyDown curPage= " + mCurPage);
        if (event.getRepeatCount() > 0) {
            return true;
        }
        mPreKeyCode = keyCode;
        if (mCurPage != null && mCurPage.onKeyDown(keyCode, event)) {
            return true;
        }
        return false;
    }

    @BySDKLibInvoke
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogPluginUtils.i("plugin onKeyUp curPage= " + mCurPage);
        if (mPreKeyCode == -1) {
            return false;
        }
        mPreKeyCode = -1;
        if (mCurPage != null) {
            if (mCurPage.onKeyUp(keyCode, event)) {
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK && mPageStack.size() >= 1) {
                mCurPage.finishPage();
                return true;
            }
        }

        return false;
    }

    @BySDKLibInvoke
    public void onConfigurationChanged(Configuration newConfig) {
        LogPluginUtils.i("plugin onConfigurationChanged curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onConfigurationChanged(newConfig);
        }
    }

    @BySDKLibInvoke
    public void onAttachedToWindow() {
        LogPluginUtils.i("plugin onAttachedToWindow curPage= " + mCurPage);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onAttachedToWindow();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        LogPluginUtils.i("plugin onWindowFocusChanged hasFocus= " + hasFocus);
        if (mCurPage != null && mCurPage.isPageLoadCompleted()) {
            mCurPage.onWindowFocusChanged(hasFocus);
        }
    }

    @BySDKLibInvoke
    public void onHandleMessage(Message msg) {

    }

    public void showToastSafe(final CharSequence text) {
        showToastSafe(text, 0);
    }

    public void showToastSafe(final int resId, final int duration) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(mActivity, resId, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, resId, duration).show();
                }
            });
        }
    }

    public void showToastSafe(final View view, final int duration) {
        if (Process.myTid() == mUIThreadId) {
            Toast toast = new Toast(mContext);
            toast.setDuration(duration);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.setView(view);
            toast.show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = new Toast(mContext);
                    toast.setDuration(duration);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.setView(view);
                    toast.show();
                }
            });
        }
    }

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     *
     * @param text     Toast内容
     * @param duration Toast的持续时间
     */
    public void showToastSafe(final CharSequence text, final int duration) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(mContext, text, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, text, duration).show();
                }
            });
        }
    }

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     *
     * @param resId      Toast内容的资源id
     * @param duration   Toast的持续时间
     * @param formatArgs Toast内容进行文字替换所使用的参数
     */
    public void showToastSafe(final int resId, final int duration, final Object... formatArgs) {
        final CharSequence cs = mContext.getString(resId, formatArgs);
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(mContext, cs, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, cs, duration).show();
                }
            });
        }
    }

    /**
     * 对dismissDialog的简易封装。线程安全，可以在非UI线程调用。
     *
     * @param dialog Dialog实例
     */
    public void dismissDialogSafe(final Dialog dialog) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            dialog.dismiss();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * 对showDialog的简易封装。线程安全，可以在非UI线程调用。
     *
     * @param dialog Dialog实例
     */
    public void showDialogSafe(final Dialog dialog) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            dialog.show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }
    }

    /**
     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
     *
     * @param what
     */
    public void sendMessage(int what) {
        if (mHandler != null)
            mHandler.sendEmptyMessage(what);
    }

    /**
     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
     *
     * @param what
     * @param obj
     */
    public void sendMessage(int what, Object obj) {
        if (mHandler != null)
            mHandler.obtainMessage(what, obj).sendToTarget();
    }

    /**
     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
     *
     * @param what
     * @param arg1
     * @param arg2
     */
    public void sendMessage(int what, int arg1, int arg2) {
        if (mHandler != null)
            mHandler.obtainMessage(what, arg1, arg2).sendToTarget();
    }

    /**
     * 向内置handler发送消息。将会触发{@link com.anzhi.plugin.framework.PluginPage#onHandleMessage(Message msg)}。
     *
     * @param what
     * @param arg1
     * @param arg2
     * @param obj
     */
    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        if (mHandler != null)
            mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        if (mHandler == null)
            return false;
        return mHandler.postDelayed(r, delayMillis);
    }

    public boolean post(Runnable r) {
        if (mHandler == null)
            return false;
        return mHandler.post(r);
    }

    public void removeCallbacks(Runnable r) {
        if (mHandler == null)
            return;
        mHandler.removeCallbacks(r);
    }

    public void removeCallbacks(Runnable r, Object obj) {
        if (mHandler == null)
            return;
        mHandler.removeCallbacks(r, obj);
    }

    /**
     * 页面跳转
     *
     * @bundle 传递参数
     */
    public void startPage(Class<?> c, Intent intent) {
        startPage(c, intent, 0, false);
    }

    /**
     * page切换
     *
     * @param pageClass      要跳转的pageclass
     * @param bundle         参数
     * @param finishAllOther 是否关闭其它page，只保留跳转后的page
     */
    public void startPage(Class<?> pageClass, Intent intent, boolean finishAllOther) {
        startPage(pageClass, intent, 0, finishAllOther);
    }

    /**
     * page切换
     *
     * @param pageClass      要跳转的pageclass
     * @param finishAllOther 是否关闭其它page，只保留跳转后的page
     */
    public void startPage(Class<?> pageClass, boolean finishAllOther) {
        startPage(pageClass, null, 0, finishAllOther);
    }

    /**
     * page切换
     *
     * @param pageClass 要跳转的pageclass
     */
    public void startPage(Class<?> c) {
        startPage(c, null, 0, false);
    }

    /**
     * page切换
     *
     * @param pageClass   要跳转的pageclass
     * @param bundle      参数
     * @param requestCode 当新的page被finish后，接入参数的page会执行onPageResult
     */
    public void startPageForResult(Class<?> pageClass, Intent intent, int requestCode) {
        if (requestCode > 0) {
            startPage(pageClass, intent, requestCode, false);
        } else {
            throw new RuntimeException("startPageForResult must request >= 0");
        }
    }

    /**
     * page切换
     *
     * @param pageClass   要跳转的pageclass
     * @param requestCode 当新的page被finish后，接入参数的page会执行onPageResult
     */
    public void startPageForResult(Class<?> pageClass, int requestCode) {
        if (requestCode > 0) {
            startPage(pageClass, null, requestCode, false);
        } else {
            throw new RuntimeException("startPageForResult must request >= 0");
        }
    }

    protected void startPage(final Class<?> c, final Intent intent, final int requestCode, final boolean finishAllOther) {
        if (Process.myTid() == mUIThreadId) {
            LogPluginUtils.w("startPage in UI thread");
            startPageInner(c, intent, requestCode, finishAllOther);
        } else {
            LogPluginUtils.w("startPage not in UI thread");
            post(new Runnable() {

                @Override
                public void run() {
                    startPageInner(c, intent, requestCode, finishAllOther);
                }
            });
        }

    }

    /**
     * 关闭某个页面
     *
     * @param page 要关闭的页面
     */
    protected void finishPage(final PluginPage page) {
        if (Process.myTid() == mUIThreadId) {
            LogPluginUtils.w("finishPage in UI thread");
            finishPageInner(page);
        } else {
            LogPluginUtils.w("finishPage not in UI thread");
            post(new Runnable() {

                @Override
                public void run() {
                    finishPageInner(page);
                }
            });
        }

    }

    /**
     * 关闭所有插件打开的界面，并关闭外壳Activity
     */
    protected void finishAllPage(final boolean force) {
        if (Process.myTid() == mUIThreadId) {
            LogPluginUtils.w("finishAllPage in UI thread");
            finishAllInner(force);
        } else {
            LogPluginUtils.w("finishAllPage not in UI thread");
            post(new Runnable() {

                @Override
                public void run() {
                    finishAllInner(force);
                }
            });
        }
    }

    private void finishPageInner(final PluginPage page) {
        try {
            LogPluginUtils.i("FINISH PAGE {" + page.getClass().getName() + "}");
            mPageStack.remove(page);
            page.onPagePause();
            page.setInvokeOnResume(false);
            page.onPageDestory();
            int requestCode = page.getRequestCode();
            int resultCode = page.getResultCode();
            Intent resultData = page.getResultData();
            mRootView.removeView(page.getRootView());
            if (mPageStack.size() == 0) {
                mCurPage = null;
                mActivity.finish();
                LogPluginUtils.w("Activity finish!");
                return;
            }
            PluginPage tmpCurPage = mPageStack.get(mPageStack.size() - 1);
            mCurPage = tmpCurPage;
            if (requestCode > 0) {
                tmpCurPage.onPageResult(requestCode, resultCode, resultData);
            }
            tmpCurPage.onPageResume();
            tmpCurPage.setInvokeOnResume(true);
            tmpCurPage.getRootView().setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPageInner(final Class<?> c, final Intent intent, final int requestCode,
                                final boolean finishAllOther) {
        try {
            LogPluginUtils.i("START_PAGE {" + c.getName() + "}, intent= " + intent + ", requestCode=" + requestCode);
            Constructor<?> contruct = c.getConstructor();
            PluginPage page = (PluginPage) contruct.newInstance();
            if (mPageStack.contains(page)) {
                LogPluginUtils.i("START_PAGE {" + c.getName() + "} is has exists, move to top");
                int index = mPageStack.indexOf(page);
                mCurPage = mPageStack.get(index);
                LogPluginUtils.i("PAGE {" + c.getName() + "} LaunchMode = " + page.getLaunchMode());

                if (finishAllOther || mCurPage.getLaunchMode() == PluginPage.LAUNCH_MODE_SINGLE) {
                    while (!mPageStack.isEmpty()) {
                        PluginPage tempPage = mPageStack.peek();
                        if (tempPage == mCurPage) {
                            break;
                        }
                        PluginPage curPage = mPageStack.pop();
                        curPage.onPagePause();
                        curPage.onPageDestory();
                        mRootView.removeView(curPage.getRootView());
                        LogPluginUtils.i("FINISH PAGE {" + curPage.getClass().getName() + "}");
                    }

                }

                if (requestCode > 0) {
                    mCurPage.setRequestCode(requestCode);
                }
                mCurPage.setIntent(intent);
                mCurPage.onPageNewIntent(intent);
                LogPluginUtils.i("PAGE {" + mCurPage.getClass().getName() + "} onPageNewIntent");
                mCurPage.onPageResume();
                LogPluginUtils.i("PAGE {" + mCurPage.getClass().getName() + "} onResume");
                View rootView = mCurPage.getRootView();
                rootView.setVisibility(View.VISIBLE);
//                if (finishAllOther) {
//                    rootView.setClickable(true);
//                    rootView.setBackgroundColor(page.getPageDefaultBackgroundColor());
//                    mRootView.addView(rootView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                            LayoutParams.MATCH_PARENT));
//                }
            } else {
                page.setVirtualActivity(PluginActivityProxy.this);
                page.setIntent(intent);
                if (mCurPage != null) {
                    mCurPage.onPagePause();
                    mCurPage.setInvokeOnResume(false);
                }
                while (finishAllOther && !mPageStack.isEmpty()) {
                    PluginPage tempPage = mPageStack.pop();
                    tempPage.onPagePause();
                    tempPage.onPageDestory();
                    mRootView.removeView(tempPage.getRootView());
                    LogPluginUtils.i("FINISH PAGE {" + tempPage.getClass().getName() + "}");
                }
                PluginPage tempPage = mCurPage;
                mCurPage = page;
                if (requestCode > 0) {
                    page.setRequestCode(requestCode);
                }
                page.onPageCreate(intent);

                page.onPageResume();
                mCurPage.setInvokeOnResume(true);
                mPageStack.push(page);
                View rootView = page.getRootView();
                rootView.setClickable(true);
                rootView.setBackgroundColor(page.getPageDefaultBackgroundColor());
                mRootView.addView(rootView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
                if (tempPage != null && mCurPage.hideLastPage()) {
                    tempPage.getRootView().setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
    }

    /**
     * 调用外壳程序的方法
     *
     * <pre>
     * DEMO<p>
     * int state = invoke("login", new Class[]{String.class, String.class} ,new Object[]{user, pass});
     * </pre>
     *
     * @param methodName     方法名 parameterTypes
     * @param parameterTypes 参数类型
     * @param params         具体参数
     * @return T
     */
    public <T> T invoke(String methodName, Class<?>[] parameterTypes, Object[] params) {
        return invoke(null, methodName, parameterTypes, params);
    }

    /**
     * 调用外壳程序的方法
     *
     * <pre>
     * DEMO<p>
     * int state = invoke(receiver, "login", new Class[]{String.class, String.class} ,new Object[]{user, pass});
     * </pre>
     *
     * @param receiver       被反射的外壳程序某对象
     * @param methodName     方法名 parameterTypes
     * @param parameterTypes 参数类型
     * @param params         具体参数
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(Object receiver, String methodName, Class<?>[] parameterTypes, Object[] params) {
        try {
            if (receiver == null) {
                receiver = getActivity();
            }
            Method method = getMethod(receiver.getClass(), methodName, parameterTypes);
            if (method != null) {
                method.setAccessible(true);
                return (T) method.invoke(receiver, params);
            }
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
        return null;
    }

    public Method getMethod(Class<?> obj, String methodName, Class<?>[] parameterTypes) {
        Method method = null;
        try {
            method = obj.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception e) {
            try {
                method = obj.getMethod(methodName, parameterTypes);
            } catch (Exception e1) {
                if (obj.getSuperclass() == null) {
                    return null;
                } else {
                    method = getMethod(obj.getSuperclass(), methodName, parameterTypes);
                    if (method != null) {
                        return method;
                    }
                }
            }
        }
        return method;
    }

    /**
     * 获取插件主Page页
     *
     * @return class
     */
    public abstract Class<?> getMainPageClass(Intent intent);

    private void finishAllInner(final boolean force) {
        try {
            LogPluginUtils.i("FINISH PAGE finishAll force= " + force);
            for (int i = mPageStack.size() - 1; i >= 0; i--) {
                PluginPage page = mPageStack.get(i);
                if (!force && !page.canFinish()) {
                    LogPluginUtils.i(page.getClass().getName() + "do not can finish!");
                    continue;
                }
                mPageStack.remove(i);
                page.onPagePause();
                page.setInvokeOnResume(false);
                page.onPageDestory();
                mRootView.removeView(page.getRootView());
                LogPluginUtils.i("FINISH PAGE {" + page.getClass().getName() + "}");
            }
            if (mPageStack.size() == 0) {
                mCurPage = null;
                mRootView.removeAllViews();
                mActivity.finish();
                return;
            } else {
                mCurPage = mPageStack.peek();
                mCurPage.onPageResume();
                mCurPage.setInvokeOnResume(true);
                mCurPage.getRootView().setVisibility(View.VISIBLE);

            }
            if (force && !mActivity.isFinishing()) {
                mPageStack.clear();
                mRootView.removeAllViews();
                mActivity.finish();
            }
        } catch (Exception e) {
            LogPluginUtils.e(e);
            mCurPage = null;
            if (mActivity != null) {
                mActivity.finish();
            }
        }
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================

}
