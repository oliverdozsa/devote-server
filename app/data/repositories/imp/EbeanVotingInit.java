package data.repositories.imp;

import crypto.EncryptedVoting;
import data.entities.Authorization;
import data.entities.JpaVoting;
import data.entities.JpaVotingAuthorizationEmail;
import data.entities.JpaVotingAuthorizationKeybase;
import dto.CreateVotingRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

class EbeanVotingInit {
    public static JpaVoting initVotingFrom(CreateVotingRequest request) {
        JpaVoting voting = new JpaVoting();

        voting.setNetwork(request.getNetwork());
        voting.setVotesCap(request.getVotesCap());
        voting.setCreatedAt(Instant.now());

        setEncryption(request, voting);
        setAuthorization(request, voting);
        voting.setStartDate(request.getStartDate());
        voting.setEndDate(request.getEndDate());

        return voting;
    }

    private static JpaVoting fromRequest(CreateVotingRequest request) {
        JpaVoting entity = new JpaVoting();
        entity.setNetwork(request.getNetwork());
        entity.setVotesCap(request.getVotesCap());

        return entity;
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
}
