package units.galactic.blockchain;

import com.typesafe.config.Config;
import galactic.blockchain.BlockchainFactory;
import galactic.blockchain.Blockchains;
import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.ChannelGeneratorAccountOperation;
import galactic.blockchain.mockblockchain.MockBlockchainChannelGeneratorAccountOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BlockchainsTest {
    @Mock
    private Config mockConfig;

    private Blockchains blockchains;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        when(mockConfig.withOnlyPath(anyString())).thenReturn(mockConfig);
        blockchains = new Blockchains(mockConfig);
    }

    @Test
    public void testDiscovery() {
        // Given
        // When
        BlockchainFactory factoryForMockBlockchain = blockchains.getFactoryByNetwork("mockblockchain");

        // Then
        assertThat(factoryForMockBlockchain, notNullValue());

        ChannelGeneratorAccountOperation channelGeneratorAccountOperation = factoryForMockBlockchain.createChannelGeneratorAccountOperation();
        assertThat(channelGeneratorAccountOperation, instanceOf(MockBlockchainChannelGeneratorAccountOperation.class));

        MockBlockchainChannelGeneratorAccountOperation mockIssuerAccount = (MockBlockchainChannelGeneratorAccountOperation) factoryForMockBlockchain.createChannelGeneratorAccountOperation();
        assertBlockchainConfigInitCalled(mockIssuerAccount);
        assertTrue(mockIssuerAccount.isInitCalled());
    }

    @Test
    public void testWithConfigInNotCorrectPackageName() {
        // Given
        // When
        // Then
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> blockchains.getFactoryByNetwork("blockchain_with_not_correct_package_name"));

        assertThat(exception.getMessage(), equalTo("Not found blockchain factory for network: blockchain_with_not_correct_package_name"));
    }

    @Test
    public void testWithNoDefaultConstructor() {
        // Given
        // When
        // Then
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> blockchains.getFactoryByNetwork("blockchain_with_no_default_constructor"));


        assertThat(exception.getMessage(), equalTo("Not found blockchain factory for network: blockchain_with_no_default_constructor"));
    }

    @Test
    public void testWithMissingImplementation() {
        // Given
        // When
        // Then
        BlockchainException exception =
                assertThrows(BlockchainException.class, () -> blockchains.getFactoryByNetwork("blockchain_with_missing_implementation"));


        assertThat(exception.getMessage(), equalTo("Not found blockchain factory for network: blockchain_with_missing_implementation"));
    }

    private void assertBlockchainConfigInitCalled(MockBlockchainChannelGeneratorAccountOperation mockIssuerAccount) {
        assertTrue(mockIssuerAccount.getConfig().isInitCalled());
    }
}
