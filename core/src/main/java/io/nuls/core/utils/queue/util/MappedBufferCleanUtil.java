package io.nuls.core.utils.queue.util;


/**
 *
 * @author Niels
 * @date 2017/9/21
 *
 */
public class MappedBufferCleanUtil {


    public static void clean1(final Object buffer) {
        if (null == buffer) {
            return;
        }
        try {
            //unmap
//            Method m = FileChannelImpl.class.getDeclaredMethod("unmap",
//                    MappedByteBuffer.class);
//            m.setAccessible(true);
//            m.invoke(FileChannelImpl.class, buffer);
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
