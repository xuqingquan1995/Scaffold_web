package top.xuqingquan.web.publics

import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import top.xuqingquan.web.nokernel.WebConfig
import top.xuqingquan.utils.Timber

import java.lang.ref.WeakReference
import java.util.Locale

import com.tencent.smtt.sdk.WebView as X5WebView

@Suppress("unused")
class JsCallback {
    private var mIndex: Int = 0
    private var mCouldGoOn: Boolean = false
    private var mWebViewRef: WeakReference<WebView>? = null
    private var mx5WebViewRef: WeakReference<X5WebView>? = null
    private var mIsPermanent: Int = 0
    private var mInjectedName: String? = null

    internal constructor(view: WebView, injectedName: String, index: Int) {
        mCouldGoOn = true
        mWebViewRef = WeakReference(view)
        mInjectedName = injectedName
        mIndex = index
    }

    internal constructor(view: X5WebView, injectedName: String, index: Int) {
        mCouldGoOn = true
        mx5WebViewRef = WeakReference(view)
        mInjectedName = injectedName
        mIndex = index
    }

    /**
     * 向网页执行js回调；
     *
     * @param args
     * @throws JsCallbackException
     */
    @Throws(JsCallbackException::class)
    fun apply(vararg args: Any) {
        if (WebConfig.isTbsEnable()) {
            if (mx5WebViewRef?.get() == null) {
                throw JsCallbackException("the WebView related to the JsCallback has been recycled")
            }
        } else {
            if (mWebViewRef?.get() == null) {
                throw JsCallbackException("the WebView related to the JsCallback has been recycled")
            }
        }
        if (!mCouldGoOn) {
            throw JsCallbackException("the JsCallback isn't permanent,cannot be called more than once")
        }
        val sb = StringBuilder()
        for (arg in args) {
            sb.append(",")
            val isStrArg = arg is String
            // 有的接口将Json对象转换成了String返回，这里不能加双引号，否则网页会认为是String而不是JavaScript对象；
            val isObjArg = isJavaScriptObject(arg)
            if (isStrArg && !isObjArg) {
                sb.append("\"")
            }
            sb.append(arg)
            if (isStrArg && !isObjArg) {
                sb.append("\"")
            }
        }
        val execJs =
            String.format(Locale.getDefault(), CALLBACK_JS_FORMAT, mInjectedName, mIndex, mIsPermanent, sb.toString())
        Timber.d(execJs)
        if (WebConfig.isTbsEnable()) {
            mx5WebViewRef?.get()?.loadUrl(execJs)
        } else {
            mWebViewRef?.get()?.loadUrl(execJs)
        }
        mCouldGoOn = mIsPermanent > 0
    }

    /**
     * 是否是JSON(JavaScript Object Notation)对象；
     *
     * @param obj
     * @return
     */
    private fun isJavaScriptObject(obj: Any): Boolean {
        if (obj is JSONObject || obj is JSONArray) {
            return true
        } else {
            val json = obj.toString()
            try {
                JSONObject(json)
            } catch (e: JSONException) {
                try {
                    JSONArray(json)
                } catch (e1: JSONException) {
                    return false
                }

            }

            return true
        }
    }

    /**
     * 一般传入到Java方法的js function是一次性使用的，即在Java层jsCallback.apply(...)之后不能再发起回调了；
     * 如果需要传入的function能够在当前页面生命周期内多次使用，请在第一次apply前setPermanent(true)；
     *
     * @param value
     */
    fun setPermanent(value: Boolean) {
        mIsPermanent = if (value) 1 else 0
    }

    class JsCallbackException internal constructor(msg: String) : Throwable(msg)

    companion object {

        private const val CALLBACK_JS_FORMAT = "javascript:%s.callback(%d, %d %s);"
    }
}
