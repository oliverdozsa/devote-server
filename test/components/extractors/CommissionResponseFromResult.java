package components.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import static components.extractors.GenericDataFromResult.jsonOf;

public class CommissionResponseFromResult {
    public static String publicKeyOf(Result result) {
        JsonNode initResponse = jsonOf(result);
        return initResponse.get("publicKey").asText();
    }

    public static String sessionJwtOf(Result result) {
        JsonNode initResponse = jsonOf(result);
        return initResponse.get("sessionJwt").asText();
    }

    public static String envelopeSignatureOf(Result result) {
        JsonNode signEnveloperResponse = jsonOf(result);
        return signEnveloperResponse.get("envelopeSignatureBase64").asText();
    }

    public static String accountCreationTransactionOf(Result result) {
        JsonNode accountCreationResponse = jsonOf(result);
        return accountCreationResponse.get("transaction").asText();
    }

    public static String transactionOfSignature(Result result) {
        JsonNode accountCreationResponse = jsonOf(result);
        return accountCreationResponse.get("transaction").asText();
    }
}
