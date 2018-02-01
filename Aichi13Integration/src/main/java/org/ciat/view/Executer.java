package org.ciat.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Executer implements Executable {
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private Date startDate;
	
	public Executer() {
		this.startDate = new Date();
	}
	

	public static void log(String message) {
		System.out.println();
		System.out.println(getTimestamp() + " " + message);
	}

	public static String getTimestamp() {
		Date date = new Date();
		return dateFormat.format(date);
	}

	@Override
	public void run() {
		
		
	}


	public Date getStartDate() {
		return startDate;
	}


}
