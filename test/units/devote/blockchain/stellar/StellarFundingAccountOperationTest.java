package units.devote.blockchain.stellar;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.stellar.StellarFundingAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.AccountResponse;

import java.io.IOException;

import static devote.blockchain.stellar.StellarUtils.toAccount;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class StellarFundingAccountOperationTest {
    private StellarMock stellarMock;

    private StellarFundingAccountOperation operation;

    @Mock
    private AccountResponse mockFundingAccount;

    @Mock
    private AccountResponse.Balance mockFundingBalance;

    private final KeyPair userGivenKeyPair = KeyPair.random();

    @Before
    public void setup() throws AccountRequiresMemoException, IOException {
        MockitoAnnotations.initMocks(this);
        stellarMock = new StellarMock();

        operation = new StellarFundingAccountOperation();
        operation.init(stellarMock.configuration);

        when(stellarMock.server.accounts().account("someFundingAccount")).thenReturn(mockFundingAccount);
        when(stellarMock.server.accounts().account(userGivenKeyPair.getAccountId())).thenReturn(mockFundingAccount);
        when(mockFundingBalance.getAssetType()).thenReturn("native");
        when(mockFundingBalance.getBalance()).thenReturn("178");

        when(mockFundingAccount.getBalances()).thenReturn(new AccountResponse.Balance[]{mockFundingBalance});
        when(mockFundingAccount.getAccountId()).thenReturn(KeyPair.random().getAccountId());
    }

    @Test
    public void testHasEnoughBalance() {
        // Given
        // When
        boolean doesNotHaveEnoughBalance = operation.doesNotHaveEnoughBalanceForVotesCap("someFundingAccount", 21);

        // Then
        assertThat(doesNotHaveEnoughBalance, is(false));
    }

    @Test
    public void testDoesNotHaveEnoughBalance() {
        // Given
        when(mockFundingBalance.getBalance()).thenReturn("42");

        // When
        boolean doesNotHaveEnoughBalance = operation.doesNotHaveEnoughBalanceForVotesCap("someFundingAccount", 21);

        // Then
        assertThat(doesNotHaveEnoughBalance, is(true));
    }

    @Test
    public void testXlmBalanceNotFound() {
        // Given
        when(mockFundingAccount.getBalances()).thenReturn(new AccountResponse.Balance[]{});

        // When
        // Then
        BlockchainException exception = assertThrows(
                BlockchainException.class,
                () -> operation.doesNotHaveEnoughBalanceForVotesCap("someFundingAccount", 21));

        assertThat(exception.getMessage(), equalTo("Could not find xlm balance!"));
    }

    @Test
    public void testFailedToGetAccount() throws IOException {
        // Given
        when(stellarMock.server.accounts().account("someFundingAccount")).thenThrow(new IOException("someIOError"));

        // When
        // Then
        BlockchainException exception = assertThrows(
                BlockchainException.class,
                () -> operation.doesNotHaveEnoughBalanceForVotesCap("someFundingAccount", 21));

        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to get info about funding account!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testCreateInternalFunding() {
        // Given
        // When
        Account internalFundingAccount = operation
                .createAndFundInternalFrom(toAccount(userGivenKeyPair), 42);

        // Then
        assertThat(internalFundingAccount, notNullValue());
        assertThat(internalFundingAccount.publik, notNullValue());
        assertThat(internalFundingAccount.secret, notNullValue());
    }
}
