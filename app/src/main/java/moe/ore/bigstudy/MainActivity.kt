package moe.ore.bigstudy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.guidview.GuideCaseView
import java.io.File

import android.view.View
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.ListCallback
import java.util.*

class MainActivity : BaseActivity() {
    private lateinit var image: ImageView
    private lateinit var titleView: TextView
    private lateinit var web: WebView
    private lateinit var change: ImageView

    private var title: String? = null

    private var top: ByteArray? = null
    private var end: ByteArray? = null

    private var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.image = findViewById(R.id.image)
        this.titleView = findViewById(R.id.title)
        this.web = findViewById(R.id.web)
        this.change = findViewById(R.id.change)

        init()
    }

    @SuppressLint("SdCardPath")
    fun init(first: Boolean = false) {
        requestPermission {
            if (first) {
                GuideCaseView.Builder(this)
                    .focusOn(change)
                    .focusCircleRadiusFactor(1.5)
                    .title("点击这里切换首图/尾图")
                    .focusBorderColor(Color.GREEN)
                    .titleStyle(0, Gravity.CENTER)
                    .fitWindowsAuto()
                    .build()
                    .show()
            }

            alert("请等待加载成功...")

            object : StudyThread() {
                override fun onBugs(e: Throwable) {
                    alert(e.toString())
                }

                override fun onTopImage(buf: ByteArray) {
                    top = buf
                }

                override fun onEndImage(buf: ByteArray) {
                    end = buf
                    mode = 1
                    runOnUiThread { image.setImage(end!!) }
                }

                override fun onTitle(title: String) {
                    runOnUiThread {
                        titleView.text = title
                        this@MainActivity.title = title
                    }
                }
            }.start()

            change.setOnClickListener {
                when (mode) {
                    0 -> {
                        alert("请等待加载完成...")
                    }
                    1 -> {
                        titleView.text = title

                        mode = 2
                        image.setImage(top!!)
                        image.visibility = VISIBLE
                        web.visibility = GONE
                    }
                    2 -> {
                        mode = 3
                        image.setImage(end!!)
                        image.visibility = VISIBLE
                        web.visibility = GONE
                    }
                }
            }
        }
    }

    fun alert(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("notice")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .create().show()
        }
    }

    override fun requiredPermission(): Array<String> = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    override fun needInitTheme(): Boolean = true

    companion object {
        fun ImageView.setImage(data: ByteArray) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            setImageBitmap(bitmap)
        }
    }
}