/*
 *
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

package io.nuls.client.processor;

import io.nuls.client.entity.CommandResult;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.BlockService;

/**
 * @author Niels
 */
public abstract class BlockProcessors implements CommandProcessor {

    protected BlockService blockService = new BlockService();

    public static class GetBlockHeader extends BlockProcessors {

        @Override
        public String getCommand() {
            return "getblockheader";
        }

        @Override
        public String getCommandDescription() {
            return "getblockheader --get the block header with hash or height";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            String arg = args[1];
            RpcClientResult result;
            if(StringUtils.isNumeric(arg)) {
                result = this.blockService.getBlockHeader(Integer.valueOf(arg));
            } else {
                result = this.blockService.getBlockHeader(arg);
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetBlock extends BlockProcessors {

        @Override
        public String getCommand() {
            return "getblock";
        }

        @Override
        public String getCommandDescription() {
            return "getblock --get the block with hash or height";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            String arg = args[1];
            RpcClientResult result;
            if(StringUtils.isNumeric(arg)) {
                result = this.blockService.getBlock(Integer.valueOf(arg));
            } else {
                result = this.blockService.getBlock(arg);
            }
            return CommandResult.getResult(result);
        }
    }

    /**
     * Get the best header of blocks
     */
    public static class GetBestBlockHeader extends BlockProcessors {

        @Override
        public String getCommand() {
            return "bestheight";
        }

        @Override
        public String getCommandDescription() {
            return "bestheight --get the best block header";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = this.blockService.getBestBlockHeader();
            return CommandResult.getResult(result);
        }
    }


    public static class ListBlockHeader extends BlockProcessors {

        @Override
        public String getCommand() {
            return "listblockheader";
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "listblockheader --get the best block header";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            Integer pageNumber = Integer.valueOf(args[2]);
            Integer pageSize = Integer.valueOf(args[1]);
            RpcClientResult result = this.blockService.listBlockHeader(pageNumber, pageSize);
            return CommandResult.getResult(result);
        }
    }



}
