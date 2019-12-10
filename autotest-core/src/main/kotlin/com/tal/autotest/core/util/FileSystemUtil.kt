package com.tal.autotest.core.util

import java.nio.file.Files
import java.nio.file.Paths

class FileSystemUtil {
    companion object {
        fun makeSureDirectoryExist(dir: String) {
            val path = Paths.get(dir)
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
        }
    }
}