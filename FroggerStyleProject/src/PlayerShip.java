import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.net.URL;

/**
 * Encapsulates user ship texture mapping data, scaling configurations, 
 * and coordinate tracking wrappers for smooth mouse tracking engine updates.
 */
public class PlayerShip {
    private Image img;
    private AffineTransform tx; // Manages complex 2D image scaling and coordinate transformations
    int x, y;
    
    // Increased scale factor by 1.5x from 0.20 to 0.30 to fill out its hitbox matrix
    double scaleWidth = 0.30; 
    double scaleHeight = 0.30;

    /**
     * Configures default player vectors and mounts asset transformations.
     */
    public PlayerShip(int x, int y) {
        this.x = x;
        this.y = y;
        img = getImage("/imgs/spaceship.png");
        tx = AffineTransform.getTranslateInstance(x, y);
        init(x, y); // Initial configuration of image coordinates
    }

    /**
     * Invoked via structural frame loops to refresh translate vectors and draw player ship.
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        init(x, y); // Re-sync translation coordinates with current x and y positions
        g2.drawImage(img, tx, null); // Render ship using the custom transform metrics
    }

    /**
     * Resets transformation matrices back to safe states before applying dynamic scales.
     */
    private void init(double a, double b) {
        tx.setToTranslation(a, b); // Overwrite transform matrices with raw translation offsets
        tx.scale(scaleWidth, scaleHeight); // Scale the ship asset smoothly based on custom width/height
    }

    /**
     * Safe file system resource loader to retrieve spatial ship textures.
     */
    private Image getImage(String path) {
        Image tempImage = null;
        try {
            URL imageURL = PlayerShip.class.getResource(path);
            tempImage = Toolkit.getDefaultToolkit().getImage(imageURL);
        } catch (Exception e) {
            System.err.println("CRITICAL: Spaceship asset failed loading inside PlayerShip node context.");
            e.printStackTrace();
        }
        return tempImage;
    }
}