package com.beggar.hotfix.autopatch;

import com.android.annotations.NonNull;
import com.android.dx.rop.code.AccessFlags;
import com.beggar.hotfix.base.ChangeRedirect;
import com.beggar.hotfix.base.Constants;
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
      @NonNull AutoPatchConfig autoPatchConfig,
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass)
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
        .insertBefore(getIsSupportMethodBody(sourceClass, patchClass, autoPatchConfig));

    patchControlCtClass
        .getDeclaredMethod("accessDispatch")
        .insertBefore(getAccessDispatchMethodBody(sourceClass, patchClass, autoPatchConfig));

    return patchControlCtClass;
  }

  /**
   * 获得AccessDispatch方法体
   */
  private static String getAccessDispatchMethodBody(
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass,
      @NonNull AutoPatchConfig autoPatchConfig) throws NotFoundException {
    /* ***************************   生成的代码如下
    PatchClass patch = null;
    // 是否是静态方法
    String isStatic = $1.spilt(Constants.PROXY_METHOD_DESC_CONTENT_SPLIT)[0];
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
    String methodNumber = $1.spilt(Constants.PROXY_METHOD_DESC_CONTENT_SPLIT)[1];
    // 这些numbr就是该类中要patch的所有的方法的编号，逐一匹配
    if (number1.equals(methodNumber)) {
      // 1. 这里如果要处理的method是private的，那么访问 PUBLIC_SUFFIX后缀的那个生成方法。
      // 2. 执行patch对象的方法访问
    }
    if (number1.equals(methodNumber)) {
      // 同上
    }
    ...
    *****************************************************/

    StringBuilder methodBodyBuilder = new StringBuilder();
    methodBodyBuilder
        .append(patchClass.getName() + "patch = null;")
        .append("String isStatic = $1.spilt(" + Constants.PROXY_METHOD_DESC_CONTENT_SPLIT + ")[0];")
        .append("if (isStatic.equals(false)) {")
        .append(" patch = new " + patchClass.getName() +
            "(" + ChangeRedirect.METHOD_PARAMS + "[" + ChangeRedirect.METHOD_PARAMS +
            ".length() - 1]);")
        .append("} else {")
        .append("patch = new " + patchClass.getName() + "(null);")
        .append("}")
        .append(
            "String methodNumber = $1.spilt(" + Constants.PROXY_METHOD_DESC_CONTENT_SPLIT +
                ")[1];");
    // 需要patch的方法都补一下处理
    for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
      String methodSignatureName = JavassistUtil.getMethodSignatureName(ctMethod);
      Integer currentMethodNo = autoPatchConfig.mCodeInsertMethodMap.get(methodSignatureName);
      if (currentMethodNo == null) {
        continue;
      }
      // 是否匹配到要处理的方法
      methodBodyBuilder
          .append("if(" + currentMethodNo + " == methodNumber) {");

      String methodName = ctMethod.getName();
      // private方法访问
      if (AccessFlags.isPrivate(ctMethod.getModifiers())) {
        methodName = PatchUtil.getPublicSuffixMethodName(ctMethod);
      }

      // void函数
      if (JavassistUtil.isVoidMethod(ctMethod)) {
        methodBodyBuilder.append("patch." + methodName + "(");
      } else {
        // 需要处理return
//        switch (ctMethod.getReturnType().getName()) {
//          case "int":
//            methodBodyBuilder.append("return Integer.valueOf(patch." + methodName + "(");
//          case "short":
//            methodBodyBuilder.append("return Short.valueOf(patch." + methodName + "(");
//          case "long":
//            methodBodyBuilder.append("return Long.valueOf(patch." + methodName + "(");
//          case "float":
//            methodBodyBuilder.append("return Float.valueOf(patch." + methodName + "(");
//          case "double":
//            methodBodyBuilder.append("return Double.valueOf(patch." + methodName + "(");
//          case "byte":
//            methodBodyBuilder.append("return Byte.valueOf(patch." + methodName + "(");
//          case "boolean":
//            methodBodyBuilder.append("return Boolean.valueOf(patch." + methodName + "(");
//          case "char":
//            methodBodyBuilder.append("return Character.valueOf(patch." + methodName + "(");
//          default:
        methodBodyBuilder.append("return patch." + methodName + "(");
//        }
      }

      // 拼接参数
      CtClass[] parameterTypes = ctMethod.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; ++i) {
        methodBodyBuilder.append(ChangeRedirect.METHOD_PARAMS + "[" + i + "]");
        if (i != parameterTypes.length - 1) {
          methodBodyBuilder.append(",");
        }
      }
      methodBodyBuilder.append(")}");
    }

    return methodBodyBuilder.toString();
  }

  /**
   * 获得isSupport方法体
   */
  private static String getIsSupportMethodBody(
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass,
      @NonNull AutoPatchConfig autoPatchConfig) {
    /* ***************************   生成的代码如下
    // 要patch的方法的number
    String methodNumber = $1.spilt(Constants.PROXY_METHOD_DESC_CONTENT_SPLIT)[1];
    // 然后找出本类支持的patch的所有方法
    // return 支持的方法中包含该方法
     */

    StringBuilder methodBodyBuilder = new StringBuilder();
    methodBodyBuilder.append(
        "String methodNumber = $1.spilt(" + Constants.PROXY_METHOD_DESC_CONTENT_SPLIT +
            ")[1];");

    // 需要patch的方法都补一下处理
    StringBuilder classSupportPatchMethodIds = new StringBuilder();
    for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
      String methodSignatureName = JavassistUtil.getMethodSignatureName(ctMethod);
      Integer currentMethodNo = autoPatchConfig.mCodeInsertMethodMap.get(methodSignatureName);
      if (currentMethodNo != null) {
        classSupportPatchMethodIds.append(":" + currentMethodNo + ":");
      }
    }
    methodBodyBuilder
        .append("return " + classSupportPatchMethodIds.toString() + ".contains(methodNumber);");
    return methodBodyBuilder.toString();
  }

}
