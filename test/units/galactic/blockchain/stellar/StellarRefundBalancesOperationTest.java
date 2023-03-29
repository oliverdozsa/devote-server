package units.galactic.blockchain.stellar;

import galactic.blockchain.api.Account;
import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.stellar.StellarRefundBalancesOperation;
import galactic.blockchain.stellar.StellarUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.AccountResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static galactic.blockchain.stellar.StellarUtils.toAccount;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StellarRefundBalancesOperationTest {
    private StellarMock stellarMock;

    private StellarRefundBalancesOperation operation;

    @Mock
    private AccountResponse mockAccountToRefund;

    @Mock
    private AccountResponse.Balance mockAccountToRefundBalance;

    private final KeyPair[] keyPairsToRefund = {KeyPair.random(), KeyPair.random(), KeyPair.random()};

    private final KeyPair keyPairThatAlreadyMerged = KeyPair.random();
    private final KeyPair keyPairThatInducesIOException = KeyPair.random();
    private final KeyPair userFundingKeyPairThatDoesntExist = KeyPair.random();

    @Before
    public void setup() throws AccountRequiresMemoException, IOException {
        MockitoAnnotations.initMocks(this);

        stellarMock = new StellarMock();

        operation = new StellarRefundBalancesOperation();
        operation.init(stellarMock.configuration);

        when(stellarMock.server.accounts().account(keyPairsToRefund[0].getAccountId())).thenReturn(mockAccountToRefund);
        when(stellarMock.server.accounts().account(keyPairsToRefund[1].getAccountId())).thenReturn(mockAccountToRefund);
        when(stellarMock.server.accounts().account(keyPairsToRefund[2].getAccountId())).thenReturn(mockAccountToRefund);
        when(stellarMock.server.accounts().account(keyPairThatAlreadyMerged.getAccountId())).thenThrow(new ErrorResponse(404, ""));
        when(stellarMock.server.accounts().account(keyPairThatInducesIOException.getAccountId())).thenThrow(new IOException(""));
        when(stellarMock.server.accounts().account(userFundingKeyPairThatDoesntExist.getAccountId())).thenThrow(new ErrorResponse(404, ""));

        when(mockAccountToRefundBalance.getAssetType()).thenReturn("native");
        when(mockAccountToRefundBalance.getBalance()).thenReturn("168");

        when(mockAccountToRefund.getBalances()).thenReturn(new AccountResponse.Balance[]{mockAccountToRefundBalance});

        when(mockAccountToRefund.getAccountId()).thenReturn(KeyPair.random().getAccountId());
    }

    @Test
    public void testRefundBalancesWithPayment() throws AccountRequiresMemoException, IOException {
        // Given
        Account destination = toAccount(KeyPair.random());
        List<Account> accountsToRefund = Arrays.stream(keyPairsToRefund)
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        operation.refundBalancesWithPayment(destination, accountsToRefund);

        // Then
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testRefundBalancesWithTermination() throws AccountRequiresMemoException, IOException {
        // Given
        Account destination = toAccount(KeyPair.random());

        ArrayList<KeyPair> keyPairsToRefundWithOneAlreadyRefund = new ArrayList<>(Arrays.asList(keyPairsToRefund));
        keyPairsToRefundWithOneAlreadyRefund.add(keyPairThatAlreadyMerged);


        List<Account> accountsToRefund = keyPairsToRefundWithOneAlreadyRefund.stream()
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        operation.refundBalancesWithTermination(destination, accountsToRefund);

        // Then
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testRefundBalancesWithTermination_AllAccountsMergedAlready() throws AccountRequiresMemoException, IOException {
        // Given
        Account destination = toAccount(KeyPair.random());

        List<Account> accountsToRefund = Arrays.asList(keyPairThatAlreadyMerged)
                .stream()
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        operation.refundBalancesWithTermination(destination, accountsToRefund);

        // Then
        verify(stellarMock.server, never()).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testRefundBalancesWithTermination_FailedToCheck() {
        // Given
        Account destination = toAccount(KeyPair.random());

        List<Account> accountsToRefund = Arrays.asList(keyPairThatInducesIOException)
                .stream()
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        // Then
        BlockchainException exception = assertThrows(
                BlockchainException.class,
                () -> operation.refundBalancesWithTermination(destination, accountsToRefund));

        assertThat(exception.getMessage(), equalTo("Failed to refund balances with termination!"));
    }

    @Test
    public void testRefundBalancesWithTermination_DestinationDoesntExist() throws AccountRequiresMemoException, IOException {
        // Given
        Account destination = toAccount(userFundingKeyPairThatDoesntExist);

        List<Account> accountsToRefund = Arrays.stream(keyPairsToRefund)
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        operation.refundBalancesWithTermination(destination, accountsToRefund);

        // Then
        verify(stellarMock.server, never()).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testRefundBalancesWithPayment_DestinationDoesntExist() throws AccountRequiresMemoException, IOException {
        // Given
        Account destination = toAccount(userFundingKeyPairThatDoesntExist);

        List<Account> accountsToRefund = Arrays.stream(keyPairsToRefund)
                .map(StellarUtils::toAccount)
                .collect(Collectors.toList());

        // When
        operation.refundBalancesWithPayment(destination, accountsToRefund);

        // Then
        verify(stellarMock.server, never()).submitTransaction(any(Transaction.class));
    }
}
