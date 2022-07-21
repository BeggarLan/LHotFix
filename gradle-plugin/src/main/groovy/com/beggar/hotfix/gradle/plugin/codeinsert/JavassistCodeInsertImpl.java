package com.beggar.hotfix.gradle.plugin.codeinsert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import com.android.annotations.NonNull;
import com.beggar.hotfix.base.Constants;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

/**
 * author: BeggarLan
 * created on: 2022/5/8 21:18
 * description: JavaAssist字节码织入
 */
public class JavassistCodeInsertImpl extends CodeInsertStrategy {

  public JavassistCodeInsertImpl
      (@NonNull List<String> hotfixPackageList,
          @NonNull List<String> hotfixMethodList,
          @NonNull List<String> exceptPackageList,
          @NonNull List<String> exceptMethodList) {
    super(hotfixPackageList, hotfixMethodList, exceptPackageList, exceptMethodList);
  }

  @Override
  public void insertCode(@NonNull List<CtClass> ctClasses, @NonNull File jarFile)
      throws IOException, CannotCompileException {
    ZipOutputStream zipOutputStream = new JarOutputStream(new FileOutputStream(jarFile));
    for (CtClass ctClass : ctClasses) {
      // 插入code
      if (isNeedInsert(ctClass.getName())) {
        // 给类修饰符加上public
        ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));

        // 接口跳过(因此default方法没法修复)、自身无方法的跳过
        if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
          // 把class文件打入zip
          zipFile(ctClass.toBytecode(), zipOutputStream,
              ctClass.getName().replaceAll("\\.", "/") + ".class");
          continue;
        }

        // 是否插入了field
        boolean hasInsertField = false;
        // 遍历该类声明的所有method(包括构造方法)
        for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
          // 添加field
          if (!hasInsertField) {
            hasInsertField = true;
            ClassPool classPool = ctBehavior.getDeclaringClass().getClassPool();
            CtClass fieldClassName = classPool.getOrNull(Constants.HOTFIX_INTERFACE_NAME);
            CtField ctField =
                new CtField(fieldClassName, Constants.HOTFIX_INSERT_FIELD_NAME, ctClass);
            ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
            ctClass.addField(ctField);
          }
          // 不满足修复条件
          if (!isQualifiedMethod(ctBehavior)) {
            continue;
          }

          // longName 带参数类型的方法或构造器的描述，比如 javassist.CtBehavior.stBody(String)
          mMethodMap.put(ctBehavior.getLongName(), mInsertMethodCount.incrementAndGet());

