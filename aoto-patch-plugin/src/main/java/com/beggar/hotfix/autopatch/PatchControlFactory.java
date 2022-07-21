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



    return patchControlCtClass;
  }

}
