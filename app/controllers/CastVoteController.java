package controllers;

import play.Logger;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class CastVoteController extends Controller {
    private final FormFactory formFactory;

    private static final Logger.ALogger logger = Logger.of(CastVoteController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    @Inject
    public CastVoteController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> init(Http.Request request) {
        // TODO
        return completedFuture(notFound());
    }
}
