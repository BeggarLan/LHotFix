package com.beggar.hotfix.base;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/5/6 22:30
 * description: 用于访问修复补丁
 */
public interface ChangeRedirect {

  String sClassName = "com.beggar.hotfix.base.ChangeRedirect";
  String sObjectName = "changeRedirect";


  /**
   * 是否可以patch
   *
   * @param patchMethodName 格式为isStatic:methodNumber
   * @param params          最后一项为所属类对象(若是静态方法则没有类对象)。null代表函数无参数
   */
  boolean isSupport(@NotNull String patchMethodName, @Nullable Object[] params);

  /**
   * patch
   *
   * @param methodName
   * @param params
   */
  Object patch(String methodName, Object[] params);

}
