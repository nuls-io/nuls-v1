package io.nuls;

import io.nuls.queue.impl.util.MappedBufferCleanUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Niels on 2017/9/21.
 * nuls.io
 */
public class TestNIOMain {

    public static void main(String[] args) throws Exception {
        File f = new File("test.f");
        RandomAccessFile raf = new RandomAccessFile(f, "rwd");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mappedByteBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
        mappedByteBuffer.put("Niels Wang".getBytes());
        mappedByteBuffer.force();
        mappedByteBuffer.clear();
        MappedBufferCleanUtil.clean(mappedByteBuffer);
        fc.close();
        raf.close();
        mappedByteBuffer = null;
        fc = null;
        raf = null;
//        Thread.sleep(10000l);
        boolean b = f.delete();
        System.out.println(b);
    }


}
