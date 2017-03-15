package es.udc.fic.mri_indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DelDocsTerm {

	private final Path indexPath;
	private final OpenMode openMode;
	private final Term termino;
	private Path indexout;

	public DelDocsTerm(Path indexPath, Path indexout, OpenMode openMode, Term termino) {
		this.indexPath = indexPath;
		this.openMode = openMode;
		this.termino = termino;
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
				writer.deleteDocuments(termino);
				writer.close();
			}else {
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				iwc.setOpenMode(openMode);
				IndexWriter writer = new IndexWriter(dirindex, iwc);
				writer.deleteDocuments(termino);
				writer.close();
			}
		} catch (IOException e) {
			System.err.println("Imposible crear IndexWriter en DelDocsTerm");
			e.printStackTrace();
		}

	}

}
