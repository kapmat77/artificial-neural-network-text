package org.network.server;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Kapmat
 */
@ApplicationScoped
@ServerEndpoint("/actions")
public class NodeWebSocketServer {
	
	private GraphBuilder graphBuilder = new GraphBuilder();
	
	@Inject
	private NodeSessionHandler sessionHandler;
	
	@OnOpen
	public void open(Session session) {
		System.out.println("Open Node session...");
		sessionHandler.addSession(session);
	}

	@OnClose
	public void close(Session session) {
		System.out.println("Close Node session...");
		sessionHandler.removeSession(session);
	}

	@OnError
	public void onError(Throwable error) {
		Logger.getLogger(NodeWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
	}

	@OnMessage
	public void handleMessage(String message, Session session) throws InterruptedException {

		System.out.println("JSON JS->Java: " + message);

		try (JsonReader reader = Json.createReader(new StringReader(message))) {
			JsonObject jsonMessage = reader.readObject();

			
			switch(jsonMessage.getString("action")) {
				case "start":
					if ("monkey".equals(jsonMessage.getString("name"))) {
						graphBuilder.buildGraph("monkey.txt", "nullMonkey", sessionHandler, jsonMessage.getString("speed"));
					} else if ("test".equals(jsonMessage.getString("name"))) {
						graphBuilder.buildGraph("test.txt", "nullTest", sessionHandler, jsonMessage.getString("speed"));
					}
					break;
				case "update":
					System.out.println("UP");
					graphBuilder.buildGraph("update", jsonMessage.getString("word"), sessionHandler, jsonMessage.getString("speed"));
					break;
				case "remove":
					break;
				case "resetLines":
					sessionHandler.resetLines();
			}
			
			
//				for (int i = 0; i < 2; i++) {
//					Device device = new Device();
//					device.setName(i + "Monkey" + i);
//					device.setLevel(i);
//					Thread.sleep(1000);
//					sessionHandler.addNode(device);
//				}
			

//			if ("remove".equals(jsonMessage.getString("action"))) {
//				int id = (int) jsonMessage.getInt("id");
//				sessionHandler.removeNode(id);
//			}

//			if ("stop".equals(jsonMessage.getString("action"))) {
////                int id = (int) jsonMessage.getInt("id");
////                sessionHandler.removeDevice(id);
//			}
//
//			if ("update".equals(jsonMessage.getString("action"))) {
//				sessionHandler.toggleNode(1);
//			}
//			
//			if ("toggle".equals(jsonMessage.getString("action"))) {
////                int id = (int) jsonMessage.getInt("id");
////                sessionHandler.toggleDevice("1Monkey1");
//			}
		}
	}
}
