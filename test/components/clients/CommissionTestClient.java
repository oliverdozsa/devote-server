package components.clients;

import controllers.routes;
import units.crypto.RsaEnvelope;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionCreateTransactionRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;

import java.util.Base64;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.*;
import static utils.JwtTestUtils.addJwtTokenTo;

public class CommissionTestClient extends TestClient {
    public CommissionTestClient(Application application) {
        super(application);
    }

    public Result init(CommissionInitRequest initRequest, String userId) {
        String jwt = jwtTestUtils.createToken(userId, userId + "@mail.com");
        return initWithJwt(initRequest, jwt);
    }

    public Result initWithJwt(CommissionInitRequest initRequest, String jwt) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public SignOnEnvelopeResult signOnEnvelope(String publicKeyPem, String userId, String message, String votingId) {
        String jwt = jwtTestUtils.createToken(userId, userId + "@mail.com");
        return signOnEnvelopeWithJwt(publicKeyPem, jwt, message, votingId);
    }

    public SignOnEnvelopeResult signOnEnvelopeWithJwt(String publicKeyPem, String jwt, String message, String votingId) {
        RsaEnvelope rsaEnvelope = new RsaEnvelope(publicKeyPem);
        byte[] envelopeAsBytes = rsaEnvelope.create(message.getBytes());
        String envelopeAsBase64 = Base64.getEncoder().encodeToString(envelopeAsBytes);

        CommissionSignEnvelopeRequest signEnvelopeRequest = new CommissionSignEnvelopeRequest();
        signEnvelopeRequest.setEnvelopeBase64(envelopeAsBase64);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(signEnvelopeRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.signEnvelope(votingId).url());

        addJwtTokenTo(httpRequest, jwt);
        Result httpResult = route(application, httpRequest);
        return new SignOnEnvelopeResult(httpResult, rsaEnvelope);
    }

    public static CommissionCreateTransactionRequest createTransactionCreationRequest(String message, String envelopeSignatureBase64, RsaEnvelope rsaEnvelope) {
        byte[] envelopeSignature = Base64.getDecoder().decode(envelopeSignatureBase64);

        byte[] revealedSignature = rsaEnvelope.revealedSignature(envelopeSignature);
        String revealedSignatureBase64 = Base64.getEncoder().encodeToString(revealedSignature);

        CommissionCreateTransactionRequest transactionCreationRequest = new CommissionCreateTransactionRequest();
        transactionCreationRequest.setMessage(message);
        transactionCreationRequest.setRevealedSignatureBase64(revealedSignatureBase64);

        return transactionCreationRequest;
    }

    public Result requestAccountCreation(CommissionCreateTransactionRequest request) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(request))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.createTransaction().url());

        return route(application, httpRequest);
    }

    public Result transactionOfSignature(String signature) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.transactionOfSignature(signature).url());

        return route(application, httpRequest);
    }

    public Result envelopeSignatureOf(String votingId, String user) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.getEnvelopeSignature(votingId).url());

        String jwt = jwtTestUtils.createToken(user, user + "@mail.com");
        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public Result encryptChoice(String votingId, String choice) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.CommissionController.getAnEncryptedChoice(votingId, choice).url());

        return route(application, httpRequest);
    }

    public static class SignOnEnvelopeResult {
        public Result http;
        public RsaEnvelope envelope;

        public SignOnEnvelopeResult(Result result, RsaEnvelope envelope) {
            this.http = result;
            this.envelope = envelope;
        }
    }
}
