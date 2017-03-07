package es.udc.fic.mri_indexer;

import java.util.ArrayList;
import java.util.List;

class TrituradoraArgumentos {

	private String index;
	private List<String> colls;
	private String om;
	private List<String> indexes1;
	private String indexes2;

	public void triturar(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-index")) {
				index = args[++i];
			} else if (args[i].startsWith("-coll")) {
				colls = new ArrayList<String>();
				for (int j = i + 1; j < args.length; j++) {
					if (args[j].startsWith("-")) {
						i = j - 1;
						j = args.length;
					} else {
						colls.add(args[j]);
					}
				}
			} else if (args[i].equals("-openmode")) {
				om = args[++i];
			} else if (args[i].equals("-indexes1")) {
				indexes1 = new ArrayList<String>();
				for (int j = i + 1; j < args.length; j++) {
					if (args[j].startsWith("-")) {
						i = j - 1;
						j = args.length;
					} else {
						indexes1.add(args[j]);
					}
				}
			} else if (args[i].equals("-indexes2")) {
				indexes2 = args[++i];
			}
		}
	}

	public void validar() {

	}
}
