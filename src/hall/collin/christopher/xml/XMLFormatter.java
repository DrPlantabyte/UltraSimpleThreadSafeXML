package hall.collin.christopher.xml;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * The SAX XML parser packaged with Java is not thread safe and fails miserably
 * in the true multithreaded application. As such, this class is
 * part of a bare-bones XML writing and reading parser that is fully thread safe.<p/>
 * This class is simply used to convert between java Strings and XML text (which
 * must use escape sequences for certain characters).<p/>
 * The characters that need to be escaped (and their corresponding escapes)
 * are:<br/>
 * <code>
 * &amp; - &amp;amp;<br/>
 * " - &amp;quot;<br/>
 * ' - &amp;apos;<br/>
 * &lt; - &amp;lt;<br/>
 * &gt; - &amp;gt;<br/>
 * </code>
 *<p/>
 * @author Christopher Collin Hall
 */
public final class XMLFormatter implements java.io.Serializable {
	/** Version string for comparing versions */
	public static final String VERSION = Element.VERSION;
	
	public static final String INDENT = "\t";
	/** Pairs of XML special characters and their escape equences
	 The characters that need to be escaped (and their corresponding escapes)
	 * are:<br/>
	 * <code>
	 * &amp;&nbsp;&nbsp;(&amp;amp;)<br/>
	 * "&nbsp;&nbsp;(&amp;quot;)<br/>
	 * '&nbsp;&nbsp;(&amp;apos;)<br/>
	 * &lt;&nbsp;&nbsp;(&amp;lt;)<br/>
	 * &gt;&nbsp;&nbsp;(&amp;gt;)<br/>
	 * </code>
	 */
	private final static String[][] escapePairs = {
		{"&","&amp;"},
		{"\"","&quot;"},
		{"'","&apos;"},
		{"<","&lt;"},
		{">","&gt;"}
		
	};
	/** singleton instance */
	private static XMLFormatter instance = null;

	/** this lock is used to make singleton initialization thread safe */
	private static final Lock initLock = new ReentrantLock();
	/**
	 * Thread-safe singleton initialization
	 * @return a singleton instance of this class
	 */
	public static XMLFormatter getInstance(){
		if(instance == null){
			initLock.lock();
			try{
				if(instance == null){
					instance = new XMLFormatter();
				}
			} finally{
				initLock.unlock();
			}
		}
		return instance;
	}
	/** This is the character of string designated for line termination. */
	private String NEWLINE;
	/** Constructor */
	private XMLFormatter(){
		String ln = "\n";
		try{
			ln = System.getProperty ( "line.separator" );
		} catch(SecurityException ex){
			//
			System.err.println("Caution: Security manager may interfere with program operation.");
		}
		if(ln == null) ln = "\n";
		NEWLINE = ln;

	}
	/** This ReadWriteLock object is used to make changes to resources thread-safe */
	protected final ReadWriteLock rwlock = new ReentrantReadWriteLock();
	/**
	 * This method is thread safe.
	 * @return Returns the currently set newline character/string
	 */
	public String getLineTerminator(){
		rwlock.readLock().lock();
		try{
			return NEWLINE;
		} finally {
			rwlock.readLock().unlock();
		}
	}
	/**
	 * Sets the current new line characters. This method is thread safe.
	 * @param newLine typically either "\r\n" or "\n"
	 */
	public void setLineTerminator(String newLine){
		rwlock.writeLock().lock();
		try{
			NEWLINE = newLine;
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * Formats the given string for XML by substituting characters with special
	 * meaning to their escape sequences.
	 * @param string A Java String that you wish to convert to XML
	 * @return The XML representation of <code>string</code>
	 */
	public static String stringToXML(String string){
		String xmlready = string;
		for(int p = 0; p < escapePairs.length; p++){
			xmlready = xmlready.replace(escapePairs[p][0], escapePairs[p][1]);
		}
		return xmlready;
	}
	/**
	 * Parses an XML formatted string and returns the Java String that it
	 * represents.
	 * @param xml an XML string
	 * @return A string with all of the XML escapes returned to their
	 * corresponding characters.
	 */
	public static String XMLToString(String xml){
		String javaready = xml;
		for(int p = escapePairs.length - 1; p >= 0; p--){// go in reverse order from stringToXML to faithfully undue that operation
			javaready = javaready.replace(escapePairs[p][1], escapePairs[p][0]);
		}
		return javaready;
	}
	/**
	 * Removes XML comments from the provided string using the following regex:<br/> 
	 * <code>input.replaceAll("&lt;!--[\\s\\S]*?--&gt;", "").replaceAll("&lt;\\?[\\s\\S]*?\\?&gt;", "")</code>
	 * @param input A string that may contain comments
	 * @return A string with the comments removed
	 */
	public static String removeComments(String input){
		return input.replaceAll("<!--[\\s\\S]*?-->", "").replaceAll("<\\?[\\s\\S]*?\\?>", "");
		// the regex <!--[\s\S]*?--> means removes everything that 'is or isn't space' between <!-- and -->
		// the regex <\\?[\\s\\S]*?\\?> means removes everything that 'is or isn't space' between <? and ?>
	}

}
