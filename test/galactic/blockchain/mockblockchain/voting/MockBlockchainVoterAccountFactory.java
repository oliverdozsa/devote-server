package galactic.blockchain.mockblockchain.voting;

import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.VoterAccountOperation;

import static utils.StringUtils.createRandomAlphabeticString;

public class MockBlockchainVoterAccountFactory implements VoterAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public void useTestNet() {

    }

    @Override
    public String createTransaction(CreateTransactionParams params) {
        String randomTransactionString = createRandomAlphabeticString(16);
        return randomTransactionString + params.voterAccountPublic.substring(0, 5);
    }
}
