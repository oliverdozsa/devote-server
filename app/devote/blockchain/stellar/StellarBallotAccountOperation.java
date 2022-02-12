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

    private static final long BALLOT_STARTING_BALANCE_BASE = 2L;

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
        String startingBalance = Long.toString(BALLOT_STARTING_BALANCE_BASE * issuers.size());
        logger.info("[STELLAR]: About to create ballot account {} with balance {}", loggableAccount, startingBalance);

        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(ballot.getAccountId(), startingBalance)
                .build();
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
