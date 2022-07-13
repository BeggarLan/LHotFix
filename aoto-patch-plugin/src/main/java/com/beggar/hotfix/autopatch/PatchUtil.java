package com.beggar.hotfix.autopatch;

import org.apache.tools.ant.util.ReflectUtil;

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
      // FieldAccess中: $proceed 代表最初访问成员的名称

    } else {
      String patchClassName = patchClass.getName();
      // 定义一个变量，默认就是[$0：表达式访问的那个字段]
      stringBuilder.append("java.lang.Object instance = \\$0;");
      stringBuilder.append("if(\\$0 instanceof " + patchClassName + "){");

      // instance = ((patchClassName)$0).AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS
      // 让instance等于patchClass对象中的sourceClass对象
      stringBuilder.append("instance = " + "((" + patchClassName + ")\\$0)." +
          AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS + ";}");

      // $_: 如果表达式是读操作，则结果值保存在 1 中，否则将舍弃存储在_ 中的值
      // $r: 如果表达式是读操作，则 r 读取结果的类型。 否则r 为 void
      stringBuilder.append("\\$_=(\\$r)" + ReflectUtil.class.getName() + ".getFieldValue")
          .append("(" + ctField.getName())
          .append(", instance")
          .append(", " + ctField.getDeclaringClass().getName() + ".class")
          .append(")");
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

}
