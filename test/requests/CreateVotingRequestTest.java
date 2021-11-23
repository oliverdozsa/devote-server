package requests;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class CreateVotingRequestTest {
    @Mock
    private Config mockConfig;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getInt("devote.vote.related.min.time.interval.sec")).thenReturn(42);
    }

    @Test
    public void testEncryptedUntilNotValid() {
        // Given
        CreateVotingRequest createVotingRequest = new CreateVotingRequest();
        createVotingRequest.setEncryptedUntil(Instant.now().minus(Duration.ofDays(1)));

        // When
        String errorMessage = createVotingRequest.validate(mockConfig);

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("If encryption is needed"));
    }

    @Test
    public void testAuthorizationEmailsNotValid() {
        // Given
        CreateVotingRequest createVotingRequest = new CreateVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(new ArrayList<>());

        // When
        String errorMessage = createVotingRequest.validate(mockConfig);

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("Invalid authorization!"));
    }

    @Test
    public void testAuthorizationKeybaseNotValid() {
        // Given
        CreateVotingRequest createVotingRequest = new CreateVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.KEYBASE);
        createVotingRequest.setAuthorizationKeybaseOptions("");

        // When
        String errorMessage = createVotingRequest.validate(mockConfig);

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("Invalid authorization!"));
    }

    @Test
    public void testStartDateNull() {
        // Given
        CreateVotingRequest createVotingRequest = new CreateVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.KEYBASE);
        createVotingRequest.setAuthorizationKeybaseOptions("someTeam");
        createVotingRequest.setStartDate(null);

        // When
        String errorMessage = createVotingRequest.validate(mockConfig);

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("The minimum difference between start and end date should be"));
    }

    @Test
    public void testStartDateWithEndDateInPast() {
        // Given
        CreateVotingRequest createVotingRequest = new CreateVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.KEYBASE);
        createVotingRequest.setAuthorizationKeybaseOptions("someTeam");
        createVotingRequest.setStartDate(Instant.now());
        createVotingRequest.setStartDate(Instant.now().minus(Duration.ofDays(1)));

        // When
        String errorMessage = createVotingRequest.validate(mockConfig);

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("The minimum difference between start and end date should be"));
    }
}
