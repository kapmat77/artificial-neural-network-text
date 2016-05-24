package org.network.model;

/**
 *
 * @author Kapmat
 */

public class Coefficients {

	private Double synapticWeight = 0.0;
	private Double synapticEffectiveness = 0.0;

	public Coefficients(Double sW, Double sE) {
		synapticWeight = sW;
		synapticEffectiveness = sE;
	}

	public Double getSynapticEffectiveness() {
		return synapticEffectiveness;
	}

	public void setSynapticEffectiveness(Double synapticEffectiveness) {
		this.synapticEffectiveness = synapticEffectiveness;
	}

	public Double getSynapticWeight() {
		return synapticWeight;
	}

	public void setSynapticWeight(Double synapticWeight) {
		this.synapticWeight = synapticWeight;
	}
}

