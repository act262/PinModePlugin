package com.jfz.plugin.pin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.ManifestMerger2.Invoker
import com.android.manifmerger.ManifestMerger2.MergeType
import com.android.manifmerger.MergingReport
import com.android.manifmerger.XmlDocument
import com.android.utils.ILogger
import com.android.utils.StdLogger
import com.android.utils.StdLogger.Level
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.jfz.plugin.pin.util.AndroidUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.jetbrains.annotations.NotNull

// multiple source arch
class PinModePlugin extends AndroidBasePlugin {

    private Project project

    @Override
    void apply(@NotNull Project project) {
        this.project = project
        Logger logger = project.logger

        if (!AndroidUtils.isAndroidProject(project)) {
            logger.warn("$project is not a Android project, skip apply to $project")
            return
        }


        println 'Pin plugin version: v0.0.2'
        println "apply to -> $project"

        applyPlugin(project)
    }

    private void applyPlugin(Project project) {
        BaseExtension android = project.extensions.getByName("android")

        def androidVariant
        if (AndroidUtils.isAndroidAppProject(project)) {
            androidVariant = android.applicationVariants
        } else if (AndroidUtils.isAndroidLibProject(project)) {
            androidVariant = android.libraryVariants
        } else {
            return
        }

        PinModeExtension extension = project.extensions.create("pinModeConfig", PinModeExtension)
        def moduleDirs = project.projectDir.listFiles()
                .findAll {
            def name = it.name
            // filter all inner's module
            it.isDirectory() && extension.modulePrefix.any { name.startsWith(it) }
        }

        // for default main sources
        sourceSetConf(android.sourceSets.main, moduleDirs, null)

        // traversal all variant
        androidVariant.all { variant ->
            Set<File> manifestSet = new HashSet<>()

            // for buildType sources, never empty
            def buildType = variant.buildType
            android.sourceSets.getByName(buildType.name) {
                sourceSetConf(it, moduleDirs, manifestSet)
            }

            // for flavor sources， maybe empty
            if (variant.flavorName) {
                android.sourceSets.getByName(variant.flavorName) {
                    sourceSetConf(it, moduleDirs, manifestSet)
                }
            }

            processManifest(variant, manifestSet)
        }
    }

    def sourceSetConf(AndroidSourceSet sourceSet, List<File> moduleDirs, Set<File> manifestSet) {
        moduleDirs.each {
            def dir = "${it.name}/src/${sourceSet.name}"

            sourceSet.assets.srcDirs += "$dir/assets"
            sourceSet.java.srcDirs += "$dir/java"
            sourceSet.res.srcDirs += "$dir/res"
            sourceSet.aidl.srcDirs += "$dir/aidl"
            sourceSet.jni.srcDirs += "$dir/jni"
            sourceSet.jniLibs.srcDirs += "$dir/jniLibs"
            sourceSet.renderscript.srcDirs += "$dir/renderscript"

            // each AndroidManifest.xml
            def manifestFile = project.file("$dir/AndroidManifest.xml")

            if (manifestFile != null && manifestFile.exists()) {
                manifestSet?.add(manifestFile)
            }
        }
    }

    def processManifest(BaseVariant variant, Set<File> manifestSet) {
        String variantName = variant.name.capitalize()
        ManifestProcessorTask processManifestTask = project.tasks["process${variantName}Manifest"]
        processManifestTask.outputs.upToDateWhen { false }

        processManifestTask.doLast {
            def mainManifest = new File(processManifestTask.manifestOutputDirectory, "AndroidManifest.xml")

            println "main AndroidManifest -> $mainManifest"
            println "split AndroidManifest -> $manifestSet"

            def reportFile = new File(project.buildDir, "outputs/logs/final-manifest-merger-${variant.flavorName}-${variant.buildType.name}-report.txt")
            if (!reportFile.exists()) {
                reportFile.createNewFile()
            }

            def libManifests = manifestSet as File[]
            merge(reportFile, mainManifest, libManifests)
        }
    }

    private static void merge(File reportFile, File mainManifest, File... libraryManifests) {
        if (libraryManifests == null || libraryManifests.length == 0) {
            return
        }

        ILogger logger = new StdLogger(Level.VERBOSE)
        Invoker manifestMergerInvoker = ManifestMerger2.newMerger(mainManifest, logger, MergeType.APPLICATION)
        manifestMergerInvoker.setMergeReportFile(reportFile)
        manifestMergerInvoker.addLibraryManifests(libraryManifests)

        println "start merge..."

        MergingReport mergingReport = manifestMergerInvoker.merge()

        println "end merge..."

        println(mergingReport.reportString)

        switch (mergingReport.result) {
            case MergingReport.Result.WARNING:
                mergingReport.log(logger)
                break

            case MergingReport.Result.SUCCESS:
                XmlDocument xmlDocument = mergingReport.getMergedXmlDocument(MergingReport.MergedManifestKind.MERGED)
                try {
                    String annotatedDocument = mergingReport.getActions().blame(xmlDocument)

//                    logger.verbose(annotatedDocument)
                } catch (Exception e) {
                    logger.error(e, "cannot print resulting xml")
                }
                save(xmlDocument, mainManifest)
                break

            case MergingReport.Result.ERROR:
            default:
                throw new RuntimeException(mergingReport.getLoggingRecords())
        }
    }

    private static void save(XmlDocument xmlDocument, File out) {
        try {
            Files.write(xmlDocument.prettyPrint(), out, Charsets.UTF_8)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

}
