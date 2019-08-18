# PluginFramework
插件加载框架

通过createFulljarRelease来打一个jar包，在build/intermediates/full_jar/release/createFullJarRelease中会有一个full.jar文件，那个就是我们所要的jar包

  

本文主要记录如何通过插件的方式来实现热更新</br>
</br>
> 我的广告sdk需要加上热更新的功能，经过调研现在主流那些大厂出的热更新框架没有直接支持sdk的，又或许是我没有认真去看因为项目时间赶又加上只有我一个人，所以也没时间去深究，所以就直接用插件的形式来做。
插件开发由于以前做过，所以用插件的形式来做热更新就比较有点思路，如果不了解需要去了解一下Java的类加载机制和Android的类加载机制这些，这里不展开。其实用一句话来概括类加载就是
##### 类加载就是将一个类加载到运行进程的运行内存中，类加载机制只是关键在于不同的类加载器只是加载不同位置的class文件或者dex文件(Android)，仅此而已。
但是这里面可能会涉及到一些双亲委托啊，类加载的安全性啊，隔离性啊这些。

> **其实插件化的原理就是通过一个DexClassLoader类加载器去加载指定位置的dex文件，然后将该Dex文件的类加载到宿主工程的内存中。** 
在具体工作中DexClassLoader可以从.jar和.apk类型的文件内部加载classes.dex文件，所以也就是说，我们可以通过DexClassLoader去加载指定位置的.jar,.apk,.dex文件。这样我们就能将某些功能做在一个插件中，然后放在指定的位置，当程序运行时需要用到这些功能的时候就用DexClassLoader这个类加载器去指定的位置加载类，而当发现写在这个文件中的功能有bug，那只要修改一下bug，然后把原先的文件替换掉就行了，这样就是一个简单的插件化开发。当然，这个插件必须放在服务端这样才能实现在线获取，否则如果写死在手机里面就毫无意义。

#### 加载插件工程中类的基本实现：

```
ClassLoader loader = ClassLoader.getSystemClassLoader();
DexClassLoaderdexLoader = new DexClassLoader(getPluginPath(), getDexPath(), null, loader.getParent());
```

参数注解：

Parameters | 解释
---|---
dexPath | 插件的位置，包含多个路径用File.pathSeparator间隔开
optimizedDirectory | 优化后的dex文件存放目录，不能为null
libraryPath | 目标类中使用的C/C++库的列表,每个目录用File.pathSeparator间隔开; 可以为 null
parent | 该类装载器的父装载器，一般用当前执行类的装载器

如果遇见**java.lang.IllegalArgumentException: optimizedDirectory not readable/writable**错误，那么请看文档：

    A class loader that loads classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.
    
    This class loader requires an application-private, writable directory to cache optimized classes. Use Context.getDir(String, int) to create such a directory:
    
    File dexOutputDir = context.getDir(“dex”,0);
    
    Do not cache optimized classes on external storage. External storage does not provide access controls necessary to protect your application from code injection attacks.

所以getDexPath():


```
Context context=getApplicationContext();//获取Context对象；
File dexOutputDir = context.getDir("dex", 0);
String dexOutputPath = dexOutputDir.getAbsolutePath();
```
通过以上的方法我们能加载固定位置的APK中的dex，我们知道dex中存放的就是已经汇编完成的Android代码。

```
ClassmLoadClass = dexLoader.loadClass(className);
Constructor construct = mLoadClass.getConstructor(new Class[] {});
ObjectmInstance = construct.newInstance(new Object[] {});
```
获得反射类实例，通过以上的方法我们能够获得将获得一个外部dex类的实例，通过这个实例我们能够找到一个调用外部dex代码的入口。

获得class 实例之后，我们可以使用java中放射方法来调用内部具体的方法。


```
public<T> T invoke(String methodName, Class<?>[] paramsTypes, Object[] params) {
    try {
        Method method = getMethod(mLoadClass, methodName, paramsTypes);
        if (method != null) {
            method.setAccessible(true);
            return(T) method.invoke(mInstance, params);
        }
        LogUtils.e("* invoke plugin error *");
        LogUtils.e("invoke plugin method: " + methodName + " is not found!");
		LogUtils.e("**********************************************");
    } catch (Exception e) {
        LogUtils.e(e);
    }
    Return null;
}
```
通过这个以上反射的方法我可以直接通过方法名就能调用外部dex生成的实例内的全部方法。

