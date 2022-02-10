package devote.blockchain.stellar;

import devote.blockchain.api.Issuer;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.util.List;

import static devote.blockchain.stellar.StellarIssuerUtils.calcNumOfAllVoteTokensOf;
import static devote.blockchain.stellar.StellarIssuerUtils.obtainsChangeTrustAssetFrom;
import static utils.StringUtils.redactWithEllipsis;

class StellarBallotAccountOperation {
    private final Transaction.Builder txBuilder;
    private final List<Issuer> issuers;

    private static final String BALLOT_STARTING_BALANCE = "2";

    private static final Logger.ALogger logger = Logger.of(StellarBallotAccountOperation.class);

    public StellarBallotAccountOperation(Transaction.Builder txBuilder, List<Issuer> issuers) {
        this.txBuilder = txBuilder;
        this.issuers = issuers;
    }

    public KeyPair prepare() {
        KeyPair ballot = prepareAccountCreation();
        allowBallotToHaveVoteTokensOfIssuers(ballot);

        return ballot;
    }

    private KeyPair prepareAccountCreation() {
        KeyPair ballot = KeyPair.random();

        String loggableAccount = redactWithEllipsis(ballot.getAccountId(), 5);
        logger.info("[STELLAR]: About to create ballot account {} with balance {}", loggableAccount, BALLOT_STARTING_BALANCE);

        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(
                ballot.getAccountId(),
                BALLOT_STARTING_BALANCE
        ).build();
        txBuilder.addOperation(createAccount);

        return ballot;
    }

    private void allowBallotToHaveVoteTokensOfIssuers(KeyPair ballot) {
        issuers.forEach(issuer -> allowBallotToHaveVoteTokenOfIssuer(ballot, issuer));
    }

    private void allowBallotToHaveVoteTokenOfIssuer(KeyPair ballot, Issuer issuer) {
        ChangeTrustAsset asset = obtainsChangeTrustAssetFrom(issuer);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuer);

        ChangeTrustOperation changeTrust = new ChangeTrustOperation.Builder(asset, allVoteTokensOfIssuer)
                .setSourceAccount(ballot.getAccountId())
                .build();

        txBuilder.addOperation(changeTrust);
    }
}
