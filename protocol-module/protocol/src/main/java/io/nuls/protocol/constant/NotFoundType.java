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

package io.nuls.protocol.constant;

/**
 * 用于网络数据获取，当对等节点获取的数据不能找到时，会返回 {@link io.nuls.protocol.model.NotFound} 结果，
 * NotFound结果根据获取的数据不同，分为几种不同的类别，具体的类别定义在本类中。
 * <p>
 * For network data acquisition, data obtained when peer node cannot be found, returns {@link io.nuls.protocol.model.NotFound}  as a result,
 * NotFound results are divided into several categories according to the data obtained, and specific categories are defined in this class.
 *
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public enum NotFoundType {
    /**
     * 获取区块找不到时，返回的Not Found 类型
     * When the block cannot be Found, the returned Not Found type.
     */
    BLOCK(1),
    /**
     * 获取交易找不到时，返回的Not Found 类型
     * When the transaction cannot be Found, the returned Not Found type.
     */
    TRANSACTION(2),
    /**
     * 获取区块头摘要找不到时，返回的Not Found 类型
     * When the block header digest data cannot be Found, the returned Not Found type.
     */
    HASHES(3);

    /**
     * 对应的code，用于{@link io.nuls.protocol.model.NotFound}类序列化使用
     * type code，for serialize of {@link io.nuls.protocol.model.NotFound}
     */
    private final int code;

    NotFoundType(int code) {
        this.code = code;
    }

    /**
     * 获取该找不到类型对应的编码
     * Gets the code that the type corresponds to.
     *
     * @return int
     */
    public int getCode() {
        return code;
    }

    /**
     * Get Enum by type code
     * 根据编码获取枚举实例
     *
     * @param code int type code
     * @return {@link NotFoundType}
     */
    public static NotFoundType getType(int code) {
        switch (code) {
            case 1:
                return BLOCK;
            case 2:
                return TRANSACTION;
            case 3:
                return HASHES;
            default:
                return null;
        }
    }
}
