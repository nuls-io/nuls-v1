/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.storage.constant;

public interface ConsensusStorageConstant {

    String DB_NAME_CONSENSUS_AGENT = "consensus_agent";
    String DB_NAME_CONSENSUS_DEPOSIT = "consensus_deposit";
    String DB_NAME_CONSENSUS_PUNISH_LOG = "consensus_punish_log";
    String DB_NAME_CONSENSUS_BIFURCATION_EVIDENCE = "consensus_bifurcation_evidence";
    String DB_BIFURCATION_EVIDENCE_KEY = "bifurcation_evidence_key";
    String DB_NAME_RANDOM_SEEDS = "random_seed";

    byte[] EMPTY_SEED = new byte[32];

}
