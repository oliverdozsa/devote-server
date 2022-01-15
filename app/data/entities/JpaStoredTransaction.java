package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
        name = "stored_transaction",
        indexes = {
                @Index(name = "ix_signature_footprint", columnList = "signature_footprint")
        }
)
public class JpaStoredTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "signature")
    @Lob
    private String signature;

    @Column(name = "transaction")
    @Lob
    private String transaction;

    @Column(name = "signature_footprint")
    private String signatureFootPrint;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getSignatureFootPrint() {
        return signatureFootPrint;
    }

    public void setSignatureFootPrint(String signatureFootPrint) {
        this.signatureFootPrint = signatureFootPrint;
    }

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }
}
