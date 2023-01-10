package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TokenAuthController extends Controller {
    private static final Logger.ALogger logger = Logger.of(TokenAuthController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    public CompletionStage<Result> auth(String token) {
        // TODO
        return completedFuture(notFound());
    }
}
