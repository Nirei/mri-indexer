package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;

/**
 * Hello world!
 *
 */
public class App {
	
    final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
    		+ " -index INDEX_PATH -coll DOC_PATH [-openmode CREATE|CREATE_OR_APPEND|APPEND]";

    public static void main( String[] args )
    {
    	String index = null;
    	String docs = null;
    	String om = null;
    	OpenMode openMode = null;
    	
    	for(int i=0; i<args.length; i++) {
    		if(args[i].startsWith("-index")) {
    			index = args[++i];
    		}
    		else if(args[i].startsWith("-coll")) {
    			docs = args[++i];
    		}
    		else if(args[i].startsWith("-openmode")) {
    			om = args[++i];
    		}
    	}
    	
    	if(index == null
    			|| docs == null) {
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
        final Path docDir = Paths.get(docs);
        if (!Files.isReadable(docDir)) {
          System.err.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
          System.exit(1);
        }
    	
    	Indexer indexer = new Indexer(indexDir,docDir,openMode);
    	try {
			indexer.index();
		} catch (IOException e) {
			System.err.println("Falló la indexación :^(");
		}
    }
}
