package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import es.udc.fic.mri_indexer.parsers.Reuters21578Parser;

public class Indexer {

	final Path indexPath;
	final Path docsPath;

	public Indexer(Path indexPath, Path docsPath) {
		this.indexPath = indexPath;
		this.docsPath = docsPath;
	}

	public void index() throws IOException {
		Directory dir = FSDirectory.open(indexPath);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try (IndexWriter writer = new IndexWriter(dir, iwc)) {
			indexDocs(writer, docsPath);
		}
	}

	private void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}

	private void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-YYYY HH:mm:ss.SS");
		String hostname = execReadToString("hostname");
		
		try (Scanner scan = new Scanner(file)) {

			// Leemos el archivo entero a un StringBuffer para cumplir con la interfaz del parser
			scan.useDelimiter("\\A");
			StringBuffer content = new StringBuffer(scan.next());
			List<List<String>> fields = Reuters21578Parser.parseString(content);
						
			for(int artn=0; artn<fields.size(); artn++) {
				List<String> art = fields.get(artn);
				
				// make a new, empty document
				Document doc = new Document();
				
				// Host del que proceden los archivos
				Field hostField = new StringField("host", hostname, Store.YES);
				doc.add(hostField);
				// Ruta de los archivos
				Field pathField = new StringField("path", file.toString(), Store.YES);
				doc.add(pathField);
				// Número de artículo dentro del archivo
				Field orderField = new StringField("order", new Integer(artn).toString(), Store.YES);
				doc.add(orderField);
				// Campos propios del artículo
				int i = 0;
				Field titleField = new TextField("title", art.get(i++), Store.NO);
				doc.add(titleField);
				Field bodyField = new TextField("body", art.get(i++), Store.NO);
				doc.add(bodyField);
				Field topicsField = new TextField("topics", art.get(i++), Store.NO);
				doc.add(topicsField);
				Field datelineField = new StringField("dateline", art.get(i++), Store.NO);
				doc.add(datelineField);
				String date = "Thu Jan 01 00:00:00 UTC 1970";
				try {				
					date = sdf.parse(art.get(i++)).toString();
				} catch (ParseException e) {
					System.err.println("Error indexando " + hostname
							+ ":" + file.toString() + "#" + artn
							+ " : No se pudo analizar la fecha, utilizando fecha por defecto");
				}
				Field dateField = new StringField("date", date, Store.NO);
				doc.add(dateField);
				
				writer.addDocument(doc);
			}
		}
	}
	
    private static String execReadToString(String execCommand) throws IOException {
        Process proc = Runtime.getRuntime().exec(execCommand);
        try (InputStream stream = proc.getInputStream()) {
            try (Scanner s = new Scanner(stream)) {
            	s.useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            }
        }
    }
}
