package com.beggar.hotfix.autopatch;

import java.util.List;

import org.gradle.api.logging.Logger;

import com.android.annotations.NonNull;
import com.beggar.hotfix.base.Constants;
import com.beggar.hotfix.base.annotation.Add;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * author: lanweihua
 * created on: 2022/6/17 7:11 下午
 * description: 注解处理
 */
public class HotFixAnnotationHandler {

  private static final String TAG = "HotFixAnnotationHandler";

  @NonNull
  private final AutoPatchConfig mAutoPatchConfig;
  @NonNull
  private final List<CtClass> mCtClasses;
  @NonNull
  private final Logger mLogger;

  public HotFixAnnotationHandler(
      @NonNull AutoPatchConfig autoPatchConfig,
      @NonNull List<CtClass> ctClasses,
      @NonNull Logger logger) {
    mAutoPatchConfig = autoPatchConfig;
    mCtClasses = ctClasses;
    mLogger = logger;
  }

  /**
   * 处理 Add和Modify
   */
  public void handleAnnotation() {
    mLogger.quiet(TAG + " handleAnnotation start.");

    // 拿到两个注解class
    Class addAnnotationClass = null;
    Class modifyAnnotationClass = null;
    try {
      addAnnotationClass =
          mCtClasses.get(0).getClassPool().get(Constants.ADD_CLASS_NAME).toClass();
      modifyAnnotationClass =
          mCtClasses.get(0).getClassPool().get(Constants.MODIFY_CLASS_NAME).toClass();
    } catch (CannotCompileException | NotFoundException e) {
      mLogger.error(TAG + " handleAnnotation: get Add or Modify class error");
      e.printStackTrace();
      return;
    }

    for (CtClass ctClass : mCtClasses) {
      // 先处理该类被add
      boolean isClassAdd = handleAddNewClass(ctClass, addAnnotationClass);
      if (isClassAdd) {
        continue;
      }
      // TODO: 2022/6/20 方法add和modify

    }
    mLogger.quiet(TAG + " handleAnnotation end.");
  }

  /**
   * 新增类处理
   *
   * @return {@code true} 该类是新增类(被add注解)
   */
  private boolean handleAddNewClass(@NonNull CtClass ctClass, @NonNull Class addAnnotationClass) {
    mLogger.quiet(TAG + " handleAddNewClass");
    try {
      Add addAnnotation = (Add) ctClass.getAnnotation(addAnnotationClass);
      if (addAnnotation != null) {
        if (!mAutoPatchConfig.mNewClassList.contains(ctClass.getName())) {
          mAutoPatchConfig.mNewClassList.add(ctClass.getName());
        }
        return true;
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return false;
  }

}
