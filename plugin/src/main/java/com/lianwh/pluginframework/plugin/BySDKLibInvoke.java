/*
 * File Name: ByLibInvoke.java 
 * History:
 * Created by Administrator on 2015年11月23日
 */
package com.lianwh.pluginframework.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <b> 使用范围 主程序、插件程序
 * <p>
 * 主程序中供插件调用的方法和插件中供主程序调用的方法加 @BySDKLibInvoke
 * 
 * @author lianwh
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BySDKLibInvoke {
    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Methods
    // ==========================================================================

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================

}
