package de.bsd.webGoo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * // TODO: Document this
 * @author Heiko W. Rupp
 */
@Path("/")
@Produces({"application/json","application/vnd.rhq.wrapped+json"})
public class GooHandler {

    public GooHandler() {
    }

    private ListeningExecutorService metricsTasks = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(4, new MetricsThreadFactory()));

    @Context
    HttpHeaders headers;

    @GET
    @Path("test")
    public void getBla(@Suspended final AsyncResponse asyncResponse, @QueryParam("immediate") boolean immediate) {

        List<String> list = new ArrayList<String>();
        list.add("Hello");
        list.add("World");

        final MediaType mediaType = headers.getAcceptableMediaTypes().get(0);

        final ListenableFuture<List<String>> future  ;

        if (immediate) {
            System.err.println("Immediate");
            future = Futures.immediateFuture(list);
        } else {
            System.err.println("Via executor");
            ListenableFuture<List<String>> tmp = Futures.immediateFuture(list);
            future = Futures.transform(tmp,new NoOpCounterMapper(),metricsTasks);
        }
        Futures.addCallback(future, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> stringList) {
                Response jaxrs = Response.ok(stringList).type(mediaType).build();
                asyncResponse.resume(jaxrs);

            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                asyncResponse.resume(t);
            }
        });


    }

    private class NoOpCounterMapper implements Function<List<String>, List<String>> {
        @Override
        public List<String> apply(List<String> input) {
            return input;
        }
    }

}
