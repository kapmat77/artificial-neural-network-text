/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.network.server;

/**
 * @author Kapmat
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.network.model.Coefficients;
import org.network.model.Node;
import org.network.model.Sentence;


public class GraphBuilder {

    private List<Node> neuralList = new ArrayList<>();
    private String coeffString = "";
    private static double time = 0;
    private Thread[] chargeThreads;

    private JsonSender jsonSender;

    private boolean firstActivation = true;


//	public static List<Node> getNeuralList() {
//		return neuralList;
//	}

    public void buildGraph(String data, String activeWord, NodeSessionHandler sessionHandler,
                           String speed) throws InterruptedException {

        jsonSender = JsonSender.getJsonSender();

        if (data.equals("resetAll")) {
            for (int i = 0; i < chargeThreads.length; i++) {
                chargeThreads[i].stop();
            }
            for (Node node: neuralList) {
                node.setChargingLevel(0);
                node.setCoeffSum(0);
                jsonSender.sendUpdateChargeLevel(node);
            }
            sessionHandler.resetNodes();
            sessionHandler.resetLines();
            jsonSender.sendAddSentenceJson(" ");
            chargeThreads = null;
        }

        if (data.equals("killChargeThread")) {
            for (int i = 0; i < chargeThreads.length; i++) {
                chargeThreads[i].stop();
            }
        }

        if (data.equals("update")) {
            String[] activeNeurons = activeWord.split(" ");

            if (firstActivation) {
                jsonSender.sendAddSentenceJson("");
                firstActivation = false;
                sessionHandler.resetNodes();
                sessionHandler.resetLines();
            }

//			Node neuron = findNodeByName("VERY");
//			sendUpdateLinesJson(sessionHandler, neuron);

            Thread.sleep(100);

            if (chargeThreads == null) {
                System.out.println("Hello Threads");
                Runnable[] runChargeUpdaters = new Runnable[neuralList.size()];
                chargeThreads = new Thread[neuralList.size()];

                for (int i = 0; i < runChargeUpdaters.length; i++) {
                    runChargeUpdaters[i] = new ChargeUpdater(neuralList.get(i));
                }

                for (int i = 0; i < runChargeUpdaters.length; i++) {
                    chargeThreads[i] = new Thread(runChargeUpdaters[i]);
                }

                for (int i = 0; i < runChargeUpdaters.length; i++) {
                    chargeThreads[i].start();
                }
            }


            //TODO i do aktualizacji czasu na stronie www
            Runnable runClock = new ClockUpdater();
            Thread clockThread = new Thread(runClock);

            ClockUpdater.setActiveClock(true);
            clockThread.start();

            for (int i = 0; i < activeNeurons.length; i++) {
                if (activeNeurons[i].startsWith("-")) {
                    activeNeurons[i] = activeNeurons[i].replace("-", "").replace(",", ".");
                    Thread.sleep((int) ((Double.valueOf(activeNeurons[i])) * 1000));
                } else {
                    for (Node node: neuralList) {
                        if (node.getName().equalsIgnoreCase(activeNeurons[i])) {
                            jsonSender.sendUpdateSentenceJson("#");
                            break;
                        }
                    }
                    findNodes(activeNeurons[i], sessionHandler, true);
                }
            }
            ClockUpdater.setActiveClock(false);

//			clockThread.stop();

        } else if (data.endsWith(".txt")) {
            jsonSender.setSpeed(speed);
            FileOperations file = new FileOperations();

            List<Sentence> inputSentences = file.readDataFromFile("C:\\Users\\Kapmat\\Desktop\\Intellij\\artificial-neural-network-text\\src\\resources\\" + data);


            Runnable runClock = new ClockUpdater();
            Thread clockThread = new Thread(runClock);

            clockThread.start();

            createGraph(inputSentences, sessionHandler);
            ChargeUpdater.setNeuralList(neuralList);

            clockThread.stop();

            time = ClockUpdater.getTime();



//			if (!chargeThread.isAlive()) {
//				chargeThread.start();
//			}

//			System.out.println("Time: " + time);


//			Runnable runChargeUpdater = new ChargeUpdater();
//			chargeThread = new Thread(runChargeUpdater);
//
//			chargeThread.start();
        }
    }

    private void findNodes(String activeNeuron, NodeSessionHandler sessionHandler, boolean firstNode) throws InterruptedException {
        activeNeuron = activeNeuron.toUpperCase();
        Node neuron = findNodeByName(activeNeuron);

        if (firstNode) {
//			if(neuron.getCoeffSum()>=1) {
            neuron.setCoeffSum(ChargeUpdater.getTeta());
            neuron.setChargingLevel(neuron.getCoeffSum()+1.01);
            jsonSender.sendActiveNeuronJson(neuron, "#00FF33");
            neuron.setChargingLevel(neuron.getCoeffSum());
//				jsonSender.sendUpdateSentenceJson(neuron.getName());
            jsonSender.sendUpdateChargeLevel(neuron);
//			}
//			sendUpdateLinesJson(sessionHandler, neuron);
//			sendUpdateBestLineJson(sessionHandler, neuron);
        } else {
//			neuron.setCoeffSum();
        }
//		jsonSender.sendUpdateNodeJson(neuron);

        List<Node> bestNeigh = new ArrayList<>();
        Node bestNode;
        Boolean first = true;
        Double bestCoeff = 0.0;
        for (Map.Entry<Node, Coefficients> entry : neuron.getNeighCoefficient().entrySet()) {
//			jsonSender.sendUpdateNodeJson(entry.getKey());
            if (first) {
                bestNode = entry.getKey();
                bestCoeff = entry.getValue().getSynapticWeight();
                bestNeigh.add(bestNode);
                first = false;
            } else if (Objects.equals(entry.getValue().getSynapticWeight(), bestCoeff)) {
                bestNode = entry.getKey();
                bestCoeff = entry.getValue().getSynapticWeight();
                bestNeigh.add(bestNode);
            } else if (entry.getValue().getSynapticWeight() >= bestCoeff) {
                bestNeigh.clear();
                bestNode = entry.getKey();
                bestCoeff = entry.getValue().getSynapticWeight();
                bestNeigh.add(bestNode);
            }
        }

        neuron.setBestNeighbours(bestNeigh);
    }

    private void createGraph(List<Sentence> inputSentences, NodeSessionHandler sessionHandler) throws InterruptedException {
        for (Sentence sentence : inputSentences) {
            List<String> neighbours = new ArrayList<>();
            for (String word : sentence.getWords()) {
                neighbours.add(word);
            }
            int index = 0;
            //Json add new sentence
            jsonSender.sendAddSentenceJson(sentence.toString());
            for (String word : sentence.getWords()) {
                Node node = findNodeByName(word);
                index++;
                //Json update single sentence
//				sendUpdateSentenceJson(sessionHandler, word);
                if (node.getName().equals("null")) {
                    node.setName(word);
                    node.setLevel(1);
                    neuralList.add(node);
                    setConnections(node, index, neighbours);
                    //Json add new node
                    jsonSender.sendAddNodeJson(node);
                    //Json update lines
                    jsonSender.sendAddLinesJson(node, coeffString);
                } else {
                    node.increaseLevel();
                    node.updateCoefficients();
                    setConnections(node, index, neighbours);
                    jsonSender.sendAddLinesJson(node, coeffString);
                    //Json update graph
                    jsonSender.sendUpdateNodeJson(node);

                }
//				coeffString = "";
            }
        }
        jsonSender.sendAddSentenceJson("Etap uczenia sieci neuronowej zakończony powodzeniem- ilość węzłów: "
                + neuralList.size());
//		for (Node node: neuralList) {
//			sendActiveNeuronJson(sessionHandler, node);
//			Thread.sleep(700);
//		}

    }

    private void setConnections(Node node, int firstIndex, List<String> neighbours) {
        int secondIndex = 0;
        for (String singleWord : neighbours) {
            secondIndex++;
            if (singleWord.equals(node.getName())) {
                break;
            } else {
                Node secondNode = findNodeByName(singleWord);
                secondNode.addNeighbour(node, false);
                secondNode.addNeighCoefficient(node, countCoefficients(node, firstIndex, secondNode, secondIndex));
//				secondNode.addNeighCoefficient(node, new Coefficients(0.0, 0.0));
                coeffString = coeffString + secondNode.getCoeffAsString();

                node.addNeighbour(secondNode, true);
//				node.addNeighActive(secondNode, node.getNeighCoefficient().get(secondNode));
                node.addNeighCoefficient(secondNode, node.getNeighCoefficient().get(secondNode));
//				node.addNeighCoefficient(secondNode, countCoefficients(node, firstIndex, secondNode, secondIndex));
            }
        }
    }

    private Coefficients countCoefficients(Node firstNode, int firstIndex, Node secondNode, int secondIndex) {

        Double tau = Double.valueOf(firstIndex - secondIndex);
        //Synaptic effectiveness
        Double sE = secondNode.getNeighCoefficient().get(firstNode).getSynapticEffectiveness() + 1 / tau;
        //Synaptic weight
        Double sW = (2 * sE) / (secondNode.getLevel() + sE);

        return new Coefficients(sW, sE);
    }

    /**
     * Bla bla bla
     * @param word
     * @return
     */
    private Node findNodeByName(String word) {
        for (Node singleNode : neuralList) {
            if (singleNode.getName().equals(word)) {
                return singleNode;
            }
        }
        return new Node();
    }


}

