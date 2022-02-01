package devote.blockchain.stellar;

import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Transaction;
import devote.blockchain.api.DistributionAndBallotAccountOperation.IssuerData;

import java.util.List;

import static devote.blockchain.stellar.StellarIssuerDataUtils.*;

class StellarDistributionAccountOperation {
    private final Transaction.Builder txBuilder;
    private final List<IssuerData> issuers;

    public StellarDistributionAccountOperation(Transaction.Builder txBuilder, List<IssuerData> issuers) {
        this.txBuilder = txBuilder;
        this.issuers = issuers;
    }

    public KeyPair prepare() {
        KeyPair distributionKeyPair = prepareAccountCreation();
        allowToHaveVoteTokenOfIssuers(distributionKeyPair);
        sendAllVoteTokensOfIssuersTo(distributionKeyPair);
        lockOutIssuers();

        return distributionKeyPair;
    }

    private KeyPair prepareAccountCreation() {
        long totalVotesCap = totalVotesCapOf(issuers);
        KeyPair distributionKeyPair = KeyPair.random();
        String distributionStartingBalance = Long.toString(totalVotesCap * 2);

        CreateAccountOperation distributionAccountCreateOp = new CreateAccountOperation.Builder(
                distributionKeyPair.getAccountId(),
                distributionStartingBalance
        ).build();
        txBuilder.addOperation(distributionAccountCreateOp);

        return distributionKeyPair;
    }

    private void allowToHaveVoteTokenOfIssuers(KeyPair distribution) {
        issuers.forEach(issuer -> allowDistributionToHaveVoteTokensOfIssuer(distribution, issuer));
    }

    private void allowDistributionToHaveVoteTokensOfIssuer(KeyPair distribution, IssuerData issuer) {
        ChangeTrustAsset chgTrustAsset = obtainsChangeTrustAssetFrom(issuer);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuer);

        ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(chgTrustAsset, allVoteTokensOfIssuer)
                .setSourceAccount(distribution.getAccountId())
                .build();
        txBuilder.addOperation(changeTrustOperation);
    }

    private void sendAllVoteTokensOfIssuersTo(KeyPair distribution) {
        issuers.forEach(issuer -> sendAllVoteTokensOfIssuerToDistribution(distribution, issuer));
    }

    private void sendAllVoteTokensOfIssuerToDistribution(KeyPair distribution, IssuerData issuer) {
        KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(issuer.keyPair);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuer);
        Asset asset = obtainAssetFrom(issuer);

        PaymentOperation paymentOperation = new PaymentOperation.Builder(distribution.getAccountId(), asset, allVoteTokensOfIssuer)
                .setSourceAccount(stellarIssuerKeyPair.getAccountId())
                .build();
        txBuilder.addOperation(paymentOperation);
    }

    private void lockOutIssuers() {
        issuers.forEach(this::lockOutIssuer);
    }

    private void lockOutIssuer(IssuerData issuer) {
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
}
