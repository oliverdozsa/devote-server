package devote.blockchain.api;

import static utils.StringUtils.redactWithEllipsis;

public interface VoterAccountOperation extends BlockchainOperation {
    String createTransaction(CreateTransactionParams params);

    class CreateTransactionParams {
        public Account channel;
        public Account distribution;
        public String issuerAccountPublic;
        public String assetCode;
        public long votesCap;
        public String voterAccountPublic;

        @Override
        public String toString() {
            return "CreateTransactionParams{" +
                    "channel=" + redactWithEllipsis(channel.publik, 5) +
                    ", distribution=" + redactWithEllipsis(distribution.publik, 5) +
                    ", issuerAccountPublic='" + redactWithEllipsis(issuerAccountPublic, 5) + '\'' +
                    ", assetCode='" + assetCode + '\'' +
                    ", votesCap=" + votesCap +
                    ", voterAccountPublic='" + redactWithEllipsis(voterAccountPublic, 5) + '\'' +
                    '}';
        }
    }
}
