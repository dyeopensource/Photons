package ui;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import modell.FileImporter;

import common.MyLogger;


public class Photons {
	
	private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd-HHmmSS");  
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: implement logging

		//String sourcePath = "/home/emil/Képek/source/";
		String sourcePath = "/media/emil/";
		String destinationPath = "/home/emil/Képek/imported/";
		String type = "jpg";
		
		if (args.length > 0) {
			sourcePath = args[0];
		}
		
		if (args.length > 1) {
			destinationPath = args[1];
		}
		
		if (args.length > 2) {
			type = args[2];
		}
		
		//logFileNameWithPath = Paths.get(destinationPath, "log_" + dateTimeFormatter.format(new Date()) + ".txt").toString();
		MyLogger.setActionLogFile(Paths.get(destinationPath, String.format("actions_%s.txt", dateTimeFormatter.format(new Date()))).toString());
        //String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		FileImporter fileImporter = new FileImporter(sourcePath, destinationPath, type);
		try {
			fileImporter.Import();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyLogger.displayActionMessage("Done");
	}
}
