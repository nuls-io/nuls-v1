package io.nuls.rpc.sdk.utils;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Niels on 2017/10/31.
 * nuls.io
 */
public class RestFulUtilsTest {
    private RestFulUtils util;

    @Before
    public void init() {
        this.util = RestFulUtils.getInstance();
        this.util.init("http://127.0.0.1:8001/nuls");
    }

    @Test
    public void test() {
        RpcClientResult result = this.util.get("/", null);
        System.out.println(result.toString());
    }
}