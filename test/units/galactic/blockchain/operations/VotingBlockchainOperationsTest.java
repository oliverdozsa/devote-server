package units.galactic.blockchain.operations;

import galactic.blockchain.BlockchainFactory;
import galactic.blockchain.Blockchains;
import galactic.blockchain.api.Account;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.api.ChannelGeneratorAccountOperation;
import galactic.blockchain.api.DistributionAndBallotAccountOperation;
import galactic.blockchain.operations.VotingBlockchainOperations;
import executioncontexts.BlockchainExecutionContext;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class VotingBlockchainOperationsTest {
    @Mock
    private BlockchainExecutionContext mockExecutionContext;

    @Mock
    private Blockchains mockBlockchains;

    @Mock
    private BlockchainFactory mockBlockchainFactory;

    @Mock
    private ChannelGeneratorAccountOperation mockChannelGeneratorAccountOperation;

    @Mock
    private DistributionAndBallotAccountOperation mockDistributionAndBallotAccountOperation;

    @Captor
    private ArgumentCaptor<Account> fundingCaptor;

    private VotingBlockchainOperations operations;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        operations = new VotingBlockchainOperations(mockExecutionContext, mockBlockchains);

        executeRunnableOnMockExecContext();

        when(mockBlockchains.getFactoryByNetwork(anyString())).thenReturn(mockBlockchainFactory);
        when(mockBlockchainFactory.createChannelGeneratorAccountOperation()).thenReturn(mockChannelGeneratorAccountOperation);
        when(mockBlockchainFactory.createDistributionAndBallotAccountOperation()).thenReturn(mockDistributionAndBallotAccountOperation);
        when(mockChannelGeneratorAccountOperation.calcNumOfAccountsNeeded(anyLong())).thenReturn(3L);
        when(mockChannelGeneratorAccountOperation.create(anyLong(), any(Account.class))).thenReturn(
                Arrays.asList(
                        new ChannelGenerator(new Account("sA", "pA"), 42L),
                        new ChannelGenerator(new Account("sB", "pB"), 42L),
                        new ChannelGenerator(new Account("sC", "pC"), 42L)
                )
        );
        when(mockDistributionAndBallotAccountOperation.create(any(Account.class), anyString(), anyLong())).thenReturn(
                new DistributionAndBallotAccountOperation.TransactionResult(
                        new Account("d", "d"),
                        new Account("b", "b"),
                        new Account("c", "c"))
        );
    }

    public void executeRunnableOnMockExecContext() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutionContext).execute(any(Runnable.class));
    }
}
