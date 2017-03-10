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
					opts.put(current, sb.toString()); // add to dict
					sb.setLength(0); // reset length
				}
				current = args[i];
			} else {
				sb.append(args[i] + " ");
			}
		}
	}
	
	public String getOpt(String name) {
		return opts.get(name);
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
				partial &= hasOpt(opts[i][j]); // elementos del mismo array son necesarios (AND)
			}
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
	public boolean isQuering() {
		// TODO: Fixme
		String[] queringOpts1 = {"-nose","-que"};
		String[] queringOpts2 = {"-va","-aqui"};
		String[] queringOpts3 = {"-asi","-que"};
		String[] queringOpts4 = {"-ya","-luego"};
		String[][] queringOpts = {queringOpts1, queringOpts2, queringOpts3, queringOpts4};
		return checkPresent(queringOpts);
	}

	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de reconstrucción de índice (parte 3 de la práctica)
	 */
	public boolean isRebuilding() {
		// TODO: Fixme
		String[] rebuildingOpts1 = {"-nose","-que"};
		String[] rebuildingOpts2 = {"-va","-aqui"};
		String[] rebuildingOpts3 = {"-asi","-que"};
		String[] rebuildingOpts4 = {"-ya","-luego"};
		String[][] rebuildingOpts = {rebuildingOpts1, rebuildingOpts2, rebuildingOpts3, rebuildingOpts4};
		return checkPresent(rebuildingOpts);
	}
}
