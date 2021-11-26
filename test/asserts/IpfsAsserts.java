package asserts;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaVoting;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import io.ipfs.cid.Cid;
import io.ipfs.multiaddr.MultiAddress;
import play.libs.Json;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class IpfsAsserts {
    private static final IPFS ipfs = new IPFS(new MultiAddress("/ip4/127.0.0.1/tcp/5001"));

    public static void assertVotingSavedToIpfs(Long votingId) throws IOException {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        Cid cid = Cid.decode(voting.getIpfsCid());
        byte[] content = ipfs.dag.get(cid);

        assertThat(content, notNullValue());
        assertThat(content.length, greaterThan(0));

        String ipfsVotingJsonStr = new String(content);
        System.out.println(ipfsVotingJsonStr);
        JsonNode ipfsVotingJson = Json.parse(ipfsVotingJsonStr);

        assertThat(ipfsVotingJson.get("network"), equalTo(voting.getNetwork()));
    }
}
