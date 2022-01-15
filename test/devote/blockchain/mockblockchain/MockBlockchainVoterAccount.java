package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.VoterAccount;

import static utils.StringUtils.createRandomAlphabeticString;

public class MockBlockchainVoterAccount implements VoterAccount {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public String createTransaction(CreationData creationData) {
        String randomTransactionString = createRandomAlphabeticString(16);
        return randomTransactionString + creationData.voterPublicKey.substring(0, 5);
    }
}
