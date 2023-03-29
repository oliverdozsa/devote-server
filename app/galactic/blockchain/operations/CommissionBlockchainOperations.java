package galactic.blockchain.operations;

import galactic.blockchain.BlockchainFactory;
import galactic.blockchain.Blockchains;
import galactic.blockchain.api.VoterAccountOperation;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class CommissionBlockchainOperations {
    private final BlockchainExecutionContext blockchainExecContext;
    private final Blockchains blockchains;

    private static final Logger.ALogger logger = Logger.of(CommissionBlockchainOperations.class);

    @Inject
    public CommissionBlockchainOperations(
            BlockchainExecutionContext blockchainExecContext,
            Blockchains blockchains
    ) {
        this.blockchainExecContext = blockchainExecContext;
        this.blockchains = blockchains;
    }

    public CompletionStage<String> createTransaction(String network, VoterAccountOperation.CreateTransactionParams data) {
        logger.info("createTransaction(): network = {}, data = {}", network, data);

        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);
        VoterAccountOperation voterAccountOperation = blockchainFactory.createVoterAccountOperation();
        if(data.isOnTestNetwork) {
            voterAccountOperation.useTestNet();
        }

        return supplyAsync(() -> voterAccountOperation.createTransaction(data), blockchainExecContext);
    }
}
