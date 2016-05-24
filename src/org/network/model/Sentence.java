/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.network.model;

/**
 *
 * @author Kapmat
 */

import java.util.ArrayList;
import java.util.List;

public class Sentence {

	private List<String> words = new ArrayList<>();

	public List<String> getWords() {
		return words;
	}

	public void setWords(List<String> words) {
		this.words = words;
	}

	public void addWord(String word) {
		boolean exist = false;

		for (String singleWord: words) {
			if (singleWord.equals(word)) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			this.words.add(word);
		}
	}
	
	@Override
	public String toString() {
		String sent = "";
		for (String word: words) {
			sent = sent.concat(" ").concat(word);
		}
		return sent;
	}
}

