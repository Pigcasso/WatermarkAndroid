package io.zhuliang.watermark

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.api.Action

/**
 * <pre>
 * author : ZhuLiang
 * time   : 2019/08/15
 * desc   :
 * version: 1.0
</pre> *
 */

class SelectPictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_select_picture)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSelectPicture(view: View) {
        PhotoPicker
                .image(this)
                .singleChoice()
                .preview(true)
                .allPhotosAlbum(true)
                .onResult(object : Action<ArrayList<String>> {
                    override fun onAction(requestCode: Int, result: ArrayList<String>) {
                        val starter = MainActivity.makeIntent(this@SelectPictureActivity, result[0])
                        startActivity(starter)
                    }
                })
                .onCancel(object : Action<String> {
                    override fun onAction(requestCode: Int, result: String) {

                    }
                })
                .start()
    }
}
