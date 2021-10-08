package blockchain;

import play.Logger;

public class BlockhainDiscovery {
    private static final Logger.ALogger logger = Logger.of(BlockhainDiscovery.class);

    public BlockhainDiscovery() {
        logger.info("BlockhainDiscovery(): started discovery of supported blockchains");
    }
}
