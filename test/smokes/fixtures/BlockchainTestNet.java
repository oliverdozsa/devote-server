package smokes.fixtures;

import devote.blockchain.api.Account;
import play.libs.ws.WSClient;

public interface BlockchainTestNet {
    Account createAccountWithBalance(long balance);
}
