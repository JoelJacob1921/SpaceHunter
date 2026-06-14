import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.net.URL;

public class Sprite {
    // Made protected so subclass objects (like Ship) can easily read them if needed
    protected Image forward, backward, left, right; 	
    protected AffineTransform tx;
    int dir = 0; 
    int width, height;
    int x, y, vx, vy;
    double scaleWidth = 1.0, scaleHeight = 1.0; 

    // --- Original Constructor (Used by PlayerShip) ---
    public Sprite() {
        forward   = getImage("/imgs/" + "forwardFile.png");
        backward  = getImage("/imgs/" + "backward.png");
        left      = getImage("/imgs/" + "left.png");
        right     = getImage("/imgs/" + "right.png");
        width = 0; height = 0; x = 0; y = 0; vx = 0; vy = 0;
        tx = AffineTransform.getTranslateInstance(0, 0);
        init(x, y); 
    }

    // --- New Overloaded Constructor (Used by custom Alien Ships) ---
    public Sprite(String forwardPath, String backwardPath, String leftPath, String rightPath) {
        forward  = getImage("/imgs/" + forwardPath);
        backward = getImage("/imgs/" + backwardPath);
        left     = getImage("/imgs/" + leftPath);
        right    = getImage("/imgs/" + rightPath);
        width = 0; height = 0; x = 0; y = 0; vx = 0; vy = 0;
        tx = AffineTransform.getTranslateInstance(0, 0);
        init(x, y); 
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        x += vx; y += vy;	
        init(x, y);
        switch(dir) {
            case 0: g2.drawImage(forward, tx, null); break;
            case 1: g2.drawImage(backward, tx, null); break;
            case 2: g2.drawImage(left, tx, null); break;
            case 3: g2.drawImage(right, tx, null); break;
        }
    }
	
    private void init(double a, double b) {
        tx.setToTranslation(a, b);
        tx.scale(scaleWidth, scaleHeight);
    }

    private Image getImage(String path) {
        Image tempImage = null;
        try {
            URL imageURL = Sprite.class.getResource(path);
            tempImage = Toolkit.getDefaultToolkit().getImage(imageURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempImage;
    }
}