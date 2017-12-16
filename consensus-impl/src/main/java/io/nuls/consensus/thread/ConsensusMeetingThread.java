package io.nuls.consensus.thread;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingThread implements Runnable {
    public static final String THREAD_NAME="Consensus-Meeting";
    private static final ConsensusMeetingThread INSTANCE = new ConsensusMeetingThread();
    private boolean running = false;
    private ConsensusMeetingThread(){}
    public static ConsensusMeetingThread getInstance(){
        return INSTANCE;
    }
    @Override
    public void run() {
        if(running){
           return;
        }
        this.running = true;
        // todo auto-generated method stub(niels)

    }

    public boolean isRunning() {
        return running;
    }
}
