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
    	String index = null;
    	List<String> colls = null;
    	String om = null;
    	OpenMode openMode = null;
    	
    	for(int i=0; i<args.length; i++) {
    		if(args[i].equals("-index")) {
    			index = args[++i];
    		} else if(args[i].startsWith("-coll")) {
    			colls = new ArrayList<String>();
    			for(int j=i+1; j<args.length; j++) {
    				if(args[j].startsWith("-")) {
    					i=j;
    					j=args.length;
    				} else {
    					colls.add(args[j]);
    				}
    			}
    		} else if(args[i].equals("-openmode")) {
    			om = args[++i];
    		}
    	}
    	
    	if(index == null
    			|| colls.isEmpty()) {
			System.err.println(usage);
			System.exit(1);
    	}
    	
    	if(om == null) {
    		openMode = OpenMode.CREATE_OR_APPEND;
    	} else {
    		try {
    			openMode = OpenMode.valueOf(om);
    		} catch (IllegalArgumentException e) {
    			System.err.println("Invalid open mode specified");
    			System.err.println(usage);
    			System.exit(1);
    		}
    	}

    	final Path indexDir = Paths.get(index);
    	final List<Path> docs = new ArrayList<Path>();
    	for(String s: colls) {
    		Path doc = Paths.get(s);
            if (!Files.isReadable(doc)) {
                System.err.println("Document directory '" + doc.toAbsolutePath() + "' does not exist or is not readable, please check the path");
                System.exit(1);
            }
            docs.add(doc);
    	}
    	try {
	    	Indexer indexer = new Indexer(indexDir, docs.get(0), openMode);
	    	indexer.index();
	    	for(int i=1; i<docs.size(); i++) {
	    		indexer = new Indexer(indexDir, docs.get(i), OpenMode.APPEND); // A partir de aquí, usamos APPEND para no sobreescribir
	    		indexer.index();
	    	}
		} catch (IOException e) {
			System.err.println("Falló la indexación :^(");
		}
    }
}
