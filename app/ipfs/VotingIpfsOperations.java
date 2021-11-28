package ipfs;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import data.entities.JpaVoting;
import data.operations.VotingDbOperations;
import devote.blockchain.Blockchains;
import devote.blockchain.api.BlockchainException;
import executioncontexts.BlockchainExecutionContext;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.cid.Cid;
import io.ipfs.multiaddr.MultiAddress;
import ipfs.data.IpfsVoting;
import ipfs.data.IpfsVotingFromJpaVoting;
import play.Logger;
import play.libs.Json;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VotingIpfsOperations {
    private final VotingDbOperations votingDbOperations;
    private final BlockchainExecutionContext executionContext;
    private final IpfsVotingFromJpaVoting ipfsVotingFromJpaVoting;

    private final IPFS ipfs;

    private static final Logger.ALogger logger = Logger.of(VotingIpfsOperations.class);

    @Inject
    public VotingIpfsOperations(
            VotingDbOperations votingDbOperations,
            BlockchainExecutionContext executionContext,
            Config config,
            Blockchains blockchains) {
        this.votingDbOperations = votingDbOperations;
        this.executionContext = executionContext;

        String ipfsNodeAddress = config.getString("devote.ipfs.node.address");
        ipfs = new IPFS(new MultiAddress(ipfsNodeAddress));

        ipfsVotingFromJpaVoting = new IpfsVotingFromJpaVoting(blockchains);
    }

    public CompletionStage<String> saveVotingToIpfs(Long votingId) {
        return votingDbOperations.single(votingId)
                .thenCompose(this::saveJpaVotingToIpfs);
    }

    private CompletionStage<String> saveJpaVotingToIpfs(JpaVoting voting) {
        return supplyAsync(() -> {
            logger.info("saveJpaVotingToIpfs(): voting id = {}", voting.getId());

            IpfsVoting ipfsVoting = ipfsVotingFromJpaVoting.convert(voting);
            JsonNode ipfsVotingJson = Json.toJson(ipfsVoting);
            String ipfsVotingJsonStr = ipfsVotingJson.toString();
            try {
                MerkleNode node = ipfs.dag.put("json", ipfsVotingJsonStr.getBytes());

                Cid cid = Cid.buildCidV1(Cid.Codec.DagCbor, node.hash.getType(), node.hash.getHash());
                return cid.toString();
            } catch (IOException e) {
                throw new BlockchainException("Failed to store voting in IPFS.", e);
            }
        }, executionContext);
    }
}
