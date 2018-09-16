import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

// TODO: https://medium.com/@savas/craig-using-neural-networks-to-learn-mario-a76036b639ad
// TODO: https://github.com/joenot443/crAIg/tree/master/NEAT
public class NEATAgent extends DevelopmentAgent {

    public static void main(String args[]) {
        NEATAgent agent = new NEATAgent();
        NEATAgent.start(agent, args);
    }

    int w;
    int h;

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
            w = Integer.parseInt(temp[1]);
            h = Integer.parseInt(temp[2]);

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }
                String[] apple = line.split(" ");
                //do stuff with apple
                int mySnakeNum = Integer.parseInt(br.readLine());
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (i == mySnakeNum) {
                        //hey! That's me :)
                    }
                    //do stuff with snakes
                }
                // finished reading, calculate move:
                // System.out.println("log calculating...");
                int move = new Random().nextInt(4);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<Species> epoch;

    double totalNormalFitness;

    void train() {
        totalNormalFitness = calcTotalNormalFitness();
    }

    int countWeaklings(Species species) {
        return (int) Math.floor(species.sumNormalFitness / totalNormalFitness) - 1;
    }

    double calcTotalNormalFitness() {
        double totalFitness = 0;
        for (Species s : epoch) {
            s.normalizeFitness();
            totalFitness += s.sumNormalFitness;
        }
        return totalFitness;
    }
}