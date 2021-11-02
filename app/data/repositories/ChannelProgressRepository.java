package data.repositories;

import data.entities.JpaChannelAccountProgress;

import java.util.List;

public interface ChannelProgressRepository {
    void channelProgressRepository(Long id, int numOfAccountsCreated);
    List<JpaChannelAccountProgress> notFinishedSampleOf(int sampleSize);
}
