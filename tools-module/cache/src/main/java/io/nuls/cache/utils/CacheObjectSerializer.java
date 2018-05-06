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

package io.nuls.cache.utils;

import io.nuls.core.tools.log.Log;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import java.nio.ByteBuffer;

/**
 * @author: Niels Wang
 * @date: 2018/5/6
 */
public class CacheObjectSerializer<T> implements Serializer<T> {

    private final RuntimeSchema<T> schema;
    private final Class clazz;

    public CacheObjectSerializer(Class clazz) {
        schema = RuntimeSchema.createFrom(clazz);
        this.clazz = clazz;
    }

    @Override
    public ByteBuffer serialize(T o) throws SerializerException {
        byte[] bytes = ProtostuffIOUtil.toByteArray(o, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public T read(ByteBuffer byteBuffer) throws SerializerException {
        T t = null;
        try {
            t = (T) clazz.newInstance();
        } catch (InstantiationException e) {
            Log.error(e);
            return null;
        } catch (IllegalAccessException e) {
            Log.error(e);
            return null;
        }
        ProtostuffIOUtil.mergeFrom(byteBuffer.array(), t, schema);
        return t;
    }

    @Override
    public boolean equals(T o, ByteBuffer byteBuffer) throws ClassNotFoundException, SerializerException {
        if(o==null){
            return false;
        }
        return o.equals(this.read(byteBuffer));
    }
}
