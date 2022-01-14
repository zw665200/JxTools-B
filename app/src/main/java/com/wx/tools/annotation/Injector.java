package com.wx.tools.annotation;

import com.wx.tools.view.base.BaseFragmentActivity;
import com.wx.tools.view.base.BaseSplashActivity;

import java.lang.reflect.Field;


/**
 * Created by fish on 16-4-25.
 */
public class Injector {



    public static void initSplash(BaseSplashActivity bsa){
        Class<? extends BaseSplashActivity> clz = bsa.getClass();
        Splash splash = clz.getAnnotation(Splash.class);
        if (splash == null) {
            return;
        }
        bsa.setConfig(splash.delay(), splash.clz());
    }

    public static void initFragmentActivity(BaseFragmentActivity bfa) {
        DefaultPage defaultPage = bfa.getClass().getAnnotation(DefaultPage.class);
        if (defaultPage == null) {
            return;
        }
        try {
            Field f = BaseFragmentActivity.class.getDeclaredField(BaseFragmentActivity.DECLARED_FIELD_DEFAULT_PAGE);
            f.setAccessible(true);
            f.set(bfa, defaultPage.value());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
