package data.repositories;

import data.entities.JpaVoting;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.api.DistributionAndBallotAccountOperation;
import galactic.blockchain.api.Account;
import requests.CreateVotingRequest;

import java.util.List;
import java.util.Optional;

public interface VotingRepository {
    Long initialize(CreateVotingRequest request, String assetCode, String userId);
    JpaVoting single(Long id);
    void channelGeneratorsCreated(Long id, List<ChannelGenerator> channelGenerators);
    void channelAccountCreated(Long id, List<Account> accounts);
    void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccountOperation.TransactionResult transactionResult);
    void votingSavedToIpfs(Long id, String ipfsCid);
    List<JpaVoting> notInitializedSampleOf(int size);
    Optional<JpaVoting> findANotRefundedEndedVoting();
    void distributionAccountRefunded(Long id);
    void internalFundingAccountCreated(Long id, Account funding);
    void internalFundingAccountRefunded(Long id);
    Optional<JpaVoting> findOneWithAuthTokenNeedsToBeCreated();
    void allAuthTokensCreated(Long id);
}
