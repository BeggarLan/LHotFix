package com.beggar.hotfix.autopatch

import com.android.annotations.NonNull

/**
 * author: lanweihua
 * created on: 2022/6/15 12:59 下午
 * description: 解析hotfix.xml
 */
class HotfixXMLParser {

    /*
     * 解析xml文件获取配置
     */
    static AutoPatchConfig parse(@NonNull String xmlFilePath) {
        def hotfixConfigNode = new XmlParser().parse(new File(xmlFilePath));

        AutoPatchConfig autoPatchConfig = new AutoPatchConfig();

        return autoPatchConfig;
    }

}
