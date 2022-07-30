package com.beggar.hotfix.autopatch;

import java.io.File;
import java.io.IOException;

import com.android.annotations.NonNull;

/**
 * author: lanweihua
 * created on: 2022/7/30 3:50 下午
 * description: 一些jar相关的命令
 */
public class JarCommandExecutor {

  // the working directory of the subprocess
  private String mSubProcessWorkingDirPath;

  // dx工具包, 此处用来把jar包转dex包
  private String mDxToolFailPath;

  /**
   * 把jar文件转为dex文件
   *
   * @param jarFileName                 输入的 [xx.jar] 文件名, 该文件和mDxToolFailPath在同一文件夹下
   * @param outputDexFileWithSuffixName 输出的 [xx.dex] 文件名, 该文件和mDxToolFailPath在同一文件夹下
   */
  public Process jar2Dex(@NonNull String jarFileName, @NonNull String outputDexFileWithSuffixName)
      throws IOException {
    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder
        .append("java -jar " + mDxToolFailPath + "--dex")
        .append(" --output=" + outputDexFileWithSuffixName)
        .append(" " + jarFileName);

    Process process = Runtime.getRuntime()
        .exec(commandBuilder.toString(), null, new File(mSubProcessWorkingDirPath));
    return process;
  }


}
