package es.udc.fic.mri_indexer;

import java.nio.file.Path;

public class MostsimilarIndexer {
	public enum SimilarField {
		BODY_FIELD,
		TITLE_FIELD
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
		
	}
	
	private class RunnableMostsimilar implements Runnable {
		
		RunnableMostsimilar() {
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
