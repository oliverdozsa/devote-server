package data.repositories;

import data.entities.JpaVoting;
import devote.blockchain.api.DistributionAndBallotAccount;
import dto.CreateVotingRequest;

import java.util.List;

public interface VotingRepository {
    Long initialize(CreateVotingRequest request);
    JpaVoting single(Long id);
    void issuerAccountsCreated(Long id, List<String> accounts);
    void channelAccountCreated(Long id, List<String> accounts);
    void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccount.TransactionResult transactionResult);
}
