/*
 * File Name: OnPluginLoadCallback.java 
 * History:
 * Created by Administrator on 2016年2月3日
 */
package com.lianwh.pluginframework.parasitifer;

import android.content.pm.PackageInfo;


public interface OnPluginLoadCallback {
    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Methods
    // ==========================================================================

    /**
     * 是否为插件开发模式
     * 
     * @return 如果为开发模式返回true，则每次程序运行时都会重新加载assets下文件名为{@link #getLocalPluginFileName()}的插件； 否则返回false
     */
    public boolean isDebugMode();
    

    /**
     * 当前主程序assets或res/raw下的插件文件名
     * 
     * @return 插件文件名, 正式环境时返回null(通常在{@link #isDebugMode()}为true或程序需要带着插件一起发布时才返回相应的名称)
     */
    public String getLocalPluginFileName();


    public boolean checkLocalPluginSupportVer(PackageInfo info);
    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================

}
