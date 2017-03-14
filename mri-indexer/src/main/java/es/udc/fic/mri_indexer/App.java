package es.udc.fic.mri_indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import es.udc.fic.mri_indexer.CommandLine.MissingArgumentException;

/**
 * Hello world!
 *
 */
public class App {
	
    final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
    		+ " -index INDEX_PATH -coll DOC_PATH|-colls DOC_PATH1 ... DOC_PATHN [-openmode CREATE|CREATE_OR_APPEND|APPEND]";
    
    public static void main( String[] args )
    {
    	CommandLine cl = new CommandLine();
    	cl.triturar(args);
    	
    	if(cl.isIndexing()) {
    		System.out.println("Starting indexing");
    		indexing(cl); // primera parte de la práctica
    	} else if(cl.isSearching()) {
    		System.out.println("Starting quering");
    		searching(cl); // segunda
    	} else if(cl.isRebuilding()) {
    		System.out.println("Starting rebuilding");
    		rebuilding(cl); // tercera
    	} else {
			System.err.println("Not enough arguments specified");
			System.err.println(usage);
			System.exit(1);
    	}
    }
    
    public static void indexing(CommandLine cl) {
    	OpenMode openMode = OpenMode.CREATE_OR_APPEND;
    	
		try {
			openMode = OpenMode.valueOf(cl.checkOpt("-om"));
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid open mode specified");
			System.err.println(usage);
			System.exit(1);
		} catch (MissingArgumentException e) {
			System.out.println("No open mode specified, asumming CREATE_OR_APPEND");
		}

    	if(cl.hasOpt("-index")) {
    		// single thread
    		Path coll = null;
			coll = Paths.get(cl.getOpt("-coll"));
			Path index = Paths.get(cl.getOpt("-index"));
			
			Indexer indXr = new Indexer(index, coll, openMode);
			try {
				indXr.index();
			} catch (IOException e) {
				System.err.println("Falló la indexación :^(");
			}
			
    	} else {
    		// multithread
    		String[] indexes = null;
    		String[] colls = null;
			colls = cl.getOpt("-colls").split(" ");
			List<Indexer> indexerList = new ArrayList<>();
			
			// Creamos una thread-pool de tantos hilos como procesadores
    		int cores = Runtime.getRuntime().availableProcessors();
    		ExecutorService executor = Executors.newFixedThreadPool(cores);
			
			if(cl.hasOpt("-indexes2")) {
				// sin índices intermedios
				String index = cl.getOpt("-indexes2");
    			Path iPath = Paths.get(index);

				for(int i=0; i<colls.length; i++) {
	    			Path docPath = Paths.get(colls[i]);
	    			Indexer indXr = new ConcurrentIndexer(iPath, docPath, openMode);
	    			indexerList.add(indXr);
	    			Runnable iTask = new RunnableIndexer(indXr);
	        		executor.execute(iTask);
	    		}
			} else {
				// con índices intermedios
				indexes = cl.getOpt("-indexes1").split(" ");
				String union = indexes[0];
				if(indexes.length < colls.length+1) {
					System.err.println("There are more document directories than index folders");
					System.err.println(usage);
					System.exit(1);
				} else if(indexes.length > colls.length + 1) {
					System.out.println("Ignoring " + (indexes.length-colls.length) + " excess index folders");
				}
				
	    		for(int i=0; i<colls.length; i++) {
	    			Path iPath = Paths.get(indexes[i+1]);
	    			Path docPath = Paths.get(colls[i]);
	    			Indexer indXr = new Indexer(iPath, docPath, openMode);
	    			// indexerList.add(indXr);
	    			Runnable iTask = new RunnableIndexer(indXr);
	        		executor.execute(iTask);
	    		}
			}

    		try {
    			executor.shutdown();
				while(!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		if(cl.hasOpt("-indexes1")) {
    			IndexMerger.merge(indexes, openMode);
    		}
    	}
    }
    
    public static void searching(CommandLine cl) {
    	String indexin;
    	Boolean poor;
    	indexin = cl.getOpt("-indexin");
    	if(cl.hasOpt("-best_idfterms")) {
    		
    		String [] argumentostemp = cl.getOpt("-best_idfterms").split(" ");
    		String field = argumentostemp[0];
    		int ranking = Integer.parseInt(argumentostemp[1]);
    		poor = false;
    	    Searchidf search = new Searchidf(Paths.get(indexin),field,ranking,poor);
    		search.searching();
    	}
    	
    	if(cl.hasOpt("-poor_idfterms")) {
    		
    		String [] argumentostemp = cl.getOpt("-poor_idfterms").split(" ");
    		String field = argumentostemp[0];
    		int ranking = Integer.parseInt(argumentostemp[1]);
    		poor = true;
    	    Searchidf search = new Searchidf(Paths.get(indexin),field,ranking,poor);
    		search.searching();
    	}
    	
    	if(cl.hasOpt("-best_tfidfterms")) {
    		
    		String [] argumentostemp = cl.getOpt("-best_tfidfterms").split(" ");
    		String field = argumentostemp[0];
    		int ranking = Integer.parseInt(argumentostemp[1]);
    		poor = false;
    	    SearchTfIdf search = new SearchTfIdf(Paths.get(indexin),field,ranking,poor);
    		search.searching();
    	}
    	
if(cl.hasOpt("-poor_tfidfterms")) {
    		
    		String [] argumentostemp = cl.getOpt("-poor_tfidfterms").split(" ");
    		String field = argumentostemp[0];
    		int ranking = Integer.parseInt(argumentostemp[1]);
    		poor = true;
    	    SearchTfIdf search = new SearchTfIdf(Paths.get(indexin),field,ranking,poor);
    		search.searching();
    	}
    	
    	
    }
    
    public static void rebuilding(CommandLine cl) {
    	
    }
    
    private static long calculateJobSize(String[] docs) throws IOException {
    	long result = 0;
    	for(String c : docs) {
	    	result += Files.walk(new File(c).toPath())
	        .map(f -> f.toFile())
	        .filter(f -> f.isFile() && f.getName().endsWith(".sgm"))
	        .mapToLong(f -> f.length()).sum(); // suma el tamaño de los archivos que terminen en .sgm
    	}
		return result;
    }
}
