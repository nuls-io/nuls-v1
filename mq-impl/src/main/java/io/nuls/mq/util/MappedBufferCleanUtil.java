package io.nuls.mq.util;

import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

/**
 * Created by Niels on 2017/9/21.
 * nuls.io
 */
public class MappedBufferCleanUtil {


    public static void clean(final Object buffer) {
        if (null == buffer) {
            return;
        }
        try {
            //unmap
            Method m = FileChannelImpl.class.getDeclaredMethod("unmap",
                    MappedByteBuffer.class);
            m.setAccessible(true);
            m.invoke(FileChannelImpl.class, buffer);
            //Another way of implementation
            //            Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
            //            getCleanerMethod.setAccessible(true);
            //            sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
            //            cleaner.clean();
            //            getCleanerMethod.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
