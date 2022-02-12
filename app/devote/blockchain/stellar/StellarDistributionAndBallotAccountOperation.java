package devote.blockchain.stellar;

import devote.blockchain.api.Account;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static devote.blockchain.stellar.StellarUtils.fromAccount;

public class StellarDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarDistributionAndBallotAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public TransactionResult create(Account fundingAccount, List<Issuer> issuers) {
        try {
            List<String> tokens = issuers.stream()
                    .map(issuer -> issuer.assetCode)
                    .collect(Collectors.toList());
            logger.info("[STELLAR]: About to create ballot and distribution account for vote tokens: {}", tokens);

            KeyPair funding = fromAccount(fundingAccount);
            Transaction.Builder txBuilder = prepareTransaction(funding);

            KeyPair distribution = prepareDistributionAccount(txBuilder, issuers);
            KeyPair ballot = prepareBallotAccount(txBuilder, issuers);

            List<KeyPair> signers = new ArrayList<>();
            signers.add(funding);
            signers.add(distribution);
            signers.add(ballot);
            issuers.forEach(issuer -> signers.add(fromAccount(issuer.account)));

            submitTransaction(txBuilder, signers);

            return createTransactionResult(distribution, ballot);
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create distribution and ballot accounts!", e);
            throw new BlockchainException("[STELLAR]: Failed to create distribution and ballot accounts!", e);
        }
    }

    private Transaction.Builder prepareTransaction(KeyPair funding) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, funding.getAccountId());
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

    private void submitTransaction(Transaction.Builder txBuilder, List<KeyPair> signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();

        signers.forEach(transaction::sign);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }
}
