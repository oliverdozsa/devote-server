package data.repositories.imp;

import crypto.EncryptedVoting;
import data.entities.Authorization;
import data.entities.JpaVoting;
import data.entities.JpaVotingAuthorizationEmail;
import data.entities.JpaVotingAuthorizationKeybase;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import data.entities.Visibility;
import requests.CreatePollOptionRequest;
import requests.CreatePollRequest;
import requests.CreateVotingRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class EbeanVotingInit {
    public static JpaVoting initVotingFrom(CreateVotingRequest request) {
        JpaVoting voting = new JpaVoting();

        voting.setNetwork(request.getNetwork());
        voting.setTitle(request.getTitle());
        voting.setVotesCap(request.getVotesCap());
        voting.setCreatedAt(Instant.now());
        voting.setFundingAccountPublic(request.getFundingAccountPublic());
        voting.setFundingAccountSecret(request.getFundingAccountSecret());

        setEncryption(request, voting);
        setAuthorization(request, voting);
        voting.setStartDate(request.getStartDate());
        voting.setEndDate(request.getEndDate());
        voting.setVisibility(Visibility.valueOf(request.getVisibility().name()));

        List<JpaVotingPoll> polls = request.getPolls().stream()
                .map(EbeanVotingInit::toVotingPoll)
                .collect(Collectors.toList());
        voting.setPolls(polls);

        return voting;
    }

    private static void setEncryption(CreateVotingRequest request, JpaVoting voting) {
        if (request.getEncryptedUntil() != null) {
            voting.setEncryptedUntil(request.getEncryptedUntil());
            voting.setEncryptionKey(EncryptedVoting.generateKey());
        }
    }

    private static void setAuthorization(CreateVotingRequest request, JpaVoting voting) {
        if (request.getAuthorization() == CreateVotingRequest.Authorization.EMAILS) {
            setAuthOptionEmails(request, voting);
        } else if (request.getAuthorization() == CreateVotingRequest.Authorization.KEYBASE) {
            setAuthOptionKeyBase(request, voting);
        }

        String authString = request.getAuthorization().name();
        voting.setAuthorization(Authorization.valueOf(authString));
    }

    private static void setAuthOptionEmails(CreateVotingRequest request, JpaVoting voting) {
        List<JpaVotingAuthorizationEmail> votingAuthorizationEmails =
                toVotingAuthorizationEmails(request.getAuthorizationEmailOptions());
        voting.setAuthOptionsEmails(votingAuthorizationEmails);
    }

    private static void setAuthOptionKeyBase(CreateVotingRequest request, JpaVoting voting) {
        JpaVotingAuthorizationKeybase votingAuthorizationKeybase = new JpaVotingAuthorizationKeybase();
        votingAuthorizationKeybase.setTeamName(request.getAuthorizationKeybaseOptions());
        voting.setAuthOptionKeybase(votingAuthorizationKeybase);
    }

    private static List<JpaVotingAuthorizationEmail> toVotingAuthorizationEmails(List<String> emails) {
        return emails.stream().map(email -> {
                    JpaVotingAuthorizationEmail votingAuthorizationEmail = new JpaVotingAuthorizationEmail();
                    votingAuthorizationEmail.setEmailAddress(email);
                    return votingAuthorizationEmail;
                })
                .collect(Collectors.toList());
    }

    public static JpaVotingPoll toVotingPoll(CreatePollRequest pollRequest) {
        JpaVotingPoll votingPoll = new JpaVotingPoll();
        votingPoll.setQuestion(pollRequest.getQuestion());

        List<JpaVotingPollOption> pollOptions = pollRequest.getOptions().stream()
                .map(EbeanVotingInit::toVotingPollOption)
                .collect(Collectors.toList());
        votingPoll.setOptions(pollOptions);

        return votingPoll;
    }

    private static JpaVotingPollOption toVotingPollOption(CreatePollOptionRequest pollOptionRequest) {
        JpaVotingPollOption pollOption = new JpaVotingPollOption();

        pollOption.setCode(pollOptionRequest.getCode());
        pollOption.setName(pollOptionRequest.getName());

        return pollOption;
    }

    private EbeanVotingInit() {
    }
}
