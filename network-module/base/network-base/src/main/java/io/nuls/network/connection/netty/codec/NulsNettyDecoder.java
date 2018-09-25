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
package io.nuls.network.connection.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.*;
import io.nuls.core.tools.log.Log;
import io.nuls.network.constant.NetworkParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.List;

import static io.nuls.network.constant.NetworkConstant.MAX_FRAME_LENGTH;

/**
 * @author tangyi
 * @date 2018/8/29
 * @description 这里只能继承ByteToMessageDecoder而不能继承LengthFieldBasedFrameDecoder
 * 因为在LengthFieldBasedFrameDecoder中decode方法被标注为final，无法重写
 */
public class NulsNettyDecoder extends ByteToMessageDecoder {

    private ByteOrder byteOrder;
    private int maxFrameLength;
    private int lengthFieldOffset;
    private int lengthFieldLength;
    private int lengthFieldEndOffset;
    private int lengthAdjustment;
    private int initialBytesToStrip;
    private boolean failFast;
    private boolean discardingTooLongFrame;
    private long tooLongFrameLength;
    private long bytesToDiscard;

    /**
     * Nuls default
     * @param maxFrameLength
     */
    public NulsNettyDecoder(int maxFrameLength) {
        this(maxFrameLength, 0, 0, 0, 0, true, ByteOrder.BIG_ENDIAN);
    }

