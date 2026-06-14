import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Handles individual projectile behaviors, visual rendering properties,
 * and piercing capabilities based on weapon tiers.
 */
public class Laser {
    // --- Vector Coordinates & Physics ---
    public int x, y;
    public int speed = 12; // Flat vertical upward velocity per refresh cycle
    
    // --- Combat System Metrics ---
    public int damage;
    public int hitsRemaining; 
    public boolean piercing;

    /**
     * Initializes a standard or piercing laser projectile instance.
     */
    public Laser(int x, int y, int damage, int allowedHits) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.hitsRemaining = allowedHits;
        // Projectiles with more than 1 allowed hit are flagged as piercing beam arrays
    }

    /**
     * Upgrades vector position, assesses beam composition types, and paints to screen.
     */
    public void paint(Graphics g) {
        // Decrement Y to continuously push the projectile upwards toward the top edge
        this.y -= speed;
        
        // Render stylized energy core glows matching structural properties
        if (piercing) {
            // Magenta outer plasma shell for heavy anti-armor piercing beams
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, 5, 22);
            // Hyper-condensed white inner core for energy depth representation
            g.setColor(Color.WHITE);
            g.fillRect(x + 1, y + 2, 3, 18);
        } else {
            // Cyan profile representation for standard-issue tier 1 weapon bolts
            g.setColor(Color.CYAN);
            g.fillRect(x, y, 4, 16);
        }
    }

    /**
     * Generates a structural boundary matrix box for computing localized laser collisions.
     */
    public Rectangle getHitbox() {
        return new Rectangle(x, y, 5, 20);
    }
}