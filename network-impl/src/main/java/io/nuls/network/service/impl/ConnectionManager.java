package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.module.AbstractNetworkModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @date 2017-11-10
 */
public class ConnectionManager implements Runnable {

    private AbstractNetworkParam network;

    private AbstractNetworkModule networkModule;

    private PeersManager peersManager;

    private ServerSocketChannel serverSocketChannel;

    private Selector selector;

    private ReentrantLock lock;

    private volatile boolean running;
    //The storage will be connected

    public ConnectionManager(AbstractNetworkModule module, AbstractNetworkParam network) {
        this.network = network;
        this.networkModule = module;
        lock = new ReentrantLock();
        init();
    }

    /**
     * open the serverSocketChannel and register accept action
     */
    private void init() {
        lock.lock();
        try {
            if (!running) {
                selector = SelectorProvider.provider().openSelector();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress(network.port()));
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                running = true;
            }
        } catch (IOException e) {
            serverClose();
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR, e);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        Log.info("----------- network connectionManager start -------------");
        ThreadManager.asynExecuteRunnable(this);
    }


    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        try {
            while (running) {
                if (selector.select(1000) > 0) {
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        handleKey(key);
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
        lock.lock();
        try {
            running = false;
            for (SelectionKey key : selector.keys()) {
                try {
                    key.channel().close();
                } catch (IOException e) {
                    Log.warn("Error closing channel", e);
                }
                key.cancel();
                if (key.attachment() instanceof ConnectionHandler) {
                    // Close connection if relevant
                    ConnectionHandler.handleKey(key);
                }
            }
            try {
                selector.close();
                selector = null;
            } catch (IOException e) {
                Log.warn("Error closing client manager selector", e);
            }

            try {
                serverSocketChannel.close();
                serverSocketChannel = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.info("Network closing connectManager");

            //todo sent a event to other module
            networkModule.setStatus(ModuleStatusEnum.DESTROYED);
        } finally {
            lock.unlock();
        }
    }

    /**
     * out peer try to connect
     *
     * @param peer
     */
    public void openConnection(Peer peer) {

        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().setReuseAddress(true);
            InetSocketAddress socketAddress = new InetSocketAddress(peer.getIp(), peer.getPort());
            channel.connect(socketAddress);
            PendingConnect data = new PendingConnect(channel, peer);
            SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
            key.attach(data);
            selector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            peer.destroy();

            //peersManager.deletePeer(peer);
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
        //check myself
        if (network.getLocalIps().contains(socketAddress.getAddress().getHostAddress())) {
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


    public void handleKey(SelectionKey key) {

        if (key.isValid() && key.isConnectable()) {
            //out peer
            PendingConnect data = (PendingConnect) key.attachment();
            Peer peer = data.peer;
            SocketChannel channel = (SocketChannel) key.channel();
            ConnectionHandler handler = new ConnectionHandler(peer, channel, key);
            //Must be connected after the completion of registration to other events
            try {
                if (channel.finishConnect()) {
                    key.interestOps((key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_CONNECT);
                    key.attach(handler);
                    peer.setWriteTarget(handler);
                    peer.connectionOpened();
                } else {
                    // Failed to connect for some reason
                    peer.destroy();
                }
            } catch (Exception e) {
                Log.warn("out peer Failed to connect", e);
                peer.destroy();
            }

        } else if (key.isValid() && key.isAcceptable()) {
            // in Peer
            SocketChannel socketChannel = null;
            Peer peer = null;
            try {
                socketChannel = serverSocketChannel.accept();
                InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                if (!allowConnection(socketAddress)) {
                    socketChannel.close();
                    return;
                }
                socketChannel.configureBlocking(false);
                SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);

                peer = new Peer(network, Peer.IN, socketAddress);
                peersManager.addPeerToGroup(NetworkConstant.NETWORK_PEER_IN_GROUP, peer);
                ConnectionHandler handler = new ConnectionHandler(peer, socketChannel, newKey);
                peer.setWriteTarget(handler);
                newKey.attach(handler);
                peer.connectionOpened();
            } catch (Exception e) {
                if (socketChannel != null) {
                    Log.warn("in peer Failed to connect", e);
                    try {
                        socketChannel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (peer != null) {
                    peer.destroy();
                }
            }
        } else {
            // read or write
            ConnectionHandler handler = (ConnectionHandler) key.attachment();
            if (handler != null) {
                ConnectionHandler.handleKey(key);
            }
        }
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
