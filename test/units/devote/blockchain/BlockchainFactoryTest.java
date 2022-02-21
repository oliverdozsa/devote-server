package units.devote.blockchain;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.ChannelGeneratorAccountOperation;
import devote.blockchain.api.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.List;
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
        mockIssuerClassSet.add(SomeMockChannelGeneratorClassWithNoDefaultCtorOperation.class);

        when(mockReflections.getSubTypesOf(any())).thenReturn(mockIssuerClassSet);

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> {
            blockchainFactory.createChannelGeneratorAccountOperation();
        });

        assertThat(exception.getMessage(), containsString("Failed to create instance of"));
    }

    private static class SomeMockChannelGeneratorClassWithNoDefaultCtorOperation implements ChannelGeneratorAccountOperation {
        public SomeMockChannelGeneratorClassWithNoDefaultCtorOperation(int someArg) {
        }

        @Override
        public void init(BlockchainConfiguration configuration) {

        }

        @Override
        public void useTestNet() {

        }

        @Override
        public List<ChannelGenerator> create(long totalVotesCap, Account funding) {
            return null;
        }

        @Override
        public long calcNumOfAccountsNeeded(long totalVotesCap) {
            return 0L;
        }
    }
}
