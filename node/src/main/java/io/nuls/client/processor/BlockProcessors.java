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
import io.nuls.client.helper.CommandBulider;
import io.nuls.client.helper.CommandHelper;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.BlockService;

/**
 * @author Niels
 */
public abstract class BlockProcessors implements CommandProcessor {

    protected BlockService blockService = BlockService.BLOCK_SERVICE;

    public static class GetBlockHeader extends BlockProcessors {

        @Override
        public String getCommand() {
            return "getblockheader";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<hash> | <height> get block header by hash or block height - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getblockheader <hash> | <height>--get the block header with hash or height";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            String arg = args[1];
            RpcClientResult result;
            if(StringUtils.isNumeric(arg)) {
                result = this.blockService.getBlockHeaderNa2Nuls(Integer.valueOf(arg));
            } else {
                result = this.blockService.getBlockHeaderNa2Nuls(arg);
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
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<hash> | <height> get block by hash or block height - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getblock --get the block with hash or height";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            String arg = args[1];
            RpcClientResult result;
            if(StringUtils.isNumeric(arg)) {
                result = this.blockService.getBlockNa2Nuls(Integer.valueOf(arg));
            } else {
                result = this.blockService.getBlockNa2Nuls(arg);
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
            return "getbestblockheader";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription());
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getbestblockheader --get the best block header";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length > 1) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = this.blockService.getBestBlockHeaderNa2Nuls();
            return CommandResult.getResult(result);
        }
    }


    public static class ListBlockHeader extends BlockProcessors {

        @Override
        public String getCommand() {
            return "listblockheader";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<pageSize> pageSize - Required")
                    .newLine("\t<pageNumber> pageNumber - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "listblockheader <pageSize> <pageNumber>--get block header list";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            if(!StringUtils.isNumeric(args[1])) {
                return false;
            }
            if(!StringUtils.isNumeric(args[2])) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            Integer pageNumber = Integer.valueOf(args[2]);
            Integer pageSize = Integer.valueOf(args[1]);
            RpcClientResult result = this.blockService.listBlockHeaderNa2Nuls(pageNumber, pageSize);
            return CommandResult.getResult(result);
        }
    }



}
