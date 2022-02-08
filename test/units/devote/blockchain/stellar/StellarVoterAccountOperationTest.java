package units.devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.Account;
import devote.blockchain.api.VoterAccountOperation;
import devote.blockchain.stellar.StellarUtils;
import devote.blockchain.stellar.StellarVoterAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;

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
    public void setup() throws IOException, AccountRequiresMemoException {
        stellarMock = new StellarMock();

        operation = new StellarVoterAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreateTransaction() {
        // Given
        VoterAccountOperation.CreateTransactionParams params = generateCreationData();

        // When
        String transactionString = operation.createTransaction(params);

        // Then
        assertThat(transactionString, notNullValue());
        assertThat(transactionString.length(), greaterThan(0));
    }

    @Test
    public void testCreateTransactionWithFailure() throws IOException {
        // Given
        VoterAccountOperation.CreateTransactionParams params = generateCreationData();
        when(stellarMock.server.accounts().account(anyString())).thenThrow(new IOException("Some IO error"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> operation.createTransaction(params));

        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create voter account transaction!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    private static VoterAccountOperation.CreateTransactionParams generateCreationData() {
        VoterAccountOperation.CreateTransactionParams params =
                new VoterAccountOperation.CreateTransactionParams();

        Account anIssuerAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());
        Issuer anIssuer = new Issuer(anIssuerAccount, 42, "ISSUER-1");
        params.issuer = anIssuer;

        Account aChannelAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());
        params.channel = aChannelAccount;

        params.voterAccountPublic = org.stellar.sdk.KeyPair.random().getAccountId();

        Account aDistributionAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());
        params.distribution = aDistributionAccount;

        return params;
    }
}
