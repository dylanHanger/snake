import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class Direction {
    public static final int NONE = -1;
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

    public static int opposite(int direction) {
        switch (direction) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: return direction;
        }
    }
}

class Point implements Comparable<Point> {
    public int x = -1;
    public int y = -1;
    public int owner = 0;

    // Path-finding stuff
    public double fScore = -1;
    public double gScore = -1;
    public double hScore = -1;

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

    @Override
    public int compareTo(Point point) {
        return Double.compare(this.fScore, point.fScore);
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

    Point head;
    Point neck;
    Point bum;
    Point tail;

    public Snake(int id, String[] data) {
        corners = new Point[data.length - 3];
        this.id = id;
        alive = data[0].equals("alive");
        length = Integer.parseInt(data[1]);
        kills = Integer.parseInt(data[2]);

        for (int i = 3; i < data.length; i++) {
            corners[i-3] = new Point(data[i].split(","));
        }

        if (!alive) return;
        head = corners[0];
        neck = corners[1];
        bum = corners[corners.length - 2];
        tail = corners[corners.length - 1];
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

    int nSnakes = 0;
    int mySnakeNum = 0;
    Snake me;
    Snake[] snakes;

    int w;
    int h;
    Point[][] grid;

    Point apple;

    public static void main(String args[])  {
        Apollo agent = new Apollo();
        Apollo.start(agent, args);
    }

    void log(Object msg) {
        System.out.println("log "+msg);
    }

    int turn = 0;
    int lastMove;

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");

            nSnakes = Integer.parseInt(temp[0]);

            snakes = new Snake[nSnakes];

            w = Integer.parseInt(temp[1]);
            h = Integer.parseInt(temp[2]);
            grid = new Point[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    grid[y][x] = new Point(x, y);
                }
            }

            Point lastApple = null;
            int appleScore = 30;

            Point center = new Point(w/2, h/2);

            while (true) {
                turn++;
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                // do stuff with apple
                apple = new Point(line.split(" "));
                if (apple.equals(lastApple)) {
                    appleScore -= 1;
                } else {
                    appleScore = 30;
                    lastApple = apple;
                }

                // reset the board
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        Point p = grid[y][x];
                        p.owner = 0;
                        p.fScore = -1;
                        p.gScore = -1;
                    }
                }

                mySnakeNum = Integer.parseInt(br.readLine());
                Snake me = null;
                for (int i = 0; i < nSnakes; i++) {
                    String[] snakeData = br.readLine().split(" ");
                    Snake newSnake = new Snake(i, snakeData);
                    snakes[i] = newSnake;
                    // add the snake onto the board
                    for (int k = 0; k < newSnake.corners.length - 1; k++) {
                        Point a = newSnake.corners[k];
                        Point b = newSnake.corners[k+1];

                        if (a.x == b.x) {
                            int minY = Math.min(a.y, b.y);
                            int maxY = Math.max(a.y, b.y);
                            for (int y = minY; y <= maxY; y++) {
                                grid[y][a.x].owner = i;
                            }
                        } else if (a.y == b.y){
                            int minX = Math.min(a.x, b.x);
                            int maxX = Math.max(a.x, b.x);
                            for (int x = minX; x <= maxX; x++) {
                                grid[a.y][x].owner = i;
                            }
                        }
                    }

                    if (i == mySnakeNum) {
                        // hey! That's me :)
                        me = newSnake;
                    }
                    // do stuff with snakes
                }
                // finished reading, calculate move:

