package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Query;

public class DelDocs {

	private final Path indexPath;
	private final OpenMode openMode;
	private Term termino;
	private Path indexout;
	private Query query = null;

	public DelDocs(Path indexPath, Path indexout, OpenMode openMode, Term termino) {
		this.indexPath = indexPath;
		this.openMode = openMode;
		this.termino = termino;
		this.indexout = indexout;
	}
	public DelDocs(Path indexPath, Path indexout, OpenMode openMode, Query termino) {
		this.indexPath = indexPath;
		this.openMode = openMode;
		this.query = termino;
		this.indexout = indexout;
	}

	public void delete() {
		Directory dirindex;
		try {
			dirindex = FSDirectory.open(indexPath);
			if (indexout != null) {
				Directory dir;
				dir = FSDirectory.open(indexout);
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				iwc.setOpenMode(openMode);
				IndexWriter writer = new IndexWriter(dir, iwc);
				writer.addIndexes(dirindex);
				if (query!=null){
					writer.deleteDocuments(query);
				}
					else {writer.deleteDocuments(termino);
				}
				writer.close();
			}else {
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				iwc.setOpenMode(openMode);
				IndexWriter writer = new IndexWriter(dirindex, iwc);
				if (query!=null){
					writer.deleteDocuments(query);
				}
					else {writer.deleteDocuments(termino);
				}
				writer.close();
			}
		} catch (IOException e) {
			System.err.println("Imposible crear IndexWriter en DelDocsTerm");
			e.printStackTrace();
		}

	}

}
