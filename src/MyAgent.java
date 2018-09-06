import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class MyAgent extends DevelopmentAgent {

    // misc
    private int turn;
    private long turnTime;

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
    boolean attacking; // Used for remembering if we are killing another snake

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
                    log("I died on turn "+turn+" at "+lastMe.head);
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

                MoveStyle1();
                //MoveStyle2();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MoveStyle2() {
        // FIXME: Move priorities are off (taking head on collisions over safe moves)
        Point center = new Point(w/2, h/2);
        int bestMove;
        int bestScore;
        // GO TO THE APPLE WITH PATH-FINDING
        int[] move = getAStarMovement(me ,apple);
        bestMove = move[0];
        bestScore = scoreMove(me, move[0]);

        // GO DIRECTLY TO THE CENTER WITHOUT PATH-FINDING
        move[0] = getSimpleMovement(me, center);
        int score = scoreMove(me, move[0]);
        if (bestScore < score) {
            bestMove = move[0];
            bestScore = score;
        }
        // GO DIRECTLY TO MY TAIL
        move[0] = getSimpleMovement(me, me.tail);
        score = scoreMove(me, move[0]);
        if (bestScore < score) {
            bestMove = move[0];
            bestScore = score;
        }
        // THREAD TOWARDS THE APPLE
        move[0] = getThreadingMovement(me, me.head.directionTo(apple));
        score = scoreMove(me, move[0]);
        if (bestScore < score) {
            bestMove = move[0];
            bestScore = score;
        }
        System.out.println(bestMove);
    }

    private void MoveStyle1() {
        // TODO: Rework this
        long startTime = System.nanoTime();
        Point center = new Point(w/2, h/2);
        int[] move = new int[2];
        // move = getAStarMovement(me, apple);
        move[0] = getSimpleMovement(me, apple);

        if (move[0] == Direction.NONE || move[1] > appleScore || getClosestSnakeID(apple) != me.id) {
            //move[0] = getThreadingMovement(me, center.directionTo(me.head));
            if (appleScore > 0) {
                move[0] = getThreadingMovement(me, me.head.directionTo(apple));
            } else {
                move[0] = getThreadingMovement(me, apple.directionTo(me.head));
            }
            //move[0] = getSimpleMovement(me, center);
        }

        // ensure our final move is safe and/or legal and output it
        move[0] = makeSafe(me, move[0]);
        if (move[0] == Direction.NONE) {
            move[0] = makeLegal(me, move[0]);
        }
        lastMove = move[0];
        long endTime = System.nanoTime();
        turnTime = endTime - startTime;

        System.out.println(move[0]);
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
                //log("Chose safe move "+Direction.toString(direction));
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
            if (!floodFill(nextHead, snake.length)) {
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

        // If I will be eating an apple if I make this move
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
            if (!floodFill(nextHead, snake.length) && 2 < score) {
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
        // FIXME: In the situation where the only 2 possible moves will both lead to an articulation point, no legal move is found
//         A) The snake reaches across the entire board and hits a wall
//         |==================================================|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |0000000000000000000000000000000000000000000000000X|
//         |1111111111111111111111111111111111111111111111111H|
//         |1111111110000000000000000000000000000000000000000X|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |==================================================|
//         B) The snake comes up against another snake and is left with only 1 option to move to, which is an articulation point
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |11111111111111111111110000000000000000000000000000|
//         |11H44444000000000000000000000000000000000000000000|
//         |00X33333000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |==================================================|
//         C) The snake closes is about to close off a circle. Is this shape even possible??
//         |==================================================|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000011111110000000000000000000000000|
//         |00000000000000001110XXXXHX000000000000000000000000|
//         |00000000000000001111111111000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |00000000000000000000000000000000000000000000000000|
//         |==================================================|
//         D) The snake closes off a loop by hitting another snake
//         |==================================================|
//         |00000000000000000000444444000000000000000000000000|
//         |00000000000000000000400000000000000000000000000000|
//         |00000000000044444444400000000000000000000000000000|
//         |0000000000004000XHXXXX0000000000000000000000000000|
//         |00000000000040000111111111111111000000000000000000|
//         |00000000000044444400000000000001111111111111110000|
//         |00000000000000000000000000000000000000000111110000|
//         |==================================================|

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

    private int getOffenseMovement(Snake snake) {
        // FIXME: 2018/09/03 It gets confused about which snakes it can kill, and forgets it was trying to kill them immediately
        for (int i = 0; i < nSnakes; i++) {
            Snake target = snakes[i];
            if (!target.alive || target.equals(snake)) {
                continue;
            }

            if (target.getHeading() == snake.getHeading()) {
                // Parallel
                double distance = target.head.distanceTo(snake.head);

            }
        }
        return Direction.NONE;
    }

//    private ArrayList<Point> findDeadEnds(Snake snake) {
//        // TODO: Find articulation points of biconnected components. Moving from an AP to a point with only 2 legal neighbours is moving into a dead end
//        ArrayList<Point> deadEnds = new ArrayList<>();
//        for(Point ap : articulationPoints) {
//            for (Point n : getNeighbours(ap)) {
//                if (articulationPoints.contains(n)){
//                    int validN = 0;
//                    for (Point m : getNeighbours(n)) {
//                        if (map[m.y][m.x] == 0 || m.equals(snake.head)) {
//                            validN++;
//                        }
//                    }
//                    if (validN <= 2) {
//                        deadEnds.add(n);
//                    }
//                }
//            }
//        }
//        return deadEnds;
//    }

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
                    dfs(p, p,null, low, ids, visited);
                    artMap[y][x] = (outEdgeCount > 1);
                }
            }
        }
    }
    private void dfs(Point root, Point at, Point parent, int[][] low, int[][] ids, boolean[][] visited) {
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
                dfs(root, to, at, low, ids, visited);
                low[at.y][at.x] = Math.min(low[at.y][at.x], low[to.y][to.x]);
                if (ids[at.y][at.x] <= low[to.y][to.x]) {
                    artMap[at.y][at.x] = true;
                }
            } else {
                low[at.y][at.x] = Math.min(low[at.y][at.x], ids[to.y][to.x]);
            }
        }
    }


