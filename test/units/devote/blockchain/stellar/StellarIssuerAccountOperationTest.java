package units.devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import devote.blockchain.stellar.StellarIssuerAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Transaction;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StellarIssuerAccountOperationTest {
    private StellarMock stellarMock;

    private StellarIssuerAccountOperation operation;

    @Before
    public void setup() throws IOException, AccountRequiresMemoException {
        stellarMock = new StellarMock();

        operation = new StellarIssuerAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreate() throws AccountRequiresMemoException, IOException {
        // Given
        // When
        devote.blockchain.api.KeyPair keyPair = operation.create(42L);

        // Then
        assertThat(keyPair, notNullValue());
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateFailsWithIOException() throws AccountRequiresMemoException, IOException {
        // Given
        when(stellarMock.server.submitTransaction(any(Transaction.class))).thenThrow(new IOException("Some IO error!"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> operation.create(42L));
        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create issuer account!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testCalcNumOfAccountsNeeded() {
        // Given
        when(stellarMock.configuration.getNumOfVoteBuckets()).thenReturn(42L);

        // When
        long numOfAccountsNeeded = operation.calcNumOfAccountsNeeded(8484);

        // Then
        assertThat(numOfAccountsNeeded, equalTo(42L));
    }
}
