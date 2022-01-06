package data.repositories;

import data.entities.JpaVoting;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.KeyPair;
import requests.CreateVotingRequest;

import java.util.List;

public interface VotingRepository {
    Long initialize(CreateVotingRequest request);
    JpaVoting single(Long id);
    void issuerAccountsCreated(Long id, List<KeyPair> keyPairs);
    void channelAccountCreated(Long id, List<KeyPair> keyPairs);
    void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccount.TransactionResult transactionResult);
    void votingSavedToIpfs(Long id, String ipfsCid);
}
