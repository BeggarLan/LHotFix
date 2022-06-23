package com.beggar.hotfix.autopatch

import com.android.annotations.NonNull
import org.gradle.api.logging.Logger

/**
 * author: lanweihua
 * created on: 2022/6/15 12:59 下午
 * description: 解析hotfix.xml
 */
class HotfixXMLParser {

    private static final String TAG = "HotfixXMLParser"

    /*
     * 解析xml文件获取配置
     */
    static void parse(
        @NonNull AutoPatchConfig autoPatchConfig, @NonNull String xmlFilePath, @NonNull Logger logger) {
        logger.quiet(TAG + "parse start.")
        def hotfixConfigNode = new XmlParser().parse(new File(xmlFilePath));
        // todo 读取必要的配置
        autoPatchConfig.mMappingFilePath = "";

        logger.quiet(TAG + "parse end.")
    }

}
