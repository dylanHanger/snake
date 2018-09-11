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

    public boolean westOf(Point other) {
        return other.x > this.x;
    }
    public boolean northOf(Point other) {
        return other.y > this.y;
    }

    public int distanceTo(Point other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
        //return Math.sqrt((this.x-other.x)*(this.x-other.x) + (this.y-other.y)*(this.y-other.y));
    }

    public String toString(){
        return "("+x+", "+y+")";
    }

}
