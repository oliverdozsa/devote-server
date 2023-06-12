package units.services.voting.commissionsubs;

import data.operations.CommissionDbOperations;
import data.operations.VoterDbOperations;
import exceptions.ForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import requests.voting.CommissionInitRequest;
import responses.voting.CommissionInitResponse;
import security.VerifiedJwt;
import services.Base62Conversions;
import services.voting.commissionsubs.CommissionInitSubService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class CommissionInitSubServiceTest {
    @Mock
    private VerifiedJwt mockVerifiedJwt;

    @Mock
    private CommissionDbOperations mockCommissionDbOperations;

    @Mock
    private VoterDbOperations mockVoterDbOperations;

    private CommissionInitSubService initSubService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockVerifiedJwt.getUserId()).thenReturn("Alice");
        when(mockVerifiedJwt.hasVoterRole()).thenReturn(true);
        when(mockVoterDbOperations.userAuthenticated(anyString(), anyString())).thenReturn(doNothing());

        initSubService = new CommissionInitSubService("somePublicKey", mockCommissionDbOperations, mockVoterDbOperations);
    }

    @Test
    public void testUserDoesNotParticipateInVoting() {
        // Given
        CommissionInitRequest request = new CommissionInitRequest();
        request.setVotingId(Base62Conversions.encode(42L));

        when(mockVoterDbOperations.doesParticipateInVoting(anyString(), anyLong())).thenReturn(completedFuture(false));

        // When
        CompletableFuture<CommissionInitResponse> response =
                initSubService.init(request, mockVerifiedJwt).toCompletableFuture();

        // Then
        ExecutionException exception = assertThrows(ExecutionException.class, response::get);
        assertThat(exception.getCause(), instanceOf(ForbiddenException.class));
        assertThat(exception.getCause().getMessage(), containsString("does not participate in voting"));
    }

    @Test
    public void testUserAlreadyInitializedSession() throws ExecutionException, InterruptedException {
        // Given
        CommissionInitRequest request = new CommissionInitRequest();
        request.setVotingId(Base62Conversions.encode(42L));

        when(mockVoterDbOperations.doesParticipateInVoting(anyString(), anyLong())).thenReturn(completedFuture(true));
        when(mockCommissionDbOperations.isVotingInitializedProperly(anyLong())).thenReturn(completedFuture(true));
        when(mockCommissionDbOperations.doesSessionExistForUserInVoting(anyLong(), anyString()))
                .thenReturn(completedFuture(true));

        // When
        CompletableFuture<CommissionInitResponse> response =
                initSubService.init(request, mockVerifiedJwt).toCompletableFuture();

        // Then
        ExecutionException exception = assertThrows(ExecutionException.class, response::get);
        assertThat(exception.getCause(), instanceOf(ForbiddenException.class));
        assertThat(exception.getCause().getMessage(), containsString("has already started a session in voting"));
    }

    @Test
    public void testUserHasNoVoterRole() throws ExecutionException, InterruptedException {
        // Given
        CommissionInitRequest request = new CommissionInitRequest();
        request.setVotingId(Base62Conversions.encode(42L));

        when(mockVerifiedJwt.hasVoterRole()).thenReturn(false);

        // When
        CompletableFuture<CommissionInitResponse> response =
                initSubService.init(request, mockVerifiedJwt).toCompletableFuture();

        // Then
        ExecutionException exception = assertThrows(ExecutionException.class, response::get);
        assertThat(exception.getCause(), instanceOf(ForbiddenException.class));
        assertThat(exception.getCause().getMessage(), containsString("has no voter role!"));
    }

    private CompletionStage<Void> doNothing() {
        return runAsync(() -> {});
    }
}
