package com.beggar.hotfix.patch;

import java.util.List;

/**
 * author: BeggarLan
 * created on: 2022/6/10 10:58 下午
 * description: 用于获取[原类]和[补丁]类信息
 */
public interface PatchedClassInfoProvider {

  List<PatchedClassInfo> getPatchedClassInfoList();

}
