package devote.blockchain.api;

public class Issuer {
    public final Account account;
    public final long votesCap;
    public final String assetCode;

    public Issuer(Account account, long votesCap, String assetCode) {
        this.account = account;
        this.votesCap = votesCap;
        this.assetCode = assetCode;
    }
}
