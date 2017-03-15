package es.udc.fic.mri_indexer;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

public class MostsimilarIndexer {
	public enum SimilarField {
		BODY_FIELD, TITLE_FIELD
	}

	private final Path indexIn;
	private final Path indexOut;
	private final SimilarField field;
	private final int hilos;

	public MostsimilarIndexer(Path indexIn, Path indexOut, SimilarField field, int hilos) {
		this.indexIn = indexIn;
		this.indexOut = indexOut;
		this.field = field;
		this.hilos = hilos;
	}

	public void recreate() {
		Executors.newFixedThreadPool(hilos);
		for(;;) {
			
		}
	}

	private class RunnableMostsimilar implements Runnable {

		private final IndexReader reader;
		private final IndexWriter writer;
		private final int startId;
		private final int docAmount;
		private final int nterms;

		RunnableMostsimilar(IndexReader reader, IndexWriter writer, int startId, int docAmount, int nterms) {
			this.reader = reader;
			this.writer = writer;
			this.startId = startId;
			this.docAmount = docAmount;
			this.nterms = nterms;
		}

		@Override
		public void run() {
			
			for(int i=0; i<docAmount; i++) {
				Document doc = getDoc(i);
				// if() // if doc is deleted
				// continue;
				String[] terms = getBestNTerms(doc);
				List<Document> candidates = findDocsWithThisTerms(terms);
				Document mostSimilar = findMostSimilar(doc, candidates);
				reindex(doc, mostSimilar);
			}
		}
		
		private Document getDoc(int index) {
			return null;
		}
		
		private String[] getBestNTerms(Document doc) {
			return null;
		}
		
		private List<Document> findDocsWithThisTerms(String[] terms) {
			return null;
		}
		
		private Document findMostSimilar(Document doc, List<Document> candidates) {
			return doc;
		}
		
		private void reindex(Document doc, Document mostSimilar) {
			
		}
	}
}
