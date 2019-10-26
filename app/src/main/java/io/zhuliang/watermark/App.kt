package io.zhuliang.watermark

import android.app.Application
import android.graphics.Color
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.zhuliang.photopicker.PhotoLoader
import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.ThemeConfig


/**
 * <pre>
 * author : ZhuLiang
 * time   : 2019/08/15
 * desc   :
 * version: 1.0
</pre> *
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val colorAccent = ContextCompat.getColor(this, R.color.colorAccent)
        val colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary)
        val colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(colorAccent)
                .bottomBarBackgroundColor(colorPrimary)
                .bottomBarTextColor(Color.WHITE)
                .arrowDropColor(Color.WHITE)
                .checkboxColor(colorAccent)
                .actionBarBackground(colorPrimary)
                .albumPickerBackgroundColor(Color.WHITE)
                .statusBarColor(colorPrimaryDark)
        PhotoPicker.photoLoader = GlidePhotoLoader()
    }

    private class GlidePhotoLoader : PhotoLoader {
        override fun loadPhoto(imageView: ImageView, imagePath: String, viewWidth: Int, viewHeight: Int) {
            Glide.with(imageView).load(imagePath).into(imageView)
        }
    }
}