    private NulsNettyDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip, boolean failFast, ByteOrder byteOrder) {
        this.maxFrameLength = maxFrameLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
        this.initialBytesToStrip = initialBytesToStrip;
        this.failFast = failFast;
        this.byteOrder = byteOrder;
        this.lengthFieldEndOffset = lengthFieldOffset + lengthFieldLength;

        if (byteOrder == null) {
            throw new NullPointerException("byteOrder");
        }

        if (maxFrameLength <= 0) {
            throw new IllegalArgumentException(
                    "maxFrameLength must be a positive integer: " +
                            maxFrameLength);
        }

        if (lengthFieldOffset < 0) {
            throw new IllegalArgumentException(
                    "lengthFieldOffset must be a non-negative integer: " +
                            lengthFieldOffset);
        }


        if (lengthFieldOffset > maxFrameLength - lengthFieldLength) {
            throw new IllegalArgumentException(
                    "maxFrameLength (" + maxFrameLength + ") " +
                            "must be equal to or greater than " +
                            "lengthFieldOffset (" + lengthFieldOffset + ") + " +
                            "lengthFieldLength (" + lengthFieldLength + ").");
        }
    }

    @Override
    /*
      重写decode方法，根据头8个字节判断，如果是magic number，则说明是新版本的encoder(NulsNettyEncoder)产生
      否则是旧版本的encoder(LengthFieldPrepender)产生
      调用不同的方法解码
     */
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        long sysMagicNumber = NetworkParam.getInstance().getPacketMagic();
        long decodeNumberAtPos0 = in.getUnsignedIntLE(0);
        long decodeNumberAtPos8 = in.getUnsignedIntLE(8);
        Log.info("Decode start: sysMagicNumber=" + sysMagicNumber + ", decodeNumberAtPos0=" + decodeNumberAtPos0 + ", decodeNumberAtPos8=" + decodeNumberAtPos8);

        Object decoded;
        if (sysMagicNumber == decodeNumberAtPos0) {
            // 从0开始读，读到magic number，说明是新版本
            Log.info("NEW  VERSION decode!!!!!decodeNumberAtPos0=" + decodeNumberAtPos0);
            //decoded = decode(ctx, in);

            ByteBuf frame = in.retainedDuplicate();
            out.add(frame);
            in.skipBytes(in.readableBytes());
        } else if (sysMagicNumber == decodeNumberAtPos8) {
            // 如果从8位之后开始读，为magic number，说明是旧版本
            Log.info("OLD  VERSION decode!!!!!decodeNumberAtPos8=" + decodeNumberAtPos8);

            // decode 方法为protected，无法从外部访问到，因此用反射方法调用
            Class cls = Class.forName("io.netty.handler.codec.LengthFieldBasedFrameDecoder");
            Constructor c = cls.getConstructor(int.class, int.class, int.class, int.class, int.class);
            LengthFieldBasedFrameDecoder preDecode = (LengthFieldBasedFrameDecoder) c.newInstance(MAX_FRAME_LENGTH, 0, 8, 0, 8);
            Method decodeMethod = cls.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class);
            decodeMethod.setAccessible(true);
            decoded = decodeMethod.invoke(preDecode, ctx, in);

            if (decoded != null) {
                out.add(decoded);
            }
        }
    }

    /*
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        //判断discardingTooLongFrame标识，看是否需要丢弃当前可读的字节缓冲区，如果为真，则执行丢弃操作
        if (discardingTooLongFrame) {
            long bytesToDiscard = this.bytesToDiscard;
            //判断需要丢弃的字节长度，由于丢弃的字节数不能大于当前缓冲区可读的字节数，所以需要通过Math.min(bytesToDiscard, in.readableBytes())函数进行选择，
            //取bytesToDiscard和缓冲区可读字节数之中的最小值。
            int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
            //计算获取需要丢弃的字节数之后，调用ByteBuf的skipBytes方法跳过需要忽略的字节长度，
            in.skipBytes(localBytesToDiscard);
            //然后bytesToDiscard减去已经忽略的字节长度。
            bytesToDiscard -= localBytesToDiscard;
            this.bytesToDiscard = bytesToDiscard;
            //最后判断是否已经达到需要忽略的字节数，达到的话对discardingTooLongFrame等进行置位
            failIfNecessary(false);
        }

        if (in.readableBytes() < lengthFieldEndOffset) {
            return null;
        }

        int actualLengthFieldOffset = in.readerIndex() + lengthFieldOffset;
        //通过读索引和lengthFieldOffset计算获取实际的长度字段索引，然后通过索引值获取消息报文的长度字段
        //根据长度字段自身的字节长度进行判断，共有以下6种可能的取值。
        //长度所占字节为1，通过ByteBuf的getUnsignedByte方法获取长度值；
        //长度所占字节为2，通过ByteBuf的getUnsignedShort方法获取长度值；
        //长度所占字节为3，通过ByteBuf的getUnsignedMedium方法获取长度值；
        //长度所占字节为4，通过ByteBuf的getUnsignedInt方法获取长度值；
        //长度所占字节为8，通过ByteBuf的getLong方法获取长度值；
        //其他长度不支持，抛出DecoderException异常。
        //本次改动就是为了增加：长度所占字节为0
        long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, lengthFieldLength, byteOrder);

        //如果长度小于0，说明报文非法，跳过lengthFieldEndOffset个字节，抛出CorruptedFrameException异常。
        if (frameLength < 0) {
            in.skipBytes(lengthFieldEndOffset);
            throw new CorruptedFrameException(
                    "negative pre-adjustment length field: " + frameLength);
        }

        //根据lengthFieldEndOffset和lengthAdjustment字段进行长度修正
        frameLength += lengthAdjustment + lengthFieldEndOffset;
        //如果修正后的报文长度小于lengthFieldEndOffset，则说明是非法数据报，需要抛出CorruptedFrameException异常。
        if (frameLength < lengthFieldEndOffset) {
            in.skipBytes(lengthFieldEndOffset);
            throw new CorruptedFrameException(
                    "Adjusted frame length (" + frameLength + ") is less " +
                            "than lengthFieldEndOffset: " + lengthFieldEndOffset);
        }

        //如果修正后的报文长度大于ByteBuf的最大容量，说明接收到的消息长度大于系统允许的最大长度上限，
        //需要设置discardingTooLongFrame，计算需要丢弃的字节数，根据情况选择是否需要抛出解码异常。
        if (frameLength > maxFrameLength) {
            //丢弃的策略如下：frameLength减去ByteBuf的可读字节数就是需要丢弃的字节长度，
            //如果需要丢弃的字节数discard小于缓冲区可读的字节数，则直接丢弃整包消息。
            //如果需要丢弃的字节数大于当前可读字节数，说明即便当前所有可读的字节数全部丢弃，也无法完成任务，则设置discardingTooLongFrame为true，下次解码的时候继续丢弃。
            //丢弃操作完成之后，调用failIfNecessary方法根据实际情况抛出异常。
            long discard = frameLength - in.readableBytes();
            tooLongFrameLength = frameLength;

            if (discard < 0) {
                // buffer contains more bytes then the frameLength so we can discard all now
                in.skipBytes((int) frameLength);
            } else {
                // Enter the discard mode and discard everything received so far.
                discardingTooLongFrame = true;
                bytesToDiscard = discard;
                in.skipBytes(in.readableBytes());
            }
            failIfNecessary(true);
            return null;
        }

        //如果当前的可读字节数小于frameLength，说明是个半包消息，需要返回空，由I/O线程继续读取后续的数据报，等待下次解码。
        // never overflows because it's less than maxFrameLength
        int frameLengthInt = (int) frameLength;
        if (in.readableBytes() < frameLengthInt) {
            return null;
        }

        //对需要忽略的消息头字段进行判断，如果大于消息长度frameLength，说明码流非法，需要忽略当前的数据报，抛出CorruptedFrameException异常。
        if (initialBytesToStrip > frameLengthInt) {
            in.skipBytes(frameLengthInt);
            throw new CorruptedFrameException(
                    "Adjusted frame length (" + frameLength + ") is less " +
                            "than initialBytesToStrip: " + initialBytesToStrip);
        }

        //通过ByteBuf的skipBytes方法忽略消息头中不需要的字段，得到整包ByteBuf。
        in.skipBytes(initialBytesToStrip);

        // extract frame
        int readerIndex = in.readerIndex();
        int actualFrameLength = frameLengthInt - initialBytesToStrip;
        //通过extractFrame方法获取解码后的整包消息缓冲区
        //根据消息的实际长度分配一个新的ByteBuf对象，将需要解码的ByteBuf可写缓冲区复制到新创建的ByteBuf中并返回，
        //返回之后更新原解码缓冲区ByteBuf为原读索引+消息报文的实际长度（actualFrameLength）。
        ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
        in.readerIndex(readerIndex + actualFrameLength);
        return frame;
    }

    private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
        if (bytesToDiscard == 0) {
            // Reset to the initial state and tell the handlers that
            // the frame was too large.
            long tooLongFrameLength = this.tooLongFrameLength;
            this.tooLongFrameLength = 0;
            discardingTooLongFrame = false;
            if (!failFast || firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        } else {
            // Keep discarding and notify handlers if necessary.
            if (failFast && firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        }
    }

    private long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
        //noinspection deprecation
        buf = buf.order(order);
        long frameLength;
        Log.info("进入switch了, length=" + length);
        switch (length) {
            case 0:
                frameLength = 0;
                break;
            case 1:
                frameLength = buf.getUnsignedByte(offset);
                break;
            case 2:
                frameLength = buf.getUnsignedShort(offset);
                break;
            case 3:
                frameLength = buf.getUnsignedMedium(offset);
                break;
            case 4:
                frameLength = buf.getUnsignedInt(offset);
                break;
            case 8:
                frameLength = buf.getLong(offset);
                break;
            default:
                throw new DecoderException(
                        "unsupported lengthFieldLength: " + lengthFieldLength + " (expected: 1, 2, 3, 4, 8, or 0)");
        }
        return frameLength;
    }

    private ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.retainedSlice(index, length);
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                    "Adjusted frame length exceeds " + maxFrameLength +
                            ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                    "Adjusted frame length exceeds " + maxFrameLength +
                            " - discarding");
        }
    }
    */
}
