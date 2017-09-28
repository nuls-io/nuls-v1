package io.nuls.rpcserver.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */

@Path("/")
public class TestResouce {

    public TestResouce() {
    }

    @GET
    @Produces("text/plain")
    public String getMessage() {
        return "Hello World!";
    }

    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String getMessage2() {
        return "Hello World!22222222222222";
    }
}