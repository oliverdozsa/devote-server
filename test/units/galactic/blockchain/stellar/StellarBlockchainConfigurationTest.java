package units.galactic.blockchain.stellar;

import com.typesafe.config.Config;
import galactic.blockchain.stellar.StellarBlockchainConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.Network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

public class StellarBlockchainConfigurationTest {
    @Mock
    private Config mockConfig;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetNonTestServer() {
        // Given
        when(mockConfig.getString("galactic.host.blockchain.stellar.url")).thenReturn("https://mock-horizon.stellar.org");
        when(mockConfig.getString("galactic.host.blockchain.stellar.testnet.url")).thenReturn("https://mock-test-horizon.stellar.org");

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        assertThat(configuration.getServer(), notNullValue());
        assertThat(configuration.getNetwork(), equalTo(Network.PUBLIC));
    }

    @Test
    public void testGetTestServer() {
        // Given
        when(mockConfig.getString("galactic.host.blockchain.stellar.url")).thenReturn("https://mock-horizon-testnet.stellar.org");
        when(mockConfig.getString("galactic.host.blockchain.stellar.testnet.url")).thenReturn("https://mock-test-horizon.stellar.org");

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        assertThat(configuration.getTestNetServer(), notNullValue());
        assertThat(configuration.getTestNetwork(), equalTo(Network.TESTNET));
    }

    @Test
    public void testGetNumOfVoteBuckets() {
        // Given
        when(mockConfig.getLong("galactic.host.vote.blockchain.stellar.votebuckets")).thenReturn(42L);

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        assertThat(configuration.getNumOfVoteBuckets(), equalTo(42L));
    }
}
