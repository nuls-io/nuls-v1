/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.netty.conn.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.nuls.network.constant.NetworkParam;

import java.nio.ByteOrder;
import java.util.List;

import static io.nuls.network.constant.NetworkConstant.MAX_FRAME_LENGTH;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/7
 */
public class NulsMessageDecoder extends ByteToMessageDecoder {

    private NulsLengthFieldBasedFrameDecoder oldDecoder = new NulsLengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 8, 0, 8);
    private NulsLengthFieldBasedFrameDecoder newDecoder = new NulsLengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH, 4, 4, 6, 0, true);


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        long sysMagicNumber = NetworkParam.getInstance().getPacketMagic();
        long readMagicNumber = in.getUnsignedIntLE(0);
        if (sysMagicNumber == readMagicNumber) {
            // 新版本
            Object decoded = newDecoder.decode(ctx, in);
            if (decoded != null) {
                out.add(decoded);
            }
        } else {
            readMagicNumber = in.getUnsignedIntLE(8);
            if (sysMagicNumber == readMagicNumber) {
                // 老版本
                Object decoded = oldDecoder.decode(ctx, in);
                if (decoded != null) {
                    out.add(decoded);
                }
            }
        }
    }
}
