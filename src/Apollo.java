import com.sun.istack.internal.NotNull;
import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Direction {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 2;
    public static final int EAST = 3;
    public static final int LEFT = 4;
    public static final int RIGHT = 5;
    public static final int FORWARD = 6;

    public static int relativeTo(Point a, Point b) {
        if (a.y < b.y) {
            return NORTH;
        } else if (a.y > b.y) {
            return SOUTH;
        }else if (a.x < b.x) {
            return WEST;
        } else {
            return EAST;
        }
    }

    public static int getHeading(Snake snake){
        Point head = snake.corners[0];
        Point neck = snake.corners[1];

        if (head.x == neck.x) {
            if (head.y < neck.y) {
                // up
                return NORTH;
            } else {
                // down
                return SOUTH;
            }
        } else if (head.y == neck.y) {
            if (head.x > neck.x) {
                // right
                return EAST;
            } else {
                // left
                return WEST;
            }
        }
        return FORWARD;
    }
}

class Point {
    public int x;
    public int y;
    public boolean occupied;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point(String[] data) {
        this.x = Integer.parseInt(data[0]);
        this.y = Integer.parseInt(data[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || getClass() != o.getClass()) return false;
        Point pt = (Point)o;
        if (pt.x != this.x) return false;
        if (pt.y != this.y) return false;
        return true;
    }

    @Override
    public int hashCode() {
        // Cantor's
        return ((x + y)*(x + y + 1)/2) + y;
    }

    public String toString(){
        return "("+x+", "+y+")";
    }
}

class Snake {
    public boolean alive;
    public int id;
    public int length;
    public int kills;
    public Point[] corners;

    public Snake(int id, String[] data) {
        corners = new Point[data.length - 3];
        this.id = id;
        alive = data[0].equals("alive");
        length = Integer.parseInt(data[1]);
        kills = Integer.parseInt(data[2]);

        for (int i = 3; i < data.length; i++) {
            corners[i-3] = new Point(data[i].split(","));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || getClass() != o.getClass()) return false;
        Snake other = (Snake)o;
        if (other.id != this.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

public class Apollo extends DevelopmentAgent {

    Point[][] grid;

    public static void main(String args[])  {
        Apollo agent = new Apollo();
        Apollo.start(agent, args);
    }

    void log(String msg) {
        System.out.println("log "+msg);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);

            int w = Integer.parseInt(temp[1]);
            int h = Integer.parseInt(temp[2]);
            grid = new Point[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    grid[y][x] = new Point(x, y);
                }
            }

            Point lastApple = null;
            int appleScore = 50;

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                // do stuff with apple
                Point apple = new Point(line.split(" "));
                if (apple.equals(lastApple)) {
                    appleScore -= 1;
                } else {
                    appleScore = 50;
                    lastApple = apple;
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                Snake me = null;
                Point myHead = null;
                Point myTail = null;
                for (int i = 0; i < nSnakes; i++) {
                    String[] snakeData = br.readLine().split(" ");
                    Snake newSnake = new Snake(i+1, snakeData);

                    // add the snake onto the board
                    for (int k = 0; k < newSnake.corners.length - 1; k++) {
                        Point a = newSnake.corners[k];
                        Point b = newSnake.corners[k+1];

                        if (a.x == b.x) {
                            int minY = Math.min(a.y, b.y);
                            int maxY = Math.max(a.y, b.y);
                            for (int y = minY; y <= maxY; y++) {
                                grid[a.x][y].occupied = true;
                            }
                        } else if (a.y == b.y){
                            int minX = Math.min(a.x, b.x);
                            int maxX = Math.max(a.x, b.x);
                            for (int x = minX; x <= maxX; x++) {
                                grid[x][a.y].occupied = true;
                            }
                        }
                    }

                    if (i == mySnakeNum) {
                        // hey! That's me :)
                        me = newSnake;
                        myHead = newSnake.corners[0];
                        myTail = newSnake.corners[newSnake.corners.length - 1];
                    }
                    // do stuff with snakes
                }
                // finished reading, calculate move:
                System.out.println(getSimpleMovement(me, apple));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSimpleMovement(Snake snake, Point target) {
        Point head = snake.corners[0];
        int direction = Direction.relativeTo(target, head);
        switch(direction) {
            case Direction.NORTH:
                if (Direction.getHeading(snake) == Direction.SOUTH) return Direction.WEST; break;
            case Direction.SOUTH:
                if (Direction.getHeading(snake) == Direction.NORTH) return Direction.EAST; break;
            case Direction.EAST:
                if (Direction.getHeading(snake) == Direction.WEST) return Direction.NORTH; break;
            case Direction.WEST:
                if (Direction.getHeading(snake) == Direction.EAST) return Direction.SOUTH; break;
        }
        return direction;
    }

}
