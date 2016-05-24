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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.network.model.Sentence;

public class FileOperations {

	private List<Sentence> input = new ArrayList<>();

	public List<Sentence> readDataFromFile(String file) {
		Path path = Paths.get(file);
		try {
			Scanner in = new Scanner(path);
			String line;
			String[] parts;
			while (in.hasNextLine()) {
				Sentence sentence = new Sentence();
				line = in.nextLine();
				parts = line.split(" ");
				for (String word: parts) {
					word = word.toUpperCase();
					if (word.endsWith(".")) {
						word = word.replace(".", "");
						sentence.addWord(word);
					} else {
						sentence.addWord(word);
					}
				}
				input.add(sentence);
			}
		} catch (IOException e) {
			System.out.println("Plik nie zostal wczytany poprawnie!");
			e.printStackTrace();
			System.exit(-1);
		}
		return input;
	}
}
