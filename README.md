## Declaring Dependencies

###### Installation
Add it in your root build.gradle in buildscript block:
```
buildscript {
  repositories {
    maven { url uri('https://raw.githubusercontent.com/sergeymild/nex/master/repo/') }
  }
  
  dependencies {
    classpath "com.nex:nex-plugin:1.0"
  }
}
```
Then apply plugin

``` apply plugin: 'nex-plugin' ```