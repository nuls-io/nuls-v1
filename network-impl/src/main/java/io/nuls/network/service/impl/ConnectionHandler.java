package io.nuls.network.service.impl;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Peer;
import io.nuls.network.service.MessageWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by win10 on 2017/11/13.
 */
public class ConnectionHandler implements MessageWriter {

    private SocketChannel channel;

    private SelectionKey key;

    private Peer peer;

    private final ReentrantLock lock = new ReentrantLock();

    private ByteBuffer readBuffer;

    private final LinkedList<ByteBuffer> bytesToWrite = new LinkedList<>();

    public ConnectionHandler(Peer peer, SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.peer = peer;
        readBuffer = ByteBuffer.allocate(Block.MAX_SIZE);
    }

    public ConnectionHandler(Peer peer, SocketChannel channel, SelectionKey key) {
        this.key = key;
        this.channel = channel;
        this.peer = peer;
        readBuffer = ByteBuffer.allocate(Block.MAX_SIZE);
    }

    private void setWriteOps() {
        // Make sure we are registered to get updated when writing is available again
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        // Refresh the selector to make sure it gets the new interestOps
        key.selector().wakeup();
    }

    // Tries to write any outstanding write bytes, runs in any thread (possibly unlocked)
    private void writeBytes() throws IOException {
        lock.lock();
        try {
            // Iterate through the outbound ByteBuff queue, pushing as much as possible into the OS' network buffer.
            Iterator<ByteBuffer> bytesIterator = bytesToWrite.iterator();
            while (bytesIterator.hasNext()) {
                ByteBuffer buff = bytesIterator.next();
                channel.write(buff);
                if (!buff.hasRemaining())
                    bytesIterator.remove();
                else {
                    setWriteOps();
                    break;
                }
            }
            // If we are done writing, clear the OP_WRITE interestOps
            if (bytesToWrite.isEmpty())
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            // Don't bother waking up the selector here, since we're just removing an op, not adding
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] message) throws IOException {
        if (message.length > Block.MAX_SIZE) {
            throw new NulsRuntimeException(ErrorCode.DATA_OVER_SIZE_ERROR);
        }
        boolean shouldUnlock = true;
        lock.lock();
        try {

            bytesToWrite.offer(ByteBuffer.wrap(message));
            setWriteOps();
        } catch (RuntimeException e) {
            lock.unlock();
            shouldUnlock = false;
            Log.warn("Error writing message to connection, closing connection", e);
            closeConnection();
            throw e;
        } finally {
            if (shouldUnlock)
                lock.unlock();
        }
    }

    @Override
    public void closeConnection() {
        try {
            channel.close();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        //   connectionClosed();
    }

    // Handle a SelectionKey which was selected
    // Runs unlocked as the caller is single-threaded (or if not, should enforce that handleKey is only called
    // atomically for a given ConnectionHandler)
    public static void handleKey(SelectionKey key) {
        ConnectionHandler handler = (ConnectionHandler) key.attachment();
        try {
            if (!key.isValid()) {
                handler.closeConnection(); // Key has been cancelled, make sure the socket gets closed
                return;
            }
            if (key.isReadable()) {
                // Do a socket read and invoke the connection's receiveBytes message
                int len = handler.channel.read(handler.readBuffer);
                if (len == 0) {
                    return; // Was probably waiting on a write
                } else if (len == -1) { // Socket was closed
                    key.cancel();
                    handler.closeConnection();
                    return;
                }
                // "flip" the buffer - setting the limit to the current position and setting position to 0
                handler.readBuffer.flip();
                // Use connection.receiveBytes's return value as a check that it stopped reading at the right location
                //int bytesConsumed = checkNotNull(handler.connection).receiveBytes(handler.readBuff);
                //checkState(handler.readBuff.position() == bytesConsumed);
                // Now drop the bytes which were read by compacting readBuff (resetting limit and keeping relative
                // position)
                String msg = new String(handler.readBuffer.array(), 0, len);
                System.out.println("------msg: " + msg);
                handler.readBuffer.compact();
            }
            if (key.isWritable())
                handler.writeBytes();
        } catch (Exception e) {
            // This can happen eg if the channel closes while the thread is about to get killed
            // (ClosedByInterruptException), or if handler.connection.receiveBytes throws something
            Throwable t = e;
            Log.warn("Error handling SelectionKey: {}", t.getMessage() != null ? t.getMessage() : t.getClass().getName());
            handler.closeConnection();
        }
    }
}
