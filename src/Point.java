public class Point {
    public int x;
    public int y;
    public int owner = 0;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point(String[] data) {
        this.x = Integer.parseInt(data[0]);
        this.y = Integer.parseInt(data[1]);
    }

    public int relativeTo(Point other) {
        if (this.y < other.y) {
            // I am above them
            return 0;
        } else if (this.y > other.y) {
            // I am below them
            return 1;
        }else if (this.x < other.x) {
            // I am left of them
            return 2;
        } else {
            // I am right of them
            return 3;
        }
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
