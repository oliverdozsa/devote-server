package units.galactic.blockchain.stellar.voting;

import galactic.blockchain.api.Account;
import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.DistributionAndBallotAccountOperation;
import galactic.blockchain.stellar.voting.StellarDistributionAndBallotAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Transaction;

import java.io.IOException;

import static galactic.blockchain.stellar.StellarUtils.toAccount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class StellarDistributionAndBallotAccountOperationTest {
    private StellarMock stellarMock;

    private StellarDistributionAndBallotAccountOperation operation;

    @Before
    public void setup() throws IOException, AccountRequiresMemoException {
        stellarMock = new StellarMock();

        operation = new StellarDistributionAndBallotAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreate() throws AccountRequiresMemoException, IOException {
        // Given
        Account funding = toAccount(KeyPair.random());

        // When
        DistributionAndBallotAccountOperation.TransactionResult result =
                operation.create(funding, "SOMECODE", 42L);

        // Then
        assertThat(result.ballot, notNullValue());
        assertThat(result.distribution, notNullValue());
        assertThat(result.issuer, notNullValue());
    }

    @Test
    public void testCreateWithFailure() throws AccountRequiresMemoException, IOException {
        // Given
        Account funding = toAccount(KeyPair.random());
        when(stellarMock.server.submitTransaction(any(Transaction.class))).thenThrow(new IOException("Some IO error!"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> operation.create(funding, "SOMECODE", 42L));

        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create distribution and ballot accounts!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }
}
