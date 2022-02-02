package data.repositories;

import data.entities.JpaVoting;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.KeyPair;
import requests.CreateVotingRequest;

import java.util.List;

public interface VotingRepository {
    Long initialize(CreateVotingRequest request);
    JpaVoting single(Long id);
    void issuerAccountsCreated(Long id, List<Issuer> issuers);
    void channelAccountCreated(Long id, List<KeyPair> keyPairs);
    void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccountOperation.TransactionResult transactionResult);
    void votingSavedToIpfs(Long id, String ipfsCid);
}
