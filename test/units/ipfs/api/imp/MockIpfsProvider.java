package units.ipfs.api.imp;

import io.ipfs.api.IPFS;

import javax.inject.Provider;

public class MockIpfsProvider implements Provider<IPFS> {
    @Override
    public IPFS get() {
        return null;
    }
}
