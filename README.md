<div align="center">
  <img src="https://github.com/JDevZone/FillProgressLayout/blob/master/lib_logo.png" alt="" width="80px" height="80px">
</div>

<h3 align="center">FillProgressLayout</h3>
<h4 align="center" >:fire:A simple and flexible Fill Progress Layout written in Kotlin:fire:</h4>

--------------

<a href="https://github.com/JDevZone/FillProgressLayout">
<img align="left" src="https://github.com/JDevZone/FillProgressLayout/blob/master/sample.gif" width="100%" height="10%" /></a>



[![](https://jitpack.io/v/JDevZone/FillProgressLayout.svg)](https://jitpack.io/#JDevZone/FillProgressLayout)
[![GitHub license](https://img.shields.io/github/license/JDevZone/FillProgressLayout.svg?style=flat)](https://github.com/JDevZone/FillProgressLayout/blob/master/LICENSE)


---------------------------
### Installation

1. Add it in your root build.gradle at the end of repositories:
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```


2. Add the dependency in app gradle

```groovy
	dependencies {
	        implementation 'com.github.JDevZone:FillProgressLayout:{latest_version}'
	}
```
### Basic usage

> As `FillProgressLayout` is direct child of `LinearLayout` you can replace LinearLayout with it as follows

```xml
<com.devzone.fillprogresslayout.FillProgressLayout
            android:id="@+id/fillL"
            android:layout_margin="30dp"
            app:fpl_backgroundColor="@color/colorRedTrans"
            app:fpl_progressColor="@color/colorGreenTrans"
            app:fpl_isRounded="false"
            app:fpl_progress="0"
            app:fpl_progressDuration="3000"
            app:fpl_progressDirection="left_to_right"
            app:fpl_shouldRestart="false"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
        <--childviews-->
    </com.devzone.fillprogresslayout.FillProgressLayout>
``` 
### Alternatively 
> You can use `FillProgressLayout` as background for other layouts. 

```xml
<RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
  
        <--as background for AppCompatTextView-->
        <com.devzone.fillprogresslayout.FillProgressLayout
                android:layout_alignBottom="@+id/tv"
                android:layout_alignParentTop="true"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"/>

        <androidx.appcompat.widget.AppCompatTextView
                android:id="@+id/tv"
                android:text="@string/app_name"
                android:gravity="center"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"/>
    </RelativeLayout>
``` 

### Customisation

Here are the attributes you can specify through XML or related setters programatically:

* `fpl_backgroundColor` - Set background color.
* `fpl_progressColor` - Set progress color.
* `fpl_isRounded` - Set true if you need rounded corners.
* `fpl_roundedCornerRadius` - Set radius for round corners.
* `fpl_progress` - Set current progress.
* `fpl_progressDuration` - Set fill duration.
* `fpl_shouldRestart` - Set if progress filling should restart from 0.
* `fpl_progressDirection` - Set fill direction. i.e. `left_to_right`,`right_to_left`,`top_to_bottom` or `bottom_to_top`


### 📄 License

Checkable TextView is released under the MIT license.
See [LICENSE](./LICENSE) for details.


          

