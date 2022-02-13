package devote.blockchain.stellar;

import devote.blockchain.api.Account;
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
import java.util.Arrays;

import static devote.blockchain.stellar.StellarUtils.fromAccount;

public class StellarDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarDistributionAndBallotAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public TransactionResult create(Account fundingAccount, String assetCode, long votesCap) {
        try {
            KeyPair funding = fromAccount(fundingAccount);
            Transaction.Builder txBuilder = prepareTransaction(funding);

            StellarPrepareVoteTokenOperation voteTokenOp = new StellarPrepareVoteTokenOperation(votesCap, txBuilder, assetCode);
            voteTokenOp.prepareAccountsCreation();
            voteTokenOp.prepareToken();

            submitTransaction(txBuilder, funding, voteTokenOp.ballot, voteTokenOp.distribution, voteTokenOp.issuer);

            return voteTokenOp.toTransactionResult();
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

    private void submitTransaction(Transaction.Builder txBuilder, KeyPair... signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        Arrays.stream(signers).forEach(transaction::sign);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }
}
