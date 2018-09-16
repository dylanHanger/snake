import java.util.ArrayList;

public class Species {

    int popSize;
    ArrayList<Genome> genomes;
    double sumNormalFitness;
    int extinctionCounter;
    Genome baselineGenome;

    public Species() {
        popSize = 0;
        genomes = new ArrayList<>();
        sumNormalFitness = 0;
        extinctionCounter = 0;
        baselineGenome = new Genome();
    }

    // TODO: Call this for every species
    public void normalizeFitness() {
        sumNormalFitness = 0;
        for (Genome g : genomes) {
            g.normalFitness = g.fitness/genomes.size();
            sumNormalFitness += g.normalFitness;
        }
    }
}
