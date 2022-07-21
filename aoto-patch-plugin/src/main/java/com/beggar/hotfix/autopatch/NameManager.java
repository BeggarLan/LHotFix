package com.beggar.hotfix.autopatch;

import java.util.HashMap;
import java.util.Map;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/6/29 1:23 下午
 * description: name工具
 */
public class NameManager {

  @NonNull
  private final static NameManager sNameManager = new NameManager();

  @NonNull
  public static NameManager getInstance() {
    return sNameManager;
  }

  /**
   * 存储所有的patchClass到原类的映射
   */
  @NonNull
  private final Map<String, String> mPatchClassNameMap = new HashMap<>();

  /**
   * 获得某类对应的补丁类名称：不带.class
   *
   * @param sourceCtClassName 原类名：不带.class
   */
  @NonNull
  public String getPatchClassName(@NonNull String sourceCtClassName) {
    // package换了，类最后增加patch后缀
    String patchClassName =
        AutoPatchConstants.PATCH_CLASS_PACKAGE_NAME + "." +
            sourceCtClassName.substring(sourceCtClassName.lastIndexOf(".") + 1) +
            AutoPatchConstants.PATCH_CLASS_NAME_SUFFIX;
    mPatchClassNameMap.put(patchClassName, sourceCtClassName);
    return patchClassName;
  }

  /**
   * 获得补丁控制类的类名
   *
   * @param sourceClassSimpleName 原类的名字
   * @return
   */
  public String getPatchControlClassName(@NonNull String sourceClassSimpleName) {
    return AutoPatchConstants.PATCH_CLASS_PACKAGE_NAME + "." + sourceClassSimpleName +
        AutoPatchConstants.PATCH_CONTROL_CLASS_NAME_SUFFIX;
  }

  /**
   * 获得某patch类对应的原类名称：不带.class
   *
   * @param patchCtClassName patch类名：不带.class
   */
  @Nullable
  public String getSourceClassName(@NonNull String patchCtClassName) {
    return mPatchClassNameMap.get(patchCtClassName);
  }

  /**
   * 类名+ASSIST_SUFFIX
   *
   * @param className 类名
   */
  public String getAssistClassName(@NonNull String className) {
    return className + AutoPatchConstants.ASSIST_SUFFIX;
  }

}
