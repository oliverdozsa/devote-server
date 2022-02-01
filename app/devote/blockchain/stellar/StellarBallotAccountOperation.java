package devote.blockchain.stellar;

import devote.blockchain.api.DistributionAndBallotAccountOperation.IssuerData;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Transaction;

import java.util.List;

import static devote.blockchain.stellar.StellarIssuerDataUtils.calcNumOfAllVoteTokensOf;
import static devote.blockchain.stellar.StellarIssuerDataUtils.obtainsChangeTrustAssetFrom;

class StellarBallotAccountOperation {
    private final Transaction.Builder txBuilder;
    private final List<IssuerData> issuers;


    private static final String BALLOT_STARTING_BALANCE = "2";

    public StellarBallotAccountOperation(Transaction.Builder txBuilder, List<IssuerData> issuers) {
        this.txBuilder = txBuilder;
        this.issuers = issuers;
    }

    public KeyPair prepare() {
        KeyPair ballot = prepareAccountCreation();
        allowBallotToHaveVoteTokensOfIssuers(ballot);

        return ballot;
    }

    private KeyPair prepareAccountCreation() {
        KeyPair ballotKeyPair = KeyPair.random();

        CreateAccountOperation ballotAccountCreateOp = new CreateAccountOperation.Builder(
                ballotKeyPair.getAccountId(),
                BALLOT_STARTING_BALANCE
        ).build();
        txBuilder.addOperation(ballotAccountCreateOp);

        return ballotKeyPair;
    }

    private void allowBallotToHaveVoteTokensOfIssuers(KeyPair ballot) {
        issuers.forEach(issuer -> allowBallotToHaveVoteTokenOfIssuer(ballot, issuer));
    }

    private void allowBallotToHaveVoteTokenOfIssuer(KeyPair ballot, IssuerData issuer) {
        ChangeTrustAsset chgTrustAsset = obtainsChangeTrustAssetFrom(issuer);
        String allVoteTokensOfIssuer = calcNumOfAllVoteTokensOf(issuer);

        ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(chgTrustAsset, allVoteTokensOfIssuer)
                .setSourceAccount(ballot.getAccountId())
                .build();

        txBuilder.addOperation(changeTrustOperation);
    }
}
