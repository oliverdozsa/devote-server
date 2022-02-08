package units.devote.blockchain;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BlockchainFactoryTest {
    @Mock
    private Reflections mockReflections;

    @Mock
    private BlockchainConfiguration mockBlockchainConfig;

    private BlockchainFactory blockchainFactory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        blockchainFactory = new BlockchainFactory(mockBlockchainConfig, mockReflections);
    }

    @Test
    public void testCreateIssuerAccountFails() {
        // Given
        Set<Class<?>> mockIssuerClassSet = new HashSet<>();
        mockIssuerClassSet.add(SomeMockIssuerClassWithNoDefaultCtorOperation.class);

        when(mockReflections.getSubTypesOf(any())).thenReturn(mockIssuerClassSet);

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> {
            blockchainFactory.createIssuerAccountOperation();
        });

        assertThat(exception.getMessage(), containsString("Failed to create instance of"));
    }

    private static class SomeMockIssuerClassWithNoDefaultCtorOperation implements IssuerAccountOperation {
        public SomeMockIssuerClassWithNoDefaultCtorOperation(int someArg) {
        }

        @Override
        public void init(BlockchainConfiguration configuration) {

        }

        @Override
        public Account create(long votesCap) {
            return null;
        }

        @Override
        public long calcNumOfAccountsNeeded(long totalVotesCap) {
            return 0L;
        }
    }
}
