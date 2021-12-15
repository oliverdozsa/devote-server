package ipfs.api;

import com.fasterxml.jackson.databind.JsonNode;

public interface IpfsApi {
    String saveJson(JsonNode json);
    JsonNode retrieveJson(String cid);
}
