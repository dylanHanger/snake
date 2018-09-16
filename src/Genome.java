import java.util.ArrayList;

public class Genome {
    // A representation of the neural network using only Synapses

    // 2500 inputs (each grid space), 3 outputs (L, R, F)
    ArrayList<Synapse> synapses;
    int newestNeuron;
    double fitness;
    double normalFitness;

    public Genome() {
        synapses = new ArrayList<>();
        // TODO: Relative or absolute movements better? I think relative
        newestNeuron = 2503;
        fitness = 0.0;
        normalFitness = 0.0;
    }

    public void calcFitness(int length, int kills, int turn) {
        fitness = (double)(length+kills)/turn;
    }
}