                int[] move = getAStarMovement(me.head, apple);
                if (move[0] != Direction.NONE && move[1] < appleScore) {
                    lastMove = move[0];
                    System.out.println(move[0]);
                } else {
                    int i = 0;
                    int newMove = lastMove;
                    if (me.alive && !isLegalMove(newMove)) {
                        lastMove = Direction.opposite(lastMove);
                        setDirection(newMove);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSimpleMovement(Point target) {
        Point head = me.corners[0];
        int direction = Direction.relativeTo(target, head);
        switch(direction) {
            case Direction.NORTH:
                if (Direction.getHeading(me) == Direction.SOUTH) return Direction.WEST; break;
            case Direction.SOUTH:
                if (Direction.getHeading(me) == Direction.NORTH) return Direction.EAST; break;
            case Direction.EAST:
                if (Direction.getHeading(me) == Direction.WEST) return Direction.NORTH; break;
            case Direction.WEST:
                if (Direction.getHeading(me) == Direction.EAST) return Direction.SOUTH; break;
        }
        return direction;
    }

    private void getSimpleMovementWithAvoidance(Point target) {
        int direction = getSimpleMovement(target);
        setDirection(direction);
    }

    private int[] getAStarMovement(Point start, Point target) {
        // Do A* path finding to find the next move
        ArrayList<Point> open = new ArrayList<>();
        ArrayList<Point> closed = new ArrayList<>();

        HashMap<Point, Point> cameFrom = new HashMap<>();

        start.gScore = 0;
        open.add(start);

        while(open.size() > 0) {

            // Find the lowest fScore in the open set
            Point current = open.get(0);
            for (Point p : open) {
                if ((p.gScore + p.hScore) < (current.gScore + current.hScore)) {
                    current = p;
                }
            }

            if (current.equals(target)) {
                break;
            }

            open.remove(current);
            closed.add(current);

            double gScore = current.gScore + 1;
            for (Point n : getNeighbours(current)) {

                if (closed.contains(n)) {
                    continue;
                }

                if (!open.contains(n)) {
                    open.add(n);
                } else if (n.gScore <= gScore) {
                    continue;
                }

                n.hScore = estimateCost(n, apple);
//                for (int i = 0; i < nSnakes; i++) {
//                    if (i == mySnakeNum || !snakes[i].alive)
//                        continue;
//                    n.hScore += 100 / estimateCost(n, snakes[i].head);
//                }
                n.gScore = gScore;
                cameFrom.put(n, current);
            }

        }

        Point nextPoint = apple;
        int distance = 0;
        while (nextPoint != null && cameFrom.get(nextPoint) != start) {
            distance += 1;
            nextPoint = cameFrom.get(nextPoint);
        }

        int[] move = new int[2];
        move[1] = distance;

        if (nextPoint == null) {
            // we failed to find a path
            move[0] = Direction.NONE;
        } else if (nextPoint.x > start.x) {
            move[0] = Direction.EAST;
        } else if (nextPoint.x < start.x){
            move[0] = Direction.WEST;
        } else if (nextPoint.y > start.y) {
            move[0] = Direction.SOUTH;
        } else {
            move[0] = Direction.NORTH;
        }
        return move;
    }

    private Snake getClosestSnake(Point start) {
        Snake closest = snakes[0];
        for (Snake snake : snakes) {
            if (!snake.alive) continue;
            if (estimateCost(snake.head, start) < estimateCost(closest.head, start)) {
                closest = snake;
            }
        }
        return closest;
    }

    private boolean isLegalMove(int direction) {
        if (me == null || !me.alive) return false;
        Point head = me.head;

        if (direction == Direction.FORWARD) {
            direction = Direction.getHeading(me);
        }

        switch (direction) {
            case Direction.NORTH:
                return (head.y > 0) && (grid[head.y - 1][head.x].owner == 0);
            case Direction.SOUTH:
                return (head.y < h - 1) && (grid[head.y + 1][head.x].owner == 0);
            case Direction.EAST:
                return (head.x < w - 1) && (grid[head.y][head.x + 1].owner == 0);
            case Direction.WEST:
                return (head.x > 0) && (grid[head.y][head.x - 1].owner == 0);
            default: return true;
        }
    }

    private void setDirection(int direction) {
        if (me == null || !me.alive) {
            log("Null or dead");
            return;
        }
        if (!isLegalMove(direction)) {
            log("Illegal move: "+direction);
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                if (me.head.x < w/2) {
                    if (isLegalMove(Direction.EAST)) {
                        direction = Direction.EAST;
                    } else {
                        direction = Direction.WEST;
                    }
                } else {
                    if (isLegalMove(Direction.WEST)) {
                        direction = Direction.WEST;
                    } else {
                        direction = Direction.EAST;
                    }
                }
            } else if (direction == Direction.WEST || direction == Direction.EAST) {
                if (me.head.y < h/2) {
                    if (isLegalMove(Direction.SOUTH)) {
                        direction = Direction.SOUTH;
                    } else {
                        direction = Direction.NORTH;
                    }
                } else {
                    if (isLegalMove(Direction.NORTH)) {
                        direction = Direction.NORTH;
                    } else {
                        direction = Direction.SOUTH;
                    }
                }
            }
        }
        System.out.println(direction);
    }

    private ArrayList<Point> getNeighbours(Point point){
        ArrayList<Point> neighbours = new ArrayList<>();
        if (point.x > 0 && grid[point.y][point.x - 1].owner == 0) {
            neighbours.add(grid[point.y][point.x - 1]);
        }
        if (point.x < w - 1 && grid[point.y][point.x + 1].owner == 0) {
            neighbours.add(grid[point.y][point.x + 1]);
        }
        if (point.y > 0 && grid[point.y - 1][point.x].owner == 0) {
            neighbours.add(grid[point.y - 1][point.x]);
        }
        if (point.y < h - 1 && grid[point.y + 1][point.x].owner == 0) {
            neighbours.add(grid[point.y + 1][point.x]);
        }
        return neighbours;
    }

    private double estimateCost(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        //return Math.sqrt((point.x - apple.x)*(point.x - apple.x) + (point.y - apple.y)*(point.y - apple.y));
    }
}
