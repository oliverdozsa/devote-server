package ipfs.api.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import galactic.blockchain.api.BlockchainException;
import ipfs.api.IpfsApi;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class Web3StorageIpfsApiImp implements IpfsApi {
    private WSClient wsClient;
    private String jwt;

    @Inject
    public Web3StorageIpfsApiImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        this.jwt = config.getString("galactic.vote.ipfs.web3storage.jwt");
    }

    @Override
    public String saveJson(JsonNode json) {
        CompletionStage<WSResponse> response = this.wsClient.url("https://api.web3.storage/upload")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + jwt)
                .post(json.toString());

        try {
            return response
                    .thenApply(r -> {
                        JsonNode responseJson = r.asJson();
                        return responseJson.get("cid").asText();
                    })
                    .toCompletableFuture().get();
        } catch (Exception e) {
            throw new BlockchainException("Failed to store json in IPFS through Web3Storage.", e);
        }
    }

    @Override
    public JsonNode retrieveJson(String cid) {
        CompletionStage<WSResponse> respone = this.wsClient.url("https://w3s.link/ipfs/" + cid)
                .addHeader("Content-Type", "application/json")
                .get();

        try {
            return respone
                    .thenApply(WSResponse::asJson)
                    .toCompletableFuture()
                    .get();
        } catch (Exception e) {
            throw new BlockchainException("Failed to get json in IPFS through Web3Storage.", e);
        }
    }
}
