package units.galactic.blockchain.stellar.voting;

import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.Account;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.stellar.voting.StellarChannelAccountOperation;
import galactic.blockchain.stellar.StellarUtils;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Transaction;

import java.io.IOException;
import java.util.List;

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
        Account someChannelGenAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());

        // When
        ChannelGenerator channelGenerator = new ChannelGenerator(someChannelGenAccount, 42);
        List<Account> channelAccounts = operation.create(channelGenerator, 84);

        // Then
        assertThat(channelAccounts, notNullValue());
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateWithFailure() throws AccountRequiresMemoException, IOException {
        // Given
        Account someChannelGenAccount = StellarUtils.toAccount(org.stellar.sdk.KeyPair.random());
        when(stellarMock.server.submitTransaction(any(Transaction.class))).thenThrow(new IOException("Some IO error!"));

        // When
        ChannelGenerator channelGenerator = new ChannelGenerator(someChannelGenAccount, 42);
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> operation.create(channelGenerator, 84));

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
