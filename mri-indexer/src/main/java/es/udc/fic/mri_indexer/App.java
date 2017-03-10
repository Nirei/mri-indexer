package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;

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
    		indexing(cl); // primera parte de la práctica
    	} else if(cl.isQuering()) {
    		quering(cl); // segunda
    	} else if(cl.isRebuilding()) {
    		rebuilding(cl); // tercera
    	}
    	
//    	if(index == null
//    			|| cl.getOpt("-coll").isEmpty()) {
//			System.err.println(usage);
//			System.exit(1);
//    	}
//    	
//
//    	final Path indexDir = Paths.get(index);
//    	final List<Path> docs = new ArrayList<Path>();
//    	for(String s: colls) {
//    		Path doc = Paths.get(s);
//            if (!Files.isReadable(doc)) {
//                System.err.println("Document directory '" + doc.toAbsolutePath() + "' does not exist or is not readable, please check the path");
//                System.exit(1);
//            }
//            docs.add(doc);
//    	}
//    	try {
//	    	Indexer indexer = new Indexer(indexDir, docs.get(0), openMode);
//	    	indexer.index();
//	    	for(int i=1; i<docs.size(); i++) {
//	    		indexer = new Indexer(indexDir, docs.get(i), OpenMode.APPEND); // A partir de aquí, usamos APPEND para no sobreescribir
//	    		indexer.index();
//	    	}
//		} catch (IOException e) {
//			System.err.println("Falló la indexación :^(");
//		}
    }
    
    public static void indexing(CommandLine cl) {
    	OpenMode openMode;
    	
    	if(cl.hasOpt("-om")) {
			openMode = OpenMode.CREATE_OR_APPEND;
		} else {
			try {
				openMode = OpenMode.valueOf(cl.getOpt("-om"));
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid open mode specified");
				System.err.println(usage);
				System.exit(1);
			}
		}
    	
    	if(cl.hasOpt("-index")) {
    		// single thread
    	} else if(cl.hasOpt("-indexes1")) {
    		// multithread fusionar
    	} else if(cl.hasOpt("-indexes2")) {
    		// multithread independientes
    	}
    }
    
    public static void quering(CommandLine cl) {
    	
    }
    
    public static void rebuilding(CommandLine cl) {
    	
    }
}
