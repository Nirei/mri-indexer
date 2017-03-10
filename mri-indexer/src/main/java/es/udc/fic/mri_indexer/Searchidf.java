package es.udc.fic.mri_indexer;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class Searchidf {
	private final Path indexFilePath;
	private final String field;
	private final int N = 21578; //MODIFICARLO
	private final int numeroTop;
	private final boolean poor;
	public Searchidf ( Path indexPath, String field, int top, boolean poor) {
		
		this.indexFilePath=indexPath;
		this.field = field;
		this.numeroTop = top;
		this.poor = poor;
	}
	
	public void searching() {
		try {
			IndexReader reader;
			reader = DirectoryReader.open(FSDirectory.open(indexFilePath));
			//IndexSearcher searcher = new IndexSearcher(reader);
			//Analyzer analyzer = new StandardAnalyzer();
			
			//Creando la lista de terminos y el iterador
			
			Fields fields = MultiFields.getFields(reader);
			Terms terms = fields.terms(field);
			TermsEnum termsEnum = terms.iterator();
			List<String> topdocs = new ArrayList<String>();
			
			 while (termsEnum.next() !=null){
				 //termsEnum.next();
				 //OBTENER IDF
				 int df_T = termsEnum.docFreq();
				 double idf = Math.log(N/df_T);
				 //OBTENER VALOR TERMINO 
				 BytesRef nombre = termsEnum.term();
				 String nombrestring = nombre.utf8ToString();
				 String termino = idf + " " + nombrestring;
				 topdocs.add(termino);		
			}
			 //Ordenamos la lista por idf
			 Collections.sort(topdocs);
			 if (poor){
				 Collections.reverse(topdocs);
			 }
			 //Mostrar los n mejores
			 for (int j = 0; j<numeroTop && j<topdocs.size(); j++){
				 System.out.println(topdocs.get(j));
				
			 }
		}catch (IOException e){
			System.err.println("Petadisima");
			e.printStackTrace();
		}
	}

}
