package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.index.PostingsEnum;

public class SearchTfIdf {
	private final Path indexFilePath;
	private final String field;
	private int N = 21578; // Se modifica en ejecucion
	private final int numeroTop;
	private final boolean poor;

	public SearchTfIdf(Path indexPath, String field, int top, boolean poor) {

		this.indexFilePath = indexPath;
		this.field = field;
		this.numeroTop = top;
		this.poor = poor;
		
	}

	public void searching() {
		try {
			IndexReader reader;
			reader = DirectoryReader.open(FSDirectory.open(indexFilePath));
			System.out.println("Numero de documentos: "+ reader.numDocs());
			N = reader.numDocs();
			Fields fields = MultiFields.getFields(reader);
			Terms terms = fields.terms(field);
			TermsEnum termsEnum = terms.iterator();
			List<String> topDocs = new ArrayList<String>();
			while (termsEnum.next() != null) {
				// termsEnum.next();
				// OBTENER IDF
				int df_T = termsEnum.docFreq();
				double idf = Math.log(N / df_T);

				// OBTENER string campo
				BytesRef nombre = termsEnum.term();
				String nombrestring = nombre.utf8ToString();
				String termino;

				// OBTENER POSTINNGS
				PostingsEnum lista;
				lista = termsEnum.postings(null, PostingsEnum.FREQS);

				if (lista != null) {
					int docx;
					while ((docx = lista.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {

						double tf_docOrigen; // Tf en el documento
						double tf_doc; // Calculo de tf con log
						// OBTENEMOS EL TF
						tf_doc = lista.freq();
						tf_docOrigen = tf_doc;
						if (tf_doc > 0) {
							tf_doc = 1 + Math.log(tf_doc);

						} else
							tf_doc = 0;
						
						// Calculamos TFIDF
						double tfidf = tf_doc * idf;
						// Añadimos los valores a la lista.
						termino = "TF*IDF: " +   tfidf +" ( docid: " + docx + ", termino: " + nombrestring + " )" + " tf: " + tf_docOrigen + " idf: " + idf;
						topDocs.add(termino);

					}
				}

			}
			Collections.sort(topDocs);
			if (!poor) {
				Collections.reverse(topDocs);
			}

			// Mostrar los n mejores//peores
			for (int j = 0; j < numeroTop && j < topDocs.size(); j++) {
				System.out.println((j + 1) + ")  " + topDocs.get(j));

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
