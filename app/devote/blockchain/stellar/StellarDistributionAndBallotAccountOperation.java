package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Issuer;
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
    public TransactionResult create(List<Issuer> issuers) {
        try {
            List<String> tokens = issuers.stream()
                    .map(issuer -> issuer.assetCode)
                    .collect(Collectors.toList());
            logger.info("[STELLAR]: About to create ballot and distribution account for vote tokens: {}", tokens);

            Transaction.Builder txBuilder = prepareTransaction(issuers.get(0));
            KeyPair distribution = prepareDistributionAccount(txBuilder, issuers);
            KeyPair ballot = prepareBallotAccount(txBuilder, issuers);
            submitTransaction(txBuilder, issuers, distribution, ballot);

            return createTransactionResult(distribution, ballot);
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create distribution and ballot accounts!", e);
            throw new BlockchainException("[STELLAR]: Failed to create distribution and ballot accounts!", e);
        }
    }

    private Transaction.Builder prepareTransaction(Issuer anIssuer) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();
        KeyPair issuer = StellarUtils.fromAccount(anIssuer.account);

        return StellarUtils.createTransactionBuilder(server, network, issuer.getAccountId());
    }

    private KeyPair prepareDistributionAccount(Transaction.Builder txBuilder, List<Issuer> issuers) {
        StellarDistributionAccountOperation distributionAccount = new StellarDistributionAccountOperation(txBuilder, issuers);
        return distributionAccount.prepare();
    }

    private KeyPair prepareBallotAccount(Transaction.Builder txBuilder, List<Issuer> issuers) {
        StellarBallotAccountOperation ballotAccount = new StellarBallotAccountOperation(txBuilder, issuers);

        return ballotAccount.prepare();
    }

    private TransactionResult createTransactionResult(KeyPair distribution, KeyPair ballot) {
        return new TransactionResult(StellarUtils.toAccount(distribution), StellarUtils.toAccount(ballot));
    }

    private void submitTransaction(Transaction.Builder txBuilder, List<Issuer> issuers, KeyPair distribution, KeyPair ballot)
            throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();

        issuers.stream()
                .map(issuer -> StellarUtils.fromAccount(issuer.account))
                .forEach(transaction::sign);

        transaction.sign(ballot);
        transaction.sign(distribution);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }
}
