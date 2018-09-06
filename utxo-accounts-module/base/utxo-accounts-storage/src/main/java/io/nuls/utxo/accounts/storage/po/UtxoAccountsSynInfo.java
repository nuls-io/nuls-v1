package io.nuls.utxo.accounts.storage.po;

import java.io.Serializable;

public class UtxoAccountsSynInfo implements Serializable {
    private Long hadSynBlockHeight=-1L;
    private Long updateTimeMillion=0L;

    public UtxoAccountsSynInfo(long hadSynBlockHeight){
        this.hadSynBlockHeight=hadSynBlockHeight;
    }
    public Long getHadSynBlockHeight() {
        return hadSynBlockHeight;
    }

    public void setHadSynBlockHeight(Long hadSynBlockHeight) {
        this.hadSynBlockHeight = hadSynBlockHeight;
    }

    public Long getUpdateTimeMillion() {
        return updateTimeMillion;
    }

    public void setUpdateTimeMillion(Long updateTimeMillion) {
        this.updateTimeMillion = updateTimeMillion;
    }
}
