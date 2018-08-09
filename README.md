一款输入框控件，支持下划线，方框，背景填充三种模式

#### 引入方式

一、添加 JitPack 仓库到项目的 build.gradle

```
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
二、在 module 的build.gradle 文件中导入
```
    dependencies {
	   implementation 'com.github.liurui-36:InputView:1.1.0'
	}
```
最新版本  [![](https://jitpack.io/v/liurui-36/InputView.svg)](https://jitpack.io/#liurui-36/InputView)

#### 效果图
<div align="center">
<img src="https://raw.githubusercontent.com/liurui-36/ReadmeResource/master/images/InputView/mode_underline.jpg" width="200px" /><img src="https://raw.githubusercontent.com/liurui-36/ReadmeResource/master/images/InputView/mode_rect.jpg" width="200px" /><img src="https://raw.githubusercontent.com/liurui-36/ReadmeResource/master/images/InputView/mode_fill.jpg" width="200px" />
</div>
#### 使用

```
    <com.qiqi.inputview.InputView
        android:id="@+id/passwordView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        app:cipherEnable="false"
        app:fillColor="#00ff00"
        app:itemHeight="40dp"
        app:itemSize="8"
        app:itemPadding="8dp"
        app:rectColor="#0000ff"
        app:textColor="#000000"
        app:textSize="18sp"
        app:underLineColor="#ff0000"
        app:itemWidth="30dp" />
```

#### 属性介绍
```
    <attr name="itemSize" format="integer" />   // 输入的个数
    <attr name="itemPadding" format="dimension" />  // 输入的间距
    <attr name="itemWidth" format="dimension" />    // 每一项的宽度
    <attr name="itemHeight" format="dimension" />   // 每一项的高度
    <attr name="border" format="dimension" />   // 边框或下划线的宽度
    <attr name="rectColor" format="color" />    // 边框颜色
    <attr name="underLineColor" format="color" />   // 下划线颜色
    <attr name="fillColor" format="color" />    // 填充颜色
    <attr name="textSize" format="dimension" /> // 文字大小
    <attr name="textColor" format="color" />    // 文字颜色
    <attr name="cursorFlashTime" format="integer" />    // 光标闪动时间间隔
    <attr name="isCursorEnable" format="boolean" /> // 是否开启光标
    <attr name="cipherEnable" format="boolean" />   // 是否开启密文（内容用  *  代替）
    <attr name="cursorColor" format="color" />  // 光标颜色
    <attr name="mode" format="enum">    // 模式
        <enum name="underline" value="0" /> // 下划线
        <enum name="rect" value="1" />  // 边框
        <enum name="fill" value="2" />  // 北京填充
    </attr>
```

#### 获取输入的内容

```
    InputView inputView = (InputView) findViewById(R.id.passwordView);
    Toast.makeText(MainActivity.this, inputView.getText(), Toast.LENGTH_SHORT).show();
```
