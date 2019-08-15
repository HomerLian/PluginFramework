/*
 * File Name: PluginManager.java 
 * History:
 * Created by Administrator on 2016年2月3日
 */
package com.lianwh.pluginframework.parasitifer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import dalvik.system.DexClassLoader;

public class PluginManager {
    // ==========================================================================
    // Constants
    // ==========================================================================
    // ==========================================================================
    // Fields
    // ==========================================================================
    private static final String VER = "2.0";
    private static PluginManager sInstance;
    private Context mContext;
    private Map<String, Class<?>> mClassMap = new HashMap<String, Class<?>>();
    private Map<String, Object> mInstanceMap = new HashMap<String, Object>();
    private Object mLock = new Object();
    private Handler mHandler;
    private Map<String, ReentrantLock> mPkgLock = new HashMap<String, ReentrantLock>();
    private Map<String, OnLoaderListener> mLoaderListeners = new HashMap<String, OnLoaderListener>();

    // ==========================================================================
    // Constructors
    // ==========================================================================

    private PluginManager(Context context) {
        mContext = context.getApplicationContext();
        new MyThreadHandler().start();
    }

    // ==========================================================================
    // Getters
    // ==========================================================================
    public synchronized static PluginManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PluginManager(context);
        }
        return sInstance;
    }

    public boolean isContainer(String pluginPkgName) {
        return mClassMap.containsKey(pluginPkgName);
    }

    public Object getPluginInstance(String pluginPkgName) {
        return mInstanceMap.get(pluginPkgName);
    }

    public Class<?> getPluginLoaderClass(String pluginPkgName) {
        return mClassMap.get(pluginPkgName);
    }

    public View getPluginRootView(String pluginPkgName) {
    	return invoke(pluginPkgName, "getRootView", null, null);
    }
    // ==========================================================================
    // Setters
    // ==========================================================================
    public void setReloadPlugin(String pluginPkgName) {
        synchronized (mLock) {
            mClassMap.remove(pluginPkgName);
            mInstanceMap.remove(pluginPkgName);
        }
    }

    public void setLoaderListener(String pkgName, OnLoaderListener Listener) {
        synchronized (mLoaderListeners) {
            mLoaderListeners.put(pkgName, Listener);
        }
    }

    public void removeLoaderListener(String pkgName) {
        synchronized (mLoaderListeners) {
            mLoaderListeners.remove(pkgName);
        }
    }

    // ==========================================================================
    // Methods
    // ==========================================================================
    
    public View createMainView(Activity activity, String pluginPkgName) {
    	invoke(pluginPkgName, "createMainView", new Class[]{Activity.class}, new Object[]{activity});
    	return invoke(pluginPkgName, "getRootView", null, null);
    }
    /**
     * 根据插件包名获取插件存放位置
     * 
     * @param pluginPkgName
     *            插件包名
     * @return 插件全路径
     */
    public String getPluginPath(String pluginPkgName) {
        return mContext.getDir("plugin", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginPkgName + ".apk";
    }

    public String getPluginSoDir(String pluginPkgName) {
        return mContext.getDir("plibs", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginPkgName + "/";
    }

    /**
     * 根据插件包名获取插件dex文件存放位置
     * 
     * @param pluginPkgName
     *            插件包名
     * @return 插件dex全路径
     */
    public String getPluginDexPath(String pluginPkgName) {
        return mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginPkgName + ".dex";
    }

    private String getDexDir() {
        return mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
    }

    /**
     * 根据包名删除某插件
     * 
     * @param pluginPkgName
     *            插件包名
     * @return 如果删除成功返回true，否则返回false
     */
    public boolean delPluginFile(String pluginPkgName) {
        synchronized (mLock) {
            File pluginFile = new File(getPluginPath(pluginPkgName));
            File dexFile = new File(getPluginDexPath(pluginPkgName));
            dexFile.delete();
            return pluginFile.delete();
        }
    }

    public void lockCtrl(String pkgName) {
        ReentrantLock lock;
        synchronized (mPkgLock) {
            lock = mPkgLock.get(pkgName);
            if (lock == null) {
                lock = new ReentrantLock();
                mPkgLock.put(pkgName, lock);
            }
        }
        LogPluginUtils.v("lock");
        lock.lock();

    }

    public void unlockCtrl(String pkgName) {
        ReentrantLock lock;
        synchronized (mPkgLock) {
            lock = mPkgLock.get(pkgName);
        }
        if (lock != null) {
            try {
                lock.unlock();
                LogPluginUtils.v("unlock");
            } catch (Exception e) {
                // LogUtils.e(e);
            }
        }

    }

    public boolean isLock(String pkgName) {
        synchronized (mPkgLock) {
            ReentrantLock lock = mPkgLock.get(pkgName);
            if (lock != null && lock.isLocked()) {
                return true;
            }
        }
        return false;

    }

    /**
     * 加载插件
     * 
     * @param pluginPkgName
     *            插件包名
     * @param loadCallback
     *            插件加载回调callback
     * @param downCallback
     *            插件下载或更新callback
     * @return 如果加载成功返回true，否则返回false
     */
    public boolean loadPlugin(final String pluginPkgName, OnPluginLoadCallback loadCallback,
            final OnPluginDownloadCallback downCallback) {
        return loadPlugin(pluginPkgName, loadCallback, downCallback, null);
    }

    public boolean loadPlugin(final String pluginPkgName, OnPluginLoadCallback loadCallback,
            final OnPluginDownloadCallback downCallback, final String entryClassName) {
        try {
            LogPluginUtils.v("Plugin ver:" + VER);
            if (pluginPkgName == null) {
                throw new NullPointerException("插件包名不能为空");
            }
            OnLoaderListener loaderActivity = mLoaderListeners.get(pluginPkgName);
            lockCtrl(pluginPkgName);
            synchronized (mLock) {
                if (mClassMap.containsKey(pluginPkgName)) {
                    LogPluginUtils.w("is exists loaded dex!");
                    if (loaderActivity != null) {
                        loaderActivity.onLoaderCompleted(pluginPkgName);
                    }
                    return true;
                }
            }
            if (loadCallback != null) {
                if (loadCallback.getLocalPluginFileName() != null) {
                    boolean state = loadPluginFromRaw(pluginPkgName, loadCallback.isDebugMode(), loadCallback);
                    LogPluginUtils.e("调试模式：" + loadCallback.isDebugMode() + "，本地插件获取 " + (state ? "成功" : "失败!!"));
                    if (loadCallback.isDebugMode()) {
                        showToastSafe("调试模式，本地插件获取 " + (state ? "成功" : "失败!!"));
                    }
                }
            }
            File pluginFile = new File(getPluginPath(pluginPkgName));
            if (!pluginFile.exists()) {
                downloadPlugin(pluginPkgName, downCallback);
                if (!pluginFile.exists()) {
                    LogPluginUtils.e(new Exception("无法找到插件"));
                    return false;
                }
            }
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            final PackageInfo pkgInfo = mContext.getPackageManager().getPackageArchiveInfo(getPluginPath(pluginPkgName),
                    PackageManager.GET_META_DATA);
            DexClassLoader dexLoader = new DexClassLoader(getPluginPath(pluginPkgName), getDexDir(), getPluginSoDir(pluginPkgName),
                    loader.getParent());
            String className = null;
            if (entryClassName != null) {
                className = entryClassName;
            } else if (pkgInfo != null && pkgInfo.activities != null && pkgInfo.activities.length > 0) {
                className = pkgInfo.activities[0].name;
            } else if (pkgInfo != null && pkgInfo.applicationInfo != null && pkgInfo.applicationInfo.metaData != null) {
                Bundle bundle = pkgInfo.applicationInfo.metaData;
                className = bundle.getString("plugin_launcher");
            } else {
                LogPluginUtils.e("插件入口找不到");
            }
            LogPluginUtils.i("load plguin class: " + className);
            LogPluginUtils.i("插件加载成功，路径：" + getPluginPath(pluginPkgName));
            synchronized (mLock) {
                Class<?> loadClass = dexLoader.loadClass(className);
                mClassMap.put(pluginPkgName, loadClass);
                Constructor<?> construct = loadClass.getConstructor(new Class[] {});
                Object instance = construct.newInstance(new Object[] {});// 获得反射类实例
                mInstanceMap.put(pluginPkgName, instance);
                Resources res = getAPKResources(getPluginPath(pluginPkgName), mContext);
                invoke(pluginPkgName, "setApplicationContext", new Class[] { Context.class },
                        new Object[] { mContext });
                invoke(pluginPkgName, "setResources", new Class[] { Resources.class }, new Object[] { res });
                if (downCallback != null) {
                    new Thread() {
                        public void run() {
                            downCallback.onCheckPluginUpdate(pkgInfo.versionCode, getPluginPath(pluginPkgName));
                        }
                    }.start();
                }
            }

            if (loaderActivity != null) {
                loaderActivity.onLoaderCompleted(pluginPkgName);
            }
        } catch (Exception e) {
            LogPluginUtils.e(e);
            File pluginFile = new File(getPluginPath(pluginPkgName));
            if (pluginFile != null && pluginFile.exists())
                pluginFile.delete();
            File dexFile = new File(getPluginDexPath(pluginPkgName));
            if (dexFile != null && dexFile.exists())
                dexFile.delete();
            return false;
        } finally {
            unlockCtrl(pluginPkgName);
        }
        return true;

    }

    private void downloadPlugin(final String pluginPkgName, final OnPluginDownloadCallback downCallback) {
        if (downCallback != null) {
            File tempFile = new File(getPluginPath(pluginPkgName) + ".ing");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tempFile);
                URL url = new URL(downCallback.getPluginDownloadUrl());
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                connect.setUseCaches(false);
                connect.setConnectTimeout(20000);
                connect.setRequestMethod("GET");
                connect.connect();
                InputStream is = connect.getInputStream();
                byte[] buffer = new byte[4096];
                int len = 0;
                int currentByte = 0;
                LogPluginUtils.v("download plugin start");
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    currentByte += len;
                    downCallback.onPluginDownloadProgress(currentByte);
                }
                fos.close();
                LogPluginUtils.v("download plugin end totalByte=" + currentByte);

                boolean checkOk = downCallback.checkPluginDownloadFileValid(tempFile.getAbsolutePath());
                if (checkOk) {
                    File pluginFile = new File(getPluginPath(pluginPkgName));
                    pluginFile.delete();
                    tempFile.renameTo(pluginFile);
                    LogPluginUtils.e("通过网络获取插件成功");
                } else {
                    tempFile.delete();
                }
            } catch (Exception e) {
                LogPluginUtils.e(e);
                LogPluginUtils.e("通过网络获取插件失败");
                tempFile.delete();
                downCallback.onPluginDownloadFailed();
                if (fos != null)
                    try {
                        fos.close();
                    } catch (Exception e1) {
                    }
                return;
            }

        }
    }

    private Resources getAPKResources(String apkPath, Context activity) throws Exception {
        String PATH_AssetManager = "android.content.res.AssetManager";
        Class<?> assetMagCls = Class.forName(PATH_AssetManager);
        Constructor<?> assetMagCt = assetMagCls.getConstructor((Class[]) null);
        Object assetMag = assetMagCt.newInstance((Object[]) null);
        Class<?>[] typeArgs = new Class[1];
        typeArgs[0] = String.class;
        Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
        Object[] valueArgs = new Object[1];
        valueArgs[0] = apkPath;
        assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
        Resources res = activity.getResources();
        typeArgs = new Class[3];
        typeArgs[0] = assetMag.getClass();
        typeArgs[1] = res.getDisplayMetrics().getClass();
        typeArgs[2] = res.getConfiguration().getClass();
        Constructor<?> resCt = Resources.class.getConstructor(typeArgs);
        valueArgs = new Object[3];
        valueArgs[0] = assetMag;
        valueArgs[1] = res.getDisplayMetrics();
        valueArgs[2] = res.getConfiguration();
        res = (Resources) resCt.newInstance(valueArgs);
        return res;
    }

    private boolean loadPluginFromRaw(String pluginPkgName, boolean isDebugMode, OnPluginLoadCallback callback) {
        try {
            InputStream is = null;
            try {
                is = mContext.getResources().openRawResource(mContext.getResources()
                        .getIdentifier(callback.getLocalPluginFileName(), "raw", mContext.getPackageName()));
            } catch (Exception e) {
                is = mContext.getResources().getAssets().open(callback.getLocalPluginFileName());
            }
            FileOutputStream fos = new FileOutputStream(getPluginPath(pluginPkgName) + ".raw");
            int len = 0;
            byte[] buffer = new byte[4096];
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();

            File rawFile = new File(getPluginPath(pluginPkgName) + ".raw");
            PackageManager manager = mContext.getPackageManager();
            PackageInfo assetpackageInfo = manager.getPackageArchiveInfo(rawFile.getAbsolutePath(),
                    PackageManager.GET_META_DATA);
            int insideCode = 0;
            int assetCode = assetpackageInfo.versionCode;
            File pluginFile = new File(getPluginPath(pluginPkgName));
            if (pluginFile.exists()) {
                PackageInfo packageInfo = manager.getPackageArchiveInfo(pluginFile.getAbsolutePath(),
                        PackageManager.GET_META_DATA);
                insideCode = packageInfo.versionCode;
            }
            if (isDebugMode || assetCode > insideCode && callback.checkLocalPluginSupportVer(assetpackageInfo)) {
                pluginFile.delete();
                rawFile.renameTo(pluginFile);
                LogPluginUtils.d(" plugin in app_plugin ver=" + insideCode + ", plugin in assets ver=" + assetCode
                        + ", replace");
            } else {
                rawFile.delete();
                LogPluginUtils.d(" plugin in app_plugin ver=" + insideCode + ", plugin in assets ver=" + assetCode
                        + ", no replace");
            }
            return true;
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
        return false;
    }

    /**
     * 反射方法的调用
     */
    public <T> T invoke(String pluginPkgName, String methodName, Class<?>[] paramsTypes, Object[] params) {
        try {
            Method method = getMethod(mClassMap.get(pluginPkgName), methodName, paramsTypes);
            if (method != null) {
                method.setAccessible(true);
                return (T) method.invoke(mInstanceMap.get(pluginPkgName), params);
            }
            LogPluginUtils.e(new Exception("invoke plugin method: " + methodName + " is not found!"));
        } catch (Exception e) {
            LogPluginUtils.e(e);
        }
        return null;
    }

    protected Method getMethod(Class<?> obj, String methodName, Class<?>[] parameterTypes) {
        if (obj == null) {
            return null;
        }
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

    private void showToastSafe(final String toast) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        new Thread() {

            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

    private class MyThreadHandler extends Thread {

        public void run() {
            Looper.prepare();
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                }

            };
            Looper.loop();
        }
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
    public interface OnLoaderListener {
        public void onLoaderCompleted(String pkgName);
    }
}
