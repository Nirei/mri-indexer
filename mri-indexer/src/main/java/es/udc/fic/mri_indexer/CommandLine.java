package es.udc.fic.mri_indexer;

import java.util.HashMap;
import java.util.Map;

class CommandLine {
	
	private final Map<String,String> opts = new HashMap<>();

	public void triturar(String[] args) {
		opts.clear();

		String current = "";
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-")) {
				if(sb.length() != 0) {
					sb.deleteCharAt(sb.length()-1); // remove trailing whitespace
					System.out.println("Parsed " + current + " argument with " + sb.toString() + " options");
					opts.put(current, sb.toString()); // add to dict
					sb.setLength(0); // reset length
				}
				current = args[i];
			} else {
				sb.append(args[i] + " ");
			}
		}
		
		// Add remaining options
		if(sb.length() != 0) {
			sb.deleteCharAt(sb.length()-1); // remove trailing whitespace
			System.out.println("Parsed " + current + " argument with " + sb.toString() + " options");
			opts.put(current, sb.toString()); // add to dict
			sb.setLength(0); // reset length
		}
		
		System.out.println("Parsed options " + opts.keySet());
	}

	/**
	 * Obtiene un argumento de línea de comandos por su nombre.
	 * @param name - nombre del argumento
	 * @return El argumento de línea de comandos especificado por name
	 */
	public String getOpt(String name) {
		return opts.get(name);
	}

	/**
	 * Como getOpt pero tira una excepción si no hay.
	 * @param name
	 * @return El argumento de línea de comandos especificado por name
	 * @throws MissingArgumentException
	 */
	public String checkOpt(String name) throws MissingArgumentException {
		if(!opts.containsKey(name)) throw new MissingArgumentException();
		return getOpt(name);
	}
	
	public boolean hasOpt(String name) {
		return opts.containsKey(name);
	}
	
	/**
	 * Comprueba si está presente alguno de los conjuntos de opciones especificados
	 * en opts.
	 * @param opts Array de arrays de opciones. Cada elemento de este array es un array
	 * de opciones que de estar presentes en el diccionario hacen que el valor devuelto
	 * sea verdadero.
	 */
	private boolean checkPresent(String[][] opts) {
		boolean result = false;
		for(int i=0; i<opts.length; i++) {
			boolean partial = true;
			for(int j=0; j<opts[i].length; j++) {
				if(hasOpt(opts[i][j]))
					System.out.println("We have opt " + opts[i][j]);
				else
					System.out.println("We dont have " + opts[i][j]);
				partial &= hasOpt(opts[i][j]); // elementos del mismo array son necesarios (AND)
			}
			System.out.println("Result is " + partial);
			result |= partial; // cada uno de los arrays es suficiente (OR)
		}
		return result;
	}
	
	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de creación de índices (parte 1 de la práctica)
	 */
	public boolean isIndexing() {
		String[] indexingOpts1 = {"-index","-coll"};
		String[] indexingOpts2 = {"-index","-colls"};
		String[] indexingOpts3 = {"-indexes1","-colls"};
		String[] indexingOpts4 = {"-indexes2","-colls"};
		String[][] indexingOpts = {indexingOpts1, indexingOpts2, indexingOpts3, indexingOpts4};
		return checkPresent(indexingOpts);
	}
	
	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de procesamiento de índice (parte 2 de la práctica)
	 */
	public boolean isSearching() {
		String[] queringOpts1 = {"-indexin","-best_idfterms"};
		String[] queringOpts2 = {"-indexin","-poor_idfterms"};
		String[] queringOpts3 = {"-indexin","-best_tfidfterms"};
		String[] queringOpts4 = {"-indexin","-poor_tfidfterms"};
		String[][] queringOpts = {queringOpts1, queringOpts2, queringOpts3, queringOpts4};
		return checkPresent(queringOpts);
	}

	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de reconstrucción de índice (parte 3 de la práctica)
	 */
	public boolean isRebuilding() {
		String[] rebuildingOpts1 = {"-indexin","-deldocsterm"};
		String[] rebuildingOpts2 = {"-indexin","-deldocsquery"};
		String[] rebuildingOpts3 = {"-indexin","-indexout","-mostsimilardoc_title"};
		String[] rebuildingOpts4 = {"-indexin","-indexout","-mostsimilardoc_body"};
		String[][] rebuildingOpts = {rebuildingOpts1, rebuildingOpts2, rebuildingOpts3, rebuildingOpts4};
		return checkPresent(rebuildingOpts);
	}
	
	class MissingArgumentException extends Exception {
		private static final long serialVersionUID = 7146926153071567017L;
	}
}
