package clients;

import controllers.routes;
import crypto.RsaEnvelope;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import utils.JwtTestUtils;

import java.util.Base64;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static utils.JwtTestUtils.addJwtTokenTo;

public class CommissionTestClient extends TestClient {
    private final JwtTestUtils jwtTestUtils;

    public CommissionTestClient(Application application) {
        super(application);
        jwtTestUtils = new JwtTestUtils(application.config());
    }

    public Result init(CommissionInitRequest initRequest, String userId) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        String jwt = jwtTestUtils.createToken(userId);
        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public SignOnEnvelopeResult signOnEnvelope(String publicKeyPem, String sessionJwt, String message) {
        RsaEnvelope rsaEnvelope = new RsaEnvelope(publicKeyPem);
        byte[] envelopeAsBytes = rsaEnvelope.create(message.getBytes());
        String envelopeAsBase64 = Base64.getEncoder().encodeToString(envelopeAsBytes);

        CommissionSignEnvelopeRequest signEnvelopeRequest = new CommissionSignEnvelopeRequest();
        signEnvelopeRequest.setEnvelopeBase64(envelopeAsBase64);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(signEnvelopeRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.signEnvelope().url());

        addJwtTokenTo(httpRequest, sessionJwt);
        Result httpResult = route(application, httpRequest);
        return new SignOnEnvelopeResult(httpResult, rsaEnvelope);
    }

    public Result requestAccountCreation(String message, String envelopeSignatureBase64, RsaEnvelope rsaEnvelope) {
        byte[] envelopeSignature = Base64.getDecoder().decode(envelopeSignatureBase64);

        byte[] revealedSignature = rsaEnvelope.revealedSignature(envelopeSignature);
        String revealedSignatureBase64 = Base64.getEncoder().encodeToString(revealedSignature);

        CommissionAccountCreationRequest accountCreationRequest = new CommissionAccountCreationRequest();
        accountCreationRequest.setMessage(message);
        accountCreationRequest.setRevealedSignatureBase64(revealedSignatureBase64);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(accountCreationRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.createAccount().url());

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
