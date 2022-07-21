package com.beggar.hotfix.autopatch;

import com.android.annotations.NonNull;
import com.beggar.hotfix.base.PatchTemplate;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

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
      @NonNull ClassPool classPool, @NonNull CtClass sourceClass, @NonNull CtClass patchClass)
      throws NotFoundException, CannotCompileException {
    // 控制类名字
    String patchControlClassName =
        NameManager.getInstance().getPatchControlClassName(sourceClass.getSimpleName());
    CtClass patchControlCtClass =
        classPool.getAndRename(PatchTemplate.class.getName(), patchControlClassName);

    // 添加获取参数真正类型的方法：方法是把原类对象替换成补丁类对象
//    public Object getRealParameter(Object parameter) {
//      if(parameter instanceOf SourceClassName) {
//        return new PatchClass(parameter);
//      }
//      return parameter;
//    }
    StringBuilder getRealParameterMethodBuilder = new StringBuilder();
    getRealParameterMethodBuilder
        .append("public Object getRealParameter(Object parameter) {")
        .append("if(parameter instanceOf " + sourceClass.getName() + ") {")
        .append("return new " + patchClass.getName() + "(parameter);")
        .append("}")
        .append("return parameter")
        .append("}");
    patchControlCtClass
        .addMethod(CtMethod.make(getRealParameterMethodBuilder.toString(), patchControlCtClass));

    // 方法中插入代码
    patchControlCtClass
        .getDeclaredMethod("isSupport")
        .insertBefore(getIsSupportMethodBody(sourceClass, patchClass));

    patchControlCtClass
        .getDeclaredMethod("accessDispatch")
        .insertBefore(getAccessDispatchMethodBody(sourceClass, patchClass));

    return patchControlCtClass;
  }

  /**
   * 获得AccessDispatch方法体
   */
  private static String getAccessDispatchMethodBody(
      @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    /* ***************************   生成的代码如下
    PatchClass patch = null;
    // 是否是静态方法
    String isStatic = $1.spil(Constants.PROXY_METHOD_DESC_CONTENT_SPLIT)[0];
    // 不是静态方法
    if (isStatic.equals(false)) {
      // 非静态方法的最后一个参数是方法所属的对象
      patch =
          new PatchClass(ChangeRedirect.METHOD_PARAMS[ChangeRedirect.METHOD_PARAMS.length() - 1]);
    } else {
      // 静态方法
      patch = new PatchClass(null);
    }

    // 要patch的方法的number
    String methodNumber = $1.spil(Constants.PROXY_METHOD_DESC_CONTENT_SPLIT)[1];
    // 这些numbr就是该类中要patch的所有的方法的编号，逐一匹配
    if (number1.equals(methodNumber)) {
      // 1. 这里如果要处理的method不是public的，那么访问 PUBLIC_SUFFIX后缀的那个生成方法。
      // 2. 执行patch对象的方法访问
    }
    if (number1.equals(methodNumber)) {
      // 同上
    }
    ...
    *****************************************************/

    StringBuilder methodBodyBuilder = new StringBuilder();


  }

  /**
   * 获得isSupport方法体
   */
  private static String getIsSupportMethodBody(
      @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {

  }

}
