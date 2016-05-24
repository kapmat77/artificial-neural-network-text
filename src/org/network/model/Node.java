package org.network.model;

/**
 *
 * @author Kapmat
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

	private int id = 0;
	private String name;
	private int level = 0;
	private List<Node> neighbours = new ArrayList<>();
	private List<Node> someOfNeighbours = new ArrayList<>();
	private List<Node> sentenceNeighbours = new ArrayList<>();
	private List<Node> bestNeighbours  = new ArrayList<>();
	private Map<Node, Coefficients> neighCoefficient = new HashMap();
	private Map<Node, Coefficients> neighActive = new HashMap();

	public Map<Node, Coefficients> getNeighActive() {
		return neighActive;
	}

	public void setNeighActive(Map<Node, Coefficients> neighActive) {
		this.neighActive = neighActive;
	}
	
	public void addNeighActive(Node neighbour, Coefficients coefficient) {
		this.neighActive.put(neighbour, coefficient);
	}


	public Node() {
		this.name = "null";
	}

	public Node(String newName) {
		this.name = newName;
		level = 1;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int newId) {
		this.id = newId;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void increaseLevel() {
		this.level = this.level + 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getNeighbours() {
		return neighbours;
	}
	
	public String getCoeffAsString() {
		String str = "";
		
		for (Node node: neighbours) {
//			str = str + " " + (int)(neighCoefficient.get(node).getSynapticWeight()*100);
			if(node.getNeighbours().size()>0 && node.getNeighCoefficient().get(this) != null) {
				str = str + " " + (int)(node.getNeighCoefficient().get(this).getSynapticWeight()*100);
			}
		}
		return str;
	}
	
	public String getNeighboursAsString() {
		String str = "";
		
		for (Node node: neighbours) {
//			if (neighCoefficient.get(node))
			str = str + " " + node.name;
		}
		
		
		return str;
	}
	
	public String getAnotherNeighboursAsString() {
		String str1[] = new String[someOfNeighbours.size()+1];
		String str2[] = new String[neighbours.size()+1];
		String str3 = "";
		
		List<String> firstList = new ArrayList<>();
		List<String> secondList = new ArrayList<>();
		
		for (Node node: someOfNeighbours) {
			firstList.add(node.name);
		}
		
		for (Node node: neighbours) {
			secondList.add(node.name);
		}
		
		secondList.removeAll(firstList);
		
		for (int j=0; j<secondList.size(); j++) {
			str3 = str3 + " " + secondList.get(j);
		}
		
		return  str3;
	}
	
	
	
	public String getSentenceNeighboursAsString() {
		String str = "";
		
		for (Node node: sentenceNeighbours) {
			str = str + " " + node.name;
		}
		return str;
	}

	public void setNeighbours(List<Node> neighbours) {
		this.neighbours = neighbours;
	}
	
	public String getStringBestNeighbour() {
		String str = "";
		for (Node node: bestNeighbours) {
			str = str + " "+ node.name;
		}
		return str;
	}
	
	public void setBestNeighboyr(List<Node> best) {
		bestNeighbours.addAll(best);
	}

	public void addNeighbour(Node neighbour, boolean nodePath) {
		boolean exist = false;

		
				for (Node node: neighbours) {
				if (node.getName().equals(neighbour.getName())) {
//				node.increaseLevel();
//				node.updateCoefficients(); // ???
					exist = true;
					break;
				}
			}

			if (!exist) {
				this.neighbours.add(neighbour);
				addNeighCoefficient(neighbour, getCoefficient());
		}
	 if(nodePath) {
							for (Node node: someOfNeighbours) {
				if (node.getName().equals(neighbour.getName())) {
//				node.increaseLevel();
//				node.updateCoefficients(); // ???
					exist = true;
					break;
				}
			}

			if (!exist) {
				this.someOfNeighbours.add(neighbour);
//				addNeighCoefficient(neighbour, getCoefficient());
		}
			
		}
		
	}
	
	public void addSentenceNeighbour(Node neighbour) {
		boolean exist = false;

		for (Node node: sentenceNeighbours) {
			if (node.getName().equals(neighbour.getName())) {
//				node.increaseLevel();
//				node.updateCoefficients(); // ???
				exist = true;
				break;
			}
		}
		if (!exist) {
			this.sentenceNeighbours.add(neighbour);
		}
	}

	private Coefficients getCoefficient() {
		//TODO implement counting coefficient
		return new Coefficients(0.0, 0.0);
	}

	public void updateCoefficients() {
		for (Node neighbour: neighbours) {
			Double sE = this.neighCoefficient.get(neighbour).getSynapticEffectiveness();
			Double newSW = (2*sE)/(this.level+sE);
			this.neighCoefficient.get(neighbour).setSynapticWeight(newSW);
		}
	}

	public Map<Node, Coefficients> getNeighCoefficient() {
		return neighCoefficient;
	}

	public void setNeighCoefficient(Map<Node, Coefficients> neighCoefficient) {
		this.neighCoefficient = neighCoefficient;
	}

	public void addNeighCoefficient(Node neighbour, Coefficients coefficient) {
		this.neighCoefficient.put(neighbour, coefficient);
	}

}
