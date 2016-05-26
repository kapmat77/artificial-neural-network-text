package org.network.server;

/**
 * Created by Kapmat on 2016-05-24.
 */
public class ClockUpdater implements Runnable {

    private static double clock = 0;
    private static boolean activeClock = true;
    private static boolean updateChargeLevel = true;
    private JsonSender jsonSender;

    public ClockUpdater() {
        jsonSender = JsonSender.getJsonSender();
    }

    public static double getTime() {
        return roundDouble(clock/Math.pow(10,9));
    }

    public static void zeroTime() {
        clock = 0;
    }

    public static void setActiveClock(boolean active) {
        activeClock = active;
    }

    public static boolean getActiveClock() {
        return activeClock;
    }

    public static void setUpdateChargeLevel(boolean update) {
        updateChargeLevel = update;
    }

    public static boolean getUpdateChargeLevel() {
        return updateChargeLevel;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        while(activeClock) {
            clock = System.nanoTime() - startTime;
//            System.out.println("CLOCK: " +roundDouble(clock/Math.pow(10,9)));
            jsonSender.sendUpdateTimerJson(roundDouble(clock/Math.pow(10,9)));
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static double roundDouble(double oldVar) {
        double newVar = oldVar*100;
        int helperValue = (int) newVar;
        newVar = ((double) helperValue)/100;
        return newVar;
    }
}
