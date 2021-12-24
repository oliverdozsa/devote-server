package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = "commission_session",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"voting_id", "user_id"})
        }
)
public class JpaCommissionSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "envelope_signature")
    @Lob
    private String envelopeSignature;

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnvelopeSignature() {
        return envelopeSignature;
    }

    public void setEnvelopeSignature(String envelopeSignature) {
        this.envelopeSignature = envelopeSignature;
    }
}
