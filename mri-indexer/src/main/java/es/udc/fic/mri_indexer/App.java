package es.udc.fic.mri_indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import es.udc.fic.mri_indexer.CommandLine.MissingArgumentException;
import es.udc.fic.mri_indexer.MostsimilarIndexer.SimilarField;

public class App {

	final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
			+ " -index INDEX_PATH -coll DOC_PATH|-colls DOC_PATH1 ... DOC_PATHN [-openmode CREATE|CREATE_OR_APPEND|APPEND]";

	public static void main(String[] args) {
		CommandLine cl = new CommandLine();
		cl.triturar(args);

		if (cl.isIndexing()) {
			System.out.println("Starting indexing");
			indexing(cl); // primera parte de la práctica
		} else if (cl.isSearching()) {
			System.out.println("Starting quering");
			searching(cl); // segunda
		} else if (cl.isRebuilding()) {
			System.out.println("Starting rebuilding");
			rebuilding(cl); // tercera
		} else {
			System.err.println("Not enough arguments specified");
			System.err.println(usage);
			System.exit(1);
		}
	}

	public static void indexing(CommandLine cl) {
		Path index = null;
		String[] indexes = null;
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

		if (cl.hasOpt("-index")) {
			// single thread
			Path coll = null;
			coll = Paths.get(cl.getOpt("-coll"));
			index = Paths.get(cl.getOpt("-index"));

			Indexer indXr = new Indexer(index, coll, openMode);
			try {
				indXr.index();
			} catch (IOException e) {
				System.err.println("Falló la indexación :^(");
			}
		} else {
			// multithread
			String[] colls = null;
			colls = cl.getOpt("-colls").split(" ");
			List<Indexer> indexerList = new ArrayList<>();

			// Creamos una thread-pool de tantos hilos como procesadores
			int cores = Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(cores);

			if (cl.hasOpt("-indexes2")) {
				// sin índices intermedios
				index = Paths.get(cl.getOpt("-indexes2"));

				for (int i = 0; i < colls.length; i++) {
					Path docPath = Paths.get(colls[i]);
					Indexer indXr = new ConcurrentIndexer(index, docPath, openMode);
					indexerList.add(indXr);
					Runnable iTask = new RunnableIndexer(indXr);
					executor.execute(iTask);
				}
			} else {
				// con índices intermedios
				indexes = cl.getOpt("-indexes1").split(" ");
				String union = indexes[0];
				if (indexes.length < colls.length + 1) {
					System.err.println("There are more document directories than index folders");
					System.err.println(usage);
					System.exit(1);
				} else if (indexes.length > colls.length + 1) {
					System.out.println("Ignoring " + (indexes.length - colls.length) + " excess index folders");
				}

				for (int i = 0; i < colls.length; i++) {
					Path iPath = Paths.get(indexes[i + 1]);
					Path docPath = Paths.get(colls[i]);
					Indexer indXr = new Indexer(iPath, docPath, openMode);
					// indexerList.add(indXr);
					Runnable iTask = new RunnableIndexer(indXr);
					executor.execute(iTask);
				}
			}

			try {
				executor.shutdown();
				while (!executor.awaitTermination(60, TimeUnit.SECONDS)) {

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (cl.hasOpt("-indexes1")) {
				IndexMerger.merge(indexes, openMode);
			}

			if (cl.hasOpt("-indexes2")) {
				ConcurrentIndexer.closeIndexer(index);
			}
		}
	}

	public static void searching(CommandLine cl) {
		String indexin;
		Boolean poor;
		indexin = cl.getOpt("-indexin");
		if (cl.hasOpt("-best_idfterms")) {

			String[] argumentostemp = cl.getOpt("-best_idfterms").split(" ");
			String field = argumentostemp[0];
			int ranking = Integer.parseInt(argumentostemp[1]);
			poor = false;
			Searchidf search = new Searchidf(Paths.get(indexin), field, ranking, poor);
			search.searching();
		}

		if (cl.hasOpt("-poor_idfterms")) {

			String[] argumentostemp = cl.getOpt("-poor_idfterms").split(" ");
			String field = argumentostemp[0];
			int ranking = Integer.parseInt(argumentostemp[1]);
			poor = true;
			Searchidf search = new Searchidf(Paths.get(indexin), field, ranking, poor);
			search.searching();
		}

		if (cl.hasOpt("-best_tfidfterms")) {

			String[] argumentostemp = cl.getOpt("-best_tfidfterms").split(" ");
			String field = argumentostemp[0];
			int ranking = Integer.parseInt(argumentostemp[1]);
			poor = false;
			SearchTfIdf search = new SearchTfIdf(Paths.get(indexin), field, ranking, poor);
			search.searching();
		}

		if (cl.hasOpt("-poor_tfidfterms")) {

			String[] argumentostemp = cl.getOpt("-poor_tfidfterms").split(" ");
			String field = argumentostemp[0];
			int ranking = Integer.parseInt(argumentostemp[1]);
			poor = true;
			SearchTfIdf search = new SearchTfIdf(Paths.get(indexin), field, ranking, poor);
			search.searching();
		}

	}

	public static void rebuilding(CommandLine cl) {
		Path indexIn;
		Path indexOut;
		indexIn = Paths.get(cl.getOpt("-indexin"));
		OpenMode openMode = OpenMode.CREATE_OR_APPEND;
		if (cl.hasOpt("-indexout")) {
			indexOut =  Paths.get(cl.getOpt("-indexout"));

		} else
			indexOut = null;

		try {
			openMode = OpenMode.valueOf(cl.checkOpt("-om"));
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid open mode specified");
			System.err.println(usage);
			System.exit(1);
		} catch (MissingArgumentException e) {
			System.out.println("No open mode specified, asumming CREATE_OR_APPEND");
		}

		
		if (cl.hasOpt("-deldocsterm")) {
			Term termino;
			String[] argumentostemp = cl.getOpt("-deldocsterm").split(" ");
			termino = new Term(argumentostemp[0], argumentostemp[1]);
			DelDocs deletedocuments = new DelDocs(indexIn, indexOut, openMode, termino);
			deletedocuments.delete();

		}
		if (cl.hasOpt("-deldocsquery")) {
			QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());
			String querystring = cl.getOpt("-deldocsquery");
			Query query;
			try {
				query = queryParser.parse(querystring);
				DelDocs deletedocuments = new DelDocs(indexIn, indexOut, openMode, query);
				deletedocuments.delete();
			} catch (ParseException e) {
				System.err.println("No se pudo parsear la query");
				e.printStackTrace();
			}
		}
		
		if (cl.hasOpt("-mostsimilardoc_title")) {
			String[] argum = cl.getOpt("-mostsimilardoc_title").split(" ");
			if(argum.length != 2) {
				System.err.println("Argumentos inválidos para -mostsimilardoc_title");
				System.err.println(usage);
				System.exit(0);
			}
			int nterms = Integer.parseInt(argum[0]);
			int hilos = Integer.parseInt(argum[1]);
			MostsimilarIndexer mSI = new MostsimilarIndexer(indexIn, indexOut, SimilarField.TITLE_FIELD, hilos, nterms);
			mSI.recreate();
		}

		if (cl.hasOpt("-mostsimilardoc_body")) {
			String[] argum = cl.getOpt("-mostsimilardoc_title").split(" ");
			if(argum.length != 2) {
				System.err.println("Argumentos inválidos para -mostsimilardoc_title");
				System.err.println(usage);
				System.exit(0);
			}
			int nterms = Integer.parseInt(argum[0]);
			int hilos = Integer.parseInt(argum[1]);
			MostsimilarIndexer mSI = new MostsimilarIndexer(indexIn, indexOut, SimilarField.BODY_FIELD, hilos, nterms);
			mSI.recreate();
		}
	}

	private static long calculateJobSize(String[] docs) throws IOException {
		long result = 0;
		// suma el tamaño de los archivos que terminen en .sgm
		for (String c : docs) {
			result += Files.walk(new File(c).toPath()).map(f -> f.toFile())
					.filter(f -> f.isFile() && f.getName().endsWith(".sgm")).mapToLong(f -> f.length()).sum();
		}
		return result;
	}
}
