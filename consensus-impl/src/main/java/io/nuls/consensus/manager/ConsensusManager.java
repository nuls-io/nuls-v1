package io.nuls.consensus.manager;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.block.BlockHeaderCacheManager;
import io.nuls.consensus.cache.manager.block.SmallBlockCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.BlockPersistenceThread;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo 加载配置、验证区块、验证交易、缓存、定时持久化、分叉处理、双花处理、共识会议、
 *
 * @author Niels
 * @date 2018/1/8
 */
public class ConsensusManager {
    private static ConsensusManager INSTANCE = new ConsensusManager();
    private BlockCacheManager blockCacheManager;
    private BlockHeaderCacheManager headerCacheManager;
    private SmallBlockCacheManager smallBlockCacheManager;
    private ConsensusCacheManager consensusCacheManager;
    private ConfirmingTxCacheManager confirmingTxCacheManager;
    private ReceivedTxCacheManager receivedTxCacheManager;
    private AccountService accountService;
    private boolean partakePacking = false;
    private List<String> seedNodeList;

    private String localAccountAddress;
    private PocMeetingRound currentRound;
    private ConsensusStatusInfo consensusStatusInfo;

    private ConsensusManager() {
    }

    public static ConsensusManager getInstance() {
        return INSTANCE;
    }

    private void loadConfigration() {
        NulsContext.getInstance().setGenesisBlock(GenesisBlock.getInstance());
        partakePacking = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_PARTAKE_PACKING, false);

        seedNodeList = new ArrayList<>();
        Set<String> seedAddressSet = new HashSet<>();
        String addresses = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_SEED_NODES, "");
        if (StringUtils.isBlank(addresses)) {
            return;
        }
        String[] array = addresses.split(PocConsensusConstant.SEED_NODES_DELIMITER);
        if (null == array) {
            return;
        }
        for (String address : array) {
            seedAddressSet.add(address);
        }
        this.seedNodeList.addAll(seedAddressSet);
    }

    public void init() {
        loadConfigration();
        accountService = NulsContext.getInstance().getService(AccountService.class);
        localAccountAddress = accountService.getDefaultAccount();

        blockCacheManager = BlockCacheManager.getInstance();
        blockCacheManager.init();
        consensusCacheManager = ConsensusCacheManager.getInstance();
        consensusCacheManager.init();
        headerCacheManager = BlockHeaderCacheManager.getInstance();
        headerCacheManager.init();
        smallBlockCacheManager = SmallBlockCacheManager.getInstance();
        smallBlockCacheManager.init();
        confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
        confirmingTxCacheManager.init();
        receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
        receivedTxCacheManager.init();

        this.initConsensusStatusInfo();
    }


    public void receiveBlock(Block block) {
        //todo
    }

    public void receiveTx(Transaction tx) {
        //todo
    }

    public void initConsensusStatusInfo() {
        ConsensusStatusInfo info = new ConsensusStatusInfo();
        info.setAddress(localAccountAddress);
        //consensus seed must partake packing
        boolean isSeed = this.seedNodeList.contains(localAccountAddress);
        if (isSeed) {
            info.setStatus(ConsensusStatusEnum.IN.getCode());
        } else {
            Consensus<Agent> memberSelf =
                    consensusCacheManager.getCachedAgent(localAccountAddress);
            if (null == memberSelf) {
                info.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
            }
            info.setStatus(memberSelf.getExtend().getStatus());
        }
        this.consensusStatusInfo = info;
    }

    public void joinMeeting() {
        TaskManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_CONSENSUS,
                ConsensusMeetingRunner.THREAD_NAME,
                ConsensusMeetingRunner.getInstance());
    }

    /**
     * data storage
     */
    public void startPersistenceWork() {
        TaskManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_CONSENSUS, BlockPersistenceThread.THREAD_NAME, BlockPersistenceThread.getInstance());
    }

    public ConsensusStatusInfo getConsensusStatusInfo() {
        return consensusStatusInfo;
    }

    public void startMaintenanceWork() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        try {
            blockMaintenanceThread.checkGenesisBlock();
            blockMaintenanceThread.syncBlock();
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            TaskManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_CONSENSUS,
                    BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
        }
    }

    public void destroy() {
        blockCacheManager.clear();
        consensusCacheManager.clear();
        headerCacheManager.clear();
        smallBlockCacheManager.clear();
        confirmingTxCacheManager.clear();
        receivedTxCacheManager.clear();
    }

    public String getLocalAccountAddress() {
        return localAccountAddress;
    }

    public void setCurrentRound(PocMeetingRound currentRound) {
        this.currentRound = currentRound;

    }

    public PocMeetingRound getCurrentRound() {
        return currentRound;
    }
}
