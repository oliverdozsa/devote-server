package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.KeyPair;
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
import java.util.stream.Collectors;

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
    private IssuerAccountOperation mockIssuerAccountOperation;

    @Mock
    private DistributionAndBallotAccountOperation mockDistributionAndBallotAccountOperation;

    @Captor
    private ArgumentCaptor<List<Issuer>> issuersCaptor;

    private VotingBlockchainOperations operations;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        operations = new VotingBlockchainOperations(mockExecutionContext, mockBlockchains);

        executeRunnableOnMockExecContext();

        when(mockBlockchains.getFactoryByNetwork(anyString())).thenReturn(mockBlockchainFactory);
        when(mockBlockchainFactory.createIssuerAccountOperation()).thenReturn(mockIssuerAccountOperation);
        when(mockBlockchainFactory.createDistributionAndBallotAccountOperation()).thenReturn(mockDistributionAndBallotAccountOperation);
        when(mockIssuerAccountOperation.calcNumOfAccountsNeeded(anyLong())).thenReturn(3);
        when(mockIssuerAccountOperation.create(anyLong())).thenReturn(
                new KeyPair("sA", "pA"),
                new KeyPair("sB", "pB"),
                new KeyPair("sC", "pC")
        );
        when(mockDistributionAndBallotAccountOperation.create(anyList())).thenReturn(
                new DistributionAndBallotAccountOperation.TransactionResult(new KeyPair("d", "d"), new KeyPair("b", "b"), new HashMap<>())
        );
    }

    @Test
    public void testCreateIssuerAccounts() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);

        CompletionStage<List<Issuer>> createIssuerAccountsStage = operations.createIssuerAccounts(request);
        CompletableFuture<List<Issuer>> createIssuerAccountsFuture = createIssuerAccountsStage.toCompletableFuture();

        List<KeyPair> createdAccounts = createIssuerAccountsFuture.get().stream()
                .map(issuer -> issuer.keyPair)
                .collect(Collectors.toList());
        assertThat(createdAccounts, containsInAnyOrder(
                new KeyPair("sA", "pA"),
                new KeyPair("sB", "pB"),
                new KeyPair("sC", "pC")
        ));
    }

    @Test
    public void testCreateDistributionAndBallot_VoteTitleUsedAndItsLongerThanBaseLength() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);
        request.setTitle("S#ome!Vo@tingWithALongTitle");

        List<Issuer> issuers = Arrays.asList(
                new Issuer(
                        new KeyPair("issuerSecret1", "issuerPublic1"),
                        42,
                        "A1"
                ),
                new Issuer(
                        new KeyPair("issuerSecret2", "issuerPublic2"),
                        21,
                        "A2"
                )
        );

        CompletionStage<DistributionAndBallotAccountOperation.TransactionResult> resultCompletionStage =
                operations.createDistributionAndBallotAccounts(request, issuers);
        resultCompletionStage.toCompletableFuture().get();

        Mockito.verify(mockDistributionAndBallotAccountOperation).create(issuersCaptor.capture());

        List<Issuer> capturedIssuers = issuersCaptor.getValue();
        capturedIssuers.forEach(issuer -> assertThat(issuer.assetCode, Matchers.startsWith("SOMEVOTI")));
    }

    @Test
    public void testTokenTitle_TokenIdentifierUsed() throws ExecutionException, InterruptedException {
        CreateVotingRequest request = new CreateVotingRequest();
        request.setNetwork("mocknetwork");
        request.setVotesCap(42L);
        request.setTitle("S#ome!Vo@tingWithALongTitle");
        request.setTokenIdentifier("SomeID");

        List<Issuer> issuers = Arrays.asList(
                new Issuer(
                        new KeyPair("issuerSecret1", "issuerPublic1"),
                        42,
                        "A1"
                ),
                new Issuer(
                        new KeyPair("issuerSecret2", "issuerPublic2"),
                        21,
                        "A2"
                )
        );

        CompletionStage<DistributionAndBallotAccountOperation.TransactionResult> resultCompletionStage =
                operations.createDistributionAndBallotAccounts(request, issuers);
        resultCompletionStage.toCompletableFuture().get();

        Mockito.verify(mockDistributionAndBallotAccountOperation).create(issuersCaptor.capture());

        List<Issuer> capturedIssuers = issuersCaptor.getValue();
        capturedIssuers.forEach(i -> assertThat(i.assetCode, Matchers.startsWith("SOMEID")));
    }

    public void executeRunnableOnMockExecContext() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutionContext).execute(any(Runnable.class));
    }
}
