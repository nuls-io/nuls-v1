package io.nuls;


/**
 * Hello world!
 *
 */
public class NetworkModule
{
    private static NetworkModule instance = null;
    private IBroadcaster broadcaster = null;

    public NetworkModule(){
        super();
        initNetworkModule();
    }

    private void initNetworkModule(){

    }

    public IBroadcaster getBroadcaster(){
        return this.broadcaster;
    }

    public static NetworkModule getInstance(){
        if(instance == null){
            instance = new NetworkModule();
        }
        return instance;
    }

}
