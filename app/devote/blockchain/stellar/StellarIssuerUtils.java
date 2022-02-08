package devote.blockchain.stellar;

import devote.blockchain.api.Issuer;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;

import java.math.BigDecimal;
import java.util.List;

public class StellarIssuerUtils {
    public static long totalVotesCapOf(List<Issuer> issuers) {
        return issuers.stream()
                .map(issuer -> issuer.votesCap)
                .reduce(0L, Long::sum);
    }

    public static ChangeTrustAsset obtainsChangeTrustAssetFrom(Issuer issuer) {
        return ChangeTrustAsset.create(obtainAssetFrom(issuer));
    }

    public static Asset obtainAssetFrom(Issuer issuer) {
        return Asset.create(null, issuer.assetCode, issuer.account.publik);

    }

    public static String calcNumOfAllVoteTokensOf(Issuer issuer) {
        BigDecimal votesCap = new BigDecimal(issuer.votesCap);
        BigDecimal divisor = new BigDecimal(10).pow(7);
        return votesCap.divide(divisor).toString();
    }
}
