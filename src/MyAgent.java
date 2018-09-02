import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MyAgent extends DevelopmentAgent {

    // Representation of map
    int w, h;
    int[][] map;

    // snakes
    int nSnakes;
    Snake[] snakes;
    Snake me;

    // apple
    Point apple;
    Point lastApple;
    int appleScore;

    // positioning
    Snake lastMe;
    int lastMove;

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    static void log(Object msg) {
        System.out.println("log "+msg);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            nSnakes = Integer.parseInt(temp[0]);
            w = Integer.parseInt(temp[1]);
            h = Integer.parseInt(temp[2]);
            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                //do stuff with apple
                apple = new Point(line);
                if (apple.equals(lastApple)) {
                    appleScore--;
                } else {
                    lastApple = apple;
                    appleScore = 30;
                }

                // read snakes in
                snakes = new Snake[nSnakes];
                int mySnakeNum = Integer.parseInt(br.readLine());
                for (int i = 0; i < nSnakes; i++) {
                    String[] snakeData = br.readLine().split(" ");
                    Snake newSnake = new Snake(i+1, snakeData);
                    if (i == mySnakeNum) {
                        me = newSnake;
                    }
                    //do stuff with snakes
                    snakes[i] = newSnake;
                }

                // debugging deaths
                if(lastMe != null && me.head.distanceTo(lastMe.head) > 1) {
                    log("I died at "+lastMe.head);
                    log("I was trying to move "+Direction.toString(lastMove) + " into "+getNextHead(lastMe, lastMove));
                    for (int x = Math.max(0, lastMe.head.x - 1); x < Math.min(w, lastMe.head.x + 2); x++) {
                        for (int y = Math.max(0, lastMe.head.y - 1); y < Math.min(h, lastMe.head.y + 2); y++) {
                            log(new Point(x,y) + ": " +map[y][x]+" ("+(isSafeMove(lastMe, lastMove) ? "safe" : "danger") +")");
                        }
                    }
                    for (int y = lastMe.head.y - 2; y < lastMe.head.y + 3; y++){
                        StringBuilder row = new StringBuilder();
                        for (int x = 0; x < w; x++) {
                            row.append(""+map[y][x]);
                        }
                        log(row.toString());
                    }
                    log("Respawned");
                }

                // reset the map
                map = new int[h][w];
                for (int i = 0; i < nSnakes; i++) {
                    for (int j = 0; j < snakes[i].body.length-1; j++) {
                        Point a = snakes[i].body[j];
                        Point b = snakes[i].body[j+1];
                        if (a.x == b.x) {
                            for (int y = Math.min(a.y, b.y); y <= Math.max(a.y, b.y); y++) {
                                map[y][a.x] = i+1;
                            }
                        } else {
                            for (int x = Math.min(a.x, b.x); x <= Math.max(a.x, b.x); x++) {
                                map[a.y][x] = i+1;
                            }
                        }
                    }
                }

                lastMe = me;

                // finished reading, calculate move:
                Point center = new Point(w/2, h/2);

                int[] move = getAStarMovement(me, apple);

                if (move[0] == Direction.NONE || move[1] > appleScore || getClosestSnakeID(apple) != me.id) {
                    move[0] = getSweepMovement(me, me.head.directionTo(center));
                }


                // ensure our final move is safe and/or legal and output it
                move[0] = makeSafe(me, move[0]);
                if (!isSafeMove(me, move[0])) {
                    move[0] = makeLegal(me, move[0]);
                }
                lastMove = move[0];
                log("Moved "+Direction.toString(move[0]));
                System.out.println(move[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getClosestSnakeID(Point point) {
        double distance = point.distanceTo(me.head);
        int closestID = me.id;
        for (int i = 0; i < nSnakes; i++) {
            if (!snakes[i].alive || snakes[i] == me) continue;
            double d = point.distanceTo(snakes[i].head);
            if (d < distance) {
                distance = d;
                closestID = snakes[i].id;
            }
        }
        return closestID;
    }

    private int makeSafe(Snake snake, int direction) {
        int i = 0;
        while (!isSafeMove(snake, direction) && i++ < 4) {
            direction = Direction.next(direction);
        }
        return direction;
    }

    private boolean isSafeMove(Snake snake, int direction) {
        if (!isLegalMove(snake, direction)) {
            return false;
        }
        Point p = getNextHead(snake, direction);

        // FIXME: This is not working. Should never return true if there is a snake head adjacent to p
        for (Snake s : snakes) {
            if ((!s.alive) || (s == snake)) {
                continue;
            }
            if (p.distanceTo(s.head) == 1.0) {
                return false;
            }
        }
        return true;
    }

    private int makeLegal(Snake snake, int direction) {
        int i = 0;
        while (!isLegalMove(snake, direction) && i++ < 4) {
            direction = Direction.next(direction);
        }
        return direction;
    }

    private boolean isLegalMove(Snake snake, int direction) {
        Point head = snake.head;

        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = snake.getHeading();
        }

        switch (direction) {
            case Direction.NORTH:
                return (head.y > 0) && (map[head.y - 1][head.x] == 0);
            case Direction.SOUTH:
                return (head.y < h - 1) && (map[head.y + 1][head.x] == 0);
            case Direction.EAST:
                return (head.x < w - 1) && (map[head.y][head.x + 1] == 0);
            case Direction.WEST:
                return (head.x > 0) && (map[head.y][head.x - 1] == 0);
            default: return false;
        }
    }

    private Point getNextHead(Snake snake, int direction) {
        Point head = snake.head;
        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = snake.getHeading();
        }
        Point point;
        switch (direction) {
            case Direction.NORTH: point = new Point(head.x, head.y - 1); break;
            case Direction.SOUTH: point = new Point(head.x, head.y + 1); break;
            case Direction.EAST: point =  new Point(head.x + 1, head.y); break;
            case Direction.WEST: point =  new Point(head.x - 1, head.y); break;
            default: return snake.head;
        }
        return point;
    }

    private int getSimpleMovement(Snake snake, Point target) {
        Point head = snake.body[0];
        int direction = head.directionTo(target);
        switch(direction) {
            case Direction.NORTH:
                if (snake.getHeading() == Direction.SOUTH) return Direction.WEST; break;
            case Direction.SOUTH:
                if (snake.getHeading() == Direction.NORTH) return Direction.EAST; break;
            case Direction.EAST:
                if (snake.getHeading() == Direction.WEST) return Direction.NORTH; break;
            case Direction.WEST:
                if (snake.getHeading() == Direction.EAST) return Direction.SOUTH; break;
        }
        return direction;
    }

    private int getSweepMovement(Snake snake, int direction) {
        if (Direction.isVertical(direction)) {
            if(Direction.isVertical(snake.getHeading())) {
                return (snake.head.x > w/2) ? Direction.WEST : Direction.EAST;
            } else if (!isLegalMove(snake, Direction.FORWARD)) {
                return direction;
            }
        } else {
            if(!Direction.isVertical(snake.getHeading())) {
                return (snake.head.y > h/2) ? Direction.NORTH : Direction.SOUTH;
            } else if (!isLegalMove(snake, Direction.FORWARD)) {
                return direction;
            }
        }
        return Direction.FORWARD;
    }

    private int[] getAStarMovement(Snake snake, Point target) {
        ArrayList<Point> open = new ArrayList<>();
        ArrayList<Point> closed = new ArrayList<>();

        HashMap<Point, Integer> gScores = new HashMap<>();
        HashMap<Point, Double> fScores = new HashMap<>();

        HashMap<Point, Point> cameFrom = new HashMap<>();

        Point start = snake.head;
        open.add(start);
        gScores.put(start, 0);
        fScores.put(start, start.distanceTo(target));

        while (open.size() > 0) {
            Point current = open.get(0);
            for (Point p : open) {
                if (fScores.get(p) < fScores.get(current)) {
                    current = p;
                }
            }

            if (current.equals(target)){
                break;
            }

            open.remove(current);
            closed.add(current);

            int gScore = gScores.get(current) + 1;
            for (Point n : getNeighbours(current)) {
                if (closed.contains(n)) {
                    continue;
                }

                if (!open.contains(n)) {
                    open.add(n);
                } else if (gScores.get(n) < gScore) {
                    continue;
                }

                gScores.put(n, gScore);
                fScores.put(n, gScore + n.distanceTo(target));
                cameFrom.put(n, current);
            }
        }

        Point nextPoint = target;
        int[] result = new int[2];
        while (nextPoint != null && cameFrom.get(nextPoint) != start) {
            result[1]++;
            nextPoint = cameFrom.get(nextPoint);
        }

        if (nextPoint == null) {
            result[0] = Direction.NONE;
        } else if (nextPoint.x > start.x) {
            result[0] = Direction.EAST;
        } else if (nextPoint.x < start.x){
            result[0] = Direction.WEST;
        } else if (nextPoint.y > start.y) {
            result[0] = Direction.SOUTH;
        }
        // defaults to 0 (NORTH)
        return result;
    }

    private ArrayList<Point> getNeighbours(Point p) {
        ArrayList<Point> points = new ArrayList<>();
        if ((p.y > 0) && (map[p.y - 1][p.x] == 0)) {
            points.add(new Point(p.x, p.y - 1));
        }
        if ((p.x > 0) && (map[p.y][p.x - 1] == 0)) {
            points.add(new Point(p.x - 1, p.y));
        }
        if ((p.y < h - 1) && (map[p.y + 1][p.x] == 0)) {
            points.add(new Point(p.x, p.y + 1));
        }
        if ((p.x < w - 1) && (map[p.y][p.x + 1] == 0)) {
            points.add(new Point(p.x + 1, p.y));
        }
        return points;
    }
}
