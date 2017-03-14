package es.udc.fic.mri_indexer;

import java.io.IOException;

public class RunnableIndexer implements Runnable {
	
	public final Indexer indexer;
	
	public RunnableIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	@Override
	public void run() {
		String tName = Thread.currentThread().getName();
		System.out.println("Thread " + tName + " started: Processing indexer " + indexer);
		try {
			indexer.index();
		} catch (IOException e) {
			// TODO Lidiar con esto
			e.printStackTrace();
		}
	}

}
