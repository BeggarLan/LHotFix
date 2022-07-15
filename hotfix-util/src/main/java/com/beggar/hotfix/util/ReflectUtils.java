package com.beggar.hotfix.util;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/7/12 12:44 下午
 * description: 反射工具类
 */
public class ReflectUtils {

  /**
   * 获得静态field的值
   *
   * @param fieldName 属性名
   * @param clazz     所在类
   * @return 该field的值
   */
  @Nullable
  public static Object getStaticFieldValue(@NonNull String fieldName, @NonNull Class clazz)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = getField(fieldName, clazz);
    return field.get(null);
  }

  /**
   * 获得field的值
   * 为啥传递了classInstance还要传递一个class，因为class.getDeclaredFields拿不到继承的field的
   *
   * @param fieldName     属性名
   * @param classInstance 所在类实例
   * @param clazz         所在类
   * @return 该field的值
   */
  @Nullable
  public static Object getFieldValue(
      @NonNull String fieldName, @NonNull Object classInstance, @NonNull Class clazz)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = getField(fieldName, clazz);
    return field.get(classInstance);
  }

  /**
   * 通过属性名获得对应的field
   *
   * @param fieldName 属性名
   * @param clazz     所在类
   */
  public static Field getField(@NonNull String fieldName, @NonNull Class clazz)
      throws NoSuchFieldException {
    return clazz.getDeclaredField(fieldName);
  }

}
