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

  /**
   * FieldAccess表达式：替换write
   *
   * @param ctField
   * @param sourceClass
   * @param patchClass
   */
  public static String setFieldAccessWriteReplaceString(
      @NonNull CtField ctField, @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    StringBuilder stringBuilder = new StringBuilder("{");

    boolean isStatic = JavassistUtil.isStatic(ctField);
    if (isStatic) {
      // TODO: 2022/7/15 这里和robust不一样了，不知道有没有坑
      // 如果是patch类的字段，那么替换为原类的
      if (ctField.getDeclaringClass().getName().equals(patchClass.getName())) {
        stringBuilder.append(ReflectUtils.class.getName() + ".setStaticFieldValue")
            .append("(" + ctField.getName())
            .append(", " + sourceClass.getName() + ".class")
            .append(", \\$1)");
      } else {
        // 原语句
        stringBuilder.append("\\$_ = \\$proceed(\\$\\$);");
      }


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
      // $1: 如果表达式是写操作，则写的值将保存在 1 中。否则1 不可用
      stringBuilder.append(ReflectUtils.class.getName() + ".setFieldValue")
          .append("(" + ctField.getName())
          .append(", instance")
          .append(", " + ctField.getDeclaringClass().getName() + ".class")
          .append(", \\$1)");
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  /**
   * FieldAccess表达式：替换read
   *
   * @param ctField
   * @param sourceClass
   * @param patchClass
   * @return
   */
  public static String getFieldAccessReadReplaceString(
      @NonNull CtField ctField, @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    StringBuilder stringBuilder = new StringBuilder("{");
    boolean isStatic = JavassistUtil.isStatic(ctField);
    if (isStatic) {
      // TODO: 2022/7/15 这里和robust不一样了，不知道有没有坑
      // 如果是patch类的字段，那么替换为原类的
      if (ctField.getDeclaringClass().getName().equals(patchClass.getName())) {
        stringBuilder.append("\\$_=(\\$r)" + ReflectUtils.class.getName() + ".getStaticFieldValue")
            .append("(" + ctField.getName())
            .append(", " + sourceClass.getName() + ".class")
            .append(")");
      } else {
        // 原语句
        stringBuilder.append("\\$_ = \\$proceed(\\$\\$);");
      }

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
      stringBuilder.append("\\$_=(\\$r)" + ReflectUtils.class.getName() + ".getFieldValue")
          .append("(" + ctField.getName())
          .append(", instance")
          .append(", " + ctField.getDeclaringClass().getName() + ".class")
          .append(")");
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

}
