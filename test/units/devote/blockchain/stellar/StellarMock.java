package units.devote.blockchain.stellar;

import devote.blockchain.stellar.StellarBlockchainConfiguration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.AccountsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class StellarMock {
    @Mock
    StellarBlockchainConfiguration configuration;

    @Mock
    Server server;

    @Mock
    AccountsRequestBuilder accountsRequestBuilder;

    @Mock
    AccountResponse accountResponse;

    public StellarMock() throws IOException {
        MockitoAnnotations.initMocks(this);

        KeyPair randomMasterKeyPair = KeyPair.random();

        when(configuration.getServer()).thenReturn(server);
        when(configuration.getNetwork()).thenReturn(Network.TESTNET);
        when(configuration.getMasterKeyPair()).thenReturn(randomMasterKeyPair);
        when(server.accounts()).thenReturn(accountsRequestBuilder);
        when(accountsRequestBuilder.account(anyString())).thenReturn(accountResponse);
        when(accountResponse.getAccountId()).thenReturn(randomMasterKeyPair.getAccountId());
    }
}
