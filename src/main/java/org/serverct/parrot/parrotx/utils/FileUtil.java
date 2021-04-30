package org.serverct.parrot.parrotx.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
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

    /**
     * 远程下载
     * @param urlString 下载链接
     * @param target 下载到文件夹
     * @return 时间戳
     * @throws IOException 链接或文件夹有错误会抛出异常
     */
    public static long download(@NotNull String urlString,@NotNull File target) throws IOException {
        long timeMillis = System.currentTimeMillis();
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        FileOutputStream out = new FileOutputStream(target);
        InputStream ins = con.getInputStream();
        byte[] b = new byte[1024];
        int i = 0;
        while ((i = ins.read(b)) != -1) {
            out.write(b, 0, i);
        }
        ins.close();
        out.close();
        return System.currentTimeMillis() - timeMillis;
    }


    /**
     * 动态加载jar
     * @param jarPath jar路径
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws MalformedURLException
     */
    public static void addUrl(@NotNull File jarPath) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, MalformedURLException {
        ClassLoader systemClassLoader = FileUtil.class.getClassLoader();
        URL url = jarPath.toURI().toURL();
        if (systemClassLoader instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) systemClassLoader;
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(classLoader, url);
        }
    }

}
