import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
//Paints the bvlack background with an array to develop stars
public class Background {
    private int width;
    private int height;
    private ArrayList<Star> stars = new ArrayList<>();
    private ShootingStar shootingStar;
    private Random rand = new Random();
//Creates screen size
    public Background(int w, int h) {
        this.width = w;
        this.height = h;
        
        for (int i = 0; i < 200; i++) {
            stars.add(new Star());
        }
        shootingStar = new ShootingStar();
    }

    public void update() {
        for (Star s : stars) {
            s.update();
        }
        shootingStar.update();
    }

    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        for (Star s : stars) {
            int twinkle = rand.nextInt(3) == 0 ? 1 : 0; 
            g.fillOval(s.x, s.y, s.size + twinkle, s.size + twinkle);
        }

        shootingStar.paint(g);
    }
//Code to generate stars
    private class Star {
        int x, y, size, speed;

        Star() {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            size = rand.nextInt(3) + 1;
            speed = rand.nextInt(2) + 1;
        }

        void update() {
            y += speed;
            if (y > height) {
                y = -5;
                x = rand.nextInt(width);
            }
        }
    }
//Generates stars that move across the screen randomly
    private class ShootingStar {
        int x, y, vx, vy, life;

        ShootingStar() {
            reset();
        }

        void reset() {
            x = rand.nextInt(width);
            y = rand.nextInt(height / 2);
            vx = rand.nextInt(10) + 5;
            vy = rand.nextInt(5) + 2;
            life = rand.nextInt(50) + 20;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
            if (life <= 0 || x > width || y > height) {
                if (rand.nextInt(100) == 1) {
                    reset();
                }
            }
        }

        void paint(Graphics g) {
            if (life > 0) {
                g.setColor(new Color(255, 255, 200, 150));
                g.drawLine(x, y, x - vx * 2, y - vy * 2);
            }
        }
    }
}