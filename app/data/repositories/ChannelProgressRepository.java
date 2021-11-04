package data.repositories;

import data.entities.JpaChannelAccountProgress;

import java.util.List;

public interface ChannelProgressRepository {
    void issuersCreated(Long votingId);
    void channelAccountsCreated(Long id, int numOfAccountsCreated);
    List<JpaChannelAccountProgress> notFinishedSampleOf(int sampleSize);
}
