package io.nuls.consensus.poc.util;

import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.protocol.base.version.ProtocolContainer;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;

public class ProtocolTransferTool {

    public static ProtocolInfoPo toProtocolInfoPo(ProtocolContainer container) {
        ProtocolInfoPo infoPo = new ProtocolInfoPo();
        infoPo.setVersion(container.getVersion());
        infoPo.setPercent(container.getPercent());
        infoPo.setDelay(container.getDelay());
        infoPo.setCurrentDelay(container.getCurrentDelay());
        infoPo.setAddressSet(container.getAddressSet());
        infoPo.setStatus(container.getStatus());
        infoPo.setRoundIndex(container.getRoundIndex());
        return infoPo;
    }

    public static ProtocolTempInfoPo createProtocolTempInfoPo(BlockExtendsData extendsData) {
        ProtocolTempInfoPo infoPo = new ProtocolTempInfoPo();
        infoPo.setVersion(extendsData.getCurrentVersion());
        infoPo.setDelay(extendsData.getDelay());
        infoPo.setPercent(extendsData.getPercent());
        return infoPo;
    }
}
