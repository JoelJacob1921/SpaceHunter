import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
//Creates coin class
public class Coin {
    public int x, y;
    public int size = 20;
    private int speed = 3;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        this.y += speed;
    }

    public void paint(Graphics g) {
        // Outer shiny border
        g.setColor(new Color(218, 165, 32)); // Goldenrod
        g.fillOval(x, y, size, size);
        
        // Inner core reflection
        g.setColor(Color.YELLOW);
        g.fillOval(x + 3, y + 3, size - 6, size - 6);
        
        // Center detailing stamp
        g.setColor(new Color(184, 134, 11));
        g.drawOval(x + 5, y + 5, size - 10, size - 10);
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, size, size);
    }
}