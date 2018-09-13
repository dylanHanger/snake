import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Random;

public class MachineLearningAgent extends DevelopmentAgent {

    public static void main(String args[]) {
        MachineLearningAgent agent = new MachineLearningAgent();
        MachineLearningAgent.start(agent, args);
    }

    Neuron[] inputs;

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

            Neuron[] inputs = new Neuron[w*h];
            Neuron[] hiddenLayer = new Neuron[10]; // idk, 10?
            Neuron[] outputs = new Neuron[4]; // TODO: this won't work, fix it. Should they even be neurons?

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


    // Inputs
        // The same input I am given? aka the coordinates of the corners of each snake, their kills and length. Different types??
        // 4 direction search + distance to features (apple, snake, wall)
        // Every single Point in the grid. (0,1) for obstacle and (1,0) for apple = 5000 inputs

    // Outputs
        // (1,0,0,0), (0,1,0,0), (0,0,1,0), (0,0,0,1) for N,S,W,E - complete gameplay
        // (x,y) the next target to path to - general strategy

}