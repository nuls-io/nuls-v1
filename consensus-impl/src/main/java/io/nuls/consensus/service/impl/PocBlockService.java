package io.nuls.consensus.service.impl;

/**
 * poc special interface
 * @author Niels
 * @date 2017/12/11
 */
public class PocBlockService  {

    private static final PocBlockService INSTANCE = new PocBlockService();

    private BlockStorageService blockStorageService = BlockStorageService.getInstance();

    private PocBlockService(){}

    public static final PocBlockService getInstance(){
        return INSTANCE;
    }

    public long getBlockCount(String address, long roundStart, long roundEnd) {
        // todo auto-generated method stub(niels)
        return 0;
    }

    public long getSumOfRoundIndexOfYellowPunish(String address, long endRoundIndex) {
        // todo auto-generated method stub(niels)
        return 0;
    }
}
