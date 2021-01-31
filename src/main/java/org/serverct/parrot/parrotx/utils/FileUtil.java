package org.serverct.parrot.parrotx.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class FileUtil {

    public static File[] listFiles(final File folder, final String suffix) {
        if (Objects.isNull(folder)) {
            return new File[0];
        }
        return folder.listFiles(file -> file.getName().endsWith(suffix));
    }

    public static File[] getYamls(final File folder) {
        return listFiles(folder, ".yml");
    }

    @NotNull
    public static String getNoExFilename(@NotNull String fileName) {
        if (fileName.length() > 0) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    @NotNull
    public static String getNoExFilename(@NotNull final File file) {
        return getNoExFilename(file.getName());
    }

}
