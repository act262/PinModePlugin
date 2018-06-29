package com.jfz.plugin.util

import com.google.common.base.Charsets
import com.google.common.io.Files

class FileUtils {

    /**
     * Save content to out file.
     */
    static void save(CharSequence content, File out) {
        try {
            Files.write(content, out, Charsets.UTF_8)
        } catch (IOException e) {
            throw RuntimeException(e)
        }
    }

}