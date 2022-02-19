package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import requests.CreateVotingRequest;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.VotingService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class VotingController extends Controller {
    private final FormFactory formFactory;
    private final VotingService votingService;

    private static final Logger.ALogger logger = Logger.of(VotingController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    @Inject
    public VotingController(FormFactory formFactory, VotingService votingService) {
        this.formFactory = formFactory;
        this.votingService = votingService;
    }

    public CompletionStage<Result> create(Http.Request request) {
        logger.info("create()");

        Form<CreateVotingRequest> form = formFactory.form(CreateVotingRequest.class).bindFromRequest(request);

        if (form.hasErrors()) {
            JsonNode errorJson = form.errorsAsJson();
            logger.warn("create(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

            return votingService.create(form.get(), jwt)
                    .thenApply(id -> toCreatedVotingResult(request, id))
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> single(String id) {
        logger.info("single(): id = {}", id);

        return votingService.single(id)
                .thenApply(v -> ok(Json.toJson(v)))
                .exceptionally(mapExceptionWithUnpack);
    }

    private static Result toCreatedVotingResult(Http.Request request, String votingId) {
        String location = routes.VotingController
                .single(votingId)
                .absoluteURL(request);
        return created().withHeader(LOCATION, location);
    }
}
