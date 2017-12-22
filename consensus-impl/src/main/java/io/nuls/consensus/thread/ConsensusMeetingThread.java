package io.nuls.consensus.thread;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.DelegateAccountDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingThread implements Runnable {
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingThread INSTANCE = new ConsensusMeetingThread();
    private ConsensusCacheService consensusCacheService = ConsensusCacheService.getInstance();
    private boolean running = false;

    private ConsensusMeetingThread() {
    }

    public static ConsensusMeetingThread getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        this.running = true;
        // todo auto-generated method stub(niels)
//        统计共识列表
        List<Consensus<Agent>> list = calcConsensusAgentList();


    }

    private List<Consensus<Agent>> getDefaultSeedList() throws IOException {
        List<Consensus<Agent>> seedList = new ArrayList<>();
        Properties prop = ConfigLoader.loadProperties(PocConsensusConstant.DEFAULT_CONSENSUS_LIST_FILE);
        if (null == prop || prop.isEmpty()) {
            return seedList;
        }
        for (Object key : prop.keySet()) {
            String address = prop.getProperty((String) key);
            Consensus<Agent> member = new Consensus<>();
            member.setAddress(address);
            Agent agent = new Agent();
            agent.setDelegateAddress(address);
            agent.setStartTime(0);
            agent.setIntroduction("seed");
            agent.setCommissionRate(0);
            agent.setStatus(ConsensusStatusEnum.IN.getCode());
            agent.setSeed(true);
            seedList.add(member);
        }
        return seedList;
    }

    private List<Consensus<Agent>> calcConsensusAgentList() {
        List<Consensus<Agent>> list = new ArrayList<>();
        try {
            List<Consensus<Agent>> seedList = getDefaultSeedList();
            list.addAll(seedList);
        } catch (IOException e) {
            Log.error(e);
        }
        list.addAll(consensusCacheService.getCachedAgentList(ConsensusStatusEnum.IN));
        return list;
    }

    public boolean isRunning() {
        return running;
    }

    private void packing() {
        //todo
    }
}
