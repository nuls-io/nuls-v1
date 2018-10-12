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

package io.nuls.kernel.cfg;


import io.nuls.core.tools.cfg.IniEntity;

/**
 * 用来管理系统配置项和系统版本信息
 * <p>
 * Used to manage system configuration items and system version information.
 *
 * @author: Niels Wang
 */
public class NulsConfig {

    /**
     * nuls底层代码的版本号
     * The version number of the underlying code for nuls.
     */
    public static String VERSION = "1.1.2";

    /**
     * 系统使用的编码方式
     * The encoding used by the nuls system.
     */
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * nuls系统配置文件中加载的配置项
     * The configuration items loaded in the nuls system configuration file.
     */
    public static IniEntity NULS_CONFIG;

    /**
     * 模块配置文件中加载的所有配置项
     * All the configuration items that are loaded in the module configuration file.
     */
    public static IniEntity MODULES_CONFIG;

}
