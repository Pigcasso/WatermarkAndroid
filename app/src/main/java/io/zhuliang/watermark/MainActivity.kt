package io.zhuliang.watermark

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.input.input
import io.zhuliang.watermark.util.DimenUtil
import io.zhuliang.watermark.view.WatermarkView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ZhuLiang
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mWatermarkView: WatermarkView

    private var mAlpha = 255
    private var mColor = Color.BLACK

    companion object {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 666

        fun makeIntent(context: Context, path: String) = Intent(context, MainActivity::class.java)
                .apply { putExtra("image_path", path) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mWatermarkView = findViewById(R.id.watermarkView)
        mWatermarkView.setWatermarkColor(Color.argb(mAlpha, Color.red(mColor), Color.green(mColor), Color.blue(mColor)))
        if (intent.hasExtra("image_path")) {
            mWatermarkView.setImageBitmap(BitmapFactory.decodeFile(intent.getStringExtra("image_path")))
        } else {
            mWatermarkView.setImageBitmap(BitmapFactory.decodeStream(assets.open("miku.png")))
        }

        findViewById<SeekBar>(R.id.seek_text_size)
                .setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        mWatermarkView.setWatermarkTextSize(
                                DimenUtil.sp2px(this@MainActivity, progress.toFloat()).toFloat())
                    }
                })

        findViewById<SeekBar>(R.id.seek_rotate)
                .setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        mWatermarkView.setWatermarkRotation(progress)
                    }
                })
        val seekColorAlpha = findViewById<SeekBar>(R.id.seek_colorA)
        seekColorAlpha.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mAlpha = progress
                val red = Color.red(mColor)
                val green = Color.green(mColor)
                val blue = Color.blue(mColor)
                mWatermarkView.setWatermarkColor(Color.argb(mAlpha, red, green, blue))
            }
        })
        findViewById<SeekBar>(R.id.seek_spacing)
                .setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        mWatermarkView.setWatermarkSpacing(DimenUtil.dip2px(this@MainActivity, progress.toFloat()))
                    }
                })
        findViewById<View>(R.id.btn_color).setOnClickListener {
            MaterialDialog(this).show {
                val colors = intArrayOf(
                        Color.BLACK,
                        Color.DKGRAY,
                        Color.GRAY,
                        Color.LTGRAY,
                        Color.WHITE,
                        Color.RED,
                        Color.GREEN,
                        Color.BLUE,
                        Color.YELLOW,
                        Color.CYAN,
                        Color.MAGENTA
                )
                colorChooser(colors = colors,
                        allowCustomArgb = true,
                        showAlphaSelector = false) { _, color ->
                    mColor = color
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    mWatermarkView.setWatermarkColor(Color.argb(mAlpha, red, green, blue))
                }
            }
        }
        findViewById<View>(R.id.btn_text_input).setOnClickListener {
            MaterialDialog(this).show {
                input(allowEmpty = false) { _, text ->
                    mWatermarkView.setWatermarkText(text.toString())
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
        menu?.findItem(R.id.wm_main_guideline)?.isChecked = mWatermarkView.isGuideline
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.wm_main_guideline -> {
                val isGuideline = !mWatermarkView.isGuideline
                mWatermarkView.isGuideline = isGuideline
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
                    val sdf = SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.getDefault())
                    val file = File(sdCard, "wm-${sdf.format(Date())}.png")

                    val fos = FileOutputStream(file)
                    val bitmap = mWatermarkView.watermarkBitmap
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    MediaScannerConnection.scanFile(this@MainActivity, arrayOf(file.toString()), null) { path, _ ->
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "保存文件到：$path", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private abstract class SimpleOnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }
}
