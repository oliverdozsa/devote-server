package data.repositories;

import data.entities.JpaVoting;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Account;
import requests.CreateVotingRequest;

import java.util.List;

public interface VotingRepository {
    Long initialize(CreateVotingRequest request);
    JpaVoting single(Long id);
    void channelGeneratorsCreated(Long id, List<ChannelGenerator> issuers);
    void channelAccountCreated(Long id, List<Account> accounts);
    void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccountOperation.TransactionResult transactionResult, String assetCode);
    void votingSavedToIpfs(Long id, String ipfsCid);
}
