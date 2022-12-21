package smokes.fixtures;

import devote.blockchain.api.Account;

public interface BlockchainTestNet {
    Account createAccountWithBalance(long balance);
}
