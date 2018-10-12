package io.nuls.consensus.poc.rpc.model;

import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.utils.AddressTool;

import java.util.Date;

/**
 * @author: Niels Wang
 * @date: 2018/10/11
 */
public class PunishLogDTO {

    private byte type;
    private String address;
    private String time;
    private long height;
    private long roundIndex;
    private String reasonCode;

    public PunishLogDTO(PunishLogPo po) {
        this.type = po.getType();
        this.address = AddressTool.getStringAddressByBytes(po.getAddress());
        this.time = DateUtil.convertDate(new Date(po.getTime()));
        this.height = po.getHeight();
        this.roundIndex = po.getRoundIndex();
        this.reasonCode = PunishReasonEnum.getEnum(po.getReasonCode()).getMessage();
    }

    public byte getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getTime() {
        return time;
    }

    public long getHeight() {
        return height;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
