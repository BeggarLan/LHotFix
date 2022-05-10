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

          if (!isQualifiedMethod(ctBehavior)) {
            continue;
          }

        }
      }
      // 把class文件打入zip
      zipFile(ctClass.toBytecode(), zipOutputStream,
          ctClass.getName().replaceAll("\\.", "/") + ".class");
    }
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

    for (String name : mHotfixMethodList) {
      if (ctBehavior.getName().matches(name)) {
        return true;
      }
    }
    return true;
  }

}
