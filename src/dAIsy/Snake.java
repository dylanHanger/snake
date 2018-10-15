package dAIsy;

class Snake {
    public final boolean alive;
    public final int id;
    public final int length;
    public final int kills;
    public final Point[] body;

    public Point head;
    private Point neck;
    public Point tail;

    public Snake(int id, String[] data) {
        this.id = id;
        alive = data[0].equals("alive");
        length = Integer.parseInt(data[1]);
        kills = Integer.parseInt(data[2]);

        body = new Point[data.length - 3];
        for (int i = 3; i < data.length; i++) {
            body[i-3] = new Point(data[i]);
        }

        if (!alive) return;

        head = body[0];
        neck = body[1];
        tail = body[body.length - 1];
    }

    public int getHeading() {
        if (head.x == neck.x) {
            if (head.y > neck.y) {
                return Direction.SOUTH;
            }
            return Direction.NORTH;
        } else {
            if (head.x > neck.x) {
                return Direction.EAST;
            }
            return Direction.WEST;
        }
    }

    public String toString() {
        switch (id) {
            case 1: return "Red";
            case 2: return "Green";
            case 3: return "Blue";
            case 4: return "Mustard";
            case 5: return "Pink";
            case 6: return "Purple";
            case 7: return "Cyan";
            case 8: return "Yellow";
            default: return "snake "+id;
        }
    }
}
