package units.devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.Account;
import devote.blockchain.stellar.StellarChannelAccountOperation;
import devote.blockchain.stellar.StellarUtils;
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

public class StellarChannelAccountOperationTest {
    private StellarMock stellarMock;

    private StellarChannelAccountOperation operation;

    @Before
    public void setup() throws IOException, AccountRequiresMemoException {
        stellarMock = new StellarMock();

        operation = new StellarChannelAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreate() throws AccountRequiresMemoException, IOException {
        // Given
        Account someIssuerAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());

        // When
        Account channelAccount = operation.create(42, someIssuerAccount);

        // Then
        assertThat(channelAccount, notNullValue());
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateWithFailure() throws AccountRequiresMemoException, IOException {
        // Given
        Account someIssuerAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());
        when(stellarMock.server.submitTransaction(any(Transaction.class))).thenThrow(new IOException("Some IO error!"));

        // When
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> operation.create(42, someIssuerAccount));

        // Then
        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create channel account!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testMaxNumOfAccountsToCreateInOneBatch() {
        // Given
        // When
        int maxNumOfOperationsInOneBranch = operation.maxNumOfAccountsToCreateInOneBatch();

        // Then
        assertThat(maxNumOfOperationsInOneBranch, equalTo(50));
    }
}
