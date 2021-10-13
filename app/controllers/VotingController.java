package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import dto.CreateVotingDto;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
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

        Form<CreateVotingDto> form = formFactory.form(CreateVotingDto.class).bindFromRequest(request);

        if(form.hasErrors()) {
            JsonNode errorJson = form.errorsAsJson();
            logger.warn("create(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            // TODO
            votingService.create();

            return completedFuture(ok());
        }
    }
}
