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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StellarDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarDistributionAndBallotAccountOperation.class);
    private static final String BALLOT_STARTING_BALANCE = "2";

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public TransactionResult create(List<IssuerData> issuers) {
        try {
            List<String> tokens = issuers.stream()
                    .map(issuer -> issuer.voteTokenTitle)
                    .collect(Collectors.toList());
            logger.info("[STELLAR]: About to create ballot and distribution account for vote tokens: {}", tokens);

            Transaction.Builder txBuilder = prepareTransaction(issuers.get(0));
            StellarDistributionAccountOperation distributionAccountOperation =
                    new StellarDistributionAccountOperation(txBuilder, issuers);
            StellarBallotAccountOperation ballotAccountOperation =
                    new StellarBallotAccountOperation(txBuilder, issuers);

            KeyPair stellarDistribution = distributionAccountOperation.prepare();
            KeyPair stellarBallot = ballotAccountOperation.prepare();
            TransactionResult transactionResult = createTransactionResult(stellarDistribution, stellarBallot, issuers);
            submitTransaction(txBuilder, issuers, transactionResult);

            return transactionResult;
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create distribution and ballot accounts!", e);
            throw new BlockchainException("[STELLAR]: Failed to create distribution and ballot accounts!", e);
        }
    }

    private Transaction.Builder prepareTransaction(IssuerData anIssuerData) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(anIssuerData.keyPair);

        return StellarUtils.createTransactionBuilder(server, network, stellarIssuerKeyPair.getAccountId());
    }

    private TransactionResult createTransactionResult(KeyPair distribution, KeyPair ballot, List<IssuerData> issuers) {
        Map<String, String> issuersAndTheirTokens = new HashMap<>();
        issuers.forEach(i -> issuersAndTheirTokens.put(i.keyPair.secretKey, i.voteTokenTitle));

        return new TransactionResult(
                StellarUtils.toDevoteKeyPair(distribution),
                StellarUtils.toDevoteKeyPair(ballot),
                issuersAndTheirTokens
        );
    }

    private void submitTransaction(Transaction.Builder txBuilder, List<IssuerData> issuers, TransactionResult result)
            throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();

        issuers.stream()
                .map(issuer -> StellarUtils.fromDevoteKeyPair(issuer.keyPair))
                .forEach(transaction::sign);

        KeyPair stellarBallot = StellarUtils.fromDevoteKeyPair(result.ballot);
        transaction.sign(stellarBallot);

        KeyPair stellarDistribution = StellarUtils.fromDevoteKeyPair(result.distribution);
        transaction.sign(stellarDistribution);

        Server server = configuration.getServer();
        server.submitTransaction(transaction);
    }
}
