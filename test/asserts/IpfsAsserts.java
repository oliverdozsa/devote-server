package asserts;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaVoting;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import io.ipfs.cid.Cid;
import io.ipfs.multiaddr.MultiAddress;
import ipfs.api.IpfsApi;
import play.Application;
import play.libs.Json;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IpfsAsserts {
    private final IpfsApi ipfsApi;

    public IpfsAsserts(Application application) {
        ipfsApi = application.injector().instanceOf(IpfsApi.class);
    }


    public void assertVotingSavedToIpfs(Long votingId) throws IOException {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        JsonNode ipfsVotingJson = ipfsApi.retrieveJson(voting.getIpfsCid());

        assertThat(ipfsVotingJson.get("network").asText(), equalTo(voting.getNetwork()));
        assertThat(ipfsVotingJson.get("authorization").asText(), equalTo(voting.getAuthorization().name()));
        assertThat(ipfsVotingJson.get("visibility").asText(), equalTo(voting.getVisibility().name()));
        assertThat(ipfsVotingJson.get("title").asText(), equalTo(voting.getTitle()));
        assertPollsAreTheSame(ipfsVotingJson, voting);
    }

    private static void assertPollsAreTheSame(JsonNode ipfsVotingJson, JpaVoting jpaVoting) {
        assertThat(ipfsVotingJson.get("polls").size(), equalTo(jpaVoting.getPolls().size()));
        ipfsVotingJson.get("polls").forEach(p -> assertHasPoll(p, jpaVoting));
    }

    private static void assertHasPoll(JsonNode jsonPoll, JpaVoting voting) {
        String jsonPollQuestion = jsonPoll.get("question").asText();
        List<JpaVotingPoll> pollsWithQuestion = voting.getPolls().stream().
                filter(jp -> jp.getQuestion().equals(jsonPollQuestion))
                .collect(Collectors.toList());

        assertThat(pollsWithQuestion, hasSize(1));

        JpaVotingPoll jpaVotingPoll = pollsWithQuestion.get(0);
        assertPollOptionsAreTheSame(jsonPoll, jpaVotingPoll);
    }

    private static void assertPollOptionsAreTheSame(JsonNode jsonPoll, JpaVotingPoll jpaVotingPoll) {
        assertThat(jsonPoll.get("pollOptions").size(), equalTo(jpaVotingPoll.getOptions().size()));
        jsonPoll.get("pollOptions").forEach(jo -> assertHasPollOption(jo, jpaVotingPoll));
    }

    private static void assertHasPollOption(JsonNode jsonPollOption, JpaVotingPoll jpaVotingPoll) {
        String jsonOptionName = jsonPollOption.get("name").asText();
        List<JpaVotingPollOption> pollOptionsWithName = jpaVotingPoll.getOptions().stream()
                .filter(jo -> jo.getName().equals(jsonOptionName))
                .collect(Collectors.toList());

        assertThat(pollOptionsWithName, hasSize(1));
        JpaVotingPollOption jpaVotingPollOption = pollOptionsWithName.get(0);

        assertThat(jpaVotingPollOption.getCode(), equalTo(jsonPollOption.get("code").asInt()));
    }
}
