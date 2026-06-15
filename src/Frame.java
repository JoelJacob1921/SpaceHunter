import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Frame extends JPanel implements ActionListener, MouseListener, KeyListener {
    
    public static void main(String[] args) {
        new Frame();
    }

    // --- Game States ---
    private enum State {
        MENU, TUTORIAL, GAME, SHOP, GAME_OVER, VICTORY
    }
    private State currentState = State.MENU;

    private SaveManager saveManager = new SaveManager();
    private int highScore = saveManager.highScore;

    // --- Window Dimensions ---
    int width = 1920;
    int height = 1080;

    // --- UI Button Bounds ---
    private final Rectangle startButtonBounds = new Rectangle(860, 510, 200, 50);
    private final Rectangle tutorialButtonBounds = new Rectangle(860, 580, 200, 50); 
    private final Rectangle resumeButtonBounds = new Rectangle(860, 650, 200, 50); 
    private final Rectangle exitToMenuBounds = new Rectangle(1920 - 150 - 30, 20, 150, 40);
    
    // Expanded button width to 260 and recentered at X = 830
    private final Rectangle nextWaveButtonBounds = new Rectangle(830, 850, 260, 60);

    // --- Interactive Shop Upgrade Nodes Box Coordinates ---
    private final Rectangle weaponCard = new Rectangle(260, 350, 240, 320);
    private final Rectangle damageCard = new Rectangle(560, 350, 240, 320);
    private final Rectangle healthCard = new Rectangle(860, 350, 240, 320);
    private final Rectangle pierceCard = new Rectangle(1160, 350, 240, 320);
    private final Rectangle speedCard  = new Rectangle(1460, 350, 240, 320);
    
    // --- Game Components ---
    Background bg = new Background(width, height); 
    ArrayList<Ship> enemies = new ArrayList<>();
    PlayerShip player = new PlayerShip(width/2 - 30, height - 250); 
    ArrayList<Laser> lasers = new ArrayList<>();
    ArrayList<Coin> coins = new ArrayList<>();
    Random rand = new Random();
    
    SimpleAudioPlayer laserSoundPlayer;

    // --- Gameplay Economy Variables ---
    int score = 0;
    int coinsBanked = 0;
    int enemyKillCounter = 0; 
    int hp = 15; 
    int maxHp = 15;
    
    boolean gameOver = false;
    boolean victory = false;
    
    int wave = 1;
    int enemiesSpawnedThisWave = 0;
    int waveTransitionTimer = 0; 
    boolean inTransition = false;

    // --- Core RPG Stat Level Progression Tracking (Starts at 1, Maxes at 5) ---
    int lvlSpread = 1;
    int lvlDamage = 1;
    int lvlHealth = 1;
    int lvlPierce = 1;
    int lvlSpeed  = 1;

    int burstTimer = 0; 
    int enemiesToBurst = 0;
    boolean burst75 = false, burst50 = false, burst25 = false;

    int bossTimerFrames = 30 * 60; 
    boolean bossTimerActive = false;

    public Frame() {
        JFrame f = new JFrame("Space Hunter - Elite");
        
        this.setPreferredSize(new Dimension(width, height));
        f.add(this);
        f.setResizable(false);
        f.pack(); 
        
        f.addMouseListener(this);
        f.addKeyListener(this);
        f.setFocusable(true);
        
        laserSoundPlayer = new SimpleAudioPlayer("lasersound.wav", false);
        
        Timer t = new Timer(16, this);
        t.start();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    //resets all variables, allows the start of a new Game when a game ends
    public void resetGame() {
        enemies.clear();
        lasers.clear();
        coins.clear();
        score = 0;
        coinsBanked = 0;
        enemyKillCounter = 0;
        
        lvlSpread = 1;
        lvlDamage = 1;
        lvlHealth = 1;
        lvlPierce = 1;
        lvlSpeed  = 1;
        
        maxHp = 15;
        hp = 15;
        
        gameOver = false;
        victory = false;
        burst75 = false; burst50 = false; burst25 = false;
        burstTimer = 0;
        enemiesToBurst = 0;
        
        player.x = width / 2 - 30;
        player.y = height - 250;
        startWave(1);
    }

    
    //loads a saved game if the user saves and exits. Restarts the wave
    public void loadSavedSession() {
        saveManager.loadGameData(); 
        if (!saveManager.hasSaveFile) return;

        enemies.clear();
        lasers.clear();
        coins.clear();

        this.score = saveManager.savedScore;
        this.hp = saveManager.savedHP;
        
        this.lvlDamage = saveManager.savedDamage;
        this.lvlSpread = saveManager.savedSpread;
        this.lvlPierce = saveManager.savedPiercing ? 3 : 1; 
        this.lvlHealth = 1; 
        this.lvlSpeed = 1;
        
        this.maxHp = 15 + ((lvlHealth - 1) * 5);
        this.coinsBanked = 0; 

        this.gameOver = false;
        this.victory = false;
        this.player.x = width / 2 - 30;
        this.player.y = height - 250;

        startWave(saveManager.savedWave);
    }

    public void startWave(int waveNum) {
        wave = waveNum;
        enemiesSpawnedThisWave = 0;
        inTransition = true;
        waveTransitionTimer = 90; 
        
        if (wave == 5) {
            bossTimerFrames = 30 * 60; 
            bossTimerActive = true;
        } else {
            bossTimerActive = false;
        }
    }

    
    //class that organizes enemy spawn
    public void spawnEnemy() {
        if (wave == 5) {
            Ship boss = null;
            for (Ship s : enemies) if (s.type == 4) boss = s;

            if (boss == null && enemiesSpawnedThisWave == 0) {
                enemies.add(new Ship(width/2 - 90, -150, 4, wave));
                enemiesSpawnedThisWave++;
            } else if (boss != null) {
                if (rand.nextInt(20) == 1 && enemies.size() < 15) {
                    spawnFromBoss(boss);
                }
                
                // --- FIXED: DYNAMIC 25% REINFORCEMENT PERCENTAGE INTERVALS ---
                if (boss.hp <= boss.maxHp * 0.75 && !burst75) { enemiesToBurst = 10; burstTimer = 240; burst75 = true; }
                if (boss.hp <= boss.maxHp * 0.50 && !burst50) { enemiesToBurst = 15; burstTimer = 240; burst50 = true; }
                if (boss.hp <= boss.maxHp * 0.25 && !burst25) { enemiesToBurst = 20; burstTimer = 240; burst25 = true; }

                if (burstTimer > 0 && enemiesToBurst > 0) {
                    if (burstTimer % (240 / enemiesToBurst) == 0) {
                        spawnFromBoss(boss);
                    }
                    burstTimer--;
                }
            }
            return;
        }

        int maxEnemies = 30 + (wave * 20); 
        if (enemiesSpawnedThisWave >= maxEnemies) return;

        if (enemies.size() < 8 + (wave * 2)) {
            int type = 1;
            int roll = rand.nextInt(100);
            if (wave >= 3) {
                if (roll > 65) type = (roll > 85) ? 3 : 2; 
            } else if (wave >= 2 && roll > 75) {
                type = 2;
            }
            
            enemies.add(new Ship(rand.nextInt(width - 100), -100, type, wave));
            enemiesSpawnedThisWave++;
        }
    }

    private void spawnFromBoss(Ship boss) {
        int roll = rand.nextInt(100);
        int type = (roll > 80) ? 3 : (roll > 65 ? 2 : 1);
        enemies.add(new Ship(boss.x + 80, boss.y + 150, type, wave));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        bg.paint(g); 
        
        switch (currentState) {
            case MENU:
                drawMenu(g);
                break;
            case TUTORIAL:
                drawTutorial(g);
                break;
            case GAME:
                runGameLoop(g);
                break;
            case SHOP:
                drawShop(g);
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
            case VICTORY:
                drawVictory(g);
                break;
        }
    }

    
    //code that draws all numerical labels
    private void drawMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.CYAN);
        g.setFont(new Font("Courier New", Font.BOLD, 72));
        g.drawString("SPACE HUNTER", width / 2 - 240, 250);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("ALL-TIME HIGH SCORE: " + highScore, width / 2 - 180, 350);

        g.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.draw(startButtonBounds);
        g.setColor(new Color(0, 255, 255, 40)); 
        g2.fill(startButtonBounds);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("NEW GAME", startButtonBounds.x + 35, startButtonBounds.y + 34);

        g.setColor(Color.WHITE);
        g2.draw(tutorialButtonBounds);
        g.setColor(new Color(255, 165, 0, 40)); 
        g2.fill(tutorialButtonBounds);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("TUTORIAL", tutorialButtonBounds.x + 43, tutorialButtonBounds.y + 34);

        if (saveManager.hasSaveFile) {
            g.setColor(Color.WHITE);
            g2.draw(resumeButtonBounds);
            g.setColor(new Color(0, 255, 0, 40)); 
            g2.fill(resumeButtonBounds);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("RESUME", resumeButtonBounds.x + 52, resumeButtonBounds.y + 34);
        }
    }

    
    //code responsible for drawing entire tutorial screen
    private void drawTutorial(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Courier New", Font.BOLD, 54));
        g.drawString("FLIGHT ACADEMY PROTOCOLS", width / 2 - 400, 150);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("1. FLIGHT MECHANICS & OBJECTIVES", 300, 260);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("\u2022 Use the MOUSE to steer. Your engines track your cursor continuously in both X and Y vector grids.", 320, 300);
        g.drawString("\u2022 Left-click to fire. Every 10th enemy down releases local validation COINS.", 320, 330);
        g.drawString("\u2022 Drive your hull directly through floating golden coins to bank them for intermediate tech upgrades.", 320, 360);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("2. LOSS OF SHIP CONDITIONS", 300, 440);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("\u2022 Escaping hostile ships bypass your weapons defense arrays and strip 1 structural HP away.", 320, 480);
        g.drawString("\u2022 Ramming directly into an enemy ship compromises structural shields, dealing 2 HP of impact damage.", 320, 510);
        g.drawString("\u2022 Dropping to 0 HP or exhausting the Final Wave Boss ticking clock values triggers complete systemic game failure.", 320, 540);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("3. HANGAR INTERMISSION MATRIX", 300, 620);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("\u2022 Complete a full wave stage to dock your vessel into the central Weapon Refitting Bay Shop.", 320, 660);
        g.drawString("\u2022 Upgrade 5 structural modules up to Level 5. Next tier costs scale dynamically (Lvl 2 = 1 Coin, Lvl 3 = 2, Lvl 4 = 3, Lvl 5 = 4).", 320, 690);

        g.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        g2.draw(exitToMenuBounds);
        g.setColor(new Color(255, 0, 0, 30));
        g2.fill(exitToMenuBounds);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("BACK TO MENU", exitToMenuBounds.x + 24, exitToMenuBounds.y + 25);
    }

    
    //draws the Shop
    private void drawShop(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Courier New", Font.BOLD, 54));
        g.drawString("ORBITAL REPAIR & REFIT HANGAR", width / 2 - 470, 120);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("BANKED COINS: " + coinsBanked, width / 2 - 160, 200);
        
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        g.drawString("Click a sub-system card to purchase adjustments. Costs scale per progressive upgrade.", width / 2 - 310, 240);

        // --- Render the 5 Upgrade Cards ---
        drawShopCard(g2, weaponCard, "WIDE SPREAD", lvlSpread, "[Lvl " + lvlSpread + " -> " + (lvlSpread == 5 ? "MAX" : (lvlSpread+1)) + "]", (lvlSpread == 5 ? 0 : lvlSpread), Color.ORANGE);
        drawShopCard(g2, damageCard, "LASER DAMAGE", lvlDamage, "[Lvl " + lvlDamage + " -> " + (lvlDamage == 5 ? "MAX" : (lvlDamage+1)) + "]", (lvlDamage == 5 ? 0 : lvlDamage), Color.RED);
        drawShopCard(g2, healthCard, "HULL HEALTH", lvlHealth, "[Lvl " + lvlHealth + " -> " + (lvlHealth == 5 ? "MAX" : (lvlHealth+1)) + "]", (lvlHealth == 5 ? 0 : lvlHealth), Color.GREEN);
        drawShopCard(g2, pierceCard, "BEAM PIERCE", lvlPierce, "[Lvl " + lvlPierce + " -> " + (lvlPierce == 5 ? "MAX" : (lvlPierce+1)) + "]", (lvlPierce == 5 ? 0 : lvlPierce), Color.MAGENTA);
        drawShopCard(g2, speedCard, "ENGINE SPEED", lvlSpeed, "[Lvl " + lvlSpeed + " -> " + (lvlSpeed == 5 ? "MAX" : (lvlSpeed+1)) + "]", (lvlSpeed == 5 ? 0 : lvlSpeed), Color.CYAN);

        // Resized and Center-Balanced Action Launch button
        g.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(3));
        g2.draw(nextWaveButtonBounds);
        g.setColor(new Color(0, 255, 0, 30));
        g2.fill(nextWaveButtonBounds);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("LAUNCH NEXT WAVE", nextWaveButtonBounds.x + 24, nextWaveButtonBounds.y + 38);
    }

    private void drawShopCard(Graphics2D g2, Rectangle r, String title, int currentLvl, String desc, int cost, Color accents) {
        g2.setColor(Color.DARK_GRAY);
        g2.fill(r);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(accents);
        g2.draw(r);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(title, r.x + 20, r.y + 40);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(desc, r.x + 20, r.y + 80);

        for(int i = 1; i <= 5; i++) {
            if(i <= currentLvl) g2.setColor(accents);
            else g2.setColor(Color.BLACK);
            g2.fillRect(r.x + 20 + (i * 30), r.y + 140, 20, 15);
            g2.setColor(Color.GRAY);
            g2.drawRect(r.x + 20 + (i * 30), r.y + 140, 20, 15);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        if (currentLvl == 5) {
            g2.setColor(Color.YELLOW);
            g2.drawString("MAX tier", r.x + 20, r.y + 260);
        } else {
            g2.setColor(Color.YELLOW);
            g2.drawString("COST: " + cost + " COINS", r.x + 20, r.y + 260);
        }
    }

    private void runGameLoop(Graphics g) {
        if (gameOver) { 
            highScore = Math.max(score, highScore);
            saveManager.clearMidGameSave(); 
            currentState = State.GAME_OVER; 
            return; 
        }
        if (victory) { 
            highScore = Math.max(score, highScore);
            saveManager.clearMidGameSave(); 
            currentState = State.VICTORY; 
            return; 
        }

        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        g2.draw(exitToMenuBounds);
        g.setColor(new Color(255, 0, 0, 30));
        g2.fill(exitToMenuBounds);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("SAVE & EXIT", exitToMenuBounds.x + 28, exitToMenuBounds.y + 25);

        if (inTransition) {
            drawWaveMessage(g);
            if (--waveTransitionTimer <= 0) inTransition = false;
            return; 
        }

        if (bossTimerActive) {
            bossTimerFrames--;
            if (bossTimerFrames <= 0) {
                bossTimerFrames = 0;
                gameOver = true;
            }
        }

        checkCollisions();
        spawnEnemy();

        for (int i = 0; i < enemies.size(); i++) {
            Ship s = enemies.get(i);
            s.paint(g); 
            if (s.y > height) { hp--; enemies.remove(i--); }
            else if (!s.visible) {
                if (s.type == 4) {
                    victory = true; 
                    bossTimerActive = false;
                }
                enemies.remove(i--);
            }
        }

        if (wave < 5 && enemies.isEmpty() && enemiesSpawnedThisWave >= (30 + (wave * 20))) {
            currentState = State.SHOP; 
            return;
        }

        for (int i = 0; i < lasers.size(); i++) {
            lasers.get(i).paint(g);
            if (lasers.get(i).y < 0) lasers.remove(i--);
        }
        
        for (int i = 0; i < coins.size(); i++) {
            Coin c = coins.get(i);
            c.update();
            c.paint(g);
            if (c.y > height) coins.remove(i--);
        }
        
        player.paint(g); 
        drawUI(g);
        if (wave == 5) drawBossUI(g);

        if (hp <= 0) { hp = 0; gameOver = true; bossTimerActive = false; }
    }

    public void checkCollisions() {
        // 1. Laser Collisions with Enemies (Kept exactly as you had it)
        for (int i = 0; i < lasers.size(); i++) {
            Laser l = lasers.get(i);
            boolean laserDestroyed = false;
            
            for (int j = 0; j < enemies.size(); j++) {
                Ship s = enemies.get(j);
                
                if (s.visible && l.getHitbox().intersects(s.getHitbox())) {
                    if (l.piercing) {
                        s.hp -= Math.min(l.damage, 2);
                    } else {
                        s.hp -= l.damage;
                    }
                    
                    if (s.hp <= 0 && s.visible) {
                        s.visible = false;
                        score += (s.type * 10);
                        enemyKillCounter++;
                        
                        if (enemyKillCounter % 10 == 0) {
                            coins.add(new Coin(s.x + 20, s.y + 20));
                        }
                    }
                    
                    l.hitsRemaining--;
                    if (l.hitsRemaining <= 0 || s.type == 2) {
                        laserDestroyed = true;
                        break;
                    }
                }
            }
            if (laserDestroyed) {
                lasers.remove(i);
                i--;
            }
        }
        
        // Create the updated user tracking hitbox context
        Rectangle playerHitbox = new Rectangle(player.x, player.y, 60, 60);
        
        // 2. Player Ramming/Colliding with Alien Ships (INSTANT DEATH ON BOSS)
        for (int i = 0; i < enemies.size(); i++) {
            Ship s = enemies.get(i);
            
            if (s.visible && playerHitbox.intersects(s.getHitbox())) {
                
                if (s.type == 4) {
                    // --- MOTHERSHIP COLLISION BEHAVIOR ---
                    hp = 0; // Instantly obliterates the player ship's health pool
                    
                    // The boss takes 0 damage and s.visible remains true.
                    // Because hp becomes 0, runGameLoop will correctly catch this 
                    // and transition directly to GAME_OVER on this exact frame.
                    break; 
                    
                } else {
                    // --- STANDARD ENEMY COLLISION BEHAVIOR ---
                    hp -= 1; 
                    s.visible = false; 
                    score += (s.type * 10);
                    enemyKillCounter++;
                    
                    if (enemyKillCounter % 10 == 0) {
                        coins.add(new Coin(s.x + 20, s.y + 20));
                    }
                }
            }
        }
        
        // 3. Collecting Floating Coins (Kept exactly as you had it)
        for (int i = 0; i < coins.size(); i++) {
            Coin c = coins.get(i);
            if (c.getHitbox().intersects(playerHitbox)) {
                coinsBanked++;
                coins.remove(i--);
            }
        }
    }

    public void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("SCORE: " + score, 30, 40);
        g.drawString("WAVE: " + (wave == 5 ? "FINAL" : wave), 30, 100);
        g.setColor(Color.RED);
        g.drawString("PLAYER HP: " + hp + " / " + maxHp, 30, 70);
        
        g.setColor(Color.YELLOW);
        g.drawString("COINS BANKED: " + coinsBanked, 30, 130);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.CYAN);
        g.drawString("LASER TIER: Lvl " + lvlDamage + " | SPREAD: Lvl " + lvlSpread, 30, 160);

        if (bossTimerActive) {
            double secondsLeft = bossTimerFrames / 60.0;
            g.setColor(secondsLeft < 10.0 ? Color.RED : Color.YELLOW);
            g.setFont(new Font("Courier New", Font.BOLD, 28));
            g.drawString(String.format("CRITICAL COUNTDOWN: %.1fs", secondsLeft), width - 390, 100);
        }
    }

    public void drawBossUI(Graphics g) {
        Ship boss = null;
        for (Ship s : enemies) if (s.type == 4) boss = s;
        if (boss != null) {
            int bw = 600, x = (width - bw) / 2, y = 40;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, bw, 20);
            g.setColor(Color.RED);
            g.fillRect(x, y, (int)(bw * ((double)boss.hp / boss.maxHp)), 20);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, bw, 20);
        }
    }

    public void drawWaveMessage(Graphics g) {
        g.setColor(wave == 5 ? Color.ORANGE : Color.CYAN);
        g.setFont(new Font("Courier New", Font.BOLD, 80));
        String txt = (wave == 5 ? "Boss Fight" : "WAVE " + wave);
        g.drawString(txt, width/2 - 160, height/2);
    }

    public void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Courier New", Font.BOLD, 70));
        g.drawString("GAME OVER", width/2 - 200, height/2);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        g.drawString("Click anywhere to return to Menu", width/2 - 170, height/2 + 80);
    }

    public void drawVictory(Graphics g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Courier New", Font.BOLD, 70));
        g.drawString("SYSTEMS CLEARED: VICTORY!", width/2 - 300, height/2);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        g.drawString("Click anywhere to return to Menu", width/2 - 170, height/2 + 80);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (currentState == State.GAME && !gameOver && !victory && !inTransition) {
            bg.update(); 
            
            Point m = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(m, this);
            
            int targetX = (int)m.getX() - 30;
            int targetY = (int)m.getY() - 30;
            
            double engineTrackingResponse = 0.06 + (lvlSpeed * 0.03); 
            
            player.x += (targetX - player.x) * engineTrackingResponse;
            player.y += (targetY - player.y) * engineTrackingResponse;

            if (player.x < 0) player.x = 0;
            if (player.x > width - 80) player.x = width - 80;
            if (player.y < 100) player.y = 100;
            if (player.y > height - 100) player.y = height - 100;
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent m) {
        Point clickPoint = m.getPoint();

        if (currentState == State.MENU) {
            if (startButtonBounds.contains(clickPoint)) {
                resetGame();
                currentState = State.GAME;
            }
            else if (tutorialButtonBounds.contains(clickPoint)) {
                currentState = State.TUTORIAL;
            }
            else if (saveManager.hasSaveFile && resumeButtonBounds.contains(clickPoint)) {
                loadSavedSession();
                currentState = State.GAME;
            }
        } 
        else if (currentState == State.TUTORIAL) {
            if (exitToMenuBounds.contains(clickPoint)) {
                currentState = State.MENU;
            }
        }
        else if (currentState == State.SHOP) {
            if (weaponCard.contains(clickPoint) && lvlSpread < 5 && coinsBanked >= lvlSpread) {
                coinsBanked -= lvlSpread;
                lvlSpread++;
            }
            else if (damageCard.contains(clickPoint) && lvlDamage < 5 && coinsBanked >= lvlDamage) {
                coinsBanked -= lvlDamage;
                lvlDamage++;
            }
            else if (healthCard.contains(clickPoint) && lvlHealth < 5 && coinsBanked >= lvlHealth) {
                coinsBanked -= lvlHealth;
                lvlHealth++;
                maxHp += 5; 
                hp += 5;    
            }
            else if (pierceCard.contains(clickPoint) && lvlPierce < 5 && coinsBanked >= lvlPierce) {
                coinsBanked -= lvlPierce;
                lvlPierce++;
            }
            else if (speedCard.contains(clickPoint) && lvlSpeed < 5 && coinsBanked >= lvlSpeed) {
                coinsBanked -= lvlSpeed;
                lvlSpeed++;
            }
            else if (nextWaveButtonBounds.contains(clickPoint)) {
                currentState = State.GAME;
                startWave(wave + 1);
            }
        }
        else if (currentState == State.GAME) {
            if (exitToMenuBounds.contains(clickPoint)) {
                highScore = Math.max(score, highScore);
                saveManager.saveGameData(highScore, wave, score, hp, lvlDamage, lvlSpread, (lvlPierce >= 3));
                saveManager.loadGameData(); 
                currentState = State.MENU;
                return;
            }

            if (!gameOver && !victory && !inTransition) {
                int laserPierceCapacity = lvlPierce; 
                
                switch(lvlSpread) {
                    case 1: 
                        lasers.add(new Laser(player.x + 28, player.y, lvlDamage, laserPierceCapacity));
                        break;
                    case 2: 
                        lasers.add(new Laser(player.x + 12, player.y, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 44, player.y, lvlDamage, laserPierceCapacity));
                        break;
                    case 3: 
                        lasers.add(new Laser(player.x + 28, player.y - 5, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 4, player.y + 10, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 52, player.y + 10, lvlDamage, laserPierceCapacity));
                        break;
                    case 4: 
                        lasers.add(new Laser(player.x + 8, player.y, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 48, player.y, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x - 12, player.y + 15, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 68, player.y + 15, lvlDamage, laserPierceCapacity));
                        break;
                    case 5: 
                        lasers.add(new Laser(player.x + 28, player.y - 10, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 8, player.y, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 48, player.y, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x - 16, player.y + 20, lvlDamage, laserPierceCapacity));
                        lasers.add(new Laser(player.x + 72, player.y + 20, lvlDamage, laserPierceCapacity));
                        break;
                }
                
                if (laserSoundPlayer != null) {
                    laserSoundPlayer.play();
                }
            }
        } 
        else if (currentState == State.GAME_OVER || currentState == State.VICTORY) {
            currentState = State.MENU;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentState == State.GAME && e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (wave < 5) {
                enemies.clear();
                currentState = State.SHOP; 
            } else if (wave == 5) {
                victory = true;
                bossTimerActive = false;
                enemies.clear();
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent arg0) {}
    @Override public void mouseEntered(MouseEvent arg0) {}
    @Override public void mouseExited(MouseEvent arg0) {}
    @Override public void mouseReleased(MouseEvent arg0) {}
    @Override public void keyTyped(KeyEvent arg0) {}
}