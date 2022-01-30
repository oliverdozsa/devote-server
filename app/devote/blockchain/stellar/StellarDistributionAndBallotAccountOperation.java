package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
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
    public TransactionResult create(List<IssuerData> issuerData) {
        try {
            Transaction.Builder txBuilder = prepareTransaction(issuerData.get(0));
            TransactionResult transactionResult = prepareDistributionAndBallotCreationOn(txBuilder, issuerData);
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

    private TransactionResult prepareDistributionAndBallotCreationOn(Transaction.Builder txBuilder, List<IssuerData> issuerData) {
        long totalVotesCap = issuerData.stream()
                .map(i -> i.votesCap)
                .reduce(0L, Long::sum);

        KeyPair distributionKeyPair = prepareDistributionCreationOn(txBuilder, totalVotesCap);
        KeyPair ballotKeyPair = prepareBallotCreationOn(txBuilder);
        sendAllVoteTokensToDistributionAccount(distributionKeyPair, issuerData);
        allowBallotToHaveVoteTokens(ballotKeyPair, issuerData);

        // TODO
        return null;
    }

    private KeyPair prepareDistributionCreationOn(Transaction.Builder txBuilder, long votesCap) {
        KeyPair distributionKeyPair = KeyPair.random();
        String distributionStartingBalance = Long.toString(votesCap * 2);

        CreateAccountOperation distributionAccountCreateOp = new CreateAccountOperation.Builder(
                distributionKeyPair.getAccountId(),
                distributionStartingBalance
        ).build();
        txBuilder.addOperation(distributionAccountCreateOp);

        return distributionKeyPair;
    }

    private KeyPair prepareBallotCreationOn(Transaction.Builder txBuilder) {
        KeyPair ballotKeyPair = KeyPair.random();

        CreateAccountOperation ballotAccountCreateOp = new CreateAccountOperation.Builder(
                ballotKeyPair.getAccountId(),
                BALLOT_STARTING_BALANCE
        ).build();
        txBuilder.addOperation(ballotAccountCreateOp);

        return ballotKeyPair;
    }

    private void sendAllVoteTokensToDistributionAccount(Transaction.Builder txBuilder, KeyPair distribution, List<IssuerData> issuers) {
        // TODO
        issuers.forEach(i -> {
            KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(i.issuerKeyPair);
            Asset asset = Asset.create(null, i.voteTokenTitle, stellarIssuerKeyPair.getAccountId());
            ChangeTrustAsset chgTrustAsset = ChangeTrustAsset.create(asset);

            // TODO: To new method: allow distribution to have vote tokens
            BigDecimal votesCap = new BigDecimal(i.votesCap);
            BigDecimal divisor = new BigDecimal(10).pow(7);
            String allVoteTokensOfIssuer = votesCap.divide(divisor).toString();

            ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(chgTrustAsset, allVoteTokensOfIssuer)
                    .setSourceAccount(distribution.getAccountId())
                    .build();
            txBuilder.addOperation(changeTrustOperation);

            // TODO: To new method: doSendVoteToken to distro
            PaymentOperation paymentOperation = new PaymentOperation.Builder(distribution.getAccountId(), asset, allVoteTokensOfIssuer)
                    .setSourceAccount(stellarIssuerKeyPair.getAccountId())
                    .build();
            txBuilder.addOperation(paymentOperation);

            // TODO: To new method: lock out issuer
            SetOptionsOperation setOptionsOperation = new SetOptionsOperation.Builder()
                    .setSourceAccount(stellarIssuerKeyPair.getAccountId())
                    .setMasterKeyWeight(0)
                    .setLowThreshold(1)
                    .setMediumThreshold(1)
                    .setHighThreshold(1)
                    .build();
            txBuilder.addOperation(setOptionsOperation);
        });


    }

    private void allowBallotToHaveVoteTokens(KeyPair ballot, List<IssuerData> issuers) {
        // TODO
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
