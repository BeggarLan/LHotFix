package com.beggar.hotfix.gradle.plugin

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import groovy.xml.XmlSlurper;


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
        mProject = project;
        mLogger = project.getLogger();
        try {
            mXmlResult = new XmlSlurper().parse(new File(mProject.getProjectDir() + File.separator + Constants.ROBUST_XML))
            initConfig()
            // 注册transform
            project.android.registerTransform(new HotFixTransform(project))
            //
            project.afterEvaluate(new HotfixApkHashAction())
        } catch (Throwable e) {
            mLogger.error("parse " + Constants.ROBUST_XM + " error");
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
