package units.devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.KeyPair;
import devote.blockchain.api.VoterAccountOperation;
import devote.blockchain.stellar.StellarUtils;
import devote.blockchain.stellar.StellarVoterAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Transaction;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class StellarVoterAccountOperationTest {
    private StellarMock stellarMock;

    private StellarVoterAccountOperation operation;

    @Before
    public void setup() throws IOException {
        stellarMock = new StellarMock();

        operation = new StellarVoterAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreateTransaction() {
        // Given
        VoterAccountOperation.CreationData creationData = generateCreationData();

        // When
        String transactionString = operation.createTransaction(creationData);

        // Then
        assertThat(transactionString, notNullValue());
        assertThat(transactionString.length(), greaterThan(0));
    }

    @Test
    public void testCreateTransactionWithFailure() throws IOException {
        // Given
        VoterAccountOperation.CreationData creationData = generateCreationData();
        when(stellarMock.server.accounts().account(anyString())).thenThrow(new IOException("Some IO error"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> operation.createTransaction(creationData));

        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create voter account transaction!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    private static VoterAccountOperation.CreationData generateCreationData() {
        VoterAccountOperation.CreationData creationData =
                new VoterAccountOperation.CreationData();

        KeyPair anIssuerKeyPair = StellarUtils.toDevoteKeyPair(org.stellar.sdk.KeyPair.random());
        Issuer anIssuer = new Issuer(anIssuerKeyPair, 42, "ISSUER-1");
        creationData.issuer = anIssuer;

        KeyPair aChannelKeyPair = StellarUtils.toDevoteKeyPair(org.stellar.sdk.KeyPair.random());
        creationData.channelKeyPair = aChannelKeyPair;

        creationData.voterPublicKey = org.stellar.sdk.KeyPair.random().getAccountId();

        KeyPair aDistributionKeyPair = StellarUtils.toDevoteKeyPair(org.stellar.sdk.KeyPair.random());
        creationData.distributionKeyPair = aDistributionKeyPair;

        return creationData;
    }
}
