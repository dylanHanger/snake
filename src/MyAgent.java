import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MyAgent extends DevelopmentAgent {

    // misc
    private int turn;
    private long turnTime;

    // multi-turn logic
    private enum State {Attacking, Seeking, Protecting}
    private State state;
    private Snake victim;
    private int attackPhase;

    // Representation of map
    private int w, h;
    private int[][] map;
    private boolean[][] artMap;

    // snakes
    private int nSnakes;
    private Snake[] snakes;
    private Snake me;

    // apple
    private Point apple;
    private Point lastApple;
    private int appleScore;

    // positioning
    private Snake lastMe;
    private int lastMove;

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    static void log(Object msg) {
        System.out.println("log "+msg);
    }

    private void logMap(int size) {
        log("==================================================");
        for (int y = Math.max(0, lastMe.head.y - size); y <= Math.min(h - 1, lastMe.head.y + size); y++){
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < w; x++) {
                Point p = new Point(x,y);
                if (p.equals(apple)) {
                    row.append("A");
                } else if (path.contains(p)) {
                    row.append("+");
                } else if (p.equals(lastMe.head)) {
                    row.append("H");
                } else if (artMap[y][x]) {
                    row.append("X");
                } else {
                    row.append(map[y][x]);
                }
            }
            log(row.toString());
        }
        log("==================================================");
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            nSnakes = Integer.parseInt(temp[0]);
            w = Integer.parseInt(temp[1]);
            h = Integer.parseInt(temp[2]);

            turn = 0;
            while (true) {
                ++turn;
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

                // finished reading, calculate move:

                // debugging deaths
                if(lastMe != null && me.head.distanceTo(lastMe.head) > 1) {
                    log("I died on turn "+turn+" at "+lastMe.head+" ("+turnTime/1000000+")");
                    log("I was trying to move "+Direction.toString(lastMove) + " into "+getNextHead(lastMe, lastMove));
                    for (int x = Math.max(0, lastMe.head.x - 1); x < Math.min(w, lastMe.head.x + 2); x++) {
                        for (int y = Math.max(0, lastMe.head.y - 1); y < Math.min(h, lastMe.head.y + 2); y++) {
                            log(new Point(x,y) + ": " +map[y][x]);
                        }
                    }
                    logMap(6);
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

                findArtPoints();
                lastMe = me;

                doDirectStyleMovement();
//                doRankedStyleMovement();
//                doStateMachineStyleMovement();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void doStateMachineStyleMovement() {
//        switch(state) {
//            case Attacking:
//                int myHeading = me.getHeading();
//                switch(attackPhase) {
//                    case 0:
//                        // We need to turn around still. We are heading in the same direction. The first turn of attack
//                        if (Direction.isVertical(myHeading)) {
//                            if (me.head.x > victim.head.x) {
//                                // I am to the east of them
//                                if (isSafeMove(me, Direction.WEST)) {
//                                    System.out.println(Direction.WEST);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                }
//                            } else {
//                                // I am to the west of them
//                                if (isSafeMove(me, Direction.EAST)) {
//                                    System.out.println(Direction.EAST);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                }
//                            }
//                        } else {
//                            if (me.head.y > victim.head.y) {
//                                // I am to the south of them
//                                if (isSafeMove(me, Direction.NORTH)) {
//                                    System.out.println(Direction.NORTH);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                }
//                            } else {
//                                // I am to the north of them
//                                if (isSafeMove(me, Direction.SOUTH)) {
//                                    System.out.println(Direction.SOUTH);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                }
//                            }
//                        }
//                    case 1:
//                        // The second turn of the attack. We need to move forward
//                        if (isSafeMove(me, Direction.FORWARD)) {
//                            System.out.println(Direction.FORWARD);
//                            attackPhase++;
//                        } else {
//                            getNextState();
//                            attackPhase = 0;
//                        }
//                    case 2:
//                        // The third turn of the attack. We need to turn again
//                        if (Direction.isVertical(myHeading)) {
//                            if (me.head.x > victim.head.x) {
//                                if (isSafeMove(me, Direction.WEST)) {
//                                    System.out.println(Direction.WEST);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                    attackPhase = 0;
//                                }
//                            } else {
//                                if (isSafeMove(me, Direction.EAST)) {
//                                    System.out.println(Direction.EAST);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                    attackPhase = 0;
//                                }
//                            }
//                        } else {
//                            if (me.head.y > victim.head.y) {
//                                if (isSafeMove(me, Direction.NORTH)) {
//                                    System.out.println(Direction.NORTH);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                    attackPhase = 0;
//                                }
//                            } else {
//                                if (isSafeMove(me, Direction.SOUTH)) {
//                                    System.out.println(Direction.SOUTH);
//                                    attackPhase++;
//                                } else {
//                                    getNextState();
//                                    attackPhase = 0;
//                                }
//                            }
//                        }
//                        case 3:
//                            // The last phase of the attack, keep moving forward until they are dead
//                            if (victim.alive && isViableTarget(me, victim)) {
//                                System.out.println(Direction.FORWARD);
//                            }
//                }
//            case Seeking:
//                int[] move = getAStarMovement(me, apple);
//
//            case Protecting: getThreadingMovement();
//        }
//        getNextState();
//    }
//
//    private void getNextState() {
//        throw new NotImplementedException();
//    }
//
//    private boolean isViableTarget(Snake snake, Snake victim) {
//        int myHeading = snake.getHeading();
//        int victimHeading = victim.getHeading();
//
//        if (!victim.alive || victim.equals(snake)) {
//            return false;
//        }
//        if (victimHeading == myHeading) {
//            // We are travelling the same direction
//            if (Direction.isVertical(myHeading)) {
//                return snake.head.x - victim.head.x == 1 && victim.head.distanceTo(snake.head) < (snake.length / 2);
//            } else {
//                return snake.head.y - victim.head.y == 1 && victim.head.distanceTo(snake.head) < (snake.length / 2);
//            }
//        } else if (attackPhase == 3 && (victimHeading == Direction.opposite(myHeading))) {
//            return true;
//        } else return attackPhase == 1 && Direction.isVertical(myHeading) != Direction.isVertical(victimHeading);
//    }
    private boolean centralise;
    private void doDirectStyleMovement() {
        // TODO: Rework this
        long startTime = System.nanoTime();
        Point center = new Point(w/2, h/2);
        path = findAStarPath(me.head, apple);
        int[] move = new int[2];
        move[1] = path.size();
        move[0] = getMoveTo(me, path.peekLast());
        if (move[1] > appleScore || getClosestSnakeID(apple) != me.id) {
            move[0] = getThreadingMovement(me, me.head.directionTo(apple));
        }

        // ensure our final move is safe and/or legal and output it
        // TODO: Tie break unsafe moves to choose the safest one (rank moves)
        move[0] = makeSafe(me, move[0]);
        if (move[0] == Direction.NONE) {
            move[0] = makeLegal(me, move[0]);
        }
        lastMove = move[0];
        long endTime = System.nanoTime();
        turnTime = endTime - startTime;

        System.out.println(move[0]);
    }

    private int getMoveTo(Snake snake, Point point) {
        Point head = snake.head;
        if (point != null) {
            if (point.westOf(snake.head)) {
                return Direction.WEST;
            } else if (point.eastOf(snake.head)) {
                return Direction.EAST;
            } else if (point.southOf(snake.head)) {
                return Direction.SOUTH;
            } else if (point.northOf(snake.head)) {
                return Direction.NORTH;
            }
        }
        log("No move");
        return Direction.NONE;
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

        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = snake.getHeading();
        }

        while (i++ < 4) {
            if (isSafeMove(snake, direction)) {
                return direction;
            }
            direction = Direction.next(direction);
        }

        return Direction.NONE;
    }

    private boolean isSafeMove(Snake snake, int direction) {
        if (!isLegalMove(snake, direction)) {
            return false;
        }

        Point nextHead = getNextHead(snake, direction);
        Point head = snake.head;

        if (nextHead.equals(apple) && appleScore < 0) {
            return false;
        }
        if (artMap[head.y][head.x]) {
            if (!canFit(nextHead, snake.length)) {
                return false;
            }
        }
        for (Snake s : snakes) {
            if ((!s.alive) || (s == snake)) {
                continue;
            }
            if (nextHead.distanceTo(s.head) == 1) {
                return false;
            }
        }
        return true;
    }

    private int scoreMove(Snake snake, int direction) {
        // Lowest of:
            // Deadly apple         0
            // Illegal move         1
            // Dead end             2
            // Possible head-on     3
            // Bad apple            4
            // Legal                5
            // Good apple           6

        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = snake.getHeading();
        }

        int score;
        if (!isLegalMove(snake, direction)) {
            score =  1; // Illegal move
        } else {
            score = 5; // Legal move
        }
        Point nextHead = getNextHead(snake, direction);
        Point head = snake.head;

        // If I will be eating an apple when I make this move
        if (nextHead.equals(apple)) {
            if (appleScore < -40 || appleScore*10 <= -snake.length) {
                score = 0; // Deadly apple
            } else if (appleScore < 0) {
                score = 4; // Bad apple
            } else {
                score = 6; // Good apple
            }
        }

        if (artMap[head.y][head.x]) {
            if (!canFit(nextHead, snake.length) && 2 < score) {
                score = 2; // Dead end
            }
        }

        for (Snake s : snakes) {
            if ((!s.alive) || (s == snake)) {
                continue;
            }
            if (nextHead.distanceTo(s.head) == 1 && 3 < score) {
                score = 3; // Possible head-on
            }
        }

        return score;
    }

    private int makeLegal(Snake snake, int direction) {
        int i = 0;

        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = snake.getHeading();
        }

        while (i++ < 4) {
            if (isLegalMove(snake, direction)) {
                logMap(3);
                log("Chose legal move "+Direction.toString(direction));
                return direction;
            }
            direction = Direction.next(direction);
        }
        return Direction.NONE;
    }

    private boolean isLegalMove(Snake snake, int direction) {
        Point p = getNextHead(snake, direction);
        return  (p.x >= 0 && p.x < w
                && p.y >= 0 && p.y < h)
                && (map[p.y][p.x] == 0
                || (p.equals(apple) && appleScore < -40));
    }

    // Dead end detection
    private int outEdgeCount;
    private int id;
    private void findArtPoints() {
        int[][] low = new int[h][w];
        int[][] ids = new int[h][w];
        boolean[][] visited = new boolean[h][w];
        artMap = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Point p = new Point(x,y);
                if (!visited[y][x]) {
                    outEdgeCount = 0;
                    findArtPoints(p, p, null, low, ids, visited);
                    artMap[y][x] = (outEdgeCount > 1);
                }
            }
        }
    }

    // Depth first search
    private void findArtPoints(Point root, Point at, Point parent, int[][] low, int[][] ids, boolean[][] visited) {
        if (root.equals(parent)) {
            outEdgeCount++;
        }
        visited[at.y][at.x] = true;
        id++;
        low[at.y][at.x] = id;
        ids[at.y][at.x] = id;

        for (Point to : getNeighbours(at)) {
            if (to.equals(parent) || (!to.equals(me.head) && map[to.y][to.x] != 0)) {
                continue;
            }
            if (!visited[to.y][to.x]) {
                findArtPoints(root, to, at, low, ids, visited);
                low[at.y][at.x] = Math.min(low[at.y][at.x], low[to.y][to.x]);
                if (ids[at.y][at.x] <= low[to.y][to.x]) {
                    artMap[at.y][at.x] = true;
                }
            } else {
                low[at.y][at.x] = Math.min(low[at.y][at.x], ids[to.y][to.x]);
            }
        }
    }

    private boolean canFit(Point from, int maxArea) {
        if (map[from.y][from.x] != 0) {
            return false;
        }
        LinkedList<Point> queue = new LinkedList<>();
        queue.add(from);

        boolean[][] visited = new boolean[h][w];

        int area = 0;
        while (!queue.isEmpty() && area < maxArea) {
            Point pW = queue.peek();
            Point pE = queue.poll();
            ArrayList<Point> row = new ArrayList<>();
            while (pW.x >= 0 && !visited[pW.y][pW.x] && map[pW.y][pW.x] == 0 && !(pW.equals(apple) && appleScore < 0)) {
                row.add(pW);
                visited[pW.y][pW.x] = true;
                pW = new Point(pW.x - 1, pW.y);
            }
            pE = new Point(pE.x + 1, pE.y);
            while (pE.x <= w - 1 && !visited[pE.y][pE.x] && map[pE.y][pE.x] == 0 && !(pW.equals(apple) && appleScore < 0)) {
                visited[pE.y][pE.x] = true;
                row.add(pE);
                pE = new Point(pE.x + 1, pE.y);
            }
            for (Point p : row) {
                area++;
                if (p.y > 0 && map[p.y - 1][p.x] == 0 && !(pW.equals(apple) && appleScore < 0)) {
                    queue.add(new Point(p.x, p.y - 1));
                }
                if (p.y < h - 1 && map[p.y + 1][p.x] == 0 && !(pW.equals(apple) && appleScore < 0)) {
                    queue.add(new Point(p.x, p.y + 1));
                }
            }
        }
        return (area >= maxArea);
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

    LinkedList<Point> path = new LinkedList<>();
    ArrayList<Point> searchGraph = new ArrayList<>(); // the vertices that have been searched
    HashMap<Point, ArrayList<Point>> edges = new HashMap<>(); // The vertices connected to each vertex in the graph
    private LinkedList<Point> findPath(Snake snake, Point target) {
        //TODO: Detect changes along the path
        // Walk along the path and check if the neighbours match my record.
        int lastIndex = 0;
        for (int i = 0; i < path.size(); i++) {
            Point p = path.get(i);
            ArrayList<Point> neighbours = getNeighbours(p);
            Iterator<Point> itr = neighbours.iterator();
            while (itr.hasNext()) {
                Point n = itr.next();
                if (map[n.y][n.x] != 0) {
                    itr.remove();
                }
            }
            if (edges.get(p) != null && !neighbours.containsAll(edges.get(p))) {
                lastIndex = i;
                break;
            }
        }
        logMap(7);
        // Update path and map from this point
        if (lastIndex > 0) {
            LinkedList newPath = findAStarPath(path.get(lastIndex), target);
            LinkedList<Point> subPath = new LinkedList<>();
            for (int i = 0; i < lastIndex; i++) {
                subPath.add(path.pollFirst());
            }
            path = subPath;
            path.addAll(newPath);
        } else if (path.size() == 0) {
            path = findAStarPath(snake.head, target);
        }
        return path;
    }

    private int getThreadingMovement(Snake snake, int direction) {
        if (Direction.isVertical(direction)) {
            if(Direction.isVertical(snake.getHeading())) {
                return (snake.head.x > w / 2) ? Direction.WEST : Direction.EAST;
            } else if (!isLegalMove(snake, Direction.FORWARD)) {
                return direction;
            }
        } else {
            if(!Direction.isVertical(snake.getHeading())) {
                return (snake.head.y > h / 2) ? Direction.NORTH : Direction.SOUTH;
            } else if (!isLegalMove(snake, Direction.FORWARD)) {
                return direction;
            }
        }
        return Direction.FORWARD;
    }

    private LinkedList<Point> findAStarPath(Point start, Point target) {
        ArrayList<Point> open = new ArrayList<>();
        ArrayList<Point> closed = new ArrayList<>();


        HashMap<Point, Integer> gScores = new HashMap<>();
        HashMap<Point, Double> fScores = new HashMap<>();

        HashMap<Point, Point> cameFrom = new HashMap<>();

        open.add(start);
        gScores.put(start, 0);
        fScores.put(start, heuristic(start, target));
        boolean found = false;
        while (!open.isEmpty()) {
            Point current = open.get(0);
            for (Point p : open) {
                if (fScores.get(p) < fScores.get(current)) {
                    current = p;
                }
            }

            if (target.equals(current)){
                found = true;
                break;
            }

            open.remove(current);
            closed.add(current);

            int gScore = gScores.get(current) + 1;
            ArrayList<Point> neighbours = getNeighbours(current);
            if (!searchGraph.contains(current)) {
                searchGraph.add(current);
            }
            edges.put(current, neighbours);
            for (Point n : neighbours) {
                if (closed.contains(n) || map[n.y][n.x] > 0) {
                    continue;
                }

                if (!open.contains(n)) {
                    open.add(n);
                } else if (gScores.get(n) < gScore) {
                    continue;
                }

                gScores.put(n, gScore);
                fScores.put(n, gScore + heuristic(n, target));
                cameFrom.put(n, current);
            }
        }

        if (!found) {
            return new LinkedList<>();
        }

        // Cycle back until we get to the start
        Point nextPoint = target;
        LinkedList<Point> path = new LinkedList<>();
        while (nextPoint != null && nextPoint != start) {
            path.add(nextPoint);
            nextPoint = cameFrom.get(nextPoint);
        }
        return path;
    }

    private int[] getAStarMovement(Snake snake, Point target) {
        /*
        TODO: Optimise and improve speed
            - JPS (https://zerowidth.com/2013/05/05/jump-point-search-explained.html)
            - LPA* / D* Lite
            - HPA*
            - Rectangular Symmetry Reduction
            - Rectangular Expansion A*
        */

        path = findAStarPath(snake.head, apple);
        int pathLength = path.size();
        Point nextPoint = path.peekLast();
        int[] result = new int[2];
        result[1] = path.size();
        if (pathLength == 0) {
            result[0] =  Direction.NONE;
        } else if (nextPoint.westOf(snake.head)) {
            result[0] = Direction.WEST;
        } else if (nextPoint.eastOf(snake.head)) {
            result[0] = Direction.EAST;
        } else if (nextPoint.southOf(snake.head)) {
            result[0] = Direction.SOUTH;
        } else if (nextPoint.northOf(snake.head)) {
            result[0] = Direction.NORTH;
        } else {
            result[0] = Direction.NONE;
        }
        return result;
    }

    private void getDStarLiteMovement(Snake snake, Point target) {
        throw new NotImplementedException();
    }

    private int getProtectionMovement(Snake snake, Point target) {
        throw new NotImplementedException();
    }

    private ArrayList<Point> getNeighbours(Point p) {
        ArrayList<Point> points = new ArrayList<>();
        if (p.y > 0) {
            points.add(new Point(p.x, p.y - 1));
        }
        if (p.x > 0) {
            points.add(new Point(p.x - 1, p.y));
        }
        if (p.y < h - 1) {
            points.add(new Point(p.x, p.y + 1));
        }
        if (p.x < w - 1) {
            points.add(new Point(p.x + 1, p.y));
        }
        return points;
    }

    private double heuristic(Point a, Point b) {
        return a.distanceTo(b); // Manhattan distance heuristic
    }
}
