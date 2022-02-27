package org.serverct.parrot.parrotx.common.dependency;


import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.utils.FileUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动态加载依赖
 * @author dakuo
 */
public class Dependency {

    public static File library = new File("./library");
    public static List<String> caches = new ArrayList<>();
    private final String[] urls;
    public List<File> files = new ArrayList<>();

    public Dependency(String... urls) {
        this.urls = urls;
    }

    /**
     * 稍后下载
     * @return dependency对象
     */
    public Dependency afterDownload() {

        if (!library.exists()) {
            library.mkdirs();
        }

        for (String url : urls) {
            String[] split = url.split("/");
            String file_name = split[split.length - 1];
            File file = new File(library, file_name);
            if (!file.exists()) {
                try {
                    Bukkit.getConsoleSender().sendMessage(I18n.color("&aParrotX &7>> &r 开始下载 " + url + "."));
                    final long download = FileUtil.download(url, file);
                    Bukkit.getConsoleSender().sendMessage(I18n.color("&aParrotX &7>> &r "+ url + " 下载完成, 总耗时: " + download + "ms."));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (file.exists()) {
                files.add(file);
            }
        }
        return this;
    }

    /**
     * 将所有依赖动态加载至内存
     */
    public void injectAll() {
        files.forEach(this::injectFile);
    }

    /**
     * 将对应依赖加载至内存
     * @param file jar包路径
     */
    public void injectFile(File file) {
        if (file.isDirectory()) {
            Arrays.stream(file.listFiles()).forEach(this::injectFile);
        } else {
            if (file.getName().endsWith(".jar") && !caches.contains(file.getName())) {
                try {
                    FileUtil.addUrl(file);
                    caches.add(file.getName());
                    Bukkit.getConsoleSender().sendMessage(I18n.color("&aParrotX &7>> &r 正在加载依赖 " + file.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

