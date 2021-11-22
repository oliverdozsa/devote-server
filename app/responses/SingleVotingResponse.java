package responses;

public class SingleVotingResponse {
    private Long id;
    private String network;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return "SingleVotingResponse{" +
                "id=" + id +
                ", network='" + network + '\'' +
                '}';
    }
}
