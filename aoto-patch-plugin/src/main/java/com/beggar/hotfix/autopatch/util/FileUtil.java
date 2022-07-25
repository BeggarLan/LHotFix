package com.beggar.hotfix.autopatch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.android.annotations.NonNull;

/**
 * author: lanweihua
 * created on: 2022/7/25 1:02 下午
 * description: 文件工具类
 */
public class FileUtil {

  /**
   * 把文件压入zip
   */
  public static void zipFile(
      @NonNull File inputFile,
      @NonNull String entryName,
      @NonNull ZipOutputStream zipOutputStream) throws IOException {
    ZipEntry entry = new ZipEntry(entryName);
    zipOutputStream.putNextEntry(entry);
    FileInputStream fis = new FileInputStream(inputFile);
    byte[] buffer = new byte[4092];
    int byteCount = 0;
    while ((byteCount = fis.read(buffer)) != -1) {
      zipOutputStream.write(buffer, 0, byteCount);
    }
    fis.close();
    zipOutputStream.closeEntry();
    zipOutputStream.flush();
  }

}
