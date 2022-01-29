package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class StellarDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarDistributionAndBallotAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public TransactionResult create(List<IssuerData> issuerData, Long votesCap) {
        try {
            Transaction.Builder txBuilder = prepareTransaction(issuerData.get(0));
            TransactionResult transactionResult = prepareDistributionAndBallotCreationOn(txBuilder, issuerData, votesCap);
            submitTransaction(txBuilder, issuerData, transactionResult);

            return null;
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create distribution and ballot accounts!", e);
            throw new BlockchainException("[STELLAR]: Failed to create distribution and ballot accounts!", e);
        }
    }

    private Transaction.Builder prepareTransaction(IssuerData anIssuerData) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(anIssuerData.issuerKeyPair);

        return StellarUtils.createTransactionBuilder(server, network, stellarIssuerKeyPair.getAccountId());
    }

    private TransactionResult prepareDistributionAndBallotCreationOn(Transaction.Builder txBuilder, List<IssuerData> issuerData, Long votesCap) {
        // TODO
        return null;
    }

    private void submitTransaction(
            Transaction.Builder txBuilder,
            List<IssuerData> issuerData,
            TransactionResult transactionResult
    ) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();

        List<KeyPair> stellarIssuerKeyPairs = issuerData.stream()
                .map(i -> StellarUtils.fromDevoteKeyPair(i.issuerKeyPair))
                .collect(Collectors.toList());

        KeyPair stellarBallot = StellarUtils.fromDevoteKeyPair(transactionResult.ballot);
        KeyPair stellarDistribution = StellarUtils.fromDevoteKeyPair(transactionResult.distribution);

        stellarIssuerKeyPairs.forEach(transaction::sign);
        transaction.sign(stellarBallot);
        transaction.sign(stellarDistribution);

        Server server = configuration.getServer();
        server.submitTransaction(transaction);
    }


}
