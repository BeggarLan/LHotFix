package com.beggar.hotfix.autopatch;

import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_CONSTRUCTOR_NAME;
import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;

import com.android.annotations.NonNull;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

/**
 * author: lanweihua
 * created on: 2022/6/27 10:44 下午
 * description: 构造补丁
 */
public class PatchFactory {

  private static final String TAG = "PatchFactory";

  private static PatchFactory sInstance = new PatchFactory();

  public static PatchFactory getInstance() {
    return sInstance;
  }

  /**
   * 创建补丁
   *
   * @param sourceClass               要补丁的class
   * @param patchClassName            补丁类name
   * @param newMethodList             新增的方法(所有类的)
   * @param modifyMethodSignatureList 修改的方法(所有类的)
   * @param patchGenerateDirPath      补丁生成文件夹路径
   * @return 生成的补丁类
   */
  public CtClass createPatchClass(
      @NonNull Logger logger,
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull String patchClassName,
      @NonNull List<String> newMethodList,
      @NonNull List<String> modifyMethodSignatureList,
      @NonNull String patchGenerateDirPath) throws NotFoundException, CannotCompileException {
    logger.quiet(TAG + "createPatchClass start. sourceClass:" + sourceClass.getName());
    // 不需要patch的方法
    List<CtMethod> noNeedPatchMethod = new ArrayList<>();
    for (CtMethod ctMethod : sourceClass.getDeclaredMethods()) {
      String methodSignatureName = JavassistUtil.getMethodSignatureName(ctMethod);
      // 新增方法
      if (newMethodList.contains(methodSignatureName)) {
        continue;
      }
      if (!modifyMethodSignatureList.contains(methodSignatureName)) {
        noNeedPatchMethod.add(ctMethod);
      }
    }

    // clone出补丁类
    CtClass patchClass =
        JavassistUtil.cloneClass(classPool, sourceClass, patchClassName, noNeedPatchMethod);
    // 没有方法的时候
    if (patchClass.getDeclaredMethods().length == 0) {
      throw new RuntimeException(patchClass.getName() + ": patch class has no method.");
    }
    // 添加构造器
    addConstructor(logger, sourceClass, patchClass);

    logger.quiet(TAG + "createPatchClass end. patchClass:" + patchClass.getName());
    return patchClass;
  }

  /**
   * 给补丁类加构造器
   *
   * @param sourceClass 原类
   * @param patchClass  补丁类
   */
  private void addConstructor(
      @NonNull Logger logger, @NonNull CtClass sourceClass, @NonNull CtClass patchClass) {
    try {
      // 类型是sourceClass，名字是mSourceClass
      CtField ctField = new CtField(sourceClass, PATCH_CLASS_FIELD_SOURCE_CLASS, patchClass);
      patchClass.addField(ctField);
      StringBuilder constructorCode = new StringBuilder();
      constructorCode
          .append("public " + PATCH_CLASS_CONSTRUCTOR_NAME + "(Object o) {")
          .append(PATCH_CLASS_FIELD_SOURCE_CLASS + "=(" + sourceClass.getName() + ")o;")
          .append("}");
      CtConstructor constructor = CtNewConstructor.make(constructorCode.toString(), patchClass);
      patchClass.addConstructor(constructor);
    } catch (CannotCompileException e) {
      logger.error(TAG + "addConstructor error");
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

}
