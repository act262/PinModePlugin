package com.jfz.plugin.util

import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.manifmerger.XmlDocument
import com.android.utils.ILogger
import com.android.utils.StdLogger

class ManifestUtils {

    static void merge(File reportFile, File mainManifest, File... libraryManifests) {
        if (libraryManifests == null || libraryManifests.length == 0) {
            return
        }

        ILogger logger = new StdLogger(StdLogger.Level.VERBOSE)
        ManifestMerger2.Invoker manifestMergerInvoker = ManifestMerger2.newMerger(mainManifest, logger, ManifestMerger2.MergeType.APPLICATION)
        manifestMergerInvoker.setMergeReportFile(reportFile)
        manifestMergerInvoker.addLibraryManifests(libraryManifests)

        MergingReport mergingReport = manifestMergerInvoker.merge()

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
                FileUtils.save(xmlDocument.prettyPrint(), mainManifest)
                break

            case MergingReport.Result.ERROR:
            default:
                throw new RuntimeException(mergingReport.getLoggingRecords())
        }
    }
}