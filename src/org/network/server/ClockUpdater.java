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
        return roundDouble(clock);
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
        clock = 0;
        while(activeClock) {
            clock = clock + 0.01;
//            System.out.println("Clock: " + roundDouble(clock));
            jsonSender.sendUpdateTimerJson(roundDouble(clock));
            try {
                Thread.sleep(10);
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