          // Returns true if this is not a constructor or a class initializer (static initializer)
          if (ctBehavior.getMethodInfo().isMethod()) {
            try {
              CtMethod ctMethod = (CtMethod) ctBehavior;
              boolean isStatic = (AccessFlag.STATIC & ctMethod.getModifiers()) != 0;
              CtClass returnType = ctMethod.getReturnType();
              String returnTypeString = returnType.getName();

              // 代码插入
              StringBuilder body = new StringBuilder("Object argThis = null;");
              if (!isStatic) {
                body.append("argThis = $0;");
              }
              String parameterClassTypesBody = getParameterClassTypesBody(ctMethod);
              body.append("   if (com.beggar.hotfix.patch.PatchProxy.isSupport($args, argThis, " +
                  Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic +
                  ", " + mMethodMap.get(ctBehavior.getLongName()) + "," + parameterClassTypesBody +
                  "," + returnTypeString + ".class)) {");
              body.append(getReturnStatement(
                  returnTypeString,
                  isStatic,
                  mMethodMap.get(ctBehavior.getLongName()),
                  parameterClassTypesBody,
                  returnTypeString + ".class"));
              body.append("  }");
              ctBehavior.insertBefore(body.toString());
            } catch (Throwable e) {
              e.printStackTrace();
            }
          }
        }
      }
      // 把class文件打入zip
      zipFile(ctClass.toBytecode(), zipOutputStream,
          ctClass.getName().replaceAll("\\.", "/") + ".class");
    }
    zipOutputStream.close();
  }

  /**
   * 返回return语句
   *
   * @param type                    函数返回类型
   * @param isStatic                是否是静态方法
   * @param methodNumber
   * @param parameterClassTypesBody 函数的参数类型
   * @param returnTypeString        返回类型(.class)
   */
  private String getReturnStatement(
      String type,
      boolean isStatic,
      Integer methodNumber,
      String parameterClassTypesBody,
      String returnTypeString) {
    switch (type) {
      case Constants.VOID:
        return "    com.beggar.hotfix.patch.PatchProxy.accessDispatchVoid( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + ", " + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + ");";
      case Constants.LANG_VOID:
        return "    com.beggar.hotfix.patch.PatchProxy.accessDispatchVoid( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + ", " + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + ");   return null;";


      case Constants.BOOLEAN:
        return "   return ((" + Constants.BOOLEAN +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch($args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_BOOLEAN:
        return
            "   return ((java.lang.Boolean)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.INT:
        return "   return ((" + Constants.INT +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + ")).intValue();";
      case Constants.LANG_INT:
        return
            "   return ((java.lang.Integer)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + ")); ";

      case Constants.LONG:
        return "   return ((" + Constants.LONG +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_LONG:
        return
            "   return ((java.lang.Long)com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args," +
                " argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.DOUBLE:
        return "   return ((" + Constants.DOUBLE +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_DOUBLE:
        return
            "   return ((java.lang.Double)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.FLOAT:
        return "   return ((" + Constants.FLOAT +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_FLOAT:
        return
            "   return ((java.lang.Float)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.SHORT:
        return "   return ((" + Constants.SHORT +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_SHORT:
        return
            "   return ((java.lang.Short)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.BYTE:
        return "   return ((" + Constants.BYTE +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + "));";
      case Constants.LANG_BYTE:
        return
            "   return ((java.lang.Byte)com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args," +
                " argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      case Constants.CHAR:
        return "   return ((" + Constants.CHAR +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + ");";
      case Constants.LANG_CHARACTER:
        return
            "   return ((java.lang.Character)com.beggar.hotfix.patch.PatchProxy.accessDispatch( " +
                "$args, argThis, " +
                Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
                parameterClassTypesBody + "," + returnTypeString + "));";

      default:
        return "   return (" + type +
            ")com.beggar.hotfix.patch.PatchProxy.accessDispatch( $args, argThis, " +
            Constants.HOTFIX_INSERT_FIELD_NAME + ", " + isStatic + "," + methodNumber + "," +
            parameterClassTypesBody + "," + returnTypeString + ");";
    }
  }

  /**
   * 获得方法的参数类型列表
   *
   * @return String： new Class[] {com.xx.xx.xx.class, com.xx.xx.xx.class}
   */
  private String getParameterClassTypesBody(@NonNull CtMethod ctMethod) throws NotFoundException {
    if (ctMethod.getParameterTypes().length == 0) {
      return "null";
    }
    StringBuilder parameterTypes = new StringBuilder();
    parameterTypes.append("new Class[] {");
    for (CtClass parameterClass : ctMethod.getParameterTypes()) {
      parameterTypes.append(parameterClass.getName()).append(".class, ");
    }

    if (',' == parameterTypes.charAt(parameterTypes.length() - 1)) {
      parameterTypes.deleteCharAt(parameterTypes.length() - 1);
    }
    parameterTypes.append("}");
    return parameterTypes.toString();
  }

  /**
   * 判断方法是否可以修复
   */
  private boolean isQualifiedMethod(@NonNull CtBehavior ctBehavior) {
    // 方法是否是静态块构造
    if (ctBehavior.getMethodInfo().isStaticInitializer()) {
      return false;
    }

    // 合成方法不处理。
    // 1. 内部类和外部类的相互访问private，是生成了public synthetic access方法
    // 2. lambda表达式，生成了private static synthetic lambda$xxx方法 (如=this::fun这宗写法的不会额外生成方法)
    if ((ctBehavior.getModifiers() & AccessFlag.SYNTHETIC) != 0) {
      return false;
    }

    // 构造函数不处理
    if (ctBehavior.getMethodInfo().isConstructor()) {
      return false;
    }

    // 抽象方法不处理
    if ((ctBehavior.getModifiers() & AccessFlag.ABSTRACT) != 0) {
      return false;
    }

    // native修饰的方法不处理
    if ((ctBehavior.getModifiers() & AccessFlag.NATIVE) != 0) {
      return false;
    }

    //方法过滤
    for (String exceptMethod : mExceptMethodList) {
      if (ctBehavior.getName().matches(exceptMethod)) {
        return false;
      }
    }

//    for (String name : mHotfixMethodList) {
//      if (ctBehavior.getName().matches(name)) {
//        return true;
//      }
//    }
    return true;
  }

}
