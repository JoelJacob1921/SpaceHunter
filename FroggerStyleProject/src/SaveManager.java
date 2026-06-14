import java.io.*;

/**
 * Handles high scores and progression saves across active player sessions using localized I/O.
 */
public class SaveManager {
    private static final String FILE_NAME = "saveData.txt"; // Target disk file

    // --- Loaded Values Cache ---
    public int highScore = 0;
    public int savedWave = 1;
    public int savedScore = 0;
    public int savedHP = 15;
    public int savedDamage = 1;
    public int savedSpread = 0;
    public boolean savedPiercing = false;
    public boolean hasSaveFile = false; // Evaluated at initialization to enable menu 'RESUME' states

    /**
     * Instantiates manager and runs automatic startup validation check routine loops.
     */
    public SaveManager() {
        loadGameData();
    }

    /**
     * Advanced State Saving: Writes current game configurations to a physical text file.
     */
    public void saveGameData(int currentHighScore, int wave, int score, int hp, int dmg, int spread, boolean pierce) {
        // Ensure local high score variables remain locked onto all-time maximums
        this.highScore = Math.max(this.highScore, currentHighScore);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("HIGHSCORE:" + this.highScore);
            writer.println("WAVE:" + wave);
            writer.println("SCORE:" + score);
            writer.println("HP:" + hp);
            writer.println("DAMAGE:" + dmg);
            writer.println("SPREAD:" + spread);
            writer.println("PIERCING:" + pierce);
            System.out.println("Game state saved successfully to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error writing save file: " + e.getMessage());
        }
    }

    /**
     * Advanced State Loading: Reads and parses the text file line-by-line using a BufferedReader.
     */
    public void loadGameData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            hasSaveFile = false;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split lines into property name and variable value at the separator token (:)
                String[] parts = line.split(":");
                if (parts.length < 2) continue; // Skip malformed lines
                
                String key = parts[0];
                String value = parts[1];

                // Dynamically route properties to their matching structural fields
                switch (key) {
                    case "HIGHSCORE" -> this.highScore = Integer.parseInt(value);
                    case "WAVE"      -> this.savedWave = Integer.parseInt(value);
                    case "SCORE"     -> this.savedScore = Integer.parseInt(value);
                    case "HP"        -> this.savedHP = Integer.parseInt(value);
                    case "DAMAGE"    -> this.savedDamage = Integer.parseInt(value);
                    case "SPREAD"    -> this.savedSpread = Integer.parseInt(value);
                    case "PIERCING"  -> this.savedPiercing = Boolean.parseBoolean(value);
                }
            }
            // An active session is only resumable if the saved wave is greater than 0
            hasSaveFile = (this.savedWave > 0);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading or parsing save file: " + e.getMessage());
            hasSaveFile = false;
        }
    }

    /**
     * Clears active mid-game states upon death/victory while preserving the persistent high score.
     */
    public void clearMidGameSave() {
        // Zero-out level variables, but leave structural records locked onto current high scores
        saveGameData(this.highScore, 0, 0, 0, 0, 0, false);
        hasSaveFile = false;
    }
}