package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import requests.CastVoteInitRequest;
import responses.CastVoteInitResponse;
import services.CastVoteService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class CastVoteController extends Controller {
    private final FormFactory formFactory;
    private final CastVoteService castVoteService;

    private static final Logger.ALogger logger = Logger.of(CastVoteController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    @Inject
    public CastVoteController(FormFactory formFactory, CastVoteService castVoteService) {
        this.formFactory = formFactory;
        this.castVoteService = castVoteService;
    }

    public CompletionStage<Result> init(Http.Request request) {
        logger.info("init()");

        Form<CastVoteInitRequest> form = formFactory.form(CastVoteInitRequest.class).bindFromRequest(request);

        if(form.hasErrors()) {
            JsonNode errorJson = form.errorsAsJson();
            logger.warn("init(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            return castVoteService.init(form.get())
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    private Result toResult(CastVoteInitResponse initResponse) {
        // TODO
        return null;
    }
}
