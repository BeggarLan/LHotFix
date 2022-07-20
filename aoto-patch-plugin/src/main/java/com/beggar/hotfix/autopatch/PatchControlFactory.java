package com.beggar.hotfix.autopatch;

import com.android.annotations.NonNull;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * author: lanweihua
 * created on: 2022/7/20 12:50 下午
 * description: 创建布丁控制类，用来调度布丁方法
 */
public class PatchControlFactory {

  /**
   * @param sourceClass 原类
   * @param patchClass  布丁类
   */
  public static CtClass createPatchControlClass(
      @NonNull ClassPool classPool, @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    classPool.getAndRename();
  }

}
