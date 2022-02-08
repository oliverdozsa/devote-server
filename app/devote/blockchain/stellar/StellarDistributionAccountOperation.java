package devote.blockchain.stellar;

import devote.blockchain.api.Issuer;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Transaction;

import java.util.List;

import static devote.blockchain.stellar.StellarIssuerUtils.*;

class StellarDistributionAccountOperation {
    private final Transaction.Builder txBuilder;
    private final List<Issuer> issuers;

    public StellarDistributionAccountOperation(Transaction.Builder txBuilder, List<Issuer> issuers) {
        this.txBuilder = txBuilder;
        this.issuers = issuers;
    }

    public KeyPair prepare() {
        KeyPair distribution = prepareAccountCreation();
        allowToHaveVoteTokenOfIssuers(distribution);
        sendAllVoteTokensOfIssuersTo(distribution);
        lockOutIssuers();

        return distribution;
    }

    private KeyPair prepareAccountCreation() {
        long totalVotesCap = totalVotesCapOf(issuers);
        KeyPair distribution = KeyPair.random();
        String startingBalance = Long.toString(totalVotesCap * 2);

        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(distribution.getAccountId(), startingBalance)
                .build();
        txBuilder.addOperation(createAccount);

        return distribution;
    }

    private void allowToHaveVoteTokenOfIssuers(KeyPair distribution) {
        issuers.forEach(issuer -> allowDistributionToHaveVoteTokensOfIssuer(distribution, issuer));
    }

    private void allowDistributionToHaveVoteTokensOfIssuer(KeyPair distribution, Issuer issuer) {
        ChangeTrustAsset asset = obtainsChangeTrustAssetFrom(issuer);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuer);

        ChangeTrustOperation changeTrust = new ChangeTrustOperation.Builder(asset, allVoteTokensOfIssuer)
                .setSourceAccount(distribution.getAccountId())
                .build();
        txBuilder.addOperation(changeTrust);
    }

    private void sendAllVoteTokensOfIssuersTo(KeyPair distribution) {
        issuers.forEach(issuer -> sendAllVoteTokensOfIssuerToDistribution(distribution, issuer));
    }

    private void sendAllVoteTokensOfIssuerToDistribution(KeyPair distribution, Issuer issuerAccount) {
        KeyPair issuer = StellarUtils.fromAccount(issuerAccount.account);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuerAccount);
        Asset asset = obtainAssetFrom(issuerAccount);

        PaymentOperation payment = new PaymentOperation.Builder(distribution.getAccountId(), asset, allVoteTokensOfIssuer)
                .setSourceAccount(issuer.getAccountId())
                .build();
        txBuilder.addOperation(payment);
    }

    private void lockOutIssuers() {
        issuers.forEach(this::lockOutIssuer);
    }

    private void lockOutIssuer(Issuer issuerAccount) {
        KeyPair issuer = StellarUtils.fromAccount(issuerAccount.account);
        SetOptionsOperation setOptions = new SetOptionsOperation.Builder()
                .setSourceAccount(issuer.getAccountId())
                .setMasterKeyWeight(0)
                .setLowThreshold(1)
                .setMediumThreshold(1)
                .setHighThreshold(1)
                .build();
        txBuilder.addOperation(setOptions);
    }
}
