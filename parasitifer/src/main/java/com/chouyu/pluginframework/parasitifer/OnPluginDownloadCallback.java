/*
 * File Name: OnPluginDownloadCallback.java 
 * History:
 * Created by Administrator on 2016年2月17日
 */
package com.chouyu.pluginframework.parasitifer;

public interface OnPluginDownloadCallback {
    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Methods
    // ==========================================================================
    /**
     * 获取插件下载地址
     * 
     * @return String
     */
    public String getPluginDownloadUrl();

    /**
     * 插件下载中
     * 
     * @param currentByte
     *            当前已读字节数
     */
    public void onPluginDownloadProgress(long currentByte);

    /**
     * 插件下载完成，检查插件合法性
     * 
     * @param path
     *            插件下载路径
     * @return 如果合法返回true，否则返回false
     */
    public boolean checkPluginDownloadFileValid(String path);

    /**
     * 插件下载失败
     */
    public void onPluginDownloadFailed();

    /**
     * 检查插件更新
     * 
     * @param oldVersion
     *            本地插件APK的versionCode
     * @param oldPluginPath
     *            本地插件APK路径
     * @return 如果有更新返回true，否则返回false
     */
    public boolean onCheckPluginUpdate(int oldVersion, String oldPluginPath);
    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================

}
