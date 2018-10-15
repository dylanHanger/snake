package dAIsy;

import za.ac.wits.snake.DevelopmentAgent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Daisy extends DevelopmentAgent {

    // misc
    private int turn;
    private int deaths;

    // stats
    private static int maxLength;
    private static int maxDeaths;
    private static int maxKills;
    private static int minLength;
    private static int minKills;
    private static int avgLength;
    private static int avgDeaths;
    private static int avgKills;
    private static int games;
    private static int totalLength;
    private static int totalDeaths;
    private static int totalKills;

    // Representation of map
    private int w, h;
    private int[][] map;
    private boolean[][] artMap;


    // my info I need to save
    private LinkedList<Point> path;
    private int threadDirection;
    private boolean suicideMode;
    private int myLongest;

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
        try {
            List<String> stats = Files.readAllLines(Paths.get("stats.txt"));
            for (String line : stats) {
                String[] data = line.split(" ");
                switch (data[0]) {
                    case "maxd":
                        maxDeaths = Integer.parseInt(data[1]);
                        break;
                    case "maxl":
                        maxLength = Integer.parseInt(data[1]);
                        break;
                    case "maxk":
                        maxKills = Integer.parseInt(data[1]);
                        break;
                    case "minl":
                        minLength = Integer.parseInt(data[1]);
                        break;
                    case "mink":
                        minKills = Integer.parseInt(data[1]);
                        break;
                    case "avgl":
                        avgLength = Integer.parseInt(data[1]);
                        break;
                    case "avgd":
                        avgDeaths = Integer.parseInt(data[1]);
                        break;
                    case "avgk":
                        avgKills = Integer.parseInt(data[1]);
                        break;
                    case "games":
                        games = Integer.parseInt(data[1]);
                        break;
                    case "totl":
                        totalLength = Integer.parseInt(data[1]);
                        break;
                    case "totd":
                        totalDeaths = Integer.parseInt(data[1]);
                        break;
                    case "totk":
                        totalKills = Integer.parseInt(data[1]);
                        break;
                }
            }
        } catch (IOException e) {
            log ("No stats file");
        }
        Daisy agent = new Daisy();
        Daisy.start(agent, args);
    }

    private static void log(Object msg) {
        System.out.println("log "+msg);
    }

    private void resetMap() {
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
        artMap = new boolean[h][w];
        findArtPoints(me.head, null, new boolean[h][w], new int[h][w], 0);
    }

    private int findArtPoints(Point v, Point parent, boolean[][] visited, int[][] lows, int id) {
        ArrayList<Point> neighbours = neighboursOf(v);
        int back = id;
        int currId = id;
        visited[v.y][v.x] = true;

        for (Point w : neighbours) {
            if (!visited[w.y][w.x]) {
                id = findArtPoints(w, v, visited, lows, id + 1);
                if (lows[w.y][w.x] >= currId) {
                    artMap[v.y][v.x] = true;
                } else {
                    back = Math.min(back, lows[w.y][w.x]);
                }
            }
            if (!w.equals(parent)) {
                back = Math.min(back, lows[w.y][w.x]);
            }
        }
        lows[v.y][v.x] = back;
        return id;
    }

    private boolean isLegalMove(Snake snake, int direction) {
        Point p = getNextHead(snake, direction);
        return (p.x >= 0 && p.x < w
                && p.y >= 0 && p.y < h)
                && (map[p.y][p.x] == 0
                || (p.equals(apple) && appleScore < -40));
    }

    private Point getNextHead(Snake snake, int direction) {
        Point head = snake.head;
        int heading = snake.getHeading();
        if (direction == Direction.FORWARD || direction == Direction.NONE) {
            direction = heading;
        } else if (direction == Direction.RIGHT) {
            switch (heading) {
                case Direction.NORTH:
                    direction = Direction.EAST;
                    break;
                case Direction.SOUTH:
                    direction = Direction.WEST;
                    break;
                case Direction.EAST:
                    direction = Direction.SOUTH;
                    break;
                case Direction.WEST:
                    direction = Direction.NORTH;
                    break;
            }
        } else if (direction == Direction.LEFT) {
            switch (heading) {
                case Direction.NORTH:
                    direction = Direction.WEST;
                    break;
                case Direction.SOUTH:
                    direction = Direction.EAST;
                    break;
                case Direction.EAST:
                    direction = Direction.NORTH;
                    break;
                case Direction.WEST:
                    direction = Direction.SOUTH;
                    break;
            }
        }
        Point point;
        switch (direction) {
            case Direction.NORTH:
                point = new Point(head.x, head.y - 1);
                break;
            case Direction.SOUTH:
                point = new Point(head.x, head.y + 1);
                break;
            case Direction.EAST:
                point = new Point(head.x + 1, head.y);
                break;
            case Direction.WEST:
                point = new Point(head.x - 1, head.y);
                break;
            default:
                return snake.head;
        }
        return point;
    }

    private int findDirectMove(Snake snake, Point target) {
        int direction = snake.getHeading();
        int bestDirection = direction;
        double bestScore = getNextHead(snake, direction).euclideanTo(target);
        for (int i = 0; i < 3; i++) {
            direction = Direction.next(direction);
            double newScore = getNextHead(snake, direction).euclideanTo(target);
            if (newScore < bestScore) {
                bestScore = newScore;
                bestDirection = direction;
            }

        }
        return bestDirection;
    }

    private Snake findClosestSnake(Point point) {
        // perform a breadth first search until we find a snake
        LinkedList<Point> queue = new LinkedList<>();
        ArrayList<Point> visited = new ArrayList<>();
        queue.add(point);
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            visited.add(current);
            ArrayList<Point> neighbours = neighboursOf(current);
            for (Point n : neighbours) {
                for (Snake snake : snakes) {
                    if (!snake.alive) continue;
                    if (n.equals(snake.head)) {
                        return snake;
                    }
                }
                if (!visited.contains(n) && !queue.contains(n) && map[n.y][n.x] == 0) {
                    queue.add(n);
                }
            }
        }
        return me;
    }

    private void logMap(int size) {
        log("==================================================");
        for (int y = Math.max(0, lastMe.head.y - size); y <= Math.min(h - 1, lastMe.head.y + size); y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < w; x++) {
                Point p = new Point(x, y);
                if (p.equals(apple)) {
                    row.append("A");
                } else if (p.equals(lastMe.head)) {
                    row.append("H");
                } else if (artMap[y][x]) {
                    row.append("X");
                } else if (path.contains(p)) {
                    row.append("+");
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
            // snakes
            nSnakes = Integer.parseInt(temp[0]);
            w = Integer.parseInt(temp[1]);
            h = Integer.parseInt(temp[2]);
            turn = 0;
            while (true) {
                ++turn;
                String line = br.readLine();
                if (turn == (6000 - deaths) || line.contains("Game Over")) {
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
                    Snake newSnake = new Snake(i + 1, snakeData);
                    if (i == mySnakeNum) {
                        me = newSnake;
                        if (me.length > myLongest) {
                            myLongest = me.length;
                        }
                    }
                    //do stuff with snakes
                    snakes[i] = newSnake;
                }

                // finished reading, calculate move:

                // debugging deaths
                if (lastMe != null && me.head.euclideanTo(lastMe.head) > 1) {
                    log("Moved " + Direction.toString(lastMove) + ", " + lastMe.head + "->" + getNextHead(lastMe, lastMove));
                    logMap(6);
                    log("Turn: " + turn);
                    deaths++;
                    //TODO: suicideMode = suicideMode || lastMe.length > 80;
                }

                long startTime = System.nanoTime();
                // reset the map
                resetMap();
                lastMe = me;
                // Make and output my move
                move();
                long turnTime = (System.nanoTime() - startTime) / 1000000;
                if (turnTime >= 15) {
                    System.out.println("log Something is wrong");
                }
                log(turnTime + "ms for turn " + turn + " (deaths:" + deaths + ")");
            }
            // Cleanup
            games++;
            totalDeaths += deaths;
            totalLength += myLongest;
            totalKills += me.kills;
            maxKills = Math.max(maxKills, me.kills);
            maxLength = Math.max(maxLength, myLongest);
            maxDeaths = Math.max(maxDeaths, deaths);
            minKills = Math.min(minKills, me.kills);
            minLength = Math.min(minLength, myLongest);
            avgKills = Math.round((float) totalKills / games);
            avgLength = Math.round((float) totalLength / games);
            avgDeaths = Math.round((float) totalDeaths / games);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter("stats.txt"));
                bw.write("maxd " + maxDeaths);
                bw.newLine();
                bw.write("maxl " + maxLength);
                bw.newLine();
                bw.write("maxk " + maxKills);
                bw.newLine();
                bw.write("minl " + minLength);
                bw.newLine();
                bw.write("mink " + minKills);
                bw.newLine();
                bw.write("avgl " + avgLength);
                bw.newLine();
                bw.write("avgd " + avgDeaths);
                bw.newLine();
                bw.write("avgk " + avgKills);
                bw.newLine();
                bw.write("games " + games);
                bw.newLine();
                bw.write("totl " + totalLength);
                bw.newLine();
                bw.write("totd " + totalDeaths);
                bw.newLine();
                bw.write("totk " + totalKills);
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                log ("No stats file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForCorridor(Snake snake, Point start) {
        Point nextHead = start;
        Point lastHead = snake.head;
        int distance = 1;
        while (true) {
            ArrayList<Point> neighbours = neighboursOf(nextHead);
            if (neighbours.size() > 2) {
                for (Snake other : snakes) {
                    if (!other.alive || other.equals(snake)) continue;
                    if (nextHead.closeTo(other.head, distance)) {
                        return true;
                    }
                }
                break;
            } else if (neighbours.size() == 1) {
                return true;
            }
            for (Point p : neighbours) {
                if (!p.equals(lastHead)) {
                    lastHead = nextHead;
                    nextHead = p;
                    distance++;
                }
            }
        }
        return false;
    }

    private void move() {
        // TODO: Improve suicide methods
        if (suicideMode) {
            // Find the longest snake
            Snake longest = null;
            for (Snake other: snakes) {
                if (!other.alive || other.equals(me)) continue;
                if (longest == null || longest.length < other.length) {
                    longest = other;
                }
            }

            // aim for their head
            if (findPath(me, getNextHead(longest, Direction.FORWARD))) {
              System.out.println(path.pollLast());
            } else {
                System.out.println(findDirectMove(me, getNextHead(longest, Direction.FORWARD)));
            }

            // stop with the rest of the move calculations
            return;
        }
        PriorityQueue<Integer> moves = new PriorityQueue<>(Comparator.comparingDouble(m -> -calcSafety(me, m)));
        int x = 0;
        int y = 0;
        for (Snake other : snakes) {
            if (!other.alive || other.equals(me)) continue;
            x += other.head.x;
            y += other.head.y;
        }
        Point averageHead = new Point(x / 3, y / 3);
        if (findPath(me, apple)) {
            threadDirection = Direction.NONE;
            if (appleScore > path.size() && findClosestSnake(apple).equals(me)) {
                moves.add(me.head.directionTo(path.pollLast()));
            } else {
                // Apple is too far away or we can't get there in time. We want the best chance at getting the next apple, so we go away from the others
                switch (averageHead.getQuadrant(w, h)) {
                    case 1:
                        findPath(me, new Point(w / 4, 3 * h / 4));
                        break;
                    case 2:
                        findPath(me, new Point(3 * w / 4, 3 * h / 4));
                        break;
                    case 3:
                        findPath(me, new Point(3 * w / 4, h / 4));
                        break;
                    case 4:
                        findPath(me, new Point(w / 4, h / 4));
                        break;
                }
                if (path.size() > 0) {
                    moves.add(me.head.directionTo(path.pollLast()));
                } else {
                    // If there is no path to the quadrant.
                    moves.add(findThreadingMove(me, me.head.directionTo(me.tail))); //TODO: Find a better move here
                }
            }
        } else {
            // If there is no path to the apple then we are probably in a dead end. TODO: Find the real best direction to move.
            // Should move to the point where the box will open
            if (threadDirection == Direction.NONE) {
                threadDirection = me.head.directionTo(me.tail);
            }
            moves.add(findThreadingMove(me, threadDirection));
        }
        moves.add(findSafestMove(me));

        int move = moves.poll();
        System.out.println(move);
        lastMove = move;
    }

    private int findCircleMove(Snake snake, int r) {
        Point center = new Point(w / 2, h / 2);
        int circleMove = Direction.FORWARD;
        double distance = -1;
        for (int move = Direction.LEFT; move <= Direction.RIGHT; move++) {
            double newDist = getNextHead(me, move).euclideanTo(center) - r;
            if (distance < 0 || newDist < distance) {
                distance = newDist;
                circleMove = move;
            }
        }
        return circleMove;
    }

    private double calcSafety(Snake snake, int move) {
        // Lowest of:
            // Deadly apple         0
            // Illegal move         1
            // Likely head-on       2
            // Dead end             [3,4)
            // Unlikely head-on     4
            // Close to head        5
            // Bad apple            6
            // Legal                7
            // Good apple           8

        double riskiness = 30;
        Point nextHead = getNextHead(snake, move);

        if (nextHead.equals(apple) && appleScore < -40) {
            return 0;
        }

        if (!isLegalMove(snake, move)) {
            return 1;
        }

        for (Snake other : snakes) {
            if (!other.alive || other.equals(snake)) continue;
            // TODO: Predict their next move instead of assuming they will move forward
            // likely head on
            if (nextHead.equals(getNextHead(other, findDirectMove(other, apple)))) {
                return 2;
            }
        }

        // TODO: Improve future dead end detection.
        // TODO: Compute connected components to determine flood mode or not?
        int area = canFit(nextHead, snake.length);
        if ((artMap[snake.head.y][snake.head.x] && area < snake.length)) {
            return 3 + ((double) area / snake.length);
        }

        for (Snake other : snakes) {
            if (!other.alive || other.equals(snake)) continue;

            // unlikely head on
            if (nextHead.adjacentTo(other.head)) {
                return 4;
            }

            if (nextHead.closeTo(other.head, (Math.max(2.0, snake.length / riskiness)))) {
                return 5;
            }
        }

        if (nextHead.equals(apple)) {
            if (appleScore < 0) {
                return 6;
            } else {
                return 8;
            }
        }

        return 7 + ((3 - countWalls(nextHead)) / 3.0);
    }



    private int canFit(Point from, int maxArea) {
        if (map[from.y][from.x] != 0) {
            return 0;
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
        return area;
    }

    private boolean findPath(Snake snake, Point target) {
        Point start = snake.head;
        int count = 0;

        HashMap<Point, Integer> gScores = new HashMap<>();
        gScores.put(start, 0);

        HashMap<Point, Double> fScores = new HashMap<>();
        fScores.put(start, heuristic(start, target));

        HashMap<Point, Point> cameFrom = new HashMap<>();

        PriorityQueue<Point> frontier = new PriorityQueue<>(Comparator.comparingDouble(fScores::get));
        frontier.add(start);

        boolean[][] open = new boolean[h][w];
        boolean[][] closed = new boolean[h][w];

        while (!frontier.isEmpty()) {
            Point current = frontier.poll();
            count++;
            if (target.equals(current) || (count >= 1000)) {
                LinkedList<Point> newPath = new LinkedList<>();
                Point nextPoint = current;
                while (nextPoint != null && !nextPoint.equals(start)) {
                    newPath.add(nextPoint);
                    nextPoint = cameFrom.get(nextPoint);
                }
                path = newPath;
                return target.equals(current);
            }

            closed[current.y][current.x] = true;

            int gScore = gScores.get(current) + 1;
            for (Point n : neighboursOf(current)) {
                if (closed[n.y][n.x] || map[n.y][n.x] > 0) continue;
                if (open[n.y][n.x] && gScores.get(n) <= gScore) continue;
                gScores.put(n, gScore);
                fScores.put(n, gScore + heuristic(n, target));
                cameFrom.put(n, current);
                frontier.add(n);
                open[n.y][n.x] = true;
            }
        }
        return false;
    }

    private int findThreadingMove(Snake snake, int direction) {
        if (Direction.isVertical(snake.getHeading()) == Direction.isVertical(direction)) {
            return isLegalMove(snake, Direction.RIGHT) ? Direction.RIGHT : Direction.LEFT;
        } else if (!isLegalMove(snake, Direction.FORWARD)) {
            return direction;
        }
        return Direction.FORWARD;
    }

    private int countWalls(Point point) {
        ArrayList<Point> neighbours = neighboursOf(point);
        int walls = 4 - neighbours.size();
        for (Point n : neighbours) {
            if (map[n.y][n.x] != 0) {
                walls++;
            }
        }
        return walls;
    }

    private int findSafestMove(Snake snake) {
        int direction = snake.getHeading();
        int bestDirection = direction;
        double bestScore = calcSafety(snake, direction);
        for (int i = 0; i < 3; i++) {
            direction = Direction.next(direction);
            double newScore = calcSafety(snake, direction);
            if (newScore > bestScore) {
                bestScore = newScore;
                bestDirection = direction;
            }

        }
        return bestDirection;
    }

    private ArrayList<Point> neighboursOf(Point p) {
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
        double score = a.euclideanTo(b);
        score += countWalls(a) - 1;
        //TODO: Create a territory map. (Voronoi diagram seeded by snake heads). Breadth first search from every point to the first snake, marking that as its "owner".
        //TODO: other heuristic stuff?
        return score;
    }
}
