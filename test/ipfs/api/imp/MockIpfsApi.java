package ipfs.api.imp;

import com.fasterxml.jackson.databind.JsonNode;
import ipfs.api.IpfsApi;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MockIpfsApi implements IpfsApi {
    private static final Random random = new Random();

    private static final Map<String, JsonNode> savedJsons = new HashMap<>();

    @Override
    public String saveJson(JsonNode json) {
        Long randomLong = random.nextLong();
        ByteBuffer longBytes = ByteBuffer.allocate(Long.BYTES);
        longBytes.putLong(randomLong);

        String mockCid = Base64.getEncoder().encodeToString(longBytes.array());
        savedJsons.put(mockCid, json);

        return mockCid;
    }

    @Override
    public JsonNode retrieveJson(String cid) {
        return savedJsons.get(cid);
    }
}
