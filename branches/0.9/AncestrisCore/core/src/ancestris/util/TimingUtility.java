package ancestris.util;

/**
 *
 * @author daniel
 */
public class TimingUtility {
    
    private long start;
    static private TimingUtility instance;

    public static TimingUtility getInstance(){
        if (instance == null)
            instance = new TimingUtility();
        return instance;
    }

    public TimingUtility(){
        reset();
    }
    
    /**
     * Reset time origine to now
     */
    public void reset(){
        start = System.currentTimeMillis();
    }

    /**
     * Gets string representation of ellapsed time in milliseconds
     * @return
     */
    public String getTime(){
        return String.format("%05d",System.currentTimeMillis() - start);
    }

}
