import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import za.ac.wits.snake.DevelopmentAgent;
@SuppressWarnings("access")
public class MyAgent extends DevelopmentAgent {

    public static void main(String args[]) throws IOException {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    Board board;

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

    private int getStep(Snake snake, Point target) {
        //TODO: Make this not stupid (more stupid?)
        return getSimpleMovement(snake, target);
    }

    private ArrayList<Point> getAStarPath(Snake snake, Point target) {
        Point head = snake.corners[0];

        ArrayList<Point> closed = new ArrayList<>();
        ArrayList<Point> open = new ArrayList<>();
        open.add(head);

        HashMap<Point, Point> parents = new HashMap<>();
        HashMap<Point, Double> gScores = new HashMap<>();
        HashMap<Point, Double> fScores = new HashMap<>();
        gScores.put(head, .0);
        fScores.put(head, calcCost(head, target));

        while (!open.isEmpty()) {
            Point current = findLowest(open, fScores);
            if (current.equals(target)) {
                return traceBack(parents, target);
            }

            open.remove(current);
            closed.add(current);

            ArrayList<Point> neighbours = getNeighbours(current);
            double gScore = gScores.get(current) + 1;
            for (Point n : neighbours) {
                if (closed.contains(n)) {
                    continue;
                }

                if (!open.contains(n)) {
                    open.add(n);
                } else if (gScore >= gScores.get(n)) {
                    continue;
                }

                parents.put(n, current);
                gScores.put(n, gScore);
                fScores.put(n, gScore + calcCost(n, target));
            }
        }
        log("no path");
        return new ArrayList<>();
    }

    private ArrayList<Point> traceBack(HashMap<Point,Point> cameFrom, Point target) {
        ArrayList<Point> path = new ArrayList<>();
        Point current = target;
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    private ArrayList<Point> getNeighbours(Point p) {
        ArrayList<Point> points = new ArrayList<>();
        if (p.x > 0 && (board.at(p.x-1, p.y).owner == 0)) {
            points.add(new Point(p.x - 1, p.y));
        }
        if (p.x < board.width - 1 && (board.at(p.x+1, p.y).owner == 0)) {
            points.add(new Point(p.x + 1, p.y));
        }
        if (p.y > 0 && (board.at(p.x, p.y-1).owner == 0)) {
            points.add(new Point(p.x , p.y - 1));
        }
        if (p.y < board.height - 1 && (board.at(p.x, p.y+1).owner == 0)) {
            points.add(new Point(p.x, p.y + 1));
        }
        return points;
    }

    private Point findLowest(ArrayList<Point> set, HashMap<Point, Double> scores) {
        Point best = set.get(0);
        for (Point p : set) {
            if (scores.get(p) < scores.get(best)) {
                best = p;
            }
        }
        return best;
    }

    private double calcCost(Point start, Point end) {

        double cost;
        cost = getDistance(start, end);

        return cost;
    }

    private double getDistance(Point start, Point end) {
         return Math.abs(start.x - end.x) + Math.abs(start.y - end.y);
        //return Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.y - end.y)*(start.y - end.y));
    }

}