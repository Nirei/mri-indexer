package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ConcurrentIndexer extends Indexer {
	
	static Map<Path, IndexWriter> indexMaps = new HashMap<>();
	private IndexWriter iWriter;

	synchronized IndexWriter getIndexWriterInstance() throws IOException {
		IndexWriter result = null;
		if(!indexMaps.containsKey(indexPath)) {
			System.out.println("Creating new IndexWriter for path " + indexPath);
			Directory dir = FSDirectory.open(indexPath);
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(super.openMode);
			result = new IndexWriter(dir, iwc);
			indexMaps.put(indexPath, result);
		} else {
			System.out.println("Using IndexWriter for path " + indexPath);
			result = indexMaps.get(indexPath);
		}
		
		return result;
	}
	
	public ConcurrentIndexer(Path indexPath, Path docs, OpenMode openMode) {
		super(indexPath, docs, openMode);
		try {
			iWriter = getIndexWriterInstance();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void index() throws IOException {
		indexDocs(iWriter, docsPath);
	}
}
