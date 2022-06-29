package com.beggar.hotfix.autopatch;

import java.util.List;

import com.android.annotations.NonNull;

import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;

/**
 * author: lanweihua
 * created on: 2022/6/27 11:16 下午
 * description: Javassist工具类
 */
public class JavassistUtil {

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
   * 获得方法签名
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

}
