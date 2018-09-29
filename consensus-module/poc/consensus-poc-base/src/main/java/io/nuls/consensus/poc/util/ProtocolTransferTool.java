/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.consensus.poc.util;

import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.protocol.base.version.ProtocolContainer;
import io.nuls.protocol.storage.po.BlockProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;

public class ProtocolTransferTool {

    public static ProtocolInfoPo toProtocolInfoPo(ProtocolContainer container) {
        ProtocolInfoPo infoPo = new ProtocolInfoPo();
        infoPo.setVersion(container.getVersion());
        infoPo.setPercent(container.getPercent());
        infoPo.setDelay(container.getDelay());
        infoPo.setCurrentDelay(container.getCurrentDelay());
        infoPo.setCurrentPercent(container.getCurrentPercent());
        infoPo.setAddressSet(container.getAddressSet());
        infoPo.setStatus(container.getStatus());
        infoPo.setRoundIndex(container.getRoundIndex());
        infoPo.setEffectiveHeight(container.getEffectiveHeight());
        infoPo.setPreAddressSet(container.getPreAddressSet());
        infoPo.setPrePercent(container.getPrePercent());
        return infoPo;
    }

    public static ProtocolTempInfoPo createProtocolTempInfoPo(BlockExtendsData extendsData) {
        ProtocolTempInfoPo infoPo = new ProtocolTempInfoPo();
        infoPo.setVersion(extendsData.getCurrentVersion());
        infoPo.setDelay(extendsData.getDelay());
        infoPo.setPercent(extendsData.getPercent());
        return infoPo;
    }


    public static BlockProtocolInfoPo toBlockProtocolInfoPo(BlockHeader header, ProtocolContainer container) {
        BlockProtocolInfoPo infoPo = new BlockProtocolInfoPo();
        infoPo.setBlockHeight(header.getHeight());
        infoPo.setVersion(container.getVersion());
        infoPo.setCurrentDelay(container.getCurrentDelay());
        infoPo.setEffectiveHeight(container.getEffectiveHeight());
        infoPo.setRoundIndex(container.getRoundIndex());
        infoPo.setStatus(container.getStatus());
        infoPo.setAddressSet(container.getAddressSet());
        infoPo.setPreAddressSet(container.getPreAddressSet());
        infoPo.setPrePercent(container.getPrePercent());
        return infoPo;
    }

    public static void copyFromBlockProtocolInfoPo(BlockProtocolInfoPo infoPo, ProtocolContainer container) {
        container.setStatus(infoPo.getStatus());
        container.setEffectiveHeight(infoPo.getEffectiveHeight());
        container.setRoundIndex(infoPo.getRoundIndex());
        container.setAddressSet(infoPo.getAddressSet());
        container.setCurrentDelay(infoPo.getCurrentDelay());
        container.setPreAddressSet(infoPo.getPreAddressSet());
        container.setPrePercent(infoPo.getPrePercent());
    }

    public static BlockProtocolInfoPo toBlockProtocolInfoPo(BlockHeader header, ProtocolTempInfoPo tempInfoPo) {
        BlockProtocolInfoPo infoPo = new BlockProtocolInfoPo();
        infoPo.setBlockHeight(header.getHeight());
        infoPo.setVersion(tempInfoPo.getVersion());
        infoPo.setCurrentDelay(tempInfoPo.getCurrentDelay());
        infoPo.setEffectiveHeight(tempInfoPo.getEffectiveHeight());
        infoPo.setRoundIndex(tempInfoPo.getRoundIndex());
        infoPo.setStatus(tempInfoPo.getStatus());
        infoPo.setAddressSet(tempInfoPo.getAddressSet());
        infoPo.setPrePercent(tempInfoPo.getPrePercent());
        infoPo.setPreAddressSet(tempInfoPo.getPreAddressSet());
        return infoPo;
    }

    public static void copyFromBlockProtocolTempInfoPo(BlockProtocolInfoPo infoPo, ProtocolTempInfoPo tempInfoPo) {
        tempInfoPo.setStatus(infoPo.getStatus());
        tempInfoPo.setEffectiveHeight(infoPo.getEffectiveHeight());
        tempInfoPo.setRoundIndex(infoPo.getRoundIndex());
        tempInfoPo.setAddressSet(infoPo.getAddressSet());
        tempInfoPo.setCurrentDelay(infoPo.getCurrentDelay());
    }
}
