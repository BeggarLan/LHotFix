package com.beggar.hotfix.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/7/12 12:44 下午
 * description: 反射工具类
 */
public class ReflectUtils {

  /**
   * 调用构造函数
   *
   * @param className        类名
   * @param parameters       构造器的参数值
   * @param parameterClasses 构造器的参数类型
   * @return null表示失败
   */
  @Nullable
  public static Object invokeConstruct(
      @NonNull String className,
      @Nullable Object[] parameters,
      @Nullable Class[] parameterClasses) {
    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> constructor = clazz.getDeclaredConstructor(parameterClasses);
      return constructor.newInstance(parameterClasses);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 调用static方法
   *
   * @param methodName
   * @param clazz            方法所在的类
   * @param parameters       参数对象
   * @param parameterClasses 参数类型
   */
  public static Object invokeStaticMethod(
      @NonNull String methodName,
      @NonNull Class clazz,
      @Nullable Object[] parameters,
      @Nullable Class[] parameterClasses)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
    method.setAccessible(true);
    return method.invoke(null, parameters);
  }

  /**
   * 调用方法
   *
   * @param targetOjebct     调用的对象
   * @param clazz            方法所在的类
   * @param parameters       参数对象
   * @param parameterClasses 参数类型
   */
  public static Object invokeMethod(
      @NonNull String methodName,
      @NonNull Object targetOjebct,
      @NonNull Class clazz,
      @Nullable Object[] parameters,
      @Nullable Class[] parameterClasses)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
    method.setAccessible(true);
    return method.invoke(targetOjebct, parameters);
  }

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
   * 设置静态field的值
   *
   * @param fieldName 属性名
   * @param clazz     所在类
   * @param value     要设置的值
   * @return 该field的值
   */
  public static void setStaticFieldValue(
      @NonNull String fieldName,
      @NonNull Class clazz,
      @Nullable Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = getField(fieldName, clazz);
    field.set(null, value);
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
   * 设置field的值
   *
   * @param fieldName     属性名
   * @param classInstance 所在类实例
   * @param clazz         所在类
   * @param value         要设置的值
   * @return 该field的值
   */
  public static void setFieldValue(
      @NonNull String fieldName,
      @NonNull Object classInstance,
      @NonNull Class clazz,
      @Nullable Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = getField(fieldName, clazz);
    field.set(classInstance, value);
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

  /**
   * 获得对象中某属性的值
   */
  @Nullable
  public static Object getFieldValue(@NonNull Object classInstance, @NonNull String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = null;
    Class<?> clazz = classInstance.getClass();
    while (clazz != null) {
      field = getField(fieldName, clazz);
      if (field != null) {
        field.setAccessible(true);
        break;
      }
      clazz = clazz.getSuperclass();
    }
    if (field != null) {
      return field.get(classInstance);
    }
    return null;
  }

}
