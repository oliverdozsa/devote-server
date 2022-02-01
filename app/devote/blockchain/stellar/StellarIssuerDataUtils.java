package devote.blockchain.stellar;

import devote.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;

import java.math.BigDecimal;
import java.util.List;

public class StellarIssuerDataUtils {
    public static long totalVotesCapOf(List<DistributionAndBallotAccountOperation.IssuerData> issuers) {
        return issuers.stream()
                .map(issuer -> issuer.votesCap)
                .reduce(0L, Long::sum);
    }

    public static ChangeTrustAsset obtainsChangeTrustAssetFrom(DistributionAndBallotAccountOperation.IssuerData issuer) {
        return ChangeTrustAsset.create(obtainAssetFrom(issuer));
    }

    public static Asset obtainAssetFrom(DistributionAndBallotAccountOperation.IssuerData issuer) {
        return Asset.create(null, issuer.voteTokenTitle, issuer.keyPair.publicKey);

    }

    public static String calcNumOfAllVoteTokensOf(DistributionAndBallotAccountOperation.IssuerData issuer) {
        BigDecimal votesCap = new BigDecimal(issuer.votesCap);
        BigDecimal divisor = new BigDecimal(10).pow(7);
        return votesCap.divide(divisor).toString();
    }
}
