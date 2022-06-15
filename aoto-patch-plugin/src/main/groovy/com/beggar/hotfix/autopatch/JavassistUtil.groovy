package com.beggar.hotfix.autopatch

import com.android.annotations.NonNull
import com.android.build.api.transform.TransformInput
import javassist.ClassPool
import javassist.CtClass;

/**
 * author: lanweihua
 * created on: 2022/6/15 7:40 下午
 * description: Javassist工具类
 */
public class JavassistUtil {

    /**
     * 把inputs中的class转换成 CtClass
     *
     * @param inputs
     * @param classPool
     */
    public static List<CtClass> toCtClasses(
        @NonNull Collection<TransformInput> inputs, @NonNull ClassPool classPool) {


    }

}
