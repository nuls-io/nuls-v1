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
import io.netty.handler.codec.MessageToMessageEncoder;
import io.nuls.core.tools.log.Log;

import java.nio.ByteOrder;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/8/28
 * @description
 */
public class NulsNettyEncoder extends MessageToMessageEncoder<ByteBuf> {

    private ByteOrder byteOrder;
    private int lengthFieldLength;
    private boolean lengthIncludesLengthFieldLength;
    private int lengthAdjustment;

    public NulsNettyEncoder(int lengthFieldLength) {
        this(ByteOrder.BIG_ENDIAN, lengthFieldLength, false, 0);
    }

    private NulsNettyEncoder(ByteOrder byteOrder, int lengthFieldLength, boolean lengthIncludesLengthFieldLength, int lengthAdjustment) {
        this.byteOrder = byteOrder;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int length = msg.readableBytes() + lengthAdjustment;
        if (lengthIncludesLengthFieldLength) {
            length += lengthFieldLength;
        }

        if (length < 0) {
            throw new IllegalArgumentException(
                    "Adjusted frame length (" + length + ") is less than zero");
        }

        switch (lengthFieldLength) {
            case 0:
                Log.info("NEW  VERSION encode!!!!!");
                break;
            case 1:
                if (length >= 256) {
                    throw new IllegalArgumentException(
                            "length does not fit into a byte: " + length);
                }
                out.add(ctx.alloc().buffer(1).order(byteOrder).writeByte((byte) length));
                break;
            case 2:
                if (length >= 65536) {
                    throw new IllegalArgumentException(
                            "length does not fit into a short integer: " + length);
                }
                out.add(ctx.alloc().buffer(2).order(byteOrder).writeShort((short) length));
                break;
            case 3:
                if (length >= 16777216) {
                    throw new IllegalArgumentException(
                            "length does not fit into a medium integer: " + length);
                }
                out.add(ctx.alloc().buffer(3).order(byteOrder).writeMedium(length));
                break;
            case 4:
                out.add(ctx.alloc().buffer(4).order(byteOrder).writeInt(length));
                break;
            case 8:
                out.add(ctx.alloc().buffer(8).order(byteOrder).writeLong(length));
                break;
            default:
                throw new Error("should not reach here");
        }
        out.add(msg.retain());
    }
}
