package io.zhuliang.watermark

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.input.input
import io.zhuliang.watermark.util.DimenUtil
import io.zhuliang.watermark.view.WatermarkView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author ZhuLiang
 */
class MainActivity : AppCompatActivity() {

    private val LOG_TAG = "MainActivity"

    private var mWatermarkView: WatermarkView? = null

    private var mAlpha = 255
    private var mColor = Color.BLACK

    companion object {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 666
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mWatermarkView = findViewById(R.id.watermarkView)
        try {
            val `is` = assets.open("miku.png")
            mWatermarkView!!.setImageBitmap(BitmapFactory.decodeStream(`is`))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        findViewById<SeekBar>(R.id.seek_text_size)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        mWatermarkView!!.setWatermarkTextSize(
                                DimenUtil.sp2px(this@MainActivity, progress.toFloat()).toFloat())
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                })

        findViewById<SeekBar>(R.id.seek_rotate)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                        mWatermarkView!!.setWatermarkRotation(i)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {

                    }
                })
        val seekColorAlpha = findViewById<SeekBar>(R.id.seek_colorA)
        seekColorAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mAlpha = progress
                val red = Color.red(mColor)
                val green = Color.green(mColor)
                val blue = Color.blue(mColor)
                mWatermarkView!!.setWatermarkColor(Color.argb(mAlpha, red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        findViewById<SeekBar>(R.id.seek_spacing)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        mWatermarkView!!.setWatermarkSpacing(DimenUtil.dip2px(this@MainActivity, progress.toFloat()))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                })
        findViewById<View>(R.id.btn_color).setOnClickListener {
            MaterialDialog(this).show {
                val colors = intArrayOf(Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE)
                colorChooser(colors = colors,
                        allowCustomArgb = false,
                        showAlphaSelector = false) { _, color ->
                    mColor = color
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    mWatermarkView!!.setWatermarkColor(Color.argb(mAlpha, red, green, blue))
                }
            }
        }
        findViewById<View>(R.id.btn_text_input).setOnClickListener {
            MaterialDialog(this).show {
                input(allowEmpty = false) { _, text ->
                    mWatermarkView!!.setWatermarkText(text.toString())
                }
            }
        }
        findViewById<View>(R.id.btn_save).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            } else {
                saveBitmapToFile()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.wm_main_guideline)?.isChecked = mWatermarkView!!.isGuideline
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.wm_main_guideline -> {
                val isGuideline = !mWatermarkView!!.isGuideline
                mWatermarkView!!.isGuideline = isGuideline
                item.isChecked = isGuideline
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBitmapToFile() {
        object : Thread() {
            override fun run() {
                super.run()
                try {
                    val sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(sdCard, "miku.png")

                    val fos = FileOutputStream(file)
                    val bitmap = mWatermarkView!!.drawToBitmap()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    MediaScannerConnection.scanFile(this@MainActivity, arrayOf(file.toString()), null) { path, _ ->
                        Log.d(LOG_TAG, "run: $path")
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
