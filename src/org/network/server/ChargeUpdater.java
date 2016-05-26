package org.network.server;

import org.network.model.Coefficients;
import org.network.model.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by Kapmat on 2016-05-25.
 */
public class ChargeUpdater implements Runnable {

    private JsonSender jsonSender;
    private static double alfa = 0.6;
    private static double beta = 0.8;
    private static double teta = 1;
    private static List<Node> neuralList;
    private boolean wasActive = false;
    private boolean shortDelay = false;
    private Node currentNode;


    public ChargeUpdater() {
        jsonSender = JsonSender.getJsonSender();
    }

    public ChargeUpdater(Node currentNode) {
        jsonSender = JsonSender.getJsonSender();
        this.currentNode = currentNode;
    }

    public JsonSender getJsonSender() {
        return jsonSender;
    }

    public void setJsonSender(JsonSender jsonSender) {
        this.jsonSender = jsonSender;
    }

    public static List<Node> getNeuralList() {
        return neuralList;
    }

    public static void setNeuralList(List<Node> neuralList) {
        ChargeUpdater.neuralList = neuralList;
    }

    public static double getTeta() {
        return teta;
    }

    public static void setTeta(double teta) {
        ChargeUpdater.teta = teta;
    }

    public static double getAlfa() {
        return alfa;
    }

    public static void setAlfa(double alfa) {
        ChargeUpdater.alfa = alfa;
    }

    public static double getBeta() {
        return beta;
    }

    public static void setBeta(double beta) {
        ChargeUpdater.beta = beta;
    }

    public static void setParameters(String parameters) {
        String[] paramTable = parameters.split(" ");
        alfa = Double.valueOf(paramTable[0]);
        beta = Double.valueOf(paramTable[1]);
        teta = Double.valueOf(paramTable[2]);
    }

    @Override
    public void run() {
        while(true) {
            try {
//                for (Node neuron: neuralList) {
//                    if (neuron.getName().equals("IS") || neuron.getName().equals("MONKEY")) {

                        updateChargingLevel(currentNode);
                        jsonSender.sendUpdateChargeLevel(currentNode);

                        if (currentNode.getChargingLevel()==-1) {
//                            Thread.sleep(1000);
                        }

                if (shortDelay) {
                    Thread.sleep(100);
                    shortDelay = false;
                } else {
                    Thread.sleep(600);
                }
//                if (currentNode.getChargingLevel()==-1) {
//
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateChargingLevel(Node node) throws InterruptedException {
        double minusPart = 0;
        double chLevel = 0;
        if (node.getName().equalsIgnoreCase("dog") && node.getChargingLevel()>0) {
            System.out.println(node.getChargingLevel());
        }
        if (node.getChargingLevel()<0) {
//            minusPart = alfa*node.getChargingLevel()+((alfa-1)*beta*Math.pow(node.getChargingLevel(),2))/teta;
//            System.out.println("MIN <0: " + minusPart);
//            chLevel = node.getChargingLevel() + 0.01;
            chLevel = node.getChargingLevel() + 0.01;
        } else if (node.getChargingLevel()>0 && node.getChargingLevel()<teta) {
//            minusPart = alfa*node.getChargingLevel()+((1-alfa)*beta*Math.pow(node.getChargingLevel(),2))/teta;
//            System.out.println("MIN >0 && <teta: " + minusPart);
            chLevel = node.getChargingLevel() - 0.01;
        } else if (node.getChargingLevel()==0) {
//            minusPart = (-beta)*teta;
//            System.out.println("MIN >=teta: " + minusPart);
            chLevel = 0;
        } else if (node.getChargingLevel()>=teta) {
//            minusPart = (-beta)*teta;
//            System.out.println("MIN >=teta: " + minusPart);
            wasActive = true;
            jsonSender.sendActiveNeuronJson(node, "#00FF33");
            jsonSender.sendUpdateLinesJson(node);
            jsonSender.sendUpdateSentenceJson(node.getName());
            jsonSender.sendUpdateChargeLevel(currentNode);

            for (Map.Entry<Node,Coefficients> entry: node.getNeighCoefficient().entrySet()) {
                entry.getKey().increaseCoeffSum(entry.getValue().getSynapticWeight());
                entry.getKey().setChargingLevel(entry.getKey().getCoeffSum());
            }

            Thread.sleep(500);
            chLevel = -1;
            jsonSender.sendActiveNeuronJson(currentNode, "#BBA2FF");
            shortDelay = true;
        }

        if (chLevel>=0 && wasActive) {
            wasActive = false;
            jsonSender.sendActiveNeuronJson(node, "#FFF");
        } else if (chLevel>0.01 && !wasActive) {
            jsonSender.sendActiveNeuronJson(node, "#AEFFB0");
        } else if (chLevel<0.01 && chLevel>-0.01) {
            jsonSender.sendActiveNeuronJson(node, "#FFF");
        }


        node.setChargingLevel(chLevel);

        if(node.getChargingLevel()!=0.0) {
            node.setCoeffSum(chLevel);
        }
    }
}
