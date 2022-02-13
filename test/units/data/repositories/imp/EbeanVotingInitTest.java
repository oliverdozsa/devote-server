package units.data.repositories.imp;

import data.entities.JpaVoting;
import data.repositories.imp.EbeanVotingInit;
import org.junit.Test;
import requests.CreateVotingRequest;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EbeanVotingInitTest {

    @Test
    public void testAuthOptionKeyBase() {
        // Given
        CreateVotingRequest request = new CreateVotingRequest();
        request.setPolls(new ArrayList<>());
        request.setVisibility(CreateVotingRequest.Visibility.PUBLIC);
        request.setAuthorization(CreateVotingRequest.Authorization.KEYBASE);
        request.setAuthorizationKeybaseOptions("someTeam");

        // When
        JpaVoting jpaVoting = EbeanVotingInit.initVotingFrom(request);

        // Then
        assertThat(jpaVoting.getAuthOptionKeybase().getTeamName(), equalTo("someTeam"));

    }
}
