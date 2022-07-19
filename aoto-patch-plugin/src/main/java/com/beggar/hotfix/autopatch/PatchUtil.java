package com.beggar.hotfix.autopatch;

import android.text.TextUtils;

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

  /**
   * @param className            类名
   * @param constructorSignature 构造函数的签名 方法描述符的字符串
   *                             A类的构造函数：A(java.lang.Integer, float, java.lang.Object[])
   *                             descriptor: (Ljava/lang/Integer;F[Ljava/lang/Object;)V
   * @param isClassStatic        该类是否是静态类
   * @param sourceClass          原类
   * @param patchClass           原类对应的补丁类
   */
  public static String getNewExprReplaceString(
      @NonNull String className,
      @NonNull String constructorSignature,
      boolean isClassStatic,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass) {
    StringBuilder stringBuilder = new StringBuilder("{");
    // 构造函数的参数签名
    String constructorParameterSignature =
        getConstructorParameterSignature(constructorSignature, sourceClass, patchClass);

    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  /**
   * 获得构造器的参数签名：替换patchClass参数
   *
   * @param constructorSignature 构造函数的签名 方法描述符的字符串
   *                             A类的构造函数：A(java.lang.Integer, float, java.lang.String)
   *                             descriptor: (Ljava/lang/Integer;FLjava/lang/String;)V
   * @param patchClass           原类对应的补丁类
   */
  private static String getConstructorParameterSignature(
      @NonNull String constructorSignature,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass) {
    StringBuilder parameterSignatureBuilder = new StringBuilder();

    // *********************** 遍历所有参数
    // 是否是数组
    boolean isArray = false;
    int parameterEndIndex = constructorSignature.indexOf(")");
    for (int i = 1; i < parameterEndIndex; ++i) {
      //当前的参数类型
      char parameterType = constructorSignature.charAt(i);
      // 如果是Object
      if (parameterType == JavaByteCodeUtil.OBJECT_TYPE) {
        // 获得搞参数的具体类型：全类名
        String parameterClassName = constructorSignature.substring(
            i + 1, constructorSignature.indexOf(JavaByteCodeUtil.OBJECT_NAME_END_FLAG, i))
            .replace("/", ".");
        // 如果类型是布丁类,那么替换为原类
        if (TextUtils.equals(parameterClassName, patchClass.getName())) {
          parameterSignatureBuilder.append(sourceClass.getName());
        } else {
          parameterSignatureBuilder.append(parameterClassName);
        }
        // 跳到下一个
        i = constructorSignature.indexOf(";", i);

        // 处理数组
        if (isArray) {
          parameterSignatureBuilder.append("[]");
          isArray = false;
        }
        // 增加class后缀
        parameterSignatureBuilder.append(".class,");

      } else if (JavaByteCodeUtil.isBasicType(parameterType)) {
        // 基本类型
        parameterSignatureBuilder.append(JavaByteCodeUtil.getByBasicTypeDescriptor(parameterType));
        // 处理数组
        if (isArray) {
          parameterSignatureBuilder.append("[]");
          isArray = false;
        }
        // 增加class后缀
        parameterSignatureBuilder.append(".class,");
      }


    }
    return parameterSignatureBuilder.toString();
  }

  /**
   * 是否是一个类的内部类
   *
   * @param ctClassName 内部类
   * @param sourceClass 所在的类
   */
  public static boolean isInnerClass(@NonNull String ctClassName, @NonNull CtClass sourceClass) {
    int index = ctClassName.indexOf("$");
    if (index < 0) {
      // 不是内部类
      return false;
    }
    return TextUtils.equals(ctClassName.substring(0, index), sourceClass.getName());
  }

}
