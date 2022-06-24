package com.wind.base.gradle.plugin;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import com.google.common.hash.Hashing;
import org.apache.commons.io.Charsets;
/**
 * Copyright (C), 2015-2022, 杭州迈优文化创意有限公司
 * FileName: Util
 * Author: wind
 * Date: 2022/6/17 10:49
 * Description: 描述该类的作用
 * Path: 路径
 * History:
 * <author> <time> <version> <desc>
 */
public class Util {

    public static boolean isRealZipOrJar(File input) {
        ZipFile zf = null;

        try {
            zf = new ZipFile(input);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(zf);
        }
        return false;

    }

    public static void closeQuietly(AutoCloseable target) {
        if (target == null) {
            return;
        }
        try {
            target.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addZipEntry(ZipOutputStream zipOutputStream, ZipEntry zipEntry, InputStream inputStream) throws Exception{

        try {
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buffer=new byte[16*1024];
            int len;
            while ((len=inputStream.read(buffer,0,buffer.length))!=-1){
                zipOutputStream.write(buffer,0,len);
                zipOutputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeQuietly(inputStream);
            zipOutputStream.closeEntry();
        }

    }

    public static String getUniqueJarName(File jarFile) {
        final String originJarName = jarFile.getName();
        final String hashing = Hashing.sha1().hashString(jarFile.getPath(), Charsets.UTF_16).toString();
        final int dotPos = originJarName.lastIndexOf(".");
        if (dotPos < 0) {
            return originJarName+"_"+hashing;
        } else {
            final String nameWithoutDotExt = originJarName.substring(0, dotPos);
            final String dotExt = originJarName.substring(dotPos);
            return nameWithoutDotExt+"_"+hashing+dotExt;
        }
    }
}
