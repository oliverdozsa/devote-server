package controllers.voting;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.DefaultExceptionMapper;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import requests.voting.CommissionCreateTransactionRequest;
import requests.voting.CommissionInitRequest;
import requests.voting.CommissionSignEnvelopeRequest;
import responses.voting.CommissionAccountCreationResponse;
import responses.voting.CommissionGetAnEncryptedChoiceResponse;
import responses.voting.CommissionGetEnvelopeSignatureResponse;
import responses.voting.CommissionInitResponse;
import responses.voting.CommissionSignEnvelopeResponse;
import responses.voting.CommissionTransactionOfSignatureResponse;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.voting.CommissionService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static utils.StringUtils.redactWithEllipsis;

public class CommissionController extends Controller {
    private final FormFactory formFactory;
    private final CommissionService commissionService;

    private static final Logger.ALogger logger = Logger.of(CommissionController.class);

    private final Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private final Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    @Inject
    public CommissionController(FormFactory formFactory, CommissionService commissionService) {
        this.formFactory = formFactory;
        this.commissionService = commissionService;
    }

    public CompletionStage<Result> init(Http.Request request) {
        logger.info("init()");

        Form<CommissionInitRequest> form = formFactory.form(CommissionInitRequest.class).bindFromRequest(request);

        if (form.hasErrors()) {
            JsonNode errorJson = form.errorsAsJson();
            logger.warn("init(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

            return commissionService.init(form.get(), jwt)
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> signEnvelope(String votingId, Http.Request request) {
        logger.info("signEnvelope()");

        Form<CommissionSignEnvelopeRequest> signEnvelopeRequestForm = formFactory.form(CommissionSignEnvelopeRequest.class)
                .bindFromRequest(request);

        if (signEnvelopeRequestForm.hasErrors()) {
            JsonNode errorJson = signEnvelopeRequestForm.errorsAsJson();
            logger.warn("signEnvelope(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

            return commissionService.signEnvelope(signEnvelopeRequestForm.get(), jwt, votingId)
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> createTransaction(Http.Request request) {
        logger.info("createTransaction()");

        Form<CommissionCreateTransactionRequest> createTransactionRequestForm = formFactory
                .form(CommissionCreateTransactionRequest.class).bindFromRequest(request);

        if (createTransactionRequestForm.hasErrors()) {
            JsonNode errorJson = createTransactionRequestForm.errorsAsJson();
            logger.warn("createTransaction(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            CommissionCreateTransactionRequest createTransactionRequest = createTransactionRequestForm.get();
            return commissionService.createTransaction(createTransactionRequest)
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> transactionOfSignature(String signature) {
        logger.info("transactionOfSignature(): signature = {}", redactWithEllipsis(signature, 5));

        return commissionService.transactionOfSignature(signature)
                .thenApply(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> getEnvelopeSignature(String votingId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        logger.info("getEnvelopeSignature(): votingId = {}, user = {}", votingId, jwt.getUserId());
        return commissionService.signatureOfEnvelope(votingId, jwt.getUserId())
                .thenApply(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> getAnEncryptedChoice(String votingId, String choice) {
        logger.info("getAnEncryptedOptionsCode(): votingId = {}", votingId);
        return commissionService.encryptChoice(votingId, choice)
                .thenApply(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(CommissionInitResponse initResponse) {
        return ok(Json.toJson(initResponse));
    }

    private Result toResult(CommissionSignEnvelopeResponse signEnvelopeResponse) {
        return ok(Json.toJson(signEnvelopeResponse));
    }

    private Result toResult(CommissionAccountCreationResponse accountCreationResponse) {
        return ok(Json.toJson(accountCreationResponse));
    }

    private Result toResult(CommissionTransactionOfSignatureResponse txOfSignatureResponse) {
        return ok(Json.toJson(txOfSignatureResponse));
    }

    private Result toResult(CommissionGetEnvelopeSignatureResponse getEnvelopeSignatureResponse) {
        return ok(Json.toJson(getEnvelopeSignatureResponse));
    }

    private Result toResult(CommissionGetAnEncryptedChoiceResponse encryptedOptionCodeResponse) {
        return ok(Json.toJson(encryptedOptionCodeResponse));
    }
}
