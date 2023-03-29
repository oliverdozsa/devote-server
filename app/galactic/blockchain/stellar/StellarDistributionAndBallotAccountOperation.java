package galactic.blockchain.stellar;

import galactic.blockchain.api.Account;
import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.io.IOException;
import java.util.Arrays;

import static galactic.blockchain.stellar.StellarUtils.fromAccount;

public class StellarDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private StellarBlockchainConfiguration configuration;
    private StellarServerAndNetwork serverAndNetwork;

    private static final Logger.ALogger logger = Logger.of(StellarDistributionAndBallotAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
        serverAndNetwork = StellarServerAndNetwork.create((StellarBlockchainConfiguration) configuration);

    }

    @Override
    public void useTestNet() {
        serverAndNetwork = StellarServerAndNetwork.createForTestNet(configuration);
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
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, funding.getAccountId());
    }

    private void submitTransaction(Transaction.Builder txBuilder, KeyPair... signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        Arrays.stream(signers).forEach(transaction::sign);

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("distribution and ballot", transaction, server);
    }
}
