package units.repositories.voting;

import data.entities.voting.JpaChannelAccountProgress;
import data.entities.voting.JpaChannelGeneratorAccount;
import data.entities.voting.JpaVoting;
import data.entities.voting.JpaVotingChannelAccount;
import data.repositories.imp.voting.EbeanCommissionRepository;
import exceptions.InternalErrorException;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import security.TokenAuthUserIdUtil;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class EbeanCommissionRepositoryTest {

    private EbeanCommissionRepository repository;

    @Mock
    private EbeanServer mockEbeanServer;

    @Mock
    private TokenAuthUserIdUtil mockTokenAuthUserIdUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        repository = new EbeanCommissionRepository(mockEbeanServer, mockTokenAuthUserIdUtil);
    }

    @Test
    public void testConsumeOneChannel_NoMoreChannelsLeft() {
        // Given
        JpaVoting mockJpaVoting = prepareConsumeOneChannelTest();
        when(mockJpaVoting.getChannelGeneratorAccounts()).thenReturn(Collections.emptyList());

        // When
        // Then
        InternalErrorException exception = assertThrows(InternalErrorException.class, () -> repository.consumeOneChannel(42L));
        assertThat(exception.getMessage(), equalTo("Could not find a free channel account!"));
    }

    @Test
    public void testConsumeOneChannel_MoreChannelWillBeCreated() {
        // Given
        JpaVoting mockJpaVoting = prepareConsumeOneChannelTest();
        JpaChannelGeneratorAccount mockVotingIssuerAccount = Mockito.mock(JpaChannelGeneratorAccount.class);
        JpaChannelAccountProgress mockChannelAccountProgress = Mockito.mock(JpaChannelAccountProgress.class);

        when(mockJpaVoting.getChannelGeneratorAccounts()).thenReturn(Collections.singletonList(mockVotingIssuerAccount));
        when(mockVotingIssuerAccount.getChannelAccountProgress()).thenReturn(mockChannelAccountProgress);
        when(mockChannelAccountProgress.getNumOfAccountsLeftToCreate()).thenReturn(1L);

        // When
        // Then
        InternalErrorException exception = assertThrows(InternalErrorException.class, () -> repository.consumeOneChannel(42L));
        assertThat(exception.getMessage(), equalTo("Could not find a free channel account! Please try again later!"));
    }

    private JpaVoting prepareConsumeOneChannelTest() {
        Query<JpaVotingChannelAccount> mockChannelAccountQuery = (Query<JpaVotingChannelAccount>) Mockito.mock(Query.class);
        ExpressionList<JpaVotingChannelAccount> mockExpressionList = (ExpressionList<JpaVotingChannelAccount>) Mockito.mock(ExpressionList.class);
        Optional<JpaVotingChannelAccount> mockChannelAccountOptional = Optional.empty();
        JpaVoting mockJpaVoting = Mockito.mock(JpaVoting.class);

        when(mockEbeanServer.createQuery(JpaVotingChannelAccount.class)).thenReturn(mockChannelAccountQuery);
        when(mockChannelAccountQuery.where()).thenReturn(mockExpressionList);
        when(mockExpressionList.eq("isConsumed", false)).thenReturn(mockExpressionList);
        when(mockExpressionList.eq("voting.id", 42L)).thenReturn(mockExpressionList);
        when(mockExpressionList.setMaxRows(1)).thenReturn(mockChannelAccountQuery);
        when(mockChannelAccountQuery.findOneOrEmpty()).thenReturn(mockChannelAccountOptional);

        when(mockEbeanServer.find(JpaVoting.class, 42L)).thenReturn(mockJpaVoting);

        return mockJpaVoting;
    }
}
