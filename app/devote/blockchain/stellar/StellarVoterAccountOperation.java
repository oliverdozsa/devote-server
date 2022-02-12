package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.VoterAccountOperation;
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

import static devote.blockchain.stellar.StellarUtils.toAssetAmount;

public class StellarVoterAccountOperation implements VoterAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarVoterAccountOperation.class);
    private static final String UNIT_TOKEN_AMOUNT = unitTokenAmount();

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public String createTransaction(CreateTransactionParams params) {
        KeyPair channel = StellarUtils.fromAccount(params.channel);

        try {
            Transaction.Builder txBuilder = prepareTransaction(channel);
            allowVoterToHaveVoteToken(txBuilder, params);
            sendTheTokenToVoter(txBuilder, params);

            Transaction transaction = createSignedTransaction(txBuilder, params);
            return transaction.toEnvelopeXdrBase64();
        } catch (IOException e) {
            logger.warn("[STELLAR]: Failed to create voter account transaction!", e);
            throw new BlockchainException("[STELLAR]: Failed to create voter account transaction!", e);
        }
    }

    private Transaction.Builder prepareTransaction(KeyPair channel) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, channel.getAccountId());
    }

    private void allowVoterToHaveVoteToken(Transaction.Builder txBuilder, CreateTransactionParams params) {
        ChangeTrustAsset changeTrustAsset = getChangeTrustAssetFrom(params);
        String limit = toAssetAmount(params.votesCap);

        ChangeTrustOperation changeTrust = new ChangeTrustOperation.Builder(changeTrustAsset, limit)
                .setSourceAccount(params.voterAccountPublic)
                .build();

        txBuilder.addOperation(changeTrust);
    }

    private void sendTheTokenToVoter(Transaction.Builder txBuilder, CreateTransactionParams params) {
        Asset asset = getAssetFrom(params);

        PaymentOperation payment = new PaymentOperation.Builder(params.voterAccountPublic, asset, UNIT_TOKEN_AMOUNT)
                .setSourceAccount(params.distribution.publik)
                .build();

        txBuilder.addOperation(payment);
    }

    private Transaction createSignedTransaction(Transaction.Builder txBuilder, CreateTransactionParams params) {
        KeyPair distribution = StellarUtils.fromAccount(params.distribution);
        KeyPair channel = StellarUtils.fromAccount(params.channel);

        Transaction transaction = txBuilder.build();
        transaction.sign(channel);
        transaction.sign(distribution);

        return transaction;
    }

    private static String unitTokenAmount() {
        BigDecimal one = new BigDecimal(1);
        BigDecimal divisor = new BigDecimal(10).pow(7);
        return one.divide(divisor).toString();
    }

    private static Asset getAssetFrom(CreateTransactionParams params) {
        return Asset.create(null, params.assetCode, params.issuerAccountPublic);
    }

    private static ChangeTrustAsset getChangeTrustAssetFrom(CreateTransactionParams params) {
        return ChangeTrustAsset.create(getAssetFrom(params));
    }
}
