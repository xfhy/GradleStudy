class Config {

    static applicationId = 'com.xfhy.gradledemo'
    static appName = 'GradleDemo'
    static compileSdkVersion = 29
    static buildToolsVersion = '29.0.2'
    static minSdkVersion = 22
    static targetSdkVersion = 29
    static versionCode = 1
    static versionName = '1.0.0'

    static kotlin_version = '1.3.41'
    static appcompat_androidx_version = '1.0.2'
    static recyclerview_androidx_version = '1.0.0'
    static leakcanary_version = '1.6.3'
    static design_version = '1.0.0'
    static multidex_version = '1.0.2'
    static constraint_version = '1.1.3'

    static depConfig = [
            support      : [
                    appcompat_androidx   : "androidx.appcompat:appcompat:$appcompat_androidx_version",
                    recyclerview_androidx: "androidx.recyclerview:recyclerview:$recyclerview_androidx_version",
                    design               : "com.google.android.material:material:$design_version",
                    multidex             : "com.android.support:multidex:$multidex_version",
                    constraint           : "com.android.support.constraint:constraint-layout:$constraint_version",
            ],
            kotlin       : "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version",
            leakcanary   : [
                    android         : "com.squareup.leakcanary:leakcanary-android:$leakcanary_version",
                    android_no_op   : "com.squareup.leakcanary:leakcanary-android-no-op:$leakcanary_version",
                    support_fragment: "com.squareup.leakcanary:leakcanary-support-fragment:$leakcanary_version",
            ],
    ]
    
}