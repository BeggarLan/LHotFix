package com.beggar.hotfix.autopatch;

import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_CONSTRUCTOR_NAME;
import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS;

import java.io.IOException;
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
import javassist.bytecode.ClassFile;

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
   * @param sourceClass          要补丁的class
   * @param patchClassName       补丁类name
   * @param patchGenerateDirPath 补丁生成文件夹路径
   * @return 生成的补丁类
   */
  public CtClass createPatchClass(
      @NonNull Logger logger,
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull String patchClassName,
      @NonNull AutoPatchConfig patchConfig,
      @NonNull String patchGenerateDirPath) throws NotFoundException, CannotCompileException {
    logger.quiet(TAG + "createPatchClass start. sourceClass:" + sourceClass.getName());
    // 不需要patch的方法
    List<CtMethod> noNeedPatchMethod = new ArrayList<>();
    for (CtMethod ctMethod : sourceClass.getDeclaredMethods()) {
      String methodSignatureName = JavassistUtil.getMethodSignatureName(ctMethod);
      // 新增方法
      if (patchConfig.mNewMethodList.contains(methodSignatureName)) {
        continue;
      }
      if (!patchConfig.mModifyMethodSignatureList.contains(methodSignatureName)) {
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

  /**
   * 处理调用super方法
   */
  private void handleInvokeSuperMethod(
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass,
      @NonNull List<CtMethod> invokeSuperMethodList,
      @NonNull String patchGenerateDirPath)
      throws NotFoundException, CannotCompileException, IOException {
    for (CtMethod ctMethod : invokeSuperMethodList) {
      // 生成新方法
      /*
       public static methodName(SourceClass sourceClassInstance, PatchClass patchClassInstance,
       xxx参数) {

       }
       */
      StringBuilder methodBuilder = new StringBuilder();

      CtClass[] parameterTypes = ctMethod.getParameterTypes();
      // 方法有参数
      if (parameterTypes.length > 0) {
        methodBuilder.append("public static ")
            .append(ctMethod.getReturnType().getName() + " " + ctMethod.getName())
            .append("(")
            .append(sourceClass.getName() + "sourceClassInstance, ")
            .append(patchClass.getName() + "patchClassInstance, ")
            .append(JavassistUtil.getMethodParameterSignature(ctMethod))
            .append("){");
      } else {
        methodBuilder.append("public static ")
            .append(ctMethod.getReturnType().getName() + " " + ctMethod.getName())
            .append("(")
            .append(sourceClass.getName() + "sourceClassInstance, ")
            .append(patchClass.getName() + "patchClassInstance, ")
            .append("){");
      }

      // 创建方法的assitClass
      createInvokeSuperMethodAssistClass(
          classPool, sourceClass, patchClass, ctMethod, patchGenerateDirPath);

      methodBuilder.append("}");

    }
  }

  /**
   * 创建super.xxx()的AssistClass
   */
  private void createInvokeSuperMethodAssistClass(
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass,
      @NonNull CtMethod invokeSuperMethod,
      @NonNull String patchGenerateDirPath)
      throws NotFoundException, CannotCompileException, IOException {
    // assist类名
    String assistClassName = NameUtil.getAssistClassName(patchClass.getName());

    // 创建assistClass
    CtClass assistClass = classPool.getOrNull(assistClassName);
    if (assistClass == null) {
      assistClass = classPool.makeClass(assistClassName);
      assistClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
      if (sourceClass.getSuperclass() != null) {
        assistClass.setSuperclass(sourceClass.getSuperclass());
      }
      if (sourceClass.getInterfaces() != null) {
        assistClass.setInterfaces(sourceClass.getInterfaces());
      }
    }

    // 先解冻，后面可以修改
    if (assistClass.isFrozen()) {
      assistClass.defrost();
    }

    // 生成方法
    /*
      public static methodName(SourceClass sourceClassInstance, PatchClass patchClassInstance,
       xxx参数) {
        return patchClassInstance.methodName(xxx参数);
       }
     */
    StringBuilder methodBuilder = new StringBuilder();
    CtClass[] parameterTypes = invokeSuperMethod.getParameterTypes();
    // 方法有参数
    if (parameterTypes.length > 0) {
      methodBuilder.append("public static ")
          .append(invokeSuperMethod.getReturnType().getName() + " " + invokeSuperMethod.getName())
          .append("(")
          .append(sourceClass.getName() + "sourceClassInstance, ")
          .append(patchClass.getName() + "patchClassInstance, ")
          .append(JavassistUtil.getMethodParameterSignature(invokeSuperMethod))
          .append("){");
    } else {
      methodBuilder.append("public static ")
          .append(invokeSuperMethod.getReturnType().getName() + " " + invokeSuperMethod.getName())
          .append("(")
          .append(sourceClass.getName() + "sourceClassInstance, ")
          .append(patchClass.getName() + "patchClassInstance, ")
          .append("){");
    }

    methodBuilder.append("return patchClassInstance." + invokeSuperMethod.getName() + "(")
        .append(JavassistUtil.getMethodParameterSignature(invokeSuperMethod))
        .append(");")
        .append("}");

    CtMethod ctMethod = CtMethod.make(methodBuilder.toString(), assistClass);
    assistClass.addMethod(ctMethod);
    assistClass.writeFile(patchGenerateDirPath);
  }

}
