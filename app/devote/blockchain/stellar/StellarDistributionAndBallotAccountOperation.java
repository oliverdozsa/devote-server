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
            TransactionResult transactionResult = prepareDistributionAndBallotCreationOn(txBuilder, issuers);
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

    private TransactionResult prepareDistributionAndBallotCreationOn(Transaction.Builder txBuilder, List<IssuerData> issuers) {
        long totalVotesCap = issuers.stream()
                .map(i -> i.votesCap)
                .reduce(0L, Long::sum);

        KeyPair distributionKeyPair = prepareDistributionCreationOn(txBuilder, totalVotesCap);
        KeyPair ballotKeyPair = prepareBallotCreationOn(txBuilder);

        allowDistributionToHaveVoteTokensOfIssuers(txBuilder, distributionKeyPair, issuers);
        sendAllVoteTokensOfIssuersToDistribution(txBuilder, distributionKeyPair, issuers);
        lockOutIssuers(txBuilder, issuers);
        allowBallotToHaveVoteTokensOfIssuers(txBuilder, ballotKeyPair, issuers);

        Map<String, String> issuersAndTheirTokens = new HashMap<>();
        issuers.forEach(i -> issuersAndTheirTokens.put(i.keyPair.secretKey, i.voteTokenTitle));

        return new TransactionResult(
                StellarUtils.toDevoteKeyPair(distributionKeyPair),
                StellarUtils.toDevoteKeyPair(ballotKeyPair),
                issuersAndTheirTokens
        );
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

    private void allowDistributionToHaveVoteTokensOfIssuers(Transaction.Builder txBuilder, KeyPair distribution, List<IssuerData> issuers) {
        issuers.forEach(issuer -> allowDistributionToHaveVoteTokensOfIssuer(txBuilder, distribution, issuer));
    }

    private void allowDistributionToHaveVoteTokensOfIssuer(Transaction.Builder txBuilder, KeyPair distribution, IssuerData issuerData) {
        ChangeTrustAsset chgTrustAsset = toChangeTrustAsset(issuerData);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOfIssuer(issuerData);

        ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(chgTrustAsset, allVoteTokensOfIssuer)
                .setSourceAccount(distribution.getAccountId())
                .build();
        txBuilder.addOperation(changeTrustOperation);
    }

    private void sendAllVoteTokensOfIssuersToDistribution(Transaction.Builder txBuilder, KeyPair distribution, List<IssuerData> issuers) {
        issuers.forEach(issuer -> sendAllVoteTokensOfIssuerToDistribution(txBuilder, distribution, issuer));
    }

    private void sendAllVoteTokensOfIssuerToDistribution(Transaction.Builder txBuilder, KeyPair distribution, IssuerData issuerData) {
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(issuerData.keyPair);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOfIssuer(issuerData);
        Asset asset = toAsset(issuerData);

        PaymentOperation paymentOperation = new PaymentOperation.Builder(distribution.getAccountId(), asset, allVoteTokensOfIssuer)
                .setSourceAccount(stellarIssuerKeyPair.getAccountId())
                .build();
        txBuilder.addOperation(paymentOperation);
    }

    private void lockOutIssuers(Transaction.Builder txBuilder, List<IssuerData> issuers) {
        issuers.forEach(issuer -> lockOutIssuer(txBuilder, issuer));
    }

    private void lockOutIssuer(Transaction.Builder txBuilder, IssuerData issuer) {
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(issuer.keyPair);
        SetOptionsOperation setOptionsOperation = new SetOptionsOperation.Builder()
                .setSourceAccount(stellarIssuerKeyPair.getAccountId())
                .setMasterKeyWeight(0)
                .setLowThreshold(1)
                .setMediumThreshold(1)
                .setHighThreshold(1)
                .build();
        txBuilder.addOperation(setOptionsOperation);
    }

    private void allowBallotToHaveVoteTokensOfIssuers(Transaction.Builder txBuilder, KeyPair ballot, List<IssuerData> issuers) {
        issuers.forEach(issuer -> allowBallotToHaveVoteTokenOfIssuer(txBuilder, ballot, issuer));
    }

    private void allowBallotToHaveVoteTokenOfIssuer(Transaction.Builder txBuilder, KeyPair ballot, IssuerData issuer) {
        ChangeTrustAsset chgTrustAsset = toChangeTrustAsset(issuer);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOfIssuer(issuer);

        ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(chgTrustAsset, allVoteTokensOfIssuer)
                .setSourceAccount(ballot.getAccountId())
                .build();

        txBuilder.addOperation(changeTrustOperation);
    }

    private Asset toAsset(IssuerData issuerData) {
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(issuerData.keyPair);
        return Asset.create(null, issuerData.voteTokenTitle, stellarIssuerKeyPair.getAccountId());

    }

    private ChangeTrustAsset toChangeTrustAsset(IssuerData issuerData) {
        return ChangeTrustAsset.create(toAsset(issuerData));
    }

    private String calcNumOfAllVoteTokensOfIssuer(IssuerData issuer) {
        BigDecimal votesCap = new BigDecimal(issuer.votesCap);
        BigDecimal divisor = new BigDecimal(10).pow(7);
        return votesCap.divide(divisor).toString();
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
