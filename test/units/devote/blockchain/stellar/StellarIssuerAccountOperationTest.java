package units.devote.blockchain.stellar;

import devote.blockchain.stellar.StellarBlockchainConfiguration;
import devote.blockchain.stellar.StellarIssuerAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.AccountsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class StellarIssuerAccountOperationTest {
    @Mock
    private StellarBlockchainConfiguration mockConfiguration;

    @Mock
    private Server mockStellarServer;

    @Mock
    private AccountsRequestBuilder mockAccountsRequestBuilder;

    @Mock
    private AccountResponse mockAccountResponse;

    private StellarIssuerAccountOperation operation;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(mockConfiguration.getServer()).thenReturn(mockStellarServer);
        when(mockConfiguration.getNetwork()).thenReturn(Network.TESTNET);
        when(mockConfiguration.getMasterKeyPair()).thenReturn(KeyPair.random());
        when(mockStellarServer.accounts()).thenReturn(mockAccountsRequestBuilder);
        when(mockAccountsRequestBuilder.account(anyString())).thenReturn(mockAccountResponse);

        // TODO: parameterized part
        KeyPair randomKey = KeyPair.random();
        when(mockAccountResponse.getAccountId()).thenReturn(randomKey.getAccountId());

        operation = new StellarIssuerAccountOperation();
        operation.init(mockConfiguration);
    }

    @Test
    public void testCreate() {
        // Given
        // TODO

        // When
        devote.blockchain.api.KeyPair keyPair = operation.create(42L);

        // Then
        // TODO

    }

    @Test
    public void testCreateFails() {
        // TODO
        fail("Implement me!");
    }

    @Test
    public void testcalcNumOfAccountsNeeded() {
        // TODO
        fail("Implement me!");
    }
}
