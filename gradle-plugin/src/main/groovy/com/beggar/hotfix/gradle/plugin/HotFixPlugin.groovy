package com.beggar.hotfix.gradle.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger;
import com.beggar.hotfix.base.Constants

import groovy.xml.*


/**
 * author: BeggarLan
 * created on: 2022/5/7 17:09
 * description: 用来插桩
 */
class HotFixPlugin implements Plugin<Project> {

    private Project mProject
    private Logger mLogger
    private def mXmlResult

    private List<String> mHotfixPackageList = new ArrayList<>()
    private List<String> mHotfixMethodList = new ArrayList<>()
    private List<String> mExceptPackageList = new ArrayList<>()
    private List<String> mExceptMethodList = new ArrayList<>()


    @Override
    void apply(Project project) {
        mProject = project
        mLogger = project.getLogger()

        try {
            mLogger.quiet("********** hitFix codeInsert plugin parse xml start. *****************")
            mXmlResult = new XmlSlurper().parse(new File("${project.projectDir}/${Constants.ROBUST_XML}"))
            mLogger.quiet("********** hitFix codeInsert plugin parse xml end. *****************")
            initConfig()
            // 注册transform
            project.android.registerTransform(new HotFixTransform(project))
            //
            project.afterEvaluate(new HotfixApkHashAction())
        } catch (Throwable e) {
            e.printStackTrace()
            mLogger.error("********** hitFix codeInsert plugin parse " + Constants.ROBUST_XML + " error.*********");
        }
    }

    // 拿到需要插桩的类、方法
    private void initConfig() {
        for (name in mXmlResult.hotfixPackage.name) {
            mHotfixPackageList.add(name.text())
        }
        for (name in mXmlResult.exceptPackage.name) {
            mExceptPackageList.add(name.text())
        }
        for (name in mXmlResult.hotfixMethod.name) {
            mHotfixMethodList.add(name.text())
        }
        for (name in mXmlResult.exceptMethod.name) {
            mExceptMethodList.add(name.text())
        }
    }

}
