package units.devote.blockchain.stellar;

import com.typesafe.config.Config;
import devote.blockchain.stellar.StellarBlockchainConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;
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
        when(mockConfig.getString("devote.blockchain.stellar.url")).thenReturn("https://mock-horizon.stellar.org");

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
        when(mockConfig.getString("devote.blockchain.stellar.url")).thenReturn("https://mock-horizon-testnet.stellar.org");

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        assertThat(configuration.getServer(), notNullValue());
        assertThat(configuration.getNetwork(), equalTo(Network.TESTNET));
    }

    @Test
    public void testGetMasterKeyPair() {
        // Given
        when(mockConfig.getString("devote.blockchain.stellar.secret"))
                .thenReturn("SBAN2HTEKACVBTWYR5IRBI6DKYW4HPBWO44TVTAULNGCGMAAKL4USTPN");

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        KeyPair masterKeyPair = configuration.getMasterKeyPair();
        assertThat(masterKeyPair, notNullValue());
        assertThat(new String(masterKeyPair.getSecretSeed()), equalTo("SBAN2HTEKACVBTWYR5IRBI6DKYW4HPBWO44TVTAULNGCGMAAKL4USTPN"));
    }

    @Test
    public void testGetNumOfVoteBuckets() {
        // Given
        when(mockConfig.getInt("devote.blockchain.stellar.votebuckets")).thenReturn(42);

        // When
        StellarBlockchainConfiguration configuration = new StellarBlockchainConfiguration();
        configuration.init(mockConfig);

        // Then
        assertThat(configuration.getNumOfVoteBuckets(), equalTo(42));
    }
}
