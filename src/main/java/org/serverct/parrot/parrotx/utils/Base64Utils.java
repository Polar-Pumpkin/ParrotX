package org.serverct.parrot.parrotx.utils;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 一个有关于 Base64 加密/解密的工具类。
 *
 * @author 洋洋
 * @since 1.3.2
 */

@SuppressWarnings("unchecked")
public class Base64Utils {

    public static String enCode(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
        bukkitObjectOutputStream.writeObject(object);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        String encodeToString = Base64.getEncoder().encodeToString(bytes);
        return encodeToString;
    }

    public static <T> T deCode(String base64String, Class<?> clazz) throws IOException, ClassNotFoundException {
        byte[] decode = Base64.getDecoder().decode(base64String);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode);
        BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
        return (T) clazz.cast(bukkitObjectInputStream.readObject());
    }

}
