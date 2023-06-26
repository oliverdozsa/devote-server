package data.entities.voting;

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
                @UniqueConstraint(columnNames = {"voting_id", "voter_id"})
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

    @ManyToOne
    @JoinColumn(name = "voter_id")
    private JpaVoter voter;

    @Column(name = "envelope_signature")
    @Lob
    private String envelopeSignature;

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
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

    public JpaVoter getVoter() {
        return voter;
    }

    public void setVoter(JpaVoter voter) {
        this.voter = voter;
    }
}
