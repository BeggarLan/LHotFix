package com.beggar.hotfix.patch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beggar.hotfix.base.ChangeRedirect;

/**
 * author: lanweihua
 * created on: 2022/5/11 1:04 下午
 * description: 补丁代理类
 */
public class PatchProxy {

  /**
   * 方法是否支持修复
   *
   * @param params           参数
   * @param FixClassThis     所在类对象，如果方法是static的话，该值为null
   * @param changeRedirect   具体的修复类
   * @param isStatic         是否是静态
   * @param methodNumber     方法的索引
   * @param paramsClassTypes 参数的类型
   * @param returnType       函数返回值的类型
   * @return {@code true} 支持修复
   */
  public static boolean isSupport(
      @NonNull Object[] params,
      @Nullable Object FixClassThis,
      @NonNull ChangeRedirect changeRedirect,
      boolean isStatic,
      int methodNumber,
      @NonNull Class<?>[] paramsClassTypes,
      @NonNull Class<?> returnType) {

    return false;
  }


}
