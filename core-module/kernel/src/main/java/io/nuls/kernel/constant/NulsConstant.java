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
package io.nuls.kernel.constant;

import io.nuls.kernel.utils.AddressTool;

/**
 * 系统常量集合
 * SYSTEM CONSTANT
 *
 * @author Niels
 */
public interface NulsConstant {

    /**
     * nuls版本升级主配置文件名
     * The nuls version upgrades the main configuration file
     */
    String NULS_VERSION_XML = "nuls-version.xml";

    byte[] BLACK_HOLE_ADDRESS = AddressTool.getAddress("Nse5FeeiYk1opxdc5RqYpEWkiUDGNuLs");

    /**
     * 系统配置文件名称
     * System configuration file name.
     */
    String USER_CONFIG_FILE = "nuls.ini";

    /**
     * 模块配置文件名称
     * Module configuration file name.
     */
    String MODULES_CONFIG_FILE = "modules.ini";

    /**
     * 模块启动类的配置项名称
     * The name of the configuration item for the module startup class.
     */
    String MODULE_BOOTSTRAP_KEY = "bootstrap";

    /**
     * ----[ System] ----
     */
    /**
     * 系统配置项section名称
     * The configuration item section name of the kernel module.
     */
    String CFG_SYSTEM_SECTION = "System";

    /**
     * 系统配置中语言设置的字段名
     * The field name of the language set in the system configuration.
     */
    String CFG_SYSTEM_LANGUAGE = "language";

    /**
     * 系统配置中编码设置的字段名
     * The field name of the code setting in the system configuration.
     */
    String CFG_SYSTEM_DEFAULT_ENCODING = "encoding";

    /**
     * 内核模块的模块id
     * The module id of micro kernel module
     */
    short MODULE_ID_MICROKERNEL = 1;

    /**
     * 共识奖励交易的类型
     * A consensus award for the type of trade.
     */
    int TX_TYPE_COINBASE = 1;

    /**
     * 转账交易的类型
     * the type of the transfer transaction
     */
    int TX_TYPE_TRANSFER = 2;


    int TX_TYPE_PROTOCOL = 10;

    /**
     * 多地址转账交易的类型
     * the type of the Multiple address transfer transaction
     */
    int TX_TYPE_TRANSFER_MULTIPLE = 11;
    /**
     * 零钱换整
     * the type of the Multiple address transfer transaction
     */
    int TX_TYPE_CHANGE_WHOLE = 12;
    /**
     * 业务数据承载交易的类型
     * Type of business data bearing transaction
     */
    int TX_TYPE_DATA = 10;

    /**
     * 空值占位符
     * Null placeholder.
     */
    byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 48位整型数据长度
     * 48 bit integer data length.
     */
    int INT48_VALUE_LENGTH = 6;

    /**
     * utxo锁定时间分界值
     * 小于该值表示按照高度锁定
     * 大于该值表示按照时间锁定
     */
    long BlOCKHEIGHT_TIME_DIVIDE = 1000000000000L;

    /**
     * 脚本标识位
     * Null placeholder.
     */
    byte[] SIGN_HOLDER = new byte[]{(byte) 0x00, (byte) 0x00};

}
