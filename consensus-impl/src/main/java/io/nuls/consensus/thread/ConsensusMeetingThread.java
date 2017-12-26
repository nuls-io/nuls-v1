package io.nuls.consensus.thread;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.TxComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.service.intf.LedgerService;
import org.spongycastle.util.Times;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingThread implements Runnable {
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingThread INSTANCE = new ConsensusMeetingThread();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private String localAccountAddress;
    private ConsensusCacheService consensusCacheService = ConsensusCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private boolean running = false;
    private NulsContext context = NulsContext.getInstance();
    private PocMeetingRound round;

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
        this.localAccountAddress = accountService.getDefaultAccount();
        nextRound();
    }

    private void nextRound() {
        round = calcRound();
        while (TimeService.currentTimeMillis() < (round.getStartTime())) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        List<Consensus<Agent>> list = calcConsensusAgentList();
        round.setMemberCount(list.size());
        List<PocMeetingMember> memberList = new ArrayList<>();
        for (Consensus<Agent> ca : list) {
            PocMeetingMember mm = new PocMeetingMember();
            mm.setAddress(ca.getAddress());
            mm.setPackerAddress(ca.getExtend().getDelegateAddress());
            mm.setRoundStartTime(round.getStartTime());
            memberList.add(mm);
        }
        Collections.sort(memberList);
        round.setMemberList(memberList);
        round.setEndTime(round.getStartTime() + round.getMemberCount() * PocConsensusConstant.BLOCK_TIME_INTERVAL * 1000L);
        startMeeting();
    }

    private void startMeeting() {
        PocMeetingMember self = round.getMember(localAccountAddress);
        if (null == self) {
            this.nextRound();
            return;
        }
        long timeUnit = 100L;
        while (TimeService.currentTimeMillis() >= (self.getPackTime() - timeUnit)) {
            try {
                Thread.sleep(timeUnit);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        packing();
    }

    private void packing() {
        List<Transaction> txList = ledgerService.getTxListFromCache();
        txList.sort(new TxComparator());
        Block block = new Block();
        //todo

    }

    private PocMeetingRound calcRound() {
        Block bestBlock = blockService.getLocalHighestBlock();
        PocMeetingRound round = new PocMeetingRound();
        do {
            if (bestBlock.getHeader().getHeight() == 1) {
                round.setStartTime(this.context.getGenesisBlock().getHeader().getTime() + 10000L);
                break;
            }
            BlockRoundData lastRoundData = null;
            try {
                lastRoundData = new BlockRoundData(bestBlock.getExtend());
            } catch (NulsException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }
            round.setStartTime(lastRoundData.getRoundEndTime());
        } while (false);
        return round;
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

}
