package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import es.udc.fic.mri_indexer.util.CustomFields;
import es.udc.fic.mri_indexer.util.TermTools;

/**
 * La clase {@link MostsimilarIndexer} se encarga de generar índices
 * con punteros al documento más similar. Lo hace usando un pool de threads
 * que ejecutan {@link RunnableMostsimilar}. Cada uno de ellos se encarga de
 * añadir al índice de salida un documento de la entrada procesado.
 */
public class MostsimilarIndexer {
	public enum SimilarField {
		BODY_FIELD("body"), TITLE_FIELD("title");
		private final String name;

		private SimilarField(String name) {
			this.name = name;
		}
	}
	
	private static final String[] SEARCH_FIELDS = {"body", "title"}; // Campos contra los que se buscan los términos relevantes

	private final Path indexIn;
	private final Path indexOut;
	private final SimilarField field;
	private final int hilos;
	private final int nterms;

	public MostsimilarIndexer(Path indexIn, Path indexOut, SimilarField field, int hilos, int nterms) {
		this.indexIn = indexIn;
		this.indexOut = indexOut;
		this.field = field;
		this.hilos = hilos;
		this.nterms = nterms;
	}

	public void recreate() {
		ExecutorService executor = Executors.newFixedThreadPool(hilos);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		
		try (
				Directory dirIn = FSDirectory.open(indexIn);
				IndexReader reader = DirectoryReader.open(dirIn);
				Directory dirOut = FSDirectory.open(indexOut);
				IndexWriter writer = new IndexWriter(dirOut, iwc);
				) {
			
			// Implementación de Collector que procesa los resultados
			TaskProcessorCollector collector = new TaskProcessorCollector(executor, reader, writer);
			MatchAllDocsQuery query = new MatchAllDocsQuery();
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.search(query, collector);
			
			while(!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				System.out.println("Still processing indexes...");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interrupted, stopping.");
		}
		
	}

	/**
	 * La tarea que encuentra el documento más similar para uno dado
	 * y lo agrega al índice de salida
	 */
	private class RunnableMostsimilar implements Runnable {
		
		private final IndexReader reader;
		private final IndexWriter writer;
		private final IndexSearcher searcher;
		private final int id;
		private final int nterms;

		RunnableMostsimilar(IndexReader reader, IndexWriter writer, int nterms, int id) {
			this.reader = reader;
			this.writer = writer;
			this.nterms = nterms;
			this.id = id;
			// Ojo con el multithreading
			searcher = new IndexSearcher(reader);
		}

		@Override
		public void run() {
				try {
					Document doc = reader.document(id);
					List<String> terms = getBestNTerms(id, field.name);
					int mostSimilar = findMostSimilar(id, terms, SEARCH_FIELDS);
					reindex(id, mostSimilar);
				} catch (IOException e) {
					System.err.println("Imposible acceder al documento " + id);
				} catch (Exception e) {
					System.err.println("No se pudo procesar el documento " + id);
				}
		}
		
		List<String> getBestNTerms(int id, String field) throws IOException {
			// Creamos un índice en RAM con un solo documento
			// para poder contar su tf
			Directory ramDir = new RAMDirectory();
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			try(IndexWriter ramWriter = new IndexWriter(ramDir, iwc)) {
				ramWriter.addDocument(reader.document(id));
				ramWriter.commit();
			}
			
			IndexReader ramReader = DirectoryReader.open(ramDir);
			
			// Obtenemos el mapa de términos a frecuencia para este doc y el campo correspondiente
			Map<String, Integer> tf = TermTools.getTermFrequencies(ramReader, 0, field);
			Map<String, Integer> idf = TermTools.getTermFrequencies(reader, id, field);
			Map<String, Integer> tfidf = new HashMap<>();
			tf.forEach((String s, Integer i) -> tfidf.put(s, idf.get(s) + i));
			// Hacemos una lista de Entries de ese mapa
			List<Entry<String,Integer>> list = new ArrayList<>(tf.entrySet());
			// Que ordenamos por valor de frecuencia
			list.sort(Entry.comparingByValue());
			// Devolvemos la sublista de los n primeros términos convirtiéndola sobre la marcha en una List<String>
			return list.subList(0, nterms).stream().map((Entry<String,Integer> e) -> e.getKey()).collect(Collectors.toCollection(ArrayList<String>::new));
		}
		
		int findMostSimilar(int id, List<String> terms, String[] fields) throws IOException {
			Builder builder = new Builder();
			for(String t : terms) {
				for(String f: fields) {
					TermQuery tq = new TermQuery(new Term(f, t));
					BooleanClause bc = new BooleanClause(tq, Occur.MUST);
					builder.add(bc);
				}
			}

			Query q = builder.build();
			TopDocs docs = searcher.search(q, 2);
			
			int result = -1;
			for(ScoreDoc sd : docs.scoreDocs) {
				if(id != sd.doc) { 
					result = sd.doc;
					System.out.println("Most similar to " + id + " is " + sd.doc);
				}
			}
			return result;
		}
		
		void reindex(int id, int mostSimilar) throws IOException {
			Field simPath = new Field("SimPathSgm",reader.document(mostSimilar).get("path"),CustomFields.TYPE_STORED);
			Field simTitle = new Field("SimTitle",reader.document(mostSimilar).get("title"),CustomFields.TYPE_STORED);
			Field simBody = new Field("SimBody",reader.document(mostSimilar).get("body"),CustomFields.TYPE_STORED);
			
			Document doc = reader.document(id);
			doc.add(simPath);
			doc.add(simTitle);
			doc.add(simBody);
			
			writer.addDocument(doc);
		}
	}
	
	/**
	 * Implementación de Collector que genera una task y la manda
	 * al ejecutor especificado para cada resultado de la búsqueda.
	 */
	class TaskProcessorCollector implements Collector {
		
		private final ExecutorService executor;
		private final IndexReader reader;
		private final IndexWriter writer;
		
		TaskProcessorCollector(ExecutorService executor, IndexReader reader, IndexWriter writer) {
			this.executor = executor;
			this.reader = reader;
			this.writer = writer;
		}
		
		private int docBase = 0;
		@Override
		public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
			this.docBase = context.docBase;
			return new LeafCollector() {
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {}
				
				@Override
				public void collect(int doc) throws IOException {
					Runnable task = new RunnableMostsimilar(reader, writer, nterms, docBase + doc);
					executor.execute(task);
				}
			};
		}

		@Override
		public boolean needsScores() {
			return false;
		}
		
	};
}
