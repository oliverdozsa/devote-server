package smokes;

import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import devote.blockchain.api.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.stellar.sdk.KeyPair;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.mvc.Result;
import requests.CommissionCreateTransactionRequest;
import requests.CommissionInitRequest;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForTests;
import smokes.fixtures.BlockchainTestNet;
import smokes.fixtures.StellarBlockchainTestNet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static asserts.DbAsserts.assertThatTransactionIsStoredFor;
import static components.controllers.VotingRequestMaker.createValidVotingRequest;
import static components.extractors.CommissionResponseFromResult.*;
import static components.extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;

public class ConductVotingSmokeTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient testClient;
    private VotingTestClient votingTestClient;

    private static final String[] supportedNetworks = new String[]{
            "stellar"
    };

    private static Map<String, BlockchainTestNet> testNets = new HashMap<>();

    public ConductVotingSmokeTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides((bind(JwtVerification.class).to(JwtVerificationForTests.class)));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() {
        testClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());

        initTestNets();
    }

    @Test
    public void testVotingOnNetworks() throws InterruptedException {
        for (String network : supportedNetworks) {
            testVotingOnNetwork(network);
        }
    }

    private void initTestNets() {
        WSClient wsClient = ruleChainForTests.getApplication().injector().instanceOf(WSClient.class);

        StellarBlockchainTestNet stellarTestNet = new StellarBlockchainTestNet(wsClient);
        testNets.put("stellar", stellarTestNet);
    }

    private void testVotingOnNetwork(String networkName) throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob", networkName);
        Thread.sleep(45 * 1000); // So that some channel accounts are present.

        String message = createMessage(votingInitData.votingId, generateVoterPublic(networkName));

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Bob", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionCreateTransactionRequest accountCreationRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(transactionOf(accountCreationRequestResult), notNullValue());
        assertThat(transactionOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());

        // When
        Result transactionOfSignatureResult = testClient.transactionOfSignature(accountCreationRequest.getRevealedSignatureBase64());

        // Then
        assertThat(statusOf(transactionOfSignatureResult), equalTo(OK));
        assertThat(transactionOfSignature(transactionOfSignatureResult), notNullValue());
    }

    private InitData initVotingFor(String userId, String network) throws InterruptedException {
        // Given
        String votingId = createValidVoting(network);

        // So that voting is initialized on blockchain.
        Thread.sleep(30 * 1000);
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest, userId);

        // Then
        assertThat(statusOf(result), equalTo(OK));

        InitData initData = new InitData();
        initData.votingId = votingId;
        initData.publicKey = publicKeyOf(result);

        return initData;
    }

    private String createValidVoting(String network) {
        Account funding = createFundingAccountIn(network, 10000);

        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("bob@mail.com", "doe@mail.come", "some@mail.com"));
        createVotingRequest.setNetwork(network);
        createVotingRequest.setVotesCap(168L);
        createVotingRequest.setFundingAccountPublic(funding.publik);
        createVotingRequest.setFundingAccountSecret(funding.secret);
        createVotingRequest.setUseTestnet(true);

        Result result = votingTestClient.createVoting(createVotingRequest, "Alice", "alice@mail.com");
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        String[] locationUrlParts = locationUrl.split("/");
        String votingId = locationUrlParts[locationUrlParts.length - 1];

        return votingId;
    }

    private String createMessage(String votingId, String voterPublicAccountId) {
        return votingId + "|" + voterPublicAccountId;
    }

    private Account createFundingAccountIn(String network, long withBalance) {
        BlockchainTestNet testNet = testNets.get(network);
        return testNet.createAccountWithBalance(withBalance);
    }

    private String generateVoterPublic(String network) {
        if (network.equals("stellar")) {
            return KeyPair.random().getAccountId();
        }

        return "someAccount";
    }

    private static class InitData {
        public String votingId;
        public String publicKey;
    }
}
