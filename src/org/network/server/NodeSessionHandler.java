package org.network.server;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import org.network.model.Coefficients;
import org.network.model.Node;

/**
 *
 * @author Kapmat
 */
@ApplicationScoped
public class NodeSessionHandler {
	
	private int nodeId = 1;
	private int sentenceId = 1000;
	private final Set sessions = new HashSet<>();
	private final Set nodes = new LinkedHashSet<>();
	private static final int DISTANCE = 300;
	
	public void addSession(Session session) {
		sessions.add(session);
		//Anty-remove refresh
//		for (Object node : nodes) {
//			JsonObject addMessage = createNodeJson((Node)node);
//			sendToSession(session, addMessage);
//		}
	}
	
	public void removeSession(Session session) {
		nodeId = 1;
		sentenceId = 1000;
		sessions.remove(session);
	}
	
	public List getNodes() {
		return new ArrayList(nodes);
	}
	
	public void addNode(Node node) {
		node.setId(nodeId);
		nodes.add(node);
		nodeId++;
		JsonObject addMessage = createNodeJson(node);
		sendToAllConnectedSessions(addMessage);
	}
	
	public void addLines(Node node, String coeffString) {
		addLinesJson(node, coeffString);
	}
	
	public void updateLines(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "updateLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("distance", 0)
				.add("neighbours", node.getAnotherNeighboursAsString())
//				.add("coeff", "0") //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void resetLines() {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "resetLines")
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(updateNode);
	}

	public void resetNodes() {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "resetNodes")
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void updateBestLine(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "updateBestLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("neighbours", node.getStringBestNeighbour())
				.add("distance", 0)
//				.add("coeff", "0") //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void activeNeuron(Node node, String color) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "activeNeuron")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("color", color)
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void updateNode(Node node) {
//		Node oldNode = getNodeByName(node.getName());
//		System.out.println("Node level:" + node.getLevel() + " OldNode level:" + oldNode.getLevel());
//		oldNode.setId(node.getId());
//		oldNode.setLevel(node.getLevel());
		updateNodeJson(node);
	}
	
	public void updateSentence(String word) {
		JsonProvider provider = JsonProvider.provider();
//		sentenceId++;
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "updateSentence")
				.add("id", sentenceId)
				.add("name", word)
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void removeSentence() {
		JsonProvider provider = JsonProvider.provider();
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "removeSentence")
				.add("id", sentenceId)
				.add("distance", 0)
				.build();
//		sentenceId++;
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void addSentence(String sentence) {
		JsonProvider provider = JsonProvider.provider();
		sentenceId++;
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "addSentence")
				.add("id", sentenceId)
				.add("name", sentence)
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	private Node getNodeByName(String name) {
		for (Object node : nodes) {
			if ((((Node)node).getName()).equals(name)) {
				return (Node)node;
			}
		}
		return null;
	}
	
	private JsonObject createNodeJson(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject addNode = provider.createObjectBuilder()
				.add("action", "add")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("neighbours", node.getNeighboursAsString())
				.add("chargeLevel", node.getChargingLevel())
				.add("distance", 0)
				.build();
		return addNode;
	}
	
	private void addLinesJson(Node node, String coeffString) {
		for (Map.Entry<Node, Coefficients> entry : node.getNeighCoefficient().entrySet()) {
//		for(Node singleNeigh: node.getAllowedNodes()) {
			double distance = 0;
			if (entry.getValue().getSynapticWeight()==0 && entry.getKey().getNeighCoefficient().containsKey(node)) {
				distance = entry.getKey().getNeighCoefficient().get(node).getSynapticWeight();
			} else {
				distance = entry.getValue().getSynapticWeight();
			}
			JsonProvider provider = JsonProvider.provider();
			JsonObject updateNode = provider.createObjectBuilder()
					.add("action", "addLines")
					.add("id", node.getId())
					.add("level", node.getLevel())
					.add("name", node.getName())
					.add("neighbours", " " + entry.getKey().getName())
					.add("coeff", " " + String.valueOf(ClockUpdater.roundDouble(600-(distance*500))))
					.add("distance", String.valueOf(ClockUpdater.roundDouble(600-(distance*500))))
					.build();
//			System.out.println("Node '" + node.getName() + "' to '" + entry.getKey().getName() + "' - distance: " + String.valueOf(ClockUpdater.roundDouble(600-(distance*500))));
//			System.out.println("Coeff " + ClockUpdater.roundDouble(distance));
			sendToAllConnectedSessions(updateNode);
		}
	}
	
	private void updateNodeJson(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateMessage = provider.createObjectBuilder()
				.add("action", "update")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("chargeLevel", node.getChargingLevel())
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(updateMessage);
	}
	
	private void removeNodeJson(int id) {

	}

	public void updateChargeLevel(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateMessage = provider.createObjectBuilder()
				.add("action", "updateCharge")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("name", node.getName())
				.add("chargeLevel", ClockUpdater.roundDouble(node.getChargingLevel()))
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(updateMessage);
	}

	public void updateTimer(double clock) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateMessage = provider.createObjectBuilder()
				.add("action", "updateTimer")
				.add("clock", clock)
				.add("distance", 0)
				.build();
		sendToAllConnectedSessions(updateMessage);
	}

	private void sendToAllConnectedSessions(JsonObject message) {
		for (Object session : sessions) {
			sendToSession((Session)session, message);
		}
    }

	private void sendToSession(Session session, JsonObject message) {
		try {
			synchronized(this) {
//				System.out.println("JSON Java->JS: " + message.toString());
				session.getBasicRemote().sendText(message.toString());
			}
		} catch (IOException ex) {
			sessions.remove(session);
			Logger.getLogger(NodeSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
