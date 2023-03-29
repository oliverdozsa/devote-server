package units.galactic.blockchain.stellar;

import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.stellar.StellarSubmitTransaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Operation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class StellarSubmitTransactionTest {
    @Mock
    private Transaction mockTransaction;

    @Mock
    private Server mockServer;

    @Mock
    private SubmitTransactionResponse mockSubmitTxResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmitWithFailure() throws AccountRequiresMemoException, IOException {
        // Given
        when(mockServer.submitTransaction(any(Transaction.class))).thenReturn(mockSubmitTxResponse);
        when(mockSubmitTxResponse.isSuccess()).thenReturn(false);

        SubmitTransactionResponse.Extras mockExtras = Mockito.mock(SubmitTransactionResponse.Extras.class);
        when(mockSubmitTxResponse.getExtras()).thenReturn(mockExtras);

        SubmitTransactionResponse.Extras.ResultCodes mockResultCodes = Mockito.mock(SubmitTransactionResponse.Extras.ResultCodes.class);
        when(mockExtras.getResultCodes()).thenReturn(mockResultCodes);

        when(mockResultCodes.getOperationsResultCodes()).thenReturn(null);

        when(mockResultCodes.getTransactionResultCode()).thenReturn("42");

        when(mockTransaction.getOperations()).thenReturn(new Operation[]{});

        // When
        // Then
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> StellarSubmitTransaction.submit("mock tx", mockTransaction, mockServer));
        assertThat(exception.getMessage(), containsString("STELLAR]: Failed to submit transaction"));
    }
}
