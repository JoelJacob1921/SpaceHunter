import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * Handles sound effects processing and looping audio track parameters.
 */
public class SimpleAudioPlayer {
    Long currentFrame; 
    Clip clip; // Internal audio data resource link
    private boolean loops;
    String status; 
    AudioInputStream audioInputStream; 
    static String filePath; 
  
    /**
     * Pre-loads stream links and prepares mixer structures for play states.
     */
    public SimpleAudioPlayer(String fileName, boolean loop) { 
        this.filePath = fileName;
        this.loops = loop;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip = AudioSystem.getClip(); 
            clip.open(audioInputStream); // Open sample arrays to minimize late performance lags
            if(loop) clip.loop(Clip.LOOP_CONTINUOUSLY); 
        } catch (Exception e) {
            System.err.println("SimpleAudioPlayer failed loading asset file source: " + fileName);
            System.out.println(e);
        }
    } 
  
    /**
     * Commands standard single playback bursts or initializes loop functions.
     */
    public void play() { 
        if (loops) loop();
        else {
            clip.setFramePosition(0); // Rewind playback marker to ensure rapid-fire sounds replay properly
            clip.start(); 
            status = "play"; 
        }
    } 
      
    /**
     * Freezes internal stream positions to allow resuming later.
     */
    public void pause() { 
        if (status.equals("paused")) return; 
        this.currentFrame = this.clip.getMicrosecondPosition(); // Stash current playback timestamp
        clip.stop(); 
        status = "paused"; 
    } 
      
    /**
     * Re-creates the core audio streams and seeks to cached timestamps.
     */
    public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException { 
        if (status.equals("play")) return; 
        clip.close(); 
        resetAudioStream(); 
        clip.setMicrosecondPosition(currentFrame); // Seek back to the cached timestamp
        this.play(); 
    } 
    
    /**
     * Loops the loaded clip infinitely.
     */
    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    } 

    /**
     * Stops current clip streams, rewinds positions, and fires from the beginning.
     */
    public void restart() throws IOException, LineUnavailableException, UnsupportedAudioFileException { 
        clip.stop(); 
        clip.close(); 
        resetAudioStream(); 
        currentFrame = 0L; 
        clip.setMicrosecondPosition(0); 
        this.play(); 
    } 
      
    /**
     * Terminates clip operations and flushes structural stream links.
     */
    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException { 
        currentFrame = 0L; 
        clip.stop(); 
        clip.close(); 
    } 
      
    /**
     * Seeks to a specific microsecond timestamp frame if within valid boundaries.
     */
    public void jump(long c) throws UnsupportedAudioFileException, IOException, LineUnavailableException { 
        if (c > 0 && c < clip.getMicrosecondLength()) { 
            clip.stop(); 
            clip.close(); 
            resetAudioStream(); 
            currentFrame = c; 
            clip.setMicrosecondPosition(c); 
            this.play(); 
        } 
    } 
      
    /**
     * Re-fetches absolute target file tracks to refresh corrupted clip contents.
     */
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException { 
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile()); 
        clip.open(audioInputStream); 
        clip.loop(Clip.LOOP_CONTINUOUSLY); 
    } 
}