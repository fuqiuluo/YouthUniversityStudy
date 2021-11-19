package moe.ore.bigstudy

import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.regex.Pattern

abstract class StudyThread(
    private val q: Int = -1,
    private val j: Int = -1
): Thread() {
    abstract fun onBugs(e: Throwable)

    abstract fun onTopImage(buf: ByteArray)

    abstract fun onEndImage(buf: ByteArray)

    abstract fun onTitle(title: String)

    override fun run() {
        val urls = allQ()
        if(q == -1 && j == -1) { // all new mode
            val newUrl = urls.last()
            val results = allJ(newUrl)
            data(results.last())
        }
    }

    private fun onTop(url: String) {
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(url).get().build()
        val call: Call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onBugs(e)
            }

            override fun onResponse(call: Call, response: Response) {
                kotlin.runCatching {
                    onTopImage(response.body!!.bytes())
                }.onFailure { onBugs(it) }
            }
        })
    }

    private fun onEnd(result: String) {
        val endJpg = "http://h5.cyol.com/special/daxuexi/$result/images/end.jpg"
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(endJpg).get().build()
        val call: Call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onBugs(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code == 200) {
                    kotlin.runCatching {
                        onEndImage(response.body!!.bytes())
                    }.onFailure { onBugs(it) }
                } else {
                    onTop2(result)
                }
            }
        })
    }

    private fun onTop2(result: String) {
        val endJpgUrl = "https://h5.cyol.com/special/daxuexi/$result/images/end/30.jpg"
        val okc2 = OkHttpClient()
        val req: Request = Request.Builder().url(endJpgUrl).get().build()
        val cal: Call = okc2.newCall(req)
        cal.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onBugs(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code == 200) {
                    kotlin.runCatching {
                        onEndImage(response.body!!.bytes())
                    }.onFailure { onBugs(it) }
                } else {
                    onBugs(java.lang.RuntimeException("not found end jpg"))
                }
            }
        })
    }

    private fun data(result: String) {
        val doc = Jsoup.connect("https://h5.cyol.com/special/daxuexi/$result/m.html?t=1&z=201").get()
        this.onTitle(doc.title())
        val selem = doc.body().getElementById("Bvideo")
        if(selem != null) {
            val topJpg = "https://h5.cyol.com/special/daxuexi/$result/" + selem.attr("poster")
            this.onTop(topJpg)

            this.onEnd(result)
        } else onBugs(RuntimeException("not found start image elem"))
    }

    private fun allJ(url: String): ArrayList<String> {
        val results = arrayListOf<String>()
        val doc = Jsoup.connect(url)
            .header("Accept-Encoding", "gzip, deflate")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .header("User-Agent", "Mobile/16D57 MicroMessenger/7.0.3(0x17000321) NetType/WIFI Language/zh_CN")
            .header("Content-Type", "text/html; charset=utf-8")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .get()
        val html = doc.data()
        val r = Pattern.compile("h5[.]cyol[.]com/special/daxuexi/([a-z0-9]+)/m.html")
        val matcher = r.matcher(html)
        while(matcher.find()) {
            results.add(matcher.group(1))
        }
        return results
    }

    private fun allQ(): ArrayList<String> {
        val urls = arrayListOf<String>()
        val doc = Jsoup.connect("http://h5.cyol.com/special/daxuexi/daxuexiall/m.html?t=1")
            .header("Accept-Encoding", "gzip, deflate")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .header("User-Agent", "Mobile/16D57 MicroMessenger/7.0.3(0x17000321) NetType/WIFI Language/zh_CN")
            .header("Content-Type", "text/html; charset=utf-8")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .get()
        val html = doc.data()
        val r = Pattern.compile("h5[.]cyol[.]com/special/(|daxuexi/)daxuexiall([0-9]+)/(m|index)[.](html|php|jsp)")
        val matcher = r.matcher(html)
        while(matcher.find()) {
            urls.add("http://" + matcher.group() + "?t=1")
        }
        return urls
    }
}