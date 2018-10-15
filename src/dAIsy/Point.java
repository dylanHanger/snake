package dAIsy;

public class Point {

    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point(String coordinates) {
        String[] data = coordinates.split("[ ,]");
        this.x = Integer.parseInt(data[0]);
        this.y = Integer.parseInt(data[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || getClass() != o.getClass()) return false;
        Point pt = (Point)o;
        if (pt.x != this.x) return false;
        return pt.y == this.y;
    }

    @Override
    public int hashCode() {
        // Cantor's
        return ((x + y)*(x + y + 1)/2) + y;
    }

    public int directionTo(Point other) {
        if (other.y < this.y) {
            return Direction.NORTH;
        } else if (other.y > this.y) {
            return Direction.SOUTH;
        } else if (other.x > this.x) {
            return Direction.EAST;
        } else {
            return Direction.WEST;
        }
    }

    public boolean adjacentTo(Point other) {
        return this.manhattanTo(other) == 1;
    }

    public boolean closeTo(Point other, double r) {
        return this.manhattanTo(other) <= r;
    }

    public int getQuadrant(int w, int h) {
        if (x < w/2) {
            if (y < h/2) {
                return 2;
            } else {
                return 3;
            }
        } else {
            if (y < h/2) {
                return 1;
            } else {
                return 4;
            }
        }
    }

    public int euclideanTo(Point other) {
        if (other == null) {
            return 0x7fffffff;
        }
        return (int)Math.sqrt((this.x-other.x)*(this.x-other.x) + (this.y-other.y)*(this.y-other.y));
    }

    public int manhattanTo(Point other) {
        if (other == null) {
            return 0x7fffffff;
        }
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public String toString(){
        return "("+x+", "+y+")";
    }

}
