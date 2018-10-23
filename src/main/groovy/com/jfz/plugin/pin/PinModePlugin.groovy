package com.jfz.plugin.pin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.jfz.plugin.util.AndroidUtils
import com.jfz.plugin.util.ManifestUtils
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.jetbrains.annotations.NotNull

// multiple source arch
class PinModePlugin implements Plugin<Project> {

    private Project project
    private Logger logger

    @Override
    void apply(@NotNull Project project) {
        this.project = project
        this.logger = project.logger

        BaseExtension extension
        def variants
        if (AndroidUtils.isAndroidAppProject(project)) {
            extension = project.extensions.getByName("android") as AppExtension
            variants = extension.applicationVariants
        } else if (AndroidUtils.isAndroidLibProject(project)) {
            extension = project.extensions.getByName("android") as LibraryExtension
            variants = extension.libraryVariants
        } else {
            this.logger.warn("$project is not a Android project, skip apply to $project")
            return
        }

        applyPlugin(extension, variants)
    }

    private void applyPlugin(BaseExtension android, DomainObjectSet<BaseVariant> variants) {
        PinModeExtension extension = project.extensions.create("pinMode", PinModeExtension)

        def moduleDirs = project.projectDir.listFiles(new FileFilter() {
            @Override
            boolean accept(File pathname) {
                // filter all directory
                return pathname.isDirectory()
            }
        }) as List<File>

        // 1. add filter match pattern
        moduleDirs = moduleDirs.findAll {
            return it.name.matches(extension.defaultPattern) ||
                    (extension.pattern != null && pathname.name.matches(extension.pattern))
        }

        // 2. add custom's include
        extension.include.each {
            moduleDirs.add(project.file(it))
        }

        if (moduleDirs.isEmpty()) {
            logger.quiet("modules directory is empty, skip it.")
            return
        }

        moduleDirs.each {
            processBuildFile(it)
        }

        // for default main sources, never empty, just apply once
        Set<File> mainManifestSet = new HashSet<>()
        sourceSetConf(android.sourceSets.main, moduleDirs, mainManifestSet)

        // traversal all variant
        variants.all { variant ->
            Set<File> manifestSet = new HashSet<>(mainManifestSet)

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

                // for buildType+flavor sources
                android.sourceSets.getByName(variant.name) {
                    sourceSetConf(it, moduleDirs, manifestSet)
                }

                // for each single flavor
                variant.productFlavors.each { flavor ->
                    android.sourceSets.getByName(flavor.name) {
                        sourceSetConf(it, moduleDirs, manifestSet)
                    }
                }
            }

            processManifest(variant, manifestSet)
        }
    }

    def processBuildFile(File moduleDir) {
        def buildFile = new File(moduleDir, 'build.gradle')
        if (buildFile && buildFile.exists()) {
            project.apply([from: buildFile])
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

            def reportFile = new File(project.buildDir, "outputs/logs/final-manifest-merger-${variant.baseName}-report.txt")
            if (!reportFile.exists()) {
                reportFile.createNewFile()
            }

            def libManifests = manifestSet as File[]
            ManifestUtils.merge(reportFile, mainManifest, libManifests)
        }
    }

}
