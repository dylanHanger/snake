import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Apollo {


    void log(String msg) {
        System.out.println("log "+msg);
    }

    int appleScore;

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);

            int w = Integer.parseInt(temp[1]);
            int h = Integer.parseInt(temp[2]);
            board = new Board(w, h);

            Point lastApple = null;
            appleScore = 50;

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    log("Game over");
                    break;
                }

                //do stuff with apple
                Point apple = new Point(line.split(" "));
                if (apple.equals(lastApple)) {
                    appleScore -= 1;
                } else {
                    appleScore = 50;
                    lastApple = apple;
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                Snake me = null;
                for (int i = 0; i < nSnakes; i++) {
                    String[] snakeData = br.readLine().split(" ");
                    Snake newSnake = new Snake(i+1, snakeData);
                    if (i == mySnakeNum) {
                        //hey! That's me :)
                        me = newSnake;
                    }
                    //do stuff with snakes
                    board.addSnake(newSnake);
                }
                // finished reading, calculate move:
                ArrayList<Point> path = getAStarPath(me, apple);
                if(path.size() > 0) {
                    System.out.println(getStep(me, path.get(path.size() - 1)));
                } else {
                    System.out.println(getSimpleMovement(me, apple));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSimpleMovement(Snake snake, Point target) {
        Point head = snake.corners[0];
        int direction = target.relativeTo(head);
        switch(direction) {
            case 0:
                if (snake.getHeading() == 1) return 3; break;
            case 1:
                if (snake.getHeading() == 0) return 2; break;
            case 2:
                if (snake.getHeading() == 3) return 0; break;
            case 3:
                if (snake.getHeading() == 2) return 1; break;
        }
        return direction;
    }
}
