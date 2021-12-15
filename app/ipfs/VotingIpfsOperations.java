package ipfs;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaVoting;
import data.operations.VotingDbOperations;
import devote.blockchain.Blockchains;
import executioncontexts.BlockchainExecutionContext;
import ipfs.api.IpfsApi;
import ipfs.data.IpfsVoting;
import ipfs.data.IpfsVotingFromJpaVoting;
import play.Logger;
import play.libs.Json;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VotingIpfsOperations {
    private final VotingDbOperations votingDbOperations;
    private final BlockchainExecutionContext executionContext;
    private final IpfsVotingFromJpaVoting ipfsVotingFromJpaVoting;

    private final IpfsApi ipfsApi;

    private static final Logger.ALogger logger = Logger.of(VotingIpfsOperations.class);

    @Inject
    public VotingIpfsOperations(
            VotingDbOperations votingDbOperations,
            BlockchainExecutionContext executionContext,
            Blockchains blockchains,
            IpfsApi ipfsApi) {
        this.votingDbOperations = votingDbOperations;
        this.executionContext = executionContext;
        this.ipfsApi = ipfsApi;

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
            return ipfsApi.saveJson(ipfsVotingJson);
        }, executionContext);
    }
}
