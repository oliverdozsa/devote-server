package data.repositories.imp;

import data.entities.JpaChannelAccountProgress;
import data.repositories.ChannelProgressRepository;

import java.util.List;

public class EbeanChannelProgressRepository implements ChannelProgressRepository {
    @Override
    public void channelProgressRepository(Long id, int numOfAccountsCreated) {
        // TODO
    }

    @Override
    public List<JpaChannelAccountProgress> notFinishedSampleOf(int sampleSize) {
        // TODO
        return null;
    }
}
