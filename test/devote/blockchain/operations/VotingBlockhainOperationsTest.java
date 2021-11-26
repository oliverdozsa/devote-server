package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.IssuerAccount;
import requests.CreateVotingRequest;
import executioncontexts.BlockchainExecutionContext;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
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

    @Mock
    private DistributionAndBallotAccount mockDistributionAndBallotAccount;

    @Captor
    private ArgumentCaptor<List<DistributionAndBallotAccount.IssuerData>> issuerDataCaptor;

    private VotingBlockchainOperations operations;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        operations = new VotingBlockchainOperations(mockExecutionContext, mockBlockchains);

        executeRunnableOnMockExecContext();

        when(mockBlockchains.getFactoryByNetwork(anyString())).thenReturn(mockBlockchainFactory);
        when(mockBlockchainFactory.createIssuerAccount()).thenReturn(mockIssuerAccount);
        when(mockBlockchainFactory.createDistributionAndBallotAccount()).thenReturn(mockDistributionAndBallotAccount);
        when(mockIssuerAccount.calcNumOfAccountsNeeded(anyLong())).thenReturn(3);
        when(mockIssuerAccount.create(anyLong())).thenReturn("A", "B", "C");
        when(mockDistributionAndBallotAccount.create(anyList(), anyLong())).thenReturn(
                new DistributionAndBallotAccount.TransactionResult("d", "b", new HashMap<>())
        );
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

    @Test
    public void testCreateDistributionAndBallot_VoteTitleUsedAndItsLongerThanBaseLength() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);
        request.setTitle("S#ome!Vo@tingWithALongTitle");

        CompletionStage<DistributionAndBallotAccount.TransactionResult> resultCompletionStage =
                operations.createDistributionAndBallotAccounts(request, Arrays.asList("issuerSecret1", "issuerSecret2"));
        resultCompletionStage.toCompletableFuture().get();

        Mockito.verify(mockDistributionAndBallotAccount).create(issuerDataCaptor.capture(), anyLong());

        List<DistributionAndBallotAccount.IssuerData> capturedIssuerData = issuerDataCaptor.getValue();
        capturedIssuerData.forEach(i -> assertThat(i.voteTokenTitle, Matchers.startsWith("SOMEVOTI")));
    }

    @Test
    public void testTokenTitle_TokenIdentifierUsed() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);
        request.setTitle("S#ome!Vo@tingWithALongTitle");
        request.setTokenIdentifier("SomeID");

        CompletionStage<DistributionAndBallotAccount.TransactionResult> resultCompletionStage =
                operations.createDistributionAndBallotAccounts(request, Arrays.asList("issuerSecret1", "issuerSecret2"));
        resultCompletionStage.toCompletableFuture().get();

        Mockito.verify(mockDistributionAndBallotAccount).create(issuerDataCaptor.capture(), anyLong());

        List<DistributionAndBallotAccount.IssuerData> capturedIssuerData = issuerDataCaptor.getValue();
        capturedIssuerData.forEach(i -> assertThat(i.voteTokenTitle, Matchers.startsWith("SOMEID")));
    }

    public void executeRunnableOnMockExecContext() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutionContext).execute(any(Runnable.class));
    }
}
