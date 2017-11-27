package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Peer;
import io.nuls.network.service.MessageWriter;

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

    private Peer peer;

    private final ReentrantLock lock = new ReentrantLock();

    private ByteBuffer readBuffer;

    private final LinkedList<ByteBuffer> bytesToWrite = new LinkedList<>();

    public ConnectionHandler(Peer peer, SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.peer = peer;
        readBuffer = ByteBuffer.allocate(NulsMessage.MAX_SIZE);
    }

    public ConnectionHandler(Peer peer, SocketChannel channel, SelectionKey key) {
        this.key = key;
        this.channel = channel;
        this.peer = peer;
        readBuffer = ByteBuffer.allocate(NulsMessage.MAX_SIZE);
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
                channel.write(buff);
                if (!buff.hasRemaining()) {
                    bytesIterator.remove();
                } else {
                    setWriteOps();
                    break;
                }
            }
            // If we are done writing, clear the OP_WRITE interestOps
            if (bytesToWrite.isEmpty()) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
            // Don't bother waking up the selector here, since we're just removing an op, not adding
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] message) {
        if (message.length > NulsMessage.MAX_SIZE) {
            throw new NulsRuntimeException(ErrorCode.DATA_OVER_SIZE_ERROR);
        }
        lock.lock();
        try {
            bytesToWrite.offer(ByteBuffer.wrap(message));
            setWriteOps();
        } finally {
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
                System.out.println("--------------------destory 1" + handler.peer.getIp());
                handler.peer.destroy();
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
                    System.out.println("--------------------destory 2" + handler.peer.getIp());
                    handler.peer.destroy();
                    return;
                }
                // "flip" the buffer - setting the limit to the current position and setting position to 0
                handler.readBuffer.flip();
                handler.peer.receiveMessage(handler.readBuffer);

                // Now drop the bytes which were read by compacting readBuff (resetting limit and keeping relative position)
                handler.readBuffer.compact();
            }
            if (key.isWritable()) {
                handler.writeBytes();
            }
        } catch (Exception e) {
            // This can happen eg if the channel closes while the thread is about to get killed
            // (ClosedByInterruptException), or if handler.connection.receiveBytes throws something
            e.printStackTrace();
            Throwable t = e;
            Log.warn("Error handling SelectionKey: {}", t.getMessage() != null ? t.getMessage() : t.getClass().getName());
            System.out.println("--------------------destory 3" + handler.peer.getIp());
            handler.peer.destroy();
        }
    }
}
