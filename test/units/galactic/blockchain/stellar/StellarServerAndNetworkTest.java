package units.galactic.blockchain.stellar;

import galactic.blockchain.stellar.StellarBlockchainConfiguration;
import galactic.blockchain.stellar.StellarServerAndNetwork;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class StellarServerAndNetworkTest {
    @Mock
    private StellarBlockchainConfiguration mockBlockchainConfiguration;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetTestServer() {
        // Given
        StellarServerAndNetwork serverAndNetwork = StellarServerAndNetwork.createForTestNet(mockBlockchainConfiguration);

        // When
        serverAndNetwork.getServer();

        // Then
        verify(mockBlockchainConfiguration).getTestNetServer();
    }

    @Test
    public void testGetTestNetwork() {
        // Given
        StellarServerAndNetwork serverAndNetwork = StellarServerAndNetwork.createForTestNet(mockBlockchainConfiguration);

        // When
        serverAndNetwork.getNetwork();

        // Then
        verify(mockBlockchainConfiguration).getTestNetwork();
    }
}
