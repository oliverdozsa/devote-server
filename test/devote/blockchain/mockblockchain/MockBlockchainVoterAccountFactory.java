package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.VoterAccountOperation;

import static utils.StringUtils.createRandomAlphabeticString;

public class MockBlockchainVoterAccountFactory implements VoterAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public String createTransaction(CreationData creationData) {
        String randomTransactionString = createRandomAlphabeticString(16);
        return randomTransactionString + creationData.voterPublicKey.substring(0, 5);
    }
}
