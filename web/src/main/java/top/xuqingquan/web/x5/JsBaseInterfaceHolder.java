package top.xuqingquan.web.x5;

import android.webkit.JavascriptInterface;

import android.support.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import top.xuqingquan.web.nokernel.JsInterfaceHolder;
import top.xuqingquan.web.nokernel.WebConfig;

@SuppressWarnings("rawtypes")
public abstract class JsBaseInterfaceHolder implements JsInterfaceHolder {

    private final WebCreator mWebCreator;

    protected JsBaseInterfaceHolder(WebCreator webCreator) {
        this.mWebCreator = webCreator;
    }

    @Override
    public boolean checkObject(@NonNull Object v) {
        if (mWebCreator.getWebViewType() == WebConfig.WEB_VIEW_AGENT_WEB_SAFE_TYPE) {
            return true;
        }
        boolean tag = false;
        Class clazz = v.getClass();
        Method[] mMethods = clazz.getMethods();
        for (Method mMethod : mMethods) {
            Annotation[] mAnnotations = mMethod.getAnnotations();
            for (Annotation mAnnotation : mAnnotations) {
                if (mAnnotation instanceof JavascriptInterface) {
                    tag = true;
                    break;
                }
            }
            if (tag) {
                break;
            }
        }
        return tag;
    }

}
