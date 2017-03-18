package es.udc.fic.mri_indexer.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public final class TermTools {
	
	private TermTools() {
		// Clase estática de utilidades, no necesita instanciarse
	}
	
	public static Map<String, Integer> getTermFrequencies(IndexReader reader, int docId, String field) throws IOException {
		Terms vector = reader.getTermVector(docId, field);

		System.out.println(vector);
		TermsEnum termsEnum = null;
		termsEnum = vector.iterator();
		Map<String, Integer> frequencies = new HashMap<>();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null) {
			String term = text.utf8ToString();
			int freq = (int) termsEnum.totalTermFreq();
			frequencies.put(term, freq);
		}
		return frequencies;
	}
}
