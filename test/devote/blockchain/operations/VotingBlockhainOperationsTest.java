package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.IssuerAccount;
import dto.CreateVotingRequest;
import executioncontexts.BlockchainExecutionContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class VotingBlockhainOperationsTest {
    @Mock
    private BlockchainExecutionContext mockExecutionContext;

    @Mock
    private Blockchains mockBlockchains;

    @Mock
    private BlockchainFactory mockBlockchainFactory;

    @Mock
    private IssuerAccount mockIssuerAccount;

    private VotingBlockchainOperations operations;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        operations = new VotingBlockchainOperations(mockExecutionContext, mockBlockchains);

        executeRunnableOnMockExecContext();

        when(mockBlockchains.getFactoryByNetwork(anyString())).thenReturn(mockBlockchainFactory);
        when(mockBlockchainFactory.createIssuerAccount()).thenReturn(mockIssuerAccount);
        when(mockIssuerAccount.calcNumOfAccountsNeeded(anyLong())).thenReturn(3);
        when(mockIssuerAccount.create(anyLong())).thenReturn("A", "B", "C");
    }

    @Test
    public void testCreateIssuerAccounts() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);

        CompletionStage<List<String>> createIssuerAccountsStage = operations.createIssuerAccounts(request);
        CompletableFuture<List<String>> createIssuerAccountsFuture = createIssuerAccountsStage.toCompletableFuture();

        List<String> createdAccounts = createIssuerAccountsFuture.get();
        assertThat(createdAccounts, containsInAnyOrder("A", "B", "C"));
    }

    public void executeRunnableOnMockExecContext() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutionContext).execute(any(Runnable.class));
    }
}
