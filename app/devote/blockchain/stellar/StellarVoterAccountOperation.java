package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.VoterAccountOperation;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.io.IOException;
import java.math.BigDecimal;

public class StellarVoterAccountOperation implements VoterAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarVoterAccountOperation.class);
    private static final String UNIT_TOKEN_AMOUNT = unitTokenAmount();

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public String createTransaction(CreationData creationData) {
        KeyPair channel = StellarUtils.fromDevoteKeyPair(creationData.channelKeyPair);

        try {
            Transaction.Builder txBuilder = prepareTransaction(channel);
            allowVoterToHaveVoteToken(txBuilder, creationData);
            sendTheTokenToVoter(txBuilder, creationData);

            Transaction transaction = createSignedTransaction(txBuilder, creationData);
            return transaction.toEnvelopeXdrBase64();
        } catch (IOException e) {
            logger.warn("[STELLAR]: Failed to create voter account transaction!", e);
            throw new BlockchainException("[STELLAR]: Failed to create issuer account!", e);
        }
    }

    private Transaction.Builder prepareTransaction(KeyPair channel) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, channel.getAccountId());
    }

    private void allowVoterToHaveVoteToken(Transaction.Builder txBuilder, CreationData creationData) {
        ChangeTrustAsset changeTrustAsset = StellarIssuerUtils.obtainsChangeTrustAssetFrom(creationData.issuer);
        String allVoteTokensOfIssuer = StellarIssuerUtils.calcNumOfAllVoteTokensOf(creationData.issuer);

        ChangeTrustOperation changeTrustOperation = new ChangeTrustOperation.Builder(changeTrustAsset, allVoteTokensOfIssuer)
                .setSourceAccount(creationData.voterPublicKey)
                .build();

        txBuilder.addOperation(changeTrustOperation);
    }

    private void sendTheTokenToVoter(Transaction.Builder txBuilder, CreationData creationData) {
        Asset asset = StellarIssuerUtils.obtainAssetFrom(creationData.issuer);

        PaymentOperation paymentOperation = new PaymentOperation.Builder(creationData.voterPublicKey, asset, UNIT_TOKEN_AMOUNT)
                .setSourceAccount(creationData.distributionKeyPair.publicKey)
                .build();

        txBuilder.addOperation(paymentOperation);
    }

    private Transaction createSignedTransaction(Transaction.Builder txBuilder, CreationData creationData) {
        KeyPair stellarDistribution = StellarUtils.fromDevoteKeyPair(creationData.distributionKeyPair);
        KeyPair stellarChannel = StellarUtils.fromDevoteKeyPair(creationData.channelKeyPair);

        Transaction transaction = txBuilder.build();
        transaction.sign(stellarChannel);
        transaction.sign(stellarDistribution);

        return transaction;
    }

    private static String unitTokenAmount() {
        BigDecimal one = new BigDecimal(1);
        BigDecimal divisor = new BigDecimal(10).pow(7);
        return one.divide(divisor).toString();
    }
}
