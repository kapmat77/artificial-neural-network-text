package org.network.server;

import org.network.model.Node;

/**
 * Created by Kapmat on 2016-05-24.
 */
public class JsonSender {

    private int DELAY = 100;
    private NodeSessionHandler sessionHandler;

    private static JsonSender jsonSender;

    public static JsonSender getJsonSender() {
        if (jsonSender == null) {
            jsonSender = new JsonSender();
        }
        return jsonSender;
    }

    public void setSessionHandler(NodeSessionHandler sH) {
        this.sessionHandler = sH;
    }

    public void setSpeed(String speed) {
        switch (speed) {
            case "1":
                DELAY = 2500;
                break;
            case "2":
                DELAY = 2000;
                break;
            case "3":
                DELAY = 1500;
                break;
            case "4":
                DELAY = 1000;
                break;
            case "5":
                DELAY = 500;
                break;
            case "6":
                DELAY = 300;
                break;
            case "7":
                DELAY = 150;
                break;
            case "8":
                DELAY = 100;
                break;
            case "9":
                DELAY = 50;
                break;
            case "10":
                DELAY = 10;
                break;
        }
    }

    public void sendAddNodeJson(Node node) throws InterruptedException {
        sessionHandler.addNode(node);
        Thread.sleep(DELAY);
    }

    public void sendUpdateNodeJson(Node node) throws InterruptedException {
        sessionHandler.updateNode(node);
        Thread.sleep(DELAY);
    }

    public void sendAddSentenceJson(String sentence) throws InterruptedException {
        sessionHandler.addSentence(sentence);
        Thread.sleep(DELAY);
    }

    public void sendUpdateSentenceJson(String word) throws InterruptedException {
        sessionHandler.updateSentence(word);
    }

    public void sendRemoveSentenceJson() throws InterruptedException {
        sessionHandler.removeSentence();
    }

    public void sendAddLinesJson(Node node, String coeffString) throws InterruptedException {
        sessionHandler.addLines(node, coeffString);
        Thread.sleep(DELAY);
    }

    public void sendUpdateLinesJson(Node node) throws InterruptedException {
        sessionHandler.updateLines(node);
        Thread.sleep(DELAY);
    }

    public void sendActiveNeuronJson(Node node, String color) throws InterruptedException {
        sessionHandler.activeNeuron(node, color);
    }

    public void sendUpdateBestLineJson(Node node) throws InterruptedException {
        sessionHandler.updateBestLine(node);
        Thread.sleep(DELAY);
    }

    public void sendStartTmerJson(Node node) throws InterruptedException {
        sessionHandler.updateBestLine(node);
    }

    public void sendStopTmerJson(Node node) throws InterruptedException {
        sessionHandler.updateBestLine(node);
    }

    public void sendUpdateTimerJson(double clock) {
        sessionHandler.updateTimer(clock);
    }

    public void sendUpdateChargeLevel(Node node) {
        sessionHandler.updateChargeLevel(node);
    }
}