//    Flood-fill (node, target-color, replacement-color):
//            1. If target-color is equal to replacement-color, return.
//            2. If color of node is not equal to target-color, return.
//            3. Set Q to the empty queue.
//            4. Add node to Q.
//            5. For each element N of Q:
//            6.     Set w and e equal to N.
//            7.     Move w to the west until the color of the node to the west of w no longer matches target-color.
//            8.     Move e to the east until the color of the node to the east of e no longer matches target-color.
//            9.     For each node n between w and e:
//            10.         Set the color of n to replacement-color.
//            11.         If the color of the node to the north of n is target-color, add that node to Q.
//            12.         If the color of the node to the south of n is target-color, add that node to Q.
//            13. Continue looping until Q is exhausted.
//            14. Return.

    // Forest fire algorithm
    private boolean floodFill(Point from, int maxArea) {
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
        log("Filled "+area+"/"+maxArea+" from "+from);
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

    private int getDirectBacktrackMovement(Snake snake, Point target) {
        // Find a straight line in the direction of the target
        Point head = snake.body[0];
        int direction = head.directionTo(target);

        ArrayList<Point> path = new ArrayList<>();

        // Walk directly to the apple until we hit an obstacle
        int x = head.x + Direction.deltaX(direction), y = head.y + Direction.deltaY(direction);
        Point p = new Point(x,y);
        while (map[y][x] == 0 && !p.equals(target)) {
            x += Direction.deltaX(direction);
            y += Direction.deltaY(direction);
            direction = p.directionTo(apple);
            if (map[y][x] != 0) {
                break;
            }
            path.add(p);
        }
        return Direction.NONE;

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

    private int[] getAStarMovement(Snake snake, Point target) {
        /*
        TODO: Optimise and improve speed
            - JPS (https://zerowidth.com/2013/05/05/jump-point-search-explained.html)
            - LPA* / D* Lite
            - HPA*
            - Rectangular Symmetry Reduction
            - Rectangular Expansion A*
        */

        ArrayList<Point> open = new ArrayList<>();
        ArrayList<Point> closed = new ArrayList<>();


        HashMap<Point, Integer> gScores = new HashMap<>();
        HashMap<Point, Double> fScores = new HashMap<>();

        HashMap<Point, Point> cameFrom = new HashMap<>();

        Point start = snake.head;
        open.add(start);
        gScores.put(start, 0);
        fScores.put(start, start.distanceTo(target));

        while (!open.isEmpty()) {
            Point current = open.get(0);
            for (Point p : open) {
                if (fScores.get(p) < fScores.get(current)) {
                    current = p;
                }
            }

            if (target.equals(current)){
                break;
            }

            open.remove(current);
            closed.add(current);

            int gScore = gScores.get(current) + 1;
            for (Point n : getNeighbours(current)) {
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

        // Cycle back until we get to the start
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
