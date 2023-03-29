package smokes.fixtures;

import galactic.blockchain.api.Account;

public interface BlockchainTestNet {
    Account createAccountWithBalance(long balance);
}
