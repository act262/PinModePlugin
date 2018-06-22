package com.jfz.plugin.pin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.api.AndroidSourceSet
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
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull

// multiple source arch
class PinModePlugin extends AndroidBasePlugin {

    @Override
    void apply(@NotNull Project project) {
        if (!isAndroidProject(project)) {
            println "No Android Project"
            return
        }

        PinModeExtension extension = project.extensions.create("pinModeConfig", PinModeExtension)
        BaseExtension android = project.extensions.getByType(BaseExtension)

        def moduleDirs = project.projectDir.listFiles()
                .findAll {
            def name = it.name
            // filter all inner's module
            it.isDirectory() && extension.modulePrefix.any { name.startsWith(it) }
        }


        Set<File> manifestSet = new HashSet<>()
        def sourceSetConf = { AndroidSourceSet sourceSet ->
            moduleDirs.each {
                def dir = "${it.name}/src/${sourceSet.name}"

                sourceSet.assets.srcDirs "${dir}/assets"
                sourceSet.java.srcDirs "${dir}/java"
                sourceSet.res.srcDirs "${dir}/res"
                sourceSet.aidl.srcDirs "${dir}/aidl"

                // each AndroidManifest.xml
                def manifestFile = project.file("${dir}/AndroidManifest.xml")

                if (manifestFile != null && manifestFile.exists()) {
                    manifestSet.add(manifestFile)
                }
            }
        }

        // for default main sources
        sourceSetConf(android.sourceSets.main)

        // for buildType sources
        android.buildTypes.each {
            def buildType = it.name
            android.sourceSets.getByName(buildType) {
                sourceSetConf(it)
            }
        }

        // for flavor sources
        android.productFlavors.each {
            def flavor = it.name
            android.sourceSets.getByName(flavor) {
                sourceSetConf(it)
            }
        }

        println manifestSet

        def baseVariant
        if (isAndroidAppProject(project)) {
            AppExtension appExtension = project.extensions.getByType(AppExtension)
            baseVariant = appExtension.applicationVariants
        } else if (isAndroidLibProject(project)) {
            LibraryExtension libraryExtension = project.extensions.getByType(LibraryExtension)
            baseVariant = libraryExtension.libraryVariants
        } else {
            return
        }

        // traversal all variant
        baseVariant.all { variant ->
            String variantName = variant.name.capitalize()
            ManifestProcessorTask processManifestTask = project.tasks["process${variantName}Manifest"]
            processManifestTask.outputs.upToDateWhen { false }

            processManifestTask.doLast {
                def mainManifest = new File(processManifestTask.manifestOutputDirectory, "AndroidManifest.xml")

                def reportFile = new File(project.buildDir, "outputs/logs/final-manifest-merger-${variant.flavorName}-${variant.buildType.name}-report.txt")
                if (!reportFile.exists()) {
                    reportFile.createNewFile()
                }

                def libManifests = manifestSet as File[]
                merge(reportFile, mainManifest, libManifests)
            }
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

                    logger.verbose(annotatedDocument)
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

    static boolean isAndroidProject(Project project) {
        isAndroidAppProject(project) || isAndroidLibProject(project)
    }

    static boolean isAndroidAppProject(Project project) {
        project.plugins.hasPlugin("com.android.application")
    }

    static boolean isAndroidLibProject(Project project) {
        project.plugins.hasPlugin("com.android.library")
    }
}
