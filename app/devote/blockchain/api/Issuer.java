package devote.blockchain.api;

public class Issuer {
    public final KeyPair keyPair;
    public final long votesCap;
    public final String assetCode;

    public Issuer(KeyPair keyPair, long votesCap, String assetCode) {
        this.keyPair = keyPair;
        this.votesCap = votesCap;
        this.assetCode = assetCode;
    }
}
