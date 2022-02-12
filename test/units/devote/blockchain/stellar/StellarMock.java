package units.devote.blockchain.stellar;

import devote.blockchain.stellar.StellarBlockchainConfiguration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.requests.AccountsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    SubmitTransactionResponse mockSubmitTxResponse;

    public StellarMock() throws IOException, AccountRequiresMemoException {
        MockitoAnnotations.initMocks(this);

        KeyPair randomFundingKey = KeyPair.random();

        when(configuration.getServer()).thenReturn(server);
        when(configuration.getNetwork()).thenReturn(Network.TESTNET);
        when(server.accounts()).thenReturn(accountsRequestBuilder);
        when(accountsRequestBuilder.account(anyString())).thenReturn(accountResponse);
        when(accountResponse.getAccountId()).thenReturn(randomFundingKey.getAccountId());
        when(server.submitTransaction(any(Transaction.class))).thenReturn(mockSubmitTxResponse);
        when(mockSubmitTxResponse.isSuccess()).thenReturn(true);
        when(configuration.getNumOfVoteBuckets()).thenReturn(3L);
    }
}
