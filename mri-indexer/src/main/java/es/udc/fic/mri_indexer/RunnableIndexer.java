package es.udc.fic.mri_indexer;

import java.io.IOException;

public class RunnableIndexer implements Runnable {
	
	private final Indexer indexer;
	
	public RunnableIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	@Override
	public void run() {
		try {
			indexer.index();
		} catch (IOException e) {
			// TODO Lidiar con esto
			e.printStackTrace();
		}
	}

}
