package org.network.server;

import org.network.model.Node;

import java.util.List;

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


    public ChargeUpdater() {
        jsonSender = JsonSender.getJsonSender();
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

    @Override
    public void run() {
        while(true) {
            try {
                for (Node neuron: neuralList) {
//                    if (neuron.getName().equals("IS") || neuron.getName().equals("MONKEY")) {

                        updateChargingLevel(neuron);
                        jsonSender.sendUpdateChargeLevel(neuron);

                        if (neuron.getChargingLevel()==-1) {
//                            Thread.sleep(1000);
                        }
//                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateChargingLevel(Node node) throws InterruptedException {
        double minusPart = 0;
        double chLevel = 0;
        System.out.println("ChargingLevel: " + node.getChargingLevel());
//        System.out.println("Coeff Sum: " + node.getCoeffSum());
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
            jsonSender.sendUpdateChargeLevel(node);
            Thread.sleep(1000);
            chLevel = -1;
            jsonSender.sendActiveNeuronJson(node, "#BBA2FF");
        }

        if (node.getChargingLevel()<0 && chLevel>=0 && wasActive) {
            wasActive = false;
            jsonSender.sendActiveNeuronJson(node, "#FFF");
        }

//        chLevel = node.getCoeffSum() - minusPart;
//        System.out.println("ChargingLevel: " + chLevel);

//        System.out.println("Min: " + minusPart);
        node.setChargingLevel(chLevel);

        if(node.getChargingLevel()!=0.0) {
            node.setCoeffSum(chLevel);
        }
    }
}
