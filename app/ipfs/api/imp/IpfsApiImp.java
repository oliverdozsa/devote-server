package ipfs.api.imp;

import com.fasterxml.jackson.databind.JsonNode;
import galactic.blockchain.api.BlockchainException;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.cid.Cid;
import ipfs.api.IpfsApi;
import play.libs.Json;

import javax.inject.Inject;
import java.io.IOException;

public class IpfsApiImp implements IpfsApi {
    private final IPFS ipfs;

    @Inject
    public IpfsApiImp(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    @Override
    public String saveJson(JsonNode json) {
        try {
            String jsonStr = json.toString();
            MerkleNode node = ipfs.dag.put("json", jsonStr.getBytes());
            Cid cid = Cid.buildCidV1(Cid.Codec.DagCbor, node.hash.getType(), node.hash.getHash());
            return cid.toString();
        } catch (IOException e) {
            throw new BlockchainException("Failed to store json in IPFS.", e);
        }
    }

    @Override
    public JsonNode retrieveJson(String cidStr) {

        try {
            Cid cid = Cid.decode(cidStr);
            byte[] content = ipfs.dag.get(cid);
            String jsonStr = new String(content);
            return Json.parse(jsonStr);
        } catch (IOException e) {
            throw new BlockchainException("Failed to read json with cid = " + cidStr + " from IPFS.", e);
        }
    }
}
