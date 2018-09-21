/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.rpc.resources.util;

import io.nuls.core.tools.log.Log;

import java.io.*;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author: Niels Wang
 */
public final class FileUtil {

    public static File compress(File source, File target) {
        if (target.exists()) {
            target.delete();
        }
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(target);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            addEntry("/", source, zos);
        } catch (IOException e) {
            Log.error(e);
        } finally {
            close(zos, fos);
        }
        return target;
    }

    private static void addEntry(String dir, File source, ZipOutputStream zos) {
        String entry = dir + source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                addEntry(entry + "/", file, zos);
            }
        } else {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(source);
                byte[] buffer = new byte[fis.available()];
                if (buffer.length == 0) {
                    return;
                }
                bis = new BufferedInputStream(fis, buffer.length);
                int size;
                zos.putNextEntry(new ZipEntry(entry));
                while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, size);
                }
                zos.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(bis, fis);
            }
        }
    }

    public static void decompress(String zipPath, String targetPath) {
        File source = null;
        try {
            source = new File(URLDecoder.decode(zipPath, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        if (null == source || !source.exists()) {
            return;
        }
        ZipInputStream zis = null;
        BufferedOutputStream bos = null;
        try {
            zis = new ZipInputStream(new FileInputStream(source));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                File target = new File(URLDecoder.decode(targetPath, "UTF-8"), entry.getName());
                if (!target.getParentFile().exists()) {
                    target.getParentFile().mkdirs();
                }
                bos = new BufferedOutputStream(new FileOutputStream(target));
                int size;
                byte[] buffer = new byte[zis.available()];
                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
            }
            zis.closeEntry();
        } catch (IOException e) {
            Log.error(e);
        } finally {
            close(zis, bos);
        }
    }

    public static void copyFile(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (in != null) {
                    in.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            Log.error(e);
        }
    }

    public static void copyFolder(File source, File target) {
        String[] filePath = source.list();

        if (null == filePath || filePath.length == 0) {
            return;
        }

        if (!target.exists()) {
            target.mkdirs();
        }

        String sourcePath = source.getPath();
        String path = target.getPath();
        try {
            sourcePath = URLDecoder.decode(sourcePath, "UTF-8");
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            Log.error(e);
        }
        for (int i = 0; i < filePath.length; i++) {
            if ((new File(sourcePath + "/" + filePath[i])).isDirectory()) {
                copyFolder(new File(sourcePath + "/" + filePath[i]), new File(path + "/" + filePath[i]));
            }

            if (new File(sourcePath + "/" + filePath[i]).isFile()) {
                copyFile(new File(sourcePath + "/" + filePath[i]), new File(path + "/" + filePath[i]));
            }

        }
    }

    public static void writeText(String text, String path) throws IOException {
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            writer = new FileWriter(file);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }

    }

    public static boolean writeFile(byte[] bytes, String filePath) throws IOException {
        File file = new File(URLDecoder.decode(filePath, "UTF-8"));
        if (file.exists()) {
            file.delete();
        }

        OutputStream output = new FileOutputStream(file);
        try {
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            Log.error(e);
            return false;
        } finally {
            output.close();
        }
        return true;
    }

    public static boolean deleteFolder(File folder) {
        if (!folder.exists()) {
            return true;
        }
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                try {
                    boolean b = file.delete();
                    if (!b) {
                        Log.info("delete " + file.getName() + " result:" + b);
                        mkNullToFile(file);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            } else {
                deleteFolder(file);
            }
        }
        try {
            boolean b = folder.delete();
        } catch (Exception e) {
            Log.error(e);
        }
        return true;
    }

    private static void mkNullToFile(File file) {
        byte[] bytes = new byte[0];
        OutputStream outputStream = null;
        try {

            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            Log.error(e);
        } catch (IOException e) {
            Log.error(e);
        } finally {
            try {
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }

    }

    public static void deleteFolder(String path) {
        deleteFolder(new File(path));
    }
}
