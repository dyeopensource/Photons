import java.io.IOException;


public class Photons {

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
		
		DatabaseUtil.openOrCreateDatabase(destinationPath);
		
		FileImporter fileImporter = new FileImporter(sourcePath, destinationPath, type);
		try {
			fileImporter.Import();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Done");
	}

}
