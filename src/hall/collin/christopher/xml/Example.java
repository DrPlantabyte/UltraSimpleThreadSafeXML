package hall.collin.christopher.xml;

import java.io.FileReader;
import java.util.Date;

/**
 *
 * @author CCHall
 */
public class Example {

	/** Version string for comparing versions */
	public static final String VERSION = Element.VERSION;
    /**
	 * This example opens an XML file, parses it, adds a timestamp attribute, 
	 * then prints the new XML as a string to the std out.
     * @param r not used
     */
   public static void main(String[] r){
		javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
		int action = fc.showOpenDialog(null);
		if (action == javax.swing.JFileChooser.APPROVE_OPTION) {
			try {
				java.io.BufferedReader in = new java.io.BufferedReader(
						new java.io.FileReader(fc.getSelectedFile()));
				StringBuilder document = new StringBuilder();
				while (in.ready()) {
					document.append(in.readLine());
					document.append('\n');
				}
				String filecontent = document.toString();
				long t0 = System.currentTimeMillis();
			//	Element doc = RootElement.parseXML(new FileReader(fc.getSelectedFile())); // alternative method
				Element doc = RootElement.parseXML(filecontent);
				long t1 = System.currentTimeMillis();
				System.out.println("Parsed XML in " + (t1 - t0) + "ms");
				doc.getAllElementChildren().get(0).setAttribute("timestamp", (new Date()).toString());
				System.out.println(doc.toString());
			} catch (java.io.IOException ex) {
				System.err.println(ex);
				System.exit(1);
			}
		}
		System.exit(0);
	}

}
