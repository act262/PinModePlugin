[ ![Download](https://api.bintray.com/packages/act262/maven/pin-plugin/images/download.svg) ](https://bintray.com/act262/maven/pin-plugin/_latestVersion)

### pin模式
起初看到微信架构的演进里提到`pin`模式，就是把各个模块代码单独出来。为什么不用module来做？因为module多了以后编译速度就很慢了，所以微信利用sourceSets这个属性把不同模块下的代码组织起来，相当于还是只有一个module，编译速度不受影响。

### 使用
#### 在项目根`build.gradle`下配置
```groovy
buildscript {
    repositories {
        // 用于本地测试使用
        mavenLocal()
        
        jcenter()
    }
    dependencies {
        // ...
        classpath 'com.jfz.plugin:pin-plugin:<latest-version>'
    }
}
```

在Android项目的build.gradle中生效插件
```groovy
// application
apply plugin: 'com.android.application'
// or library
apply plugin: 'com.android.library'

apply plugin: 'com.jfz.plugin.pin'
```

如果需要全部项目生效,需要在根build.gradle中`afterEvaluate`配置
```groovy
subprojects {
    it.afterEvaluate {
        it.apply plugin: 'com.jfz.plugin.pin'
    }
}
```

#### 修改Android代码结构

然后代码结构参考demo上的结构即可


---

### 实现
gradle 默认配置中可以对源码位置配置修改
```groovy

android{
    // ...
    
    def dir = "xxx/yyy"
    sourceSets {

        main {
            assets.srcDirs "${dir}/assets"
            java.srcDirs "${dir}/java"
            res.srcDirs "${dir}/res"
            aidl.srcDirs "${dir}/aidl"
            manifest.srcFile "$dir/AndroidManifest.xml"
        }
    }
}
```

但是每个sourceSets只能设置一个AndroidManifest文件，为了能够实现把AndroidManifest文件也可以像其他资源分散到具体目录下，就需要手动把这些合并起来。

#### AndroidManifest合并
 一开始想到的是在sourceSets里面把指定的AndroidManifest文件hack进去，
 `ManifestProcessorTask`
 `MergeManifests`
 `ProcessManifest`
 
 操作下来还是不行，
 
 最后参考了AndroidManifest文件合并的代码实现(`ManifestMerger2`)。
 
 最后项目结构如图
 ![k](sourcesSets.jpg)