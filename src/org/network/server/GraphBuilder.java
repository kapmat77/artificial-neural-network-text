/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.network.server;

/**
 *
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
	private int DELAY = 100;

	public void buildGraph(String data, String activeWord, NodeSessionHandler sessionHandler,
			String speed) throws InterruptedException {
		switch(speed) {
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
		
		if (data.equals("update")) {
			String activeNeuron = activeWord;
			sendAddSentenceJson(sessionHandler, "");
//			Node neuron = findNodeByName("VERY");
//			sendUpdateLinesJson(sessionHandler, neuron);
			sessionHandler.resetLines();
			Thread.sleep(100);
			
//			activeNeuron = "E4";
			
			findNodes(activeNeuron, sessionHandler, true);
		} else {
			
			FileOperations file = new FileOperations();

			List<Sentence> inputSentences = file.readDataFromFile("C:\\Users\\Kapmat\\Desktop\\Java projects\\NetBeans_Projects\\WebsocketGraph\\src\\java\\Resources\\" + data);
			
//			List<Sentence> inputSentences = new ArrayList<>();
			Sentence sentence = new Sentence();
			sentence.addWord("E2");
			sentence.addWord("E8");
//			inputSentences.add(sentence);
			
//			inputSentences.add(sentence);
			
			createGraph(inputSentences, sessionHandler);
		}

	}
	
	private void findNodes(String activeNeuron, NodeSessionHandler sessionHandler, boolean firstNode) throws InterruptedException {
		activeNeuron = activeNeuron.toUpperCase();
		Node neuron = findNodeByName(activeNeuron);
		
		
		if (firstNode) {
			sendActiveNeuronJson(sessionHandler, neuron);
			sendUpdateSentenceJson(sessionHandler, neuron.getName());
//			sendUpdateLinesJson(sessionHandler, neuron);
//			sendUpdateBestLineJson(sessionHandler, neuron);
		}

		List<Node> bestNeigh = new ArrayList<>();
		Node bestNode;
		Boolean first = true;
		Double bestCoeff = 0.0;
		for (Map.Entry<Node, Coefficients> entry: neuron.getNeighCoefficient().entrySet()) {
			if (first){
				bestNode = entry.getKey();
				bestCoeff = entry.getValue().getSynapticWeight();
				bestNeigh.add(bestNode);
				first = false;
			} else if (Objects.equals(entry.getValue().getSynapticWeight(), bestCoeff)) {
				bestNode = entry.getKey();
				bestCoeff = entry.getValue().getSynapticWeight();
				bestNeigh.add(bestNode);
			} else if (entry.getValue().getSynapticWeight()>=bestCoeff) {
				bestNeigh.clear();
				bestNode = entry.getKey();
				bestCoeff = entry.getValue().getSynapticWeight();
				bestNeigh.add(bestNode);
			}
		}
		
		neuron.setBestNeighboyr(bestNeigh);
		
		if (bestCoeff!=0.0) {
			sendUpdateLinesJson(sessionHandler, neuron);
			Thread.sleep(1600);
			sendUpdateBestLineJson(sessionHandler, neuron);
//			Thread.sleep(2000);
			for(Node node: bestNeigh) {
				sendActiveNeuronJson(sessionHandler, node);
				sendUpdateSentenceJson(sessionHandler, node.getName());
				
			}
			for(Node node: bestNeigh) {
				findNodes(node.getName(), sessionHandler, false);
			}
			
		}	
	}

	private void createGraph(List<Sentence> inputSentences, NodeSessionHandler sessionHandler) throws InterruptedException {
		for (Sentence sentence: inputSentences) {
			List<String> neighbours = new ArrayList<>();
			for (String word: sentence.getWords()) {
				neighbours.add(word);
			}
			int index = 0;
			//Json add new sentence
			sendAddSentenceJson(sessionHandler, sentence.toString());
			for (String word: sentence.getWords()) {
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
					sendAddNodeJson(sessionHandler, node);
					//Json update lines
					sendAddLinesJson(sessionHandler, node);
				} else {
					node.increaseLevel();
					node.updateCoefficients();
					setConnections(node, index, neighbours);
					sendAddLinesJson(sessionHandler, node);
					//Json update graph
					sendUpdateNodeJson(sessionHandler, node);
					
				}
//				coeffString = "";
			}
		}
		sendAddSentenceJson(sessionHandler, "Etap uczenia sieci neuronowej zakończony powodzeniem- ilość węzłów: " 
				+ neuralList.size());
//		for (Node node: neuralList) {
//			sendActiveNeuronJson(sessionHandler, node);
//			Thread.sleep(700);
//		}
		
	}
	
	private void setConnections(Node node, int firstIndex, List<String> neighbours) {
		int secondIndex = 0;
		for (String singleWord: neighbours) {
			secondIndex++;
			if (singleWord.equals(node.getName())) {
				break;
			} else {
				Node secondNode = findNodeByName(singleWord);
				secondNode.addNeighbour(node,false);
				secondNode.addNeighCoefficient(node, countCoefficients(node, firstIndex, secondNode, secondIndex));
//				secondNode.addNeighCoefficient(node, new Coefficients(0.0, 0.0));
				coeffString = coeffString + secondNode.getCoeffAsString();

				node.addNeighbour(secondNode,true);
//				node.addNeighActive(secondNode, node.getNeighCoefficient().get(secondNode));
				node.addNeighCoefficient(secondNode, node.getNeighCoefficient().get(secondNode));
//				node.addNeighCoefficient(secondNode, countCoefficients(node, firstIndex, secondNode, secondIndex));
			}
		}
	}

	private Coefficients countCoefficients(Node firstNode, int firstIndex, Node secondNode, int secondIndex) {

		Double tau = Double.valueOf(firstIndex - secondIndex);
		//Synaptic effectiveness
		Double sE = secondNode.getNeighCoefficient().get(firstNode).getSynapticEffectiveness() + 1/tau;
		//Synaptic weight
		Double sW = (2*sE)/(secondNode.getLevel()+sE);

		return new Coefficients(sW,sE);
	}

	/**
	 * Bla bla bla
	 * @param word
	 * @return 
	 */
	private Node findNodeByName(String word) {
		for (Node singleNode: neuralList) {
			if (singleNode.getName().equals(word)) {
				return singleNode;
			}
		}
		return new Node();
	}

	private void sendAddNodeJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.addNode(node);
		Thread.sleep(DELAY);
	}

	private void sendUpdateNodeJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.updateNode(node);
		Thread.sleep(DELAY);
	}

	private void sendAddSentenceJson(NodeSessionHandler sessionHandler, String sentence) throws InterruptedException {
		sessionHandler.addSentence(sentence);
		Thread.sleep(DELAY);
	}

	private void sendUpdateSentenceJson(NodeSessionHandler sessionHandler, String word) throws InterruptedException {
		sessionHandler.updateSentence(word);
	}
	
	private void sendRemoveSentenceJson(NodeSessionHandler sessionHandler) throws InterruptedException {
		sessionHandler.removeSentence();
	}

	private void sendAddLinesJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.addLines(node,coeffString);
		Thread.sleep(DELAY);
	}
	
	private void sendUpdateLinesJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.updateLines(node);
		Thread.sleep(DELAY);
	}

	private void sendActiveNeuronJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.activeNeuron(node);
	}
	
	private void sendUpdateBestLineJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.updateBestLine(node);
		Thread.sleep(DELAY);
	}


}

