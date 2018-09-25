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
package io.nuls.contract;

public class KVStorageServiceImpl {

    private String dir = "/tmp/";

    /*@Override
    public Result createArea(String areaName) {
        return null;
    }

    @Override
    public Result createArea(String areaName, Long cacheSize) {
        return null;
    }

    @Override
    public Result createArea(String areaName, Comparator<byte[]> comparator) {
        return null;
    }

    @Override
    public Result createArea(String areaName, Long cacheSize, Comparator<byte[]> comparator) {
        return null;
    }

    @Override
    public String[] listArea() {
        return new String[0];
    }

    @Override
    public Result put(String area, byte[] key, byte[] value) {
        try {
            FileUtils.writeByteArrayToFile(getFile(key), value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public <T> Result putModel(String area, byte[] key, T value) {
        return null;
    }

    @Override
    public Result delete(String area, byte[] key) {
        return null;
    }


    @Override
    public byte[] get(String area, byte[] key) {
        try {
            return FileUtils.readFileToByteArray(getFile(key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T getModel(String area, byte[] key, Class<T> clazz) {
        return null;
    }

    @Override
    public Object getModel(String area, byte[] key) {
        return null;
    }

    @Override
    public Set<byte[]> keySet(String area) {
        return null;
    }

    @Override
    public List<byte[]> keyList(String area) {
        return null;
    }

    @Override
    public List<byte[]> valueList(String area) {
        return null;
    }

    @Override
    public Set<Entry<byte[], byte[]>> entrySet(String area) {
        return null;
    }

    @Override
    public List<Entry<byte[], byte[]>> entryList(String area) {
        return null;
    }

    @Override
    public <T> List<Entry<byte[], T>> entryList(String area, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> List<T> values(String area, Class<T> clazz) {
        return null;
    }

    @Override
    public BatchOperation createWriteBatch(String area) {
        return null;
    }

    public File getFile(byte[] key) {
        String file = dir + DigestUtils.md5Hex(key);
        return new File(file);
    }

    @Override
    public Result destroyArea(String areaName) {
        return null;
    }

    @Override
    public Result clearArea(String area) {
        return null;
    }*/

}
