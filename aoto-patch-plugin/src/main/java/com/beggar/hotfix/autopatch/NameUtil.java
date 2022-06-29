package com.beggar.hotfix.autopatch;

import com.android.annotations.NonNull;

/**
 * author: lanweihua
 * created on: 2022/6/29 1:23 下午
 * description: name工具
 */
public class NameUtil {

  /**
   * 类名+ASSIST_SUFFIX
   *
   * @param className 类名
   */
  public static String getAssistClassName(@NonNull String className) {
    return className + AutoPatchConstants.ASSIST_SUFFIX;
  }

}
