public class Direction {
    public static final int NONE = -1;
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 2;
    public static final int EAST = 3;
    public static final int LEFT = 4;
    public static final int FORWARD = 5;
    public static final int RIGHT = 6;

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

    public static int next(int direction) {
        switch (direction) {
            case NORTH: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return EAST;
            case EAST: return NORTH;
            default: return direction;
        }
    }

    public static boolean isVertical(int direction) {
        return direction == NORTH || direction == SOUTH;
    }

    public static String toString(int direction) {
        switch (direction) {
            case NORTH: return "NORTH";
            case SOUTH: return "SOUTH";
            case WEST: return "WEST";
            case EAST: return "EAST";
            case LEFT: return "LEFT";
            case FORWARD: return "FORWARD";
            case RIGHT: return "RIGHT";
            default: return "NONE";
        }
    }
}
