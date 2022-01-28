package devote.blockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccountFactory;
import devote.blockchain.api.KeyPair;
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
        mockIssuerClassSet.add(SomeMockIssuerClassWithNoDefaultCtorFactory.class);

        when(mockReflections.getSubTypesOf(any())).thenReturn(mockIssuerClassSet);

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> {
            blockchainFactory.createIssuerAccount();
        });

        assertThat(exception.getMessage(), containsString("Failed to create instance of"));
    }

    private static class SomeMockIssuerClassWithNoDefaultCtorFactory implements IssuerAccountFactory {
        public SomeMockIssuerClassWithNoDefaultCtorFactory(int someArg) {
        }

        @Override
        public void init(BlockchainConfiguration configuration) {

        }

        @Override
        public KeyPair create(long votesCap, int i) {
            return null;
        }

        @Override
        public int calcNumOfAccountsNeeded(long votesCap) {
            return 0;
        }
    }
}
