package com.beggar.hotfix.autopatch;

import com.android.annotations.NonNull;

import javassist.CtClass;
import javassist.CtField;

/**
 * author: lanweihua
 * created on: 2022/7/4 12:45 下午
 * description: 工具方法
 */
public class PatchUtil {

  public static String getFieldAccessReplaceString(
      @NonNull CtField ctField, @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    StringBuilder stringBuilder = new StringBuilder("{");
    boolean isStatic = JavassistUtil.isStatic(ctField);
    if (isStatic) {

    } else {
      // 定义一个变量，默认就是[$0：表达式访问的那个字段]
      stringBuilder.append("java.lang.Object instance = \\$0;");
      stringBuilder.append("if(\\$0 instanceof " + patchClass.getName() + "){");

      // instance = ((patchClassName)$0).AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS
      // 让instance等于patchClass对象中的sourceClass对象
      stringBuilder.append("instance = " + "((patchClassName)\\$0)." +
          AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS + ";}");

      stringBuilder.append();
    }
    return stringBuilder.toString();
  }

}
