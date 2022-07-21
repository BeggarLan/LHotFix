package com.beggar.hotfix.patch;

import static com.beggar.hotfix.base.Constants.PROXY_METHOD_DESC_CONTENT_SPLIT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beggar.hotfix.base.ChangeRedirect;

/**
 * author: BeggarLan
 * created on: 2022/5/11 1:04 下午
 * description: 补丁代理类
 * TODO: 2022/5/26 可以对外暴露各个事件供其他业务(如access方法的时候等)
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
      @Nullable ChangeRedirect changeRedirect,
      boolean isStatic,
      int methodNumber,
      @NonNull Class<?>[] paramsClassTypes,
      @NonNull Class<?> returnType) {
    if (changeRedirect == null) {
      return false;
    }
    String proxyMethodStr = getProxyMethodStr(isStatic, methodNumber);
    Object[] objects = getObjects(params, FixClassThis, isStatic);
    return changeRedirect.isSupport(proxyMethodStr, objects);
  }

  /**
   * 访问void方法
   */
  public static void accessDispatchVoid(
      @NonNull Object[] params,
      @Nullable Object FixClassThis,
      @NonNull ChangeRedirect changeRedirect,
      boolean isStatic,
      int methodNumber,
      @NonNull Class<?>[] paramsClassTypes,
      @NonNull Class<?> returnType) {
    String proxyMethodStr = getProxyMethodStr(isStatic, methodNumber);
    Object[] objects = getObjects(params, FixClassThis, isStatic);
    changeRedirect.accessDispatch(proxyMethodStr, objects);
  }

  /**
   * 访问void方法
   */
  public static Object accessDispatch(
      @NonNull Object[] params,
      @Nullable Object FixClassThis,
      @NonNull ChangeRedirect changeRedirect,
      boolean isStatic,
      int methodNumber,
      @NonNull Class<?>[] paramsClassTypes,
      @NonNull Class<?> returnType) {
    String proxyMethodStr = getProxyMethodStr(isStatic, methodNumber);
    Object[] objects = getObjects(params, FixClassThis, isStatic);
    return changeRedirect.accessDispatch(proxyMethodStr, objects);
  }

  /**
   * 获取将要访问的方法标示
   *
   * @return isStatic:methodNumber
   */
  private static String getProxyMethodStr(boolean isStatic, int methodNumber) {
    return isStatic + PROXY_METHOD_DESC_CONTENT_SPLIT + methodNumber;
  }

  /**
   * @return 数组的前几个是函数的参数，最后一个是所属类对象；若函数参数为空，那么返回null
   */
  @Nullable
  private static Object[] getObjects(Object[] params, Object fixClassThis, boolean isStatic) {
    if (params == null) {
      return null;
    }
    Object[] objects;
    int argLength = params.length;
    // 静态方法没有类对象
    if (isStatic) {
      objects = new Object[argLength];
    } else {
      objects = new Object[argLength + 1];
    }
    int x = 0;
    for (; x < argLength; x++) {
      objects[x] = params[x];
    }
    if (!(isStatic)) {
      objects[x] = fixClassThis;
    }
    return objects;
  }

}
