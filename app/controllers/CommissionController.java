package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionAccountCreationResponse;
import responses.CommissionGetEnvelopeSignatureResponse;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import responses.CommissionTransactionOfSignatureResponse;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.CommissionService;

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

    public CompletionStage<Result> signEnvelope(Http.Request request) {
        logger.info("signEnvelope()");

        Form<CommissionSignEnvelopeRequest> signEnvelopeRequestForm = formFactory.form(CommissionSignEnvelopeRequest.class)
                .bindFromRequest(request);

        if (signEnvelopeRequestForm.hasErrors()) {
            JsonNode errorJson = signEnvelopeRequestForm.errorsAsJson();
            logger.warn("signEnvelope(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

            return commissionService.signEnvelope(signEnvelopeRequestForm.get(), jwt)
                    .thenApply(this::toResult)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> createAccount(Http.Request request) {
        logger.info("createAccount()");

        Form<CommissionAccountCreationRequest> accountCreationRequestForm = formFactory
                .form(CommissionAccountCreationRequest.class).bindFromRequest(request);

        if (accountCreationRequestForm.hasErrors()) {
            JsonNode errorJson = accountCreationRequestForm.errorsAsJson();
            logger.warn("createAccount(): Form has errors! error json:\n{}", errorJson.toPrettyString());

            return completedFuture(badRequest(errorJson));
        } else {
            CommissionAccountCreationRequest accountCreationRequest = accountCreationRequestForm.get();
            return commissionService.createAccount(accountCreationRequest)
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

    public CompletionStage<Result> getEnvelopeSignature(String votingId, String user) {
        logger.info("getEnvelopeSignature(): votingId = {}, user = {}", votingId, user);
        return commissionService.signatureOfEnvelope(votingId, user)
                .thenApply(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(CommissionInitResponse initResponse) {
        Result result = ok(Json.toJson(initResponse));
        return result.withHeader("SESSION-TOKEN", initResponse.getSessionJwt());
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
}
