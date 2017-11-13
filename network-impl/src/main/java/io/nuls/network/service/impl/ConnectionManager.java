package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.NulsThread;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.NetworkModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by win10 on 2017/11/6.
 */
public class ConnectionManager {

    private NetworkParam network;

    private NetworkModule networkModule;

    private PeersManager peersManager;

    private ServerSocketChannel serverSocketChannel;

    private Selector selector;

    private ReentrantLock lock;

    private volatile boolean inited;
    //The storage will be connected
    final Queue<PendingConnect> newConnectionChannels = new LinkedBlockingQueue<>();

    public ConnectionManager(NetworkModule module, NetworkParam network) {
        this.network = network;
        this.networkModule = module;
        lock = new ReentrantLock();
    }

    /**
     * open the serverSocketChannel and register accept action
     */
    private void init() {
        lock.lock();
        try {
            if (!inited) {
                selector = SelectorProvider.provider().openSelector();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress(network.port()));
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                inited = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        init();
        new NulsThread(networkModule, "networkConnManagerThread") {
            @Override
            public void run() {
                ConnectionManager.this.run();
            }
        }.start();
    }


    public void openConnection(Peer peer) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(peer.getHostName(), peer.getPort());
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setReuseAddress(true);
        channel.connect(socketAddress);
        PendingConnect data = new PendingConnect(channel, peer);
        newConnectionChannels.offer(data);
        selector.wakeup();
    }


    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        try {

            while (inited) {
                //Processing wait connections
                PendingConnect conn;
                while ((conn = newConnectionChannels.poll()) != null) {
                    try {
                        //  Only connection events are registered here
                        SelectionKey key = conn.channel.register(selector, SelectionKey.OP_CONNECT);
                        key.attach(conn);
                    } catch (ClosedChannelException e) {
                        Log.warn("SocketChannel was closed before it could be registered");
                    }
                }

                //
                if (selector.select() > 0) {
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        handleKey(key);
                    }
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverClose();
        }
    }

    private void serverClose() {
        inited = false;
        for (SelectionKey key : selector.keys()) {
            try {
                key.channel().close();
            } catch (IOException e) {
                // log.warn("Error closing channel", e);
            }
            key.cancel();
//            if (key.attachment() instanceof ConnectionHandler)
//                ConnectionHandler.handleKey(key); // Close connection if relevant
        }
        try {
            selector.close();
            selector = null;
        } catch (IOException e) {
            //log.warn("Error closing client manager selector", e);
        }

        try {
            serverSocketChannel.close();
            serverSocketChannel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean allowConnection(InetSocketAddress socketAddress) {
        //check the connecting peers count
        boolean inAble = true;
        boolean outAble = true;
        PeerGroup inPeers = peersManager.getPeerGroup("inPeers");
        if (inPeers.size() >= network.maxInCount()) {
            inAble = false;
        }
        PeerGroup outPeers = peersManager.getPeerGroup("inPeers");
        if (outPeers.size() >= network.maxOutCount()) {
            outAble = false;
        }
        if (!inAble && !outAble) {
            return false;
        }

        //check it already connected
        for (Peer peer : inPeers.getPeers()) {
            if (peer.getIp().equals(socketAddress.getAddress().getHostAddress()) &&
                    peer.getPort() == socketAddress.getPort()) {
                return false;
            }
        }

        for (Peer peer : outPeers.getPeers()) {
            if (peer.getIp().equals(socketAddress.getAddress().getHostAddress()) &&
                    peer.getPort() == socketAddress.getPort()) {
                return false;
            }
        }
        return true;
    }


    public void handleKey(SelectionKey key) throws IOException {
        if (key.isValid() && key.isConnectable()) {
            PendingConnect data = (PendingConnect) key.attachment();
            Peer peer = data.peer;
            SocketChannel channel = (SocketChannel) key.channel();
            //Must be connected after the completion of registration to other events
            if(channel.finishConnect()) {
                key.interestOps((key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_CONNECT);
                key.attach(null);
            }
        }

        if (key.isValid() && key.isAcceptable()) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (allowConnection((InetSocketAddress) socketChannel.getRemoteAddress())) {
                return;
            }
            socketChannel.configureBlocking(false);
            SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);

            Peer peer = new Peer();
        }
    }

    public PeersManager getPeersManager() {
        return peersManager;
    }

    public void setPeersManager(PeersManager peersManager) {
        this.peersManager = peersManager;
    }


    class PendingConnect {
        SocketChannel channel;
        Peer peer;

        PendingConnect(SocketChannel channel, Peer peer) {
            this.channel = channel;
            this.peer = peer;
        }
    }
}
