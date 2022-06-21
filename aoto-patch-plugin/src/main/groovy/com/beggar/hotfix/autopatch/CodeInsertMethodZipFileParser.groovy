package com.beggar.hotfix.autopatch

import com.android.annotations.NonNull
import org.gradle.api.logging.Logger

import java.util.zip.GZIPInputStream;

/**
 * author: lanweihua
 * created on: 2022/6/21 9:47 下午
 * description: 代码插桩的method文件解析器
 */
public class CodeInsertMethodZipFileParser {

    private static final String TAG = "CodeInsertMethodZipFileParser"

    /*
    * 解析文件获取配置
    */
    static Map<String, Integer> parse(
        @NonNull AutoPatchConfig autoPatchConfig, @NonNull String filePath, @NonNull Logger logger) {
        logger.quiet(TAG + "parse start.")
        File file = new File(filePath);
        if (!file.exists()) {
            logger.quiet(TAG + filePath + " not exist")
            throw IllegalArgumentException(TAG + " methodFile not exist: " + filePath)
        }
        FileInputStream fileInputStream
        GZIPInputStream gzipInputStream
        ByteArrayInputStream byteArrayInputStream
        ObjectInputStream objectInputStream
        try {
            fileInputStream = new FileInputStream(file)
            gzipInputStream = new GZIPInputStream(fileInputStream)

            int count
            byte[] bytes = new byte[1024]
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            while ((count = gzipInputStream.read(bytes, 0, 1024)) != -1) {
                byteArrayOutputStream.write(bytes, 0, count)
            }
            byteArrayInputStream
                = new ByteArrayOutputStream(byteArrayOutputStream.toByteArray())
            objectInputStream = new ObjectInputStream(byteArrayInputStream)

            logger.quiet(TAG + "parse end.")
            return objectInputStream.readObject()
        } finally {
            fileInputStream.close
            gzipInputStream.close()
            byteArrayInputStream.close()
            objectInputStream.close()
        }
    }

}
