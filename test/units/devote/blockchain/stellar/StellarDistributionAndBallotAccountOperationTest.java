package units.devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.KeyPair;
import devote.blockchain.stellar.StellarDistributionAndBallotAccountOperation;
import devote.blockchain.stellar.StellarUtils;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Transaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StellarDistributionAndBallotAccountOperationTest {
    private StellarMock stellarMock;

    private StellarDistributionAndBallotAccountOperation operation;

    @Before
    public void setup() throws IOException, AccountRequiresMemoException {
        stellarMock = new StellarMock();

        operation = new StellarDistributionAndBallotAccountOperation();
        operation.init(stellarMock.configuration);
    }

    @Test
    public void testCreate() throws AccountRequiresMemoException, IOException {
        // Given
        List<Issuer> issuers = createIssuers();

        // When
        DistributionAndBallotAccountOperation.TransactionResult transactionResult = operation.create(issuers);

        // Then
        assertThat(transactionResult, notNullValue());
        verify(stellarMock.server).submitTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateWithFailure() throws AccountRequiresMemoException, IOException {
        // Given
        List<Issuer> issuers = createIssuers();
        when(stellarMock.server.submitTransaction(any(Transaction.class))).thenThrow(new IOException("Some IO error!"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> operation.create(issuers));


        // Then
        assertThat(exception.getMessage(), equalTo("[STELLAR]: Failed to create distribution and ballot accounts!"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    private static List<Issuer> createIssuers() {
        KeyPair anIssuerKeyPair = StellarUtils.toDevoteKeyPair(org.stellar.sdk.KeyPair.random());
        Issuer anIssuer = new Issuer(anIssuerKeyPair, 42, "ISSUER-1");

        KeyPair anotherIssuerKeyPair = StellarUtils.toDevoteKeyPair(org.stellar.sdk.KeyPair.random());
        Issuer anotherIssuer = new Issuer(anotherIssuerKeyPair, 42, "ISSUER-2");

        return Arrays.asList(anIssuer, anotherIssuer);
    }
}
