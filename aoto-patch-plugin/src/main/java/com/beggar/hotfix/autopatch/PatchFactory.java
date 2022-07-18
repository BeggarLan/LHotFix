package com.beggar.hotfix.autopatch;

import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_CONSTRUCTOR_NAME;
import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCH_CLASS_FIELD_SOURCE_CLASS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;

import com.android.annotations.NonNull;
import com.android.dx.rop.code.AccessFlags;
import com.beggar.hotfix.base.util.JavassistUtil;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

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
      @NonNull String patchGenerateDirPath)
      throws NotFoundException, CannotCompileException, IOException {
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
    logger.quiet(TAG + "createPatchClass clone class");
    CtClass patchClass =
        JavassistUtil.cloneClass(classPool, sourceClass, patchClassName, noNeedPatchMethod);
    // 没有方法的时候
    if (patchClass.getDeclaredMethods().length == 0) {
      throw new RuntimeException(patchClass.getName() + ": patch class has no method.");
    }
    // 添加构造器
    addConstructor(logger, sourceClass, patchClass);
    // 处理super.xxx()
    List<CtMethod> addedSuperAccessMethodList = handleInvokeSuperMethod(
        logger,
        classPool,
        sourceClass,
        patchClass,
        patchConfig.mInvokeSuperMethodMap.get(sourceClass),
        patchGenerateDirPath);

    // 为类中的每一个private方法增加public的访问方法
    List<CtMethod> addedPublicMethodForPrivate = createPublicMethodForPrivate(logger, patchClass);

    for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
      // 为super.xxx()增加的访问方法
      if (addedSuperAccessMethodList.contains(ctMethod)) {
        continue;
      }
      // 为private方法增加的public方法
      if (addedPublicMethodForPrivate.contains(ctMethod)) {
        continue;
      }

      // 替换方法的表达式
      ctMethod.instrument(new ExprEditor() {

        // 编辑字段访问表达式（可覆盖）。字段访问意味着读取和写入。默认实现不执行任何操作
        // 如果读写的字段是patchClass的，那么替换为原类的对应字段
        @Override
        public void edit(FieldAccess f) throws CannotCompileException {
          // 新增类不需要处理
          if (patchConfig.mNewClassList.contains(f.getClassName())) {
            return;
          }
          // 把patchClassInstance内的字段(类filed)操作都转移到sourceClassInstance中
          // 字段访问
          if (f.isReader()) {
            try {
              f.replace(
                  PatchUtil.getFieldAccessReadReplaceString(f.getField(), sourceClass, patchClass));
            } catch (NotFoundException e) {
              e.printStackTrace();
              // TODO: 2022/7/4 log
            }
          } else if (f.isWriter()) {
            // 字段赋值
            try {
              f.replace(
                  PatchUtil
                      .setFieldAccessWriteReplaceString(f.getField(), sourceClass, patchClass));
            } catch (NotFoundException e) {
              e.printStackTrace();
            }
          }
        }

        // 编辑新表达式（可覆盖）。默认实现不执行任何操作。参数： e - 创建对象的新表达式
        // 如果构造器中的某参数是patchClass的，那么替换为SourceClass的
        @Override
        public void edit(NewExpr e) throws CannotCompileException {
          super.edit(e);
        }

        // 编辑显式类型转换的表达式（可重写）。默认实现不执行任何操作。
        @Override
        public void edit(Cast c) throws CannotCompileException {
          super.edit(c);
        }

        // 编辑方法调用（可覆盖）。默认实现不执行任何操作。
        @Override
        public void edit(MethodCall m) throws CannotCompileException {
          super.edit(m);
        }
      });
    }

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
      logger.quiet(
          TAG + "addConstructor, sourceClass:" + sourceClass.getName() + ", patchClass:" +
              patchClass.getName());
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
   * 处理调用super方法, 会生成同名的public static的代理方法
   *
   * @return 增加的访问方法
   */
  private List<CtMethod> handleInvokeSuperMethod(
      @NonNull Logger logger,
      @NonNull ClassPool classPool,
      @NonNull CtClass sourceClass,
      @NonNull CtClass patchClass,
      @NonNull List<CtMethod> invokeSuperMethodList,
      @NonNull String patchGenerateDirPath)
      throws NotFoundException, CannotCompileException, IOException {
    logger.quiet(
        TAG + "handleInvokeSuperMethod, invokeSuperMethodList:" + invokeSuperMethodList);
    List<CtMethod> addedSuperAccessMethodList = new ArrayList<>();
    for (CtMethod ctMethod : invokeSuperMethodList) {
      // 生成新方法
      /*
       public static methodName(SourceClass sourceClassInstance, PatchClass patchClassInstance,
       xxx参数) {
          // 这个assist类：PatchClass.getName+ASSIST_SUFFIX
          return assist类.methodName(sourceClassInstance,patchClassInstance,xxx参数);
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

      if (ctMethod.getReturnType().equals(CtClass.voidType)) {
        methodBuilder.append("return ");
      }
      methodBuilder.append(NameManager.getInstance().getAssistClassName(patchClass.getName()))
          .append(".")
          .append(ctMethod.getName())
          .append("(")
          .append("sourceClassInstance, patchClassInstance");
      if (parameterTypes.length > 0) {
        methodBuilder.append(",")
            .append(JavassistUtil.getMethodParameterSignature(ctMethod))
            .append(");");
      }
      methodBuilder.append("}");

      CtMethod newMethod = CtMethod.make(methodBuilder.toString(), patchClass);
      patchClass.addMethod(newMethod);
      addedSuperAccessMethodList.add(newMethod);
    }
    return addedSuperAccessMethodList;
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
    String assistClassName = NameManager.getInstance().getAssistClassName(patchClass.getName());

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

  /**
   * 为类中的每一个private方法增加public的访问方法
   * private returnType methodName(xx参数)
   * -->
   * public returnType methodNameHotfixPublic(xx参数) {
   * return methodName(xx参数)
   * }
   *
   * @return 增加的方法
   */
  private List<CtMethod> createPublicMethodForPrivate(@NonNull Logger logger,
      @NonNull CtClass ctClass)
      throws CannotCompileException, NotFoundException {
    logger.quiet(TAG + "createPublicMethodForPrivate start.");
    List<CtMethod> addedPublicMethod = new ArrayList<>();

    CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
    for (CtMethod ctMethod : declaredMethods) {
      // private方法
      if (AccessFlags.isPrivate(ctMethod.getModifiers())) {
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public ");
        // static修饰和原方法保持一致
        if (AccessFlags.isStatic(ctMethod.getModifiers())) {
          methodBuilder.append(" static ");
        }
        methodBuilder.append(ctMethod.getReturnType().getName())
            .append(" ")
            .append(ctMethod.getName() + AutoPatchConstants.PUBLIC_SUFFIX)
            .append("(")
            .append(JavassistUtil.getMethodParameterSignature(ctMethod))
            .append(") {");
        if (ctMethod.getReturnType().equals(CtClass.voidType)) {
          methodBuilder.append("return ");
        }
        methodBuilder.append(ctMethod.getName())
            .append("(")
            .append(JavassistUtil.getMethodParameterSignature(ctMethod))
            .append(")")
            .append("}");

        CtMethod newMethod = CtMethod.make(methodBuilder.toString(), ctClass);
        ctClass.addMethod(newMethod);
        addedPublicMethod.add(newMethod);
      }
    }
    return addedPublicMethod;
  }

}
