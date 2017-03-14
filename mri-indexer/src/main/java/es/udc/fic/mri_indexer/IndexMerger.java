package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexMerger {

	public static void merge(String[] paths, OpenMode openMode) {
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(openMode);
		
		Directory dir = null;
		try {
			dir = FSDirectory.open(Paths.get(paths[0]));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try (IndexWriter writer = new IndexWriter(dir, iwc)) {

			Directory[] dirs = new Directory[paths.length-1];
			for(int i=1; i<paths.length; i++) {
				dirs[i-1] = FSDirectory.open(Paths.get(paths[i]));
			}
		
			writer.addIndexes(dirs);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
