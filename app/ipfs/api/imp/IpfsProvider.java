package ipfs.api.imp;

import com.typesafe.config.Config;
import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;

import javax.inject.Inject;
import javax.inject.Provider;

public class IpfsProvider implements Provider<IPFS> {
    private final Config config;

    @Inject
    public IpfsProvider(Config config) {
        this.config = config;
    }

    @Override
    public IPFS get() {
        String ipfsNodeAddress = config.getString("galactic.vote.ipfs.node.address");
        return new IPFS(new MultiAddress(ipfsNodeAddress));
    }
}
