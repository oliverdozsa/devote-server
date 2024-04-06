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
    private String token;

    @Inject
    public Web3StorageIpfsApiImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        this.token = config.getString("galactic.host.ipfs.web3storage.token");
    }

    @Override
    public String saveJson(JsonNode json) {
        CompletionStage<WSResponse> response = this.wsClient.url("https://bridge.galactic.pub/voting-ipfs")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .post(json.toString());

        try {
            return response
                    .thenApply(r -> {
                        if(r.getStatus() < 200 && r.getStatus() >= 300) {
                            throw new BlockchainException("Failed to store through bridge! status = " + r.getStatus());
                        }

                        String locationHeader = r.getSingleHeader("Location").get();
                        return parseCidFromUrl(locationHeader);
                    })
                    .toCompletableFuture().get();
        } catch (Exception e) {
            throw new BlockchainException("Failed to store json in IPFS through Web3Storage.", e);
        }
    }

    @Override
    public JsonNode retrieveJson(String cid) {
        CompletionStage<WSResponse> respone = this.wsClient.url("https://" + cid + ".ipfs.w3s.link")
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

    private static String parseCidFromUrl(String url) {
        String cidPart = url.split("\\.")[0];
        return cidPart.split("https://")[1];
    }
}
