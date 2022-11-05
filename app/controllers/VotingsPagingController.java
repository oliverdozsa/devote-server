package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.PageVotingsRequest;
import responses.Page;
import responses.PageVotingItemResponse;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.VotingsPagingService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class VotingsPagingController {
    private static final Logger.ALogger logger = Logger.of(VotingsPagingController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private final FormFactory formFactory;
    private final VotingsPagingService votingsPagingService;

    @Inject
    public VotingsPagingController(FormFactory formFactory, VotingsPagingService votingsPagingService) {
        this.formFactory = formFactory;
        this.votingsPagingService = votingsPagingService;
    }

    public CompletionStage<Result> publicVotings(Http.Request request) {
        logger.info("publicVotings()");
        return preprocess(request, votingsPagingService::publicVotings);
    }

    public CompletionStage<Result> votingsOfVoteCaller(Http.Request request) {
        logger.info("votingsOfVoteCaller()");

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return preprocess(request, pagingRequest -> votingsPagingService.votingsOfVoteCaller(pagingRequest, jwt));
    }

    public CompletionStage<Result> votingsOfVoter(Http.Request request) {
        logger.info("votingsOfVoter()");

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return preprocess(request, pagingRequest -> votingsPagingService.votingsOfVoter(pagingRequest, jwt));
    }

    private CompletionStage<Result> preprocess(Http.Request request, Function<PageVotingsRequest, CompletionStage<Page<PageVotingItemResponse>>> onSuccess) {
        Form<PageVotingsRequest> form = formFactory.form(PageVotingsRequest.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            JsonNode errorJson = form.errorsAsJson();
            logger.warn("pagingsBase(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            return onSuccess.apply(form.get())
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    private Result toResult(Page<PageVotingItemResponse> page) {
        return ok(Json.toJson(page));
    }
}
