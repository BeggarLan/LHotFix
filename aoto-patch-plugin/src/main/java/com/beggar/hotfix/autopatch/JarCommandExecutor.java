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

  // baksmali-2.1.3.jar，将dex文件转换成便于阅读的smali文件
  private String mBaksmaliToolFilePath;

  // smali-2.1.3.jar，将smali文件重新生成回dex文件
  private String mSmaliToolFilePath;

  public JarCommandExecutor(
      @NonNull String subProcessWorkingDirPath,
      @NonNull String dxToolFailPath,
      @NonNull String baksmaliToolFilePath,
      @NonNull String smaliToolFilePath) {
    mSubProcessWorkingDirPath = subProcessWorkingDirPath;
    mDxToolFailPath = dxToolFailPath;
    mBaksmaliToolFilePath = baksmaliToolFilePath;
    mSmaliToolFilePath = smaliToolFilePath;
  }

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

    return executeCommand(commandBuilder.toString());
  }

  /**
   * dex文件转smali文件
   * java -jar baksmali-2.1.3.jar -o [输出文件夹] dex文件
   *
   * @param dexFileName      输入的dex文件
   * @param outputSubFileDir 输出文件所在当前目录的子文件夹
   */
  public Process dex2Smali(@NonNull String dexFileName, @NonNull String outputSubFileDir)
      throws IOException {
    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder
        .append("java -jar " + mBaksmaliToolFilePath)
        .append(" -o " + outputSubFileDir)
        .append(" " + dexFileName);
    return executeCommand(commandBuilder.toString());
  }

  /**
   * smali文件转dex文件
   * java -jar smali-2.1.3.jar -o 目标dex文件 [smali文件夹]
   *
   * @param smaliFileSubDirPath smali文件夹相对当前目录的文件夹路径
   * @param outputDexFileName   [工具包文件夹下]输出的dex文件
   */
  public Process smali2Dex(@NonNull String smaliFileSubDirPath, @NonNull String outputDexFileName)
      throws IOException {
    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder
        .append("java -jar " + mSmaliToolFilePath)
        .append(" -o " + outputDexFileName)
        .append(" " + smaliFileSubDirPath);
    return executeCommand(commandBuilder.toString());
  }

  /**
   * 执行命令
   */
  @NonNull
  private Process executeCommand(@NonNull String command) throws IOException {
    return Runtime.getRuntime().exec(
        command, null, new File(mSubProcessWorkingDirPath));
  }

}
