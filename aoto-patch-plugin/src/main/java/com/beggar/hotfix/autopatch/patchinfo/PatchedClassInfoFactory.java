package com.beggar.hotfix.autopatch.patchinfo;

import static com.beggar.hotfix.autopatch.AutoPatchConstants.PATCHED_CLASS_INFO_PROVIDER_IMPL;

import java.util.ArrayList;
import java.util.List;

import com.android.annotations.NonNull;
import com.beggar.hotfix.autopatch.AutoPatchConfig;
import com.beggar.hotfix.autopatch.AutoPatchConstants;
import com.beggar.hotfix.autopatch.NameManager;
import com.beggar.hotfix.patch.PatchedClassInfo;
import com.beggar.hotfix.patch.PatchedClassInfoProvider;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;

/**
 * author: lanweihua
 * created on: 2022/7/23 3:24 下午
 * description: 用来构造{@link PatchedClassInfoProvider}
 */
public class PatchedClassInfoFactory {

  private static final String TAG = "PatchedClassInfoFactory";

  @NonNull
  public static CtClass createPatchedClassInfoProviderClass(
      @NonNull ClassPool classPool,
      @NonNull AutoPatchConfig autoPatchConfig) {
    try {
      // com.beggar.hotfix.patch.PatchedClassInfoProviderImpl
      CtClass patchedClassInfoProviderImplClass =
          classPool.makeClass(
              AutoPatchConstants.PATCH_CLASS_PACKAGE_NAME + "." + PATCHED_CLASS_INFO_PROVIDER_IMPL);
      // 设置java版本
      patchedClassInfoProviderImplClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
      // 设置继承自{@link PatchedClassInfoProvider}
      patchedClassInfoProviderImplClass
          .setInterfaces(new CtClass[]{classPool.get(PatchedClassInfoProvider.class.getName())});

      // 实现接口方法
//      public java.util.List getPatchedClassInfoList() {
//        java.util.List patchedClassesInfos = new java.util.ArrayList();
//
//        // *************** 这里循环把所有的PatchedClassInfo都添加进去
//        PatchedClassInfo patchedClassInfoX = new PatchedClassInfo(sourceClassName,
//        patchControlClassName);
//        patchedClassesInfos.add(patchedClassInfoX);
//        // *********************************
//
//        return patchedClassesInfos;
//      }
      StringBuilder getPatchClassInfoListMethodBuilder = new StringBuilder();
      getPatchClassInfoListMethodBuilder
          .append("public " + List.class.getName() + " getPatchedClassInfoList(){");

      getPatchClassInfoListMethodBuilder
          .append(List.class.getName() + "patchedClassesInfos = new " + ArrayList.class.getName() +
              "();");
      for (int i = 0; i < autoPatchConfig.mModifyClassList.size(); ++i) {
        String sourceClassName = autoPatchConfig.mModifyClassList.get(i);
        String patchedClassInfoName = "patchedClassInfo" + i;
        getPatchClassInfoListMethodBuilder
            .append(PatchedClassInfo.class.getName() + patchedClassInfoName + "=")
            .append("new " + PatchedClassInfo.class.getName() + "(")
            .append(sourceClassName + ", ")
            .append(NameManager.getInstance().getPatchControlClassName(
                sourceClassName.substring(sourceClassName.lastIndexOf(".") + 1)))
            .append(");");

        getPatchClassInfoListMethodBuilder
            .append("patchedClassesInfos.add(" + patchedClassInfoName + ");");
      }

      getPatchClassInfoListMethodBuilder
          .append("return patchedClassesInfos;")
          .append("}");

      CtMethod getPatchClassInfoListMethod = CtMethod
          .make(getPatchClassInfoListMethodBuilder.toString(), patchedClassInfoProviderImplClass);
      patchedClassInfoProviderImplClass.addMethod(getPatchClassInfoListMethod);

      return patchedClassInfoProviderImplClass;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(TAG + "[createPatchClassInfoProviderClass] error");
    }
  }

}
