/*
 * File Name: PluginBaseActivity.java 
 * History:
 * Created by Administrator on 2016年1月29日
 */
package com.chouyu.pluginframework.parasitifer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <b> 使用范围 主程序(非插件工程)
 * <p>
 * 主程序显示插件的Activity需继承该类
 * 
 * @author Wang xiaoqi
 * @version 1.0
 */
public abstract class PluginBaseActivity extends Activity {
    // ==========================================================================
    // Constants
    // ==========================================================================
    // ==========================================================================
    // Fields
    // ==========================================================================
    private int mUIThreadId;

    private LayoutInflater mInflater;

    /**
     * 内置handler
     */
    private Handler mDefaultHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            onHandleMessage(msg);
        }

    };

    // ==========================================================================
    // Constructors
    // ==========================================================================

    // ==========================================================================
    // Getters
    // ==========================================================================
    /**
     * 获取UI线程ID
     * 
     * @return UI线程ID
     */
    public int getUIThreadId() {
        return mUIThreadId;
    }

    /**
     * 获取内置handler
     * 
     * @return 内置handler
     */
    public Handler getDefaultHandler() {
        return mDefaultHandler;
    }

    // ==========================================================================
    // Setters
    // ==========================================================================

    // ==========================================================================
    // Methods
    // ==========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUIThreadId = Process.myTid();
        super.onCreate(savedInstanceState);
        initInflater();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mUIThreadId = Process.myTid();
        super.onNewIntent(intent);
    }

    private boolean initInflater() {
        try {
            mInflater = LayoutInflater.from(this);
        } catch (Throwable tr) {
            mInflater = null;
        }
        return null != mInflater;
    }

    /**
     * 向内置handler的消息队列中增加一个任务。该任务会在将来的某一时刻在UI线程执行。
     * 
     * @param r
     *            任务
     */
    public boolean post(Runnable r) {
        return mDefaultHandler.post(r);
    }

    /**
     * 向内置handler的消息队列中增加一个任务。该任务会在指定延时后的某一时刻在UI线程执行。
     * 
     * @param r
     *            任务
     * @param delayMillis
     *            延时的毫秒数
     */
    public boolean postDelayed(Runnable r, long delayMillis) {
        return mDefaultHandler.postDelayed(r, delayMillis);
    }

    /**
     * 向内置handler发送消息。将会触发{@link #onHandleMessage(Message msg)}。
     * 
     * @param what
     */
    public void sendMessage(int what) {
        mDefaultHandler.sendEmptyMessage(what);
    }

    /**
     * 向内置handler发送消息。将会触发{@link #onHandleMessage(Message msg)}。
     * 
     * @param what
     * @param obj
     */
    public void sendMessage(int what, Object obj) {
        mDefaultHandler.obtainMessage(what, obj).sendToTarget();
    }

    /**
     * 向内置handler发送消息。将会触发{@link #onHandleMessage(Message msg)}。
     * 
     * @param what
     * @param arg1
     * @param arg2
     */
    public void sendMessage(int what, int arg1, int arg2) {
        mDefaultHandler.obtainMessage(what, arg1, arg2).sendToTarget();
    }

    /**
     * 向内置handler发送消息。将会触发{@link #onHandleMessage(Message msg)}。
     * 
     * @param what
     * @param arg1
     * @param arg2
     * @param obj
     */
    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        mDefaultHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    /**
     * 从队列中移除回调
     * 
     * @param r
     */
    public void removeCallback(Runnable r) {
        mDefaultHandler.removeCallbacks(r);
    }

    /**
     * 内置handler处理到某个消息时，该方法被回调。子类实现该方法以定义对消息的处理。
     * 
     * @param msg
     *            消息
     */
    public abstract void onHandleMessage(Message msg);

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param resId
     *            Toast内容的资源id
     * @param duration
     *            Toast的持续时间
     */
    public void showToastSafe(final int resId, final int duration) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(getBaseContext(), resId, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), resId, duration).show();
                }
            });
        }
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
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(getBaseContext(), text, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), text, duration).show();
                }
            });
        }
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
        final CharSequence cs = getString(resId, formatArgs);
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            Toast.makeText(getBaseContext(), cs, duration).show();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), cs, duration).show();
                }
            });
        }
    }

    /**
     * 对showDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param id
     *            Dialog的id
     */
    public void showDialogSafe(final int id) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            if (!isFinishing()) {
                try {
                    showDialog(id);
                } catch (IllegalArgumentException e) {
                    // android2.1会在dialog为null时抛出异常，捕获该异常
                    LogPluginUtils.e(e);
                } catch (Exception e) {
                    LogPluginUtils.e(e);
                }
            }
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        try {
                            showDialog(id);
                        } catch (IllegalArgumentException e) {
                            // android2.1会在dialog为null时抛出异常，捕获该异常
                            LogPluginUtils.e(e);
                        } catch (Exception e) {
                            LogPluginUtils.e(e);
                        }
                    }
                }
            });
        }
    }

    // /**
    // * 对showDialog的简易封装。线程安全，可以在非UI线程调用。
    // * @param id Dialog的id
    // * @param args 传入的参数
    // */
    // public void showDialogSafe(final int id, final Bundle args) {
    // if (Process.myTid() == mUIThreadId) {
    // // 调用在UI线程
    // if (!isFinishing()) {
    // try {
    // showDialog(id, args);
    // } catch (IllegalArgumentException e) {
    // // android2.1会在dialog为null时抛出异常，捕获该异常
    // LogUtils.e(e);
    // } catch (Exception e) {
    // LogUtils.e(e);
    // }
    // }
    // } else {
    // // 调用在非UI线程
    // post(new Runnable() {
    // @Override
    // public void run() {
    // if (!isFinishing()) {
    // try {
    // showDialog(id, args);
    // } catch (IllegalArgumentException e) {
    // // android2.1会在dialog为null时抛出异常，捕获该异常
    // LogUtils.e(e);
    // } catch (Exception e) {
    // LogUtils.e(e);
    // }
    // }
    // }
    // });
    // }
    // }

    /**
     * 对showDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param dialog
     *            Dialog实例
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
     * 对dismissDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param id
     *            Dialog的id
     */
    public void dismissDialogSafe(final int id) {
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            if (!isFinishing()) {
                try {
                    dismissDialog(id);
                } catch (IllegalArgumentException e) {
                    // android2.1会在dialog为null时抛出异常，捕获该异常
                    LogPluginUtils.e(e);
                } catch (Exception e) {
                    LogPluginUtils.e(e);
                }
            }
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        try {
                            dismissDialog(id);
                        } catch (IllegalArgumentException e) {
                            // android2.1会在dialog为null时抛出异常，捕获该异常
                            LogPluginUtils.e(e);
                        } catch (Exception e) {
                            LogPluginUtils.e(e);
                        }
                    }
                }
            });
        }
    }

    /**
     * 对dismissDialog的简易封装。线程安全，可以在非UI线程调用。
     * 
     * @param dialog
     *            Dialog实例
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

    public void refreshAdapterViewSafe(final BaseAdapter adapter) {
        if (null == adapter) {
            return;
        }
        if (Process.myTid() == mUIThreadId) {
            // 调用在UI线程
            adapter.notifyDataSetChanged();
        } else {
            // 调用在非UI线程
            post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 判断intent是否有对应程序支持
     * 
     * @param intent
     * @return
     */
    public boolean isIntentAvailable(Intent intent) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 检测一个Motion事件是否在指定View的区域内
     * 
     * @param v
     *            指定的View
     * @param ev
     *            Motion事件
     * @return 在区域内返回true，否则返回false
     */
    public static boolean isMotionEventInView(View v, MotionEvent ev) {
        if (v == null || ev == null) {
            return false;
        }
        int[] coord = new int[2];

        v.getLocationOnScreen(coord);
        final int absLeft = coord[0];
        final int absRight = coord[0] + v.getWidth();
        final int absTop = coord[1];
        final int absBottom = coord[1] + v.getHeight();

        return (ev.getX() >= absLeft && ev.getX() < absRight && ev.getY() >= absTop && ev.getY() < absBottom);
    }

    /**
     * 将一个Motion事件的坐标转换为相对于某个View的坐标
     * 
     * @param v
     *            指定的View
     * @param srcEvent
     *            Motion事件
     * @param inView
     *            如果Motion事件发生在指定的View内，该引用将被赋为true，反之false
     * @return 返回转换后的结果
     */
    public static MotionEvent getRelativeMotionEventInView(View v, MotionEvent srcEvent, AtomicBoolean inView) {
        if (v == null || srcEvent == null) {
            return null;
        }
        int[] coord = new int[2];

        v.getLocationOnScreen(coord);
        final int absLeft = coord[0];
        final int absRight = coord[0] + v.getWidth();
        final int absTop = coord[1];
        final int absBottom = coord[1] + v.getHeight();

        inView.set(srcEvent.getX() >= absLeft && srcEvent.getX() < absRight && srcEvent.getY() >= absTop
                && srcEvent.getY() < absBottom);

        MotionEvent relativeEvent = MotionEvent.obtain(srcEvent);
        relativeEvent.offsetLocation(-absLeft, -absTop);

        return relativeEvent;
    }

    /**
     * 根据资源上下文，将dip值转换为pixel值
     * 
     * @param dipValue
     *            dip值
     * @return pixel值
     */
    public int dip2px(float dipValue) {
        return dip2px(this, dipValue);
    }

    /**
     * 根据资源上下文，将pixel值转换为dip值
     * 
     * @param pxValue
     *            pixel值
     * @return dip值
     */
    public int px2dip(float pxValue) {
        return px2dip(this, pxValue);
    }

    /**
     * 获取资源图片
     * 
     * @param resId
     *            资源id
     * @return 图片
     */
    public Drawable getResDrawable(int resId) {// 5.0方法冲突
        return getResources().getDrawable(resId);
    }

    /**
     * 获取资源色值
     * 
     * @param resId
     *            资源id
     * @return 色值
     */
    public int getColorRes(int resId) {
        return getResources().getColor(resId);
    }

    /**
     * 获取资源像素值
     * 
     * @param resId
     *            资源id
     * @return 像素值
     */
    public int getDimensionPixel(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    /**
     * 根据指定的layout索引，创建一个View
     * 
     * @param resId
     *            指定的layout索引
     * @return 新的View
     */
    public View inflate(int resId) {
        return inflate(resId, null, false);
    }

    /**
     * 根据指定的layout索引，创建一个View
     * 
     * @param resId
     *            指定的layout索引
     * @param parent
     *            父view 没有可以传null
     * @param attachToRoot
     *            视图生成后是否直接加入到parent中
     * @return 新的View
     */
    public View inflate(int resId, ViewGroup parent, boolean attachToRoot) {
        if (null == mInflater && !initInflater()) {
            return null;
        }
        return mInflater.inflate(resId, parent, attachToRoot);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}
