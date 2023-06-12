package units.galactic.blockchain.stellar.voting;

import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.Account;
import galactic.blockchain.api.VoterAccountOperation;
import galactic.blockchain.stellar.StellarUtils;
import galactic.blockchain.stellar.voting.StellarVoterAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;

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

        params.issuerAccountPublic = KeyPair.random().getAccountId();

        Account aChannelAccount = StellarUtils.toAccount(KeyPair.random());
        params.channel = aChannelAccount;

        params.voterAccountPublic = KeyPair.random().getAccountId();

        Account aDistributionAccount = StellarUtils.toAccount(KeyPair.random());
        params.distribution = aDistributionAccount;

        params.assetCode = "SOMECEDE";
        params.votesCap = 42L;

        return params;
    }
}
