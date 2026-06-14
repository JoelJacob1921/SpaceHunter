import java.awt.*;

/**
 * Dictates structural configurations for drops dropped by destroyed alien entities.
 */
public class PowerUp {
    // --- Position & Classification Tracker ---
    public int x, y, type;
    public int speed = 3; // Fixed downward fall rate speed profile
    public boolean visible = true;
    
    public PowerUp(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Updates physics position vectors and colors properties dynamically by type classification.
     */
    public void paint(Graphics g) {
        y += speed; // Progress drop downward
        
        // Select custom color signatures based on power-up classification code indices
        switch(type) {
            case 0: g.setColor(Color.CYAN); break;    // [S] - Spread Module Modifiers
            case 1: g.setColor(Color.GREEN); break;   // [H] - Hull Armor Replenishments 
            case 2: g.setColor(Color.RED); break;     // [D] - Damage Amplifiers
            case 3: g.setColor(Color.MAGENTA); break; // [P] - Pierce Bolt Charges
            case 4: g.setColor(Color.ORANGE); break;  // [W] - Weapon Attunement Matrices
        }
        
        // Draw the main colored capsule container
        g.fillOval(x, y, 25, 25);
        g.setColor(Color.WHITE);
        g.drawOval(x, y, 25, 25); // Core border outline overlay
        
        // Render a centered text identifier on top of the drop circle
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String label = switch(type) {
            case 0 -> "S"; case 1 -> "H"; case 2 -> "D";
            case 3 -> "P"; case 4 -> "W"; default -> "?";
        };
        g.drawString(label, x + 8, y + 17);
    }

    /**
     * Boundaries box mapping configuration used for calculating player collection bounds.
     */
    public Rectangle getHitbox() {
        return new Rectangle(x, y, 25, 25);
    }
}