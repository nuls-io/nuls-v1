/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.service.MessageWriter;
import io.nuls.network.service.NetworkService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @date 2017.11.10
 */
public class ConnectionHandler implements MessageWriter {

    private SocketChannel channel;

    private SelectionKey key;

    private Node node;

    private final ReentrantLock lock = new ReentrantLock();

    private ByteBuffer readBuffer;

    private final LinkedList<ByteBuffer> bytesToWrite = new LinkedList<>();

    public ConnectionHandler(Node node, SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.node = node;
        readBuffer = ByteBuffer.allocate(NetworkConstant.MESSAGE_MAX_SIZE);
    }

    public ConnectionHandler(Node node, SocketChannel channel, SelectionKey key) {
        this.key = key;
        this.channel = channel;
        this.node = node;
        readBuffer = ByteBuffer.allocate(NetworkConstant.MESSAGE_MAX_SIZE);
    }

    private void setWriteOps() {
        // Make sure we are registered to get updated when writing is available again
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        // Refresh the selector to make sure it gets the new interestOps
        key.selector().wakeup();
    }

    /**
     * Tries to write any outstanding write bytes, runs in any thread (possibly unlocked)
     *
     * @throws IOException
     */
    private void writeBytes() throws IOException {
        lock.lock();
        try {
            // Iterate through the outbound ByteBuff queue, pushing as much as possible into the OS' network buffer.
            Iterator<ByteBuffer> bytesIterator = bytesToWrite.iterator();
            while (bytesIterator.hasNext()) {
                ByteBuffer buff = bytesIterator.next();
                if (channel != null) {
                    channel.write(buff);
                }
                if (!buff.hasRemaining()) {
                    bytesIterator.remove();
                } else {
                    setWriteOps();
                    break;
                }
            }
            // If we are done writing, clear the OP_WRITE interestOps
            if (bytesToWrite.isEmpty() && key != null) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
            // Don't bother waking up the selector here, since we're just removing an op, not adding
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] message) {
        if (message.length > NetworkConstant.MESSAGE_MAX_SIZE) {
            throw new NulsRuntimeException(ErrorCode.DATA_OVER_SIZE_ERROR);
        }
        lock.lock();
        try {
            bytesToWrite.offer(ByteBuffer.wrap(message));
            setWriteOps();
        } catch (Exception e){
            Log.warn("Error handling SelectionKey: {}", e);
            key.cancel();
            node.destroy();
            NulsContext.getServiceBean(NetworkService.class).removeNode(node.getHash());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void closeConnection() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                Log.warn("channel close error", e);
            }
            channel = null;
        }
    }

    /**
     * Handle a SelectionKey which was selected
     * Runs unlocked as the caller is single-threaded (or if not, should enforce that handleKey is only called
     * atomically for a given ConnectionHandler)
     *
     * @param key
     */
    public static void handleKey(SelectionKey key) {
        ConnectionHandler handler = (ConnectionHandler) key.attachment();
        try {
            if (!key.isValid()) {
                // Key has been cancelled, make sure the socket gets closed
                key.cancel();
                handler.node.destroy();
                return;
            }
            if (key.isReadable()) {
                // Do a socket read and invoke the connection's receiveBytes message
                int len = handler.channel.read(handler.readBuffer);
                if (len == 0) {
                    // Was probably waiting on a write
                    return;
                } else if (len == -1) {
                    // Socket was closed
                    key.cancel();
                    handler.node.destroy();
                    return;
                }
                // "flip" the buffer - setting the limit to the current position and setting position to 0
                handler.node.receiveMessage(handler.readBuffer);
                // Now drop the bytes which were read by compacting readBuff (resetting limit and keeping relative position)
            } else if (key.isWritable()) {
                handler.writeBytes();
            }
        } catch (Exception e) {
            // This can happen eg if the channel closes while the thread is about to get killed
            // (ClosedByInterruptException), or if handler.connection.receiveBytes throws something
            Log.warn("Error handling SelectionKey: {}", e);
            key.cancel();
            handler.node.destroy();
            NulsContext.getServiceBean(NetworkService.class).removeNode(handler.node.getHash());
        }
    }
}