####   在插件开发中，有两个方面的问题是关键：
###### 1.   宿主工程如何去访问插件工程中的资源</br>
> 我们知道Android的开发除了代码以外还需要大量的资源文件，因为插件内部使用的context是宿主程序的，所以肯定无法查找到插件的资源文件。我们的处理思路是在宿主工程中创建好Resources对象，然后传给插件工程，在插件中直接使用宿主传递过来的Resources来获取资源。生成的Resources我们想指定为到某一个地方去读取资源，所以要通过AssetManager指定路径，AssetManager不能直接访问，所以我们处理的方式是通过AssetManager类反射出里面的方法来读出外部apk内部的资源文件。以下为详细的代码：
```
public static Resources getAPKResources(Context context, String apkPath) throws Exception {
    String PATH_AssetManager = "android.content.res.AssetManager";
    Class assetMagCls = Class.forName(PATH_AssetManager);
    Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
    Object assetMag = assetMagCt.newInstance((Object[]) null);
    Class[] typeArgs = new Class[1];
    typeArgs[0] = String.class;
    Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
    Object[] valueArgs = new Object[1];
    valueArgs[0] = apkPath;
    assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
    Resources res = context.getResources();
    typeArgs = new Class[3];
    typeArgs[0] = assetMag.getClass();
    typeArgs[1] = res.getDisplayMetrics().getClass();
    typeArgs[2] = res.getConfiguration().getClass();
    Constructor resCt = Resources.class.getConstructor(typeArgs);
    valueArgs = new Object[3];
    valueArgs[0] = assetMag;
    valueArgs[1] = res.getDisplayMetrics();
    valueArgs[2] = res.getConfiguration();
    res = (Resources) resCt.newInstance(valueArgs);
    return res;
}
```
通过以上的代码我们可以看到我们可以通过android.content.res.AssetManager这个类将外部apk的资源文件读出并且生产一个Resources的实例，将这个实例set到插件中去，插件的代码就可以直接使用了。列如获得一个drawable直接用以下的方法即可。

mResources便是从宿主传递给插件工程的。

```
public Drawable getDrawable(int resId) {
    return mResources.getDrawable(resId);
}
```

###### 2. 插件工程中的原生四大组件生命周期丢失的问题  
到目前为止我们已经知道了如何加载外部apk的dex代码，并且顺利的调用其内部的代码。但是仅仅这些是不够的，我们知道Android开发中除了代码调用之外，Android基本组件的时候也非常重要，比如activity的生命；因为外部的代码是动态加载的，里面不能生成一个合法的activity。本节主要讲述acticity处理。

> 我们的处理方案比较简单，采用的是代理模式。即在我们的宿主程序里面写一个代理的activity，通过这个代理的activity来控制其相对应的生命周期方法。具体的方式是，我们书写一个单例的管理类来进行初始化加载dex，但需要从外部启动activity时，我们只主要启动代理activity即可，在代理的activity中通过反射的方法调用相对应的生命周期方法。

> 也就是说，我们在宿主工程中启动一个Activity，然后在该Activity的各个声明周期调用插件中相对应的Activity的各个声明周期，这样就能达到控制插件中Activity声明周期的目的了。这种方法很笨，但是对我目前项目是最快捷有效的。

```
@Override
protectedvoid onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AgencyAnzhiUcenter.getAnzhiSDKMidManage().invoke("setActivity", new Class[] { Activity.class }, new Object[] { this });  AgencyAnzhiUcenter.getAnzhiSDKMidManage().invoke("setHandler", new Class[] { Handler.class },  new Object[] { mHandler });
    AgencyAnzhiUcenter.getAnzhiSDKMidManage().invoke("onCreate", new Class[] { Bundle.class },  new Object[] { savedInstanceState });
    rootView = AgencyAnzhiUcenter.getAnzhiSDKMidManage().invoke("getRootView", null, null);
    try {
    	setContentView(rootView);
    	getWindow().setBackgroundDrawable(new ColorDrawable(0x88000000));
	} catch (Exception e) {
		Log.e("TAG", "rootView -------> NULL");
		e.printStackTrace();
	}
}
```

如上代码便是代理activity中oncreate方法的所有代码</br>
第一句和第二句就是将宿主的context和handler传入插件之中。</br>
第三句就是调用插件内部的类的oncreate方法。其他的方法参考oncreate方法。</br>
本处还有一个问题，每一个程序的activity都是有好多个的，但是我们的代理activity是只有一个的如何在启动时候找到对应的插件类呢，我们使用的方法是通过传入事先约定的参数来启动不同的类。

#### 简易原理流程图：
> 根据上诉内容，这个插件的具体流程为首先我们有一个宿主程序，这个程序给我提供了必要的Android 环境，然后我们书写一个单例的管理类，在单例的管理类中我们加载外部的dex，并将外部apk内的资源多出并且set到插件内部的管理中。当我们需要启动内部“acticity”的时候，首先启动外部代理的activity，然后通过特定标签加载出所需要的页面。
![简易原理流程图](https://github.com/HomerLian/PluginFramework/blob/master/docs/%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91%E7%AE%80%E6%98%93%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

到这里为止为主要的插件开发思路，其中还有像插件工程中的AndroidMinifest.xml等这些因为插件apk并不是通过安装这种正常方式被加载到工程中的，所以在这里声明权限，声明组件并不生效。

#### sdk插件更新原理：
篇幅有点长不想再写了，详见我另一份文档,这篇文档是因为当时协调开发一个插件更新后台的时候，后台不理解为什么要有两个版本号，沟通好久之后我就写了篇文档给后台小伙伴看：
[插件更新原理](https://github.com/HomerLian/PluginFramework/blob/master/docs/%E6%8F%92%E4%BB%B6%E7%83%AD%E6%9B%B4%E6%96%B0%E6%96%87%E6%A1%A3.md)
