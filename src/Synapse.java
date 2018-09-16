public class Synapse {
    // A connection between 2 neurons

    int innovationNumber;
    int toNeuron;
    int fromNeuron;
    double weight;
    boolean enabled;

    public Synapse(int innovationNumber) {
        this.innovationNumber = innovationNumber;
        toNeuron = 0;
        fromNeuron = 0;
        weight = 0.0;
        enabled = true;
    }
}
