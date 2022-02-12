package devote.blockchain.stellar;

import devote.blockchain.api.Account;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.math.BigDecimal;

import static devote.blockchain.stellar.StellarUtils.toAccount;
import static devote.blockchain.stellar.StellarUtils.toAssetAmount;
import static utils.StringUtils.redactWithEllipsis;

class StellarPrepareVoteTokenOperation {
    private final long votesCap;
    private final Transaction.Builder txBuilder;
    private final String assetCode;

    public KeyPair distribution;
    public KeyPair ballot;
    public KeyPair issuer;

    private static final Logger.ALogger logger = Logger.of(StellarPrepareVoteTokenOperation.class);

    public StellarPrepareVoteTokenOperation(long votesCap, Transaction.Builder txBuilder, String assetCode) {
        this.votesCap = votesCap;
        this.txBuilder = txBuilder;
        this.assetCode = assetCode;
    }

    public void prepareAccountsCreation() {
        distribution = prepareDistributionAccountCreation();
        ballot = prepareBallotAccountCreation();
        issuer = prepareIssuerAccountCreation();
    }

    public void prepareToken() {
        allowAccountsToHaveVoteTokens(ballot, distribution);
        sendAllVoteTokensToDistribution();
        lockIssuer();
    }

    public DistributionAndBallotAccountOperation.TransactionResult toTransactionResult() {
        Account ballotAccount = toAccount(ballot);
        Account distributionAccount = toAccount(distribution);
        Account issuerAccount = toAccount(issuer);
        return new DistributionAndBallotAccountOperation.TransactionResult(
                distributionAccount, ballotAccount, issuerAccount
        );
    }

    private KeyPair prepareDistributionAccountCreation() {
        return prepareNewAccountCreationOn("distribution", votesCap * 2);
    }

    private KeyPair prepareBallotAccountCreation() {
        return prepareNewAccountCreationOn("ballot", 2L);
    }

    private KeyPair prepareIssuerAccountCreation() {
        return prepareNewAccountCreationOn("issuer", 2L);
    }

    private ChangeTrustAsset getChangeTrustAsset() {
        return ChangeTrustAsset.create(getAsset());
    }

    private void allowAccountsToHaveVoteTokens(KeyPair... accounts) {
        ChangeTrustAsset asset = getChangeTrustAsset();
        String limit = toAssetAmount(votesCap);

        for(KeyPair account: accounts) {
            ChangeTrustOperation changeTrust = new ChangeTrustOperation.Builder(asset, limit)
                    .setSourceAccount(account.getAccountId())
                    .build();
            txBuilder.addOperation(changeTrust);
        }
    }

    private Asset getAsset() {
        return Asset.create(null, assetCode, issuer.getAccountId());
    }

    private void sendAllVoteTokensToDistribution() {
        String votesCapAsAssetAmount = toAssetAmount(votesCap);
        logger.info("[STELLAR]: Preparing the sending of {} vote tokens to distribution {}", votesCapAsAssetAmount, redactWithEllipsis(distribution.getAccountId(), 5));

        Asset asset = getAsset();
        PaymentOperation payment = new PaymentOperation.Builder(distribution.getAccountId(), asset, votesCapAsAssetAmount)
                .setSourceAccount(issuer.getAccountId())
                .build();
        txBuilder.addOperation(payment);
    }

    private void lockIssuer() {
        logger.info("[STELLAR]: Preparing the locking of issuer {}", redactWithEllipsis(issuer.getAccountId(), 5));
        SetOptionsOperation setOptions = new SetOptionsOperation.Builder()
                .setSourceAccount(issuer.getAccountId())
                .setMasterKeyWeight(0)
                .setLowThreshold(1)
                .setMediumThreshold(1)
                .setHighThreshold(1)
                .build();
        txBuilder.addOperation(setOptions);
    }

    private KeyPair prepareNewAccountCreationOn(String withAccountName, long withStartingBalance) {
        KeyPair newAccount = KeyPair.random();
        String startingBalance = Long.toString(withStartingBalance);

        logger.info("[STELLAR]: About to create {} account with starting balance {}", withAccountName, withStartingBalance);

        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(newAccount.getAccountId(), startingBalance)
                .build();
        txBuilder.addOperation(createAccount);

        return newAccount;
    }
}
