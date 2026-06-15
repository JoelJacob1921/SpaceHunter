import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

/**
 * Handles the logic for hostile entity variations, individual health scaling parameters, 
 * dynamic movement curves, and dynamic hitbox generation profiles.
 */
public class Ship extends Sprite {
    public int type;    // 1 = Swaying Grunt, 2 = Tank Shield, 3 = Heavy Fast Sweeper, 4 = Boss
    public int hp;
    public int maxHp;
    public boolean visible = true;
    private int speed;
    
    private Random rand = new Random();
    private int horizontalDirection = 1; // Directs horizontal vector offsets (-1 left, 1 right)
    private double swayTimer = 0;        // Used for Type 1 slow wave movement (fed to Sine math)

    /**
     * Maps variant parameters, configures scale bounds, and calculates scaled hit points.
     */
    public Ship(int x, int y, int type, int wave) {
        // Initialize super Sprite template class with standard dynamic textures
        super(
            getAlienFile(type), 
            getAlienFile(type), 
            getAlienFile(type), 
            getAlienFile(type)  
        );
        
        this.x = x;
        this.y = y;
        this.type = type;
        this.dir = 1; // Lock animation direction index onto standard downward configurations

        // --- IMAGE SCALE TRANSFORMATIONS ---
        if (type == 4) {
            // Upscaled from 2.5 to 3.75 (1.5x increase) to match the new dimensions
            this.scaleWidth = 3.75;
            this.scaleHeight = 3.75;
            this.speed = 1; // Boss creeps down into arena view space at a slow pace
            this.horizontalDirection = rand.nextBoolean() ? 1 : -1;
        } else if (type == 3) {
            this.scaleWidth = 1.6;
            this.scaleHeight = 1.6;
            
            // ADJUSTMENT: Fast vertical progression, dialed down horizontal zigzag tracking
            this.speed = 5; // Re-scaled vertical movement up (~1.5x of 3)
            this.horizontalDirection = rand.nextBoolean() ? 6 : -6; // Slowed down horizontal (0.8x of 8)
        } else {
            // Default configuration scales applied cleanly to Type 1 and Type 2 standard grunts
            this.scaleWidth = 2.0;
            this.scaleHeight = 2.0;
        }

        // --- CORE RPG BASE HEALTH STRUCTURING ---
        int baseHp = 1;
        
        if (type == 1) { 
            baseHp = 1;  
            this.speed = 4; 
            this.swayTimer = rand.nextDouble() * 100; // Offset start positions to unsynchronize swarming curves
        }
        if (type == 2) { 
            baseHp = 3;  // Tank variants receive enhanced structural plating
            this.speed = 2; // Platings heavily lower vertical engine speeds
        }
        if (type == 3) { 
            baseHp = 2;  
        } 
        if (type == 4) {
            baseHp = 400; // Boss encounter health parameters
        }

        // Apply health multiplier logic according to active level stage waves
        this.hp = baseHp * wave;
        this.maxHp = this.hp;
    }

    /**
     * Determines which asset string filename map needs routing to Sprite frames.
     */
    private static String getAlienFile(int type) {
        if (type == 2) return "alien2.png"; 
        if (type == 3) return "alien3.png"; 
        if (type == 4) return "boss.png";   
        return "alien1.png"; 
    }

    /**
     * Updates AI vector trajectories and runs super structural drawing modules.
     */
    @Override
    public void paint(Graphics g) {
        // --- CUSTOM AI BEHAVIOR PATTERNS ---
        
        if (type == 1) {
            // Type 1: Sine Wave Sweeper
            this.y += speed;
            swayTimer += 0.05; 
            this.x += (int)(Math.sin(swayTimer) * 2); // Creates a gentle left-to-right sway pattern
            
        } else if (type == 3) {
            // Type 3: Aggressive Diagonal Attacker
            this.y += speed;
            this.x += horizontalDirection;
            
            // Reverse direction if hitting the left wall
            if (this.x <= 0) { 
                this.x = 0; 
                horizontalDirection = 6; 
            }
            // Reverse direction if hitting the right wall (subtracting asset graphic boundaries)
            if (this.x >= 1920 - 80) { 
                this.x = 1920 - 80; 
                horizontalDirection = -6; 
            }
            
        } else if (type == 4) {
            // Type 4: Main Factory Flagship Destroyer Boss
            this.x += (horizontalDirection * 4);
            
            // Left boundary check
            if (this.x <= 50) { 
                this.x = 50; 
                horizontalDirection = 1; 
            }
            // Adjusted right boundary buffer to cleanly match new 180px width context
            if (this.x >= 1920 - 230) { 
                this.x = 1920 - 230; 
                horizontalDirection = -1; 
            }
            
            // Slowly creep down from the top edge until a comfortable combat altitude is achieved
            if (this.y < 80) this.y += speed;
            
        } else {
            // Type 2: Slow, Linear Plated Defender Drone
            this.y += speed; 
        }
        
        super.paint(g); // Delegate drawing transformations to parent class structures
    }

    /**
     * Generates custom hitbox dimension outlines optimized for tracking asymmetrical alien assets.
     */
    public Rectangle getHitbox() {
        if (type == 4) {
            // Guideline dimensions multiplied directly by 1.5x -> 180 width, 225 height
            return new Rectangle(x, y, 180, 225);
        }
        if (type == 3) {
            return new Rectangle(x, y, 80, 80);
        }
        // Base size configurations assigned to Type 1 and Type 2 standard drones
        return new Rectangle(x, y, 100, 100);
    }
}