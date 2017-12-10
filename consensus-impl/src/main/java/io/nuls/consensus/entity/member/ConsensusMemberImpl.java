package io.nuls.consensus.entity.member;

import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusMemberImpl extends ConsensusMember<ConsensusMemberData> {

    @Override
    protected ConsensusMemberData parseExtend(NulsByteBuffer byteBuffer) {
        ConsensusMemberData data = new ConsensusMemberData();
        data.parse(byteBuffer);
        return data;
    }
}


