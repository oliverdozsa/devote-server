package galactic.blockchain.api;

import java.util.Objects;

public class Account {
    public final String secret;
    public final String publik;

    public Account(String secret, String publik) {
        this.secret = secret;
        this.publik = publik;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return secret.equals(account.secret) && publik.equals(account.publik);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secret, publik);
    }
}
