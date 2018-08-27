public class Board {
    public int width;
    public int height;
    Point[][] grid;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new Point[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = new Point(x, y);
            }
        }
    }

    public void addSnake(Snake snake) {
        for (int i = 0; i < snake.corners.length - 1; i++) {
            Point a = snake.corners[i];
            Point b = snake.corners[i+1];

            if (a.x == b.x) {
                int minY = Math.min(a.y, b.y);
                int maxY = Math.max(a.y, b.y);
                for (int y = minY; y <= maxY; y++) {
                    grid[a.x][y].owner = snake.id;
                }
            } else if (a.y == b.y){
                int minX = Math.min(a.x, b.x);
                int maxX = Math.max(a.x, b.x);
                for (int x = minX; x <= maxX; x++) {
                    grid[x][a.y].owner = snake.id;
                }
            }
        }
    }

    public Point at(int x, int y) {
        return grid[x][y];
    }
}
