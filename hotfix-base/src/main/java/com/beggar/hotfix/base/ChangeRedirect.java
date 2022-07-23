package com.beggar.hotfix.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * author: BeggarLan
 * created on: 2022/5/6 22:30
 * description: 用于访问修复补丁
 */
public interface ChangeRedirect {

  String sClassName = "com.beggar.hotfix.base.ChangeRedirect";
  String sObjectName = "changeRedirect";

  // 表示方法中的params参数
  String METHOD_PARAMS = "params";


  /**
   * 是否可以patch
   *
   * @param patchMethodDesc 格式为isStatic:methodNumber
   * @param params          最后一项为所属类对象(若是静态方法则没有类对象)。null代表函数无参数
   */
  boolean isSupport(@NonNull String patchMethodDesc, @Nullable Object[] params);


  /**
   * 访问方法
   *
   * @param patchMethodDesc 格式为isStatic:methodNumber
   * @param params          最后一项为所属类对象(若是静态方法则没有类对象)。null代表函数无参数
   * @return 函数的返回值
   */
  Object accessDispatch(String patchMethodDesc, Object[] params);

}
