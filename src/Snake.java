public class Snake {
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

    public int getHeading(){
        Point head = corners[0];
        Point neck = corners[1];

        if (head.x == neck.x) {
            if (head.y < neck.y) {
                // up
                return 0;
            } else {
                // down
                return 1;
            }
        } else if (head.y == neck.y) {
            if (head.x > neck.x) {
                // right
                return 3;
            } else {
                // left
                return 2;
            }
        }
        return 6;
    }

}
