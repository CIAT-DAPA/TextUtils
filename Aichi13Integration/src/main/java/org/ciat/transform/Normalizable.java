package org.ciat.transform;

import java.io.File;
import org.ciat.model.Basis;
import org.ciat.model.DataSourceName;

public interface Normalizable {
	
	public static Basis getBasis(String basisofrecord) {
		return null;
	}
	
	public static DataSourceName getDataSourceName() {
		return null;
	}
	
	public static boolean isUseful(String[] values) {
		return false;
	}
	
	public static void process(File input, File output){
		
	}
	
	public static String normalize(String line){
		return line;
		
	}

}
