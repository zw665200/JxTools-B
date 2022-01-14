package com.wx.tools.annotation;

import com.wx.tools.view.base.BaseFragment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by fish on 16-4-25.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fragments {
    Class<? extends BaseFragment>[] value();
}
