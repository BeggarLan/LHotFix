package com.beggar.hotfix.gradle.plugin.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;

import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;

/**
 * author: BeggarLan
 * created on: 2022/6/27 11:16 下午
 * description: Javassist工具类
 * <a href="https://blog.csdn.net/sid1109217623/article/details/89851056"> 官方文档翻译 </a>
 */
public class JavassistUtil {

  public static final String VOID = "void";

  /**
   * 把inputs中的class转换成 CtClass
   *
   * @param inputs
   * @param classPool
   */
  public static List<CtClass> toCtClasses(@NonNull Collection<TransformInput> inputs,
      @NonNull ClassPool classPool) {
    List<String> classNames = new ArrayList<>();
    List<CtClass> ctClasses = new ArrayList<>();

    long startTime = System.currentTimeMillis();
    for (TransformInput input : inputs) {
      // 源码
      for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
        File dirFile = directoryInput.getFile();
        String dirPath = dirFile.getAbsolutePath();
        //将当前路径加入类池,不然找不到这个类
        try {
          classPool.insertClassPath(dirPath);
          // 所有的子文件(递归)
          Collection<File> childFiles = FileUtils.listFiles(dirFile, null, true);
          for (File childFile : childFiles) {
            String childFileAbsolutePath = childFile.getAbsolutePath();
            // .class结尾
            if (childFileAbsolutePath.endsWith(SdkConstants.DOT_CLASS)) {
              String className = childFileAbsolutePath
                  .substring(dirPath.length() + 1,
                      childFileAbsolutePath.length() - SdkConstants.DOT_CLASS.length())
                  .replaceAll(Matcher.quoteReplacement(File.separator), ".");
              if (classNames.contains(className)) {
                throw new RuntimeException(
                    "with the same name : " + className + " please remove duplicate classes ");
              }
              classNames.add(className);
            }
          }
        } catch (NotFoundException e) {
          e.printStackTrace();
          System.out.println("class dir not found");
        }

      }

      //jar(aar等)
      for (JarInput jarInput : input.getJarInputs()) {
        try {
          classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
          JarFile jarFile = new JarFile(jarInput.getFile());
          Enumeration<JarEntry> classes = jarFile.entries();

          while (classes.hasMoreElements()) {
            JarEntry libClass = classes.nextElement();
            String className = libClass.getName();
            if (className.endsWith(SdkConstants.DOT_CLASS)) {
              className = className
                  .substring(0, className.length() - SdkConstants.DOT_CLASS.length())
                  .replaceAll("/", ".");
              if (classNames.contains(className)) {
                throw new RuntimeException(
                    "You have duplicate classes with the same name : " + className +
                        " please remove duplicate classes ");
              }
              classNames.add(className);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("jarInput to JarFile IOException");
        } catch (NotFoundException e) {
          e.printStackTrace();
          System.out.println("jarInput to JarFile NotFoundException");
        }
      }
    }
    // 花费的时间
    long costTimeSec = (System.currentTimeMillis() - startTime) / 1000;
    System.out.println("read all class file cost " + costTimeSec + " second");
    for (String className : classNames) {
      try {
        ctClasses.add(classPool.get(className));
      } catch (NotFoundException e) {
        e.printStackTrace();
        System.out.println("class not found exception class name: " + className);
      }
    }

    Collections.sort(ctClasses, new Comparator<CtClass>() {
      @Override
      public int compare(CtClass class1, CtClass class2) {
        return class1.getName().compareTo(class2.getName());
      }
    });
    return ctClasses;
  }

  /**
   * clone class
   *
   * @param sourceClass      原class
   * @param targetClassName  目标类name
   * @param exceptMethodList class中哪些方法不需要clone
   * @return targetClass
   */
  public static CtClass cloneClass(
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull String targetClassName,
      @NonNull List<CtMethod> exceptMethodList) throws NotFoundException, CannotCompileException {
    CtClass patchClass = classPool.makeClass(targetClassName);
    // 设置java版本
    patchClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
    // 设置父类、interface
    patchClass.setSuperclass(sourceClass.getSuperclass());
    patchClass.setInterfaces(sourceClass.getInterfaces());

    // clone field
    for (CtField ctField : sourceClass.getDeclaredFields()) {
      patchClass.addField(new CtField(ctField, patchClass));
    }

    // 替换类定义、方法中的类名
    ClassMap classMap = new ClassMap();
    classMap.put(targetClassName, sourceClass.getName());
    classMap.fix(sourceClass);

    // clone方法
    for (CtMethod ctMethod : sourceClass.getDeclaredMethods()) {
      // 过滤
      if (exceptMethodList.contains(ctMethod)) {
        continue;
      }
      CtMethod cloneMethod = new CtMethod(ctMethod, patchClass, classMap);
      patchClass.addMethod(cloneMethod);
    }

    return patchClass;
  }

  /**
   * 获得方法签名，
   * 带参数类型的描述，例如：javassist.CtMethod.setBody(String)
   */
  @NonNull
  public static String getMethodSignatureName(@NonNull CtMethod ctMethod) {
    return ctMethod.getLongName();
  }


  /**
   * 获得方法的参数签名
   */
  @NonNull
  public static String getMethodParameterSignature(@NonNull CtMethod ctMethod)
      throws NotFoundException {
    StringBuilder stringBuilder = new StringBuilder();
    CtClass[] parameterTypes = ctMethod.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; ++i) {
      CtClass parameterClass = parameterTypes[i];
      stringBuilder.append(parameterClass.getName()).append(" arg").append(i);
      if (i != parameterTypes.length - 1) {
        stringBuilder.append(", ");
      }
    }
    return stringBuilder.toString();
  }

  public static boolean isStatic(@NonNull CtField ctField) {
    return (ctField.getModifiers() & AccessFlag.STATIC) != 0;
  }

  /**
   * 判断某方法是否是void类型的
   */
  public static boolean isVoidMethod(@NonNull CtMethod ctMethod) throws NotFoundException {
    return ctMethod.getReturnType().getName().equals(VOID);
  }

}
