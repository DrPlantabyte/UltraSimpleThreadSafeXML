package hall.collin.christopher.xml;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This class is used to represent the unnamed root of an XML tree.
 * 
 * @author Christopher Collin Hall
 */
public class RootElement extends Element implements java.io.Serializable {
	/** In case the tagname is needed for the root, this is it */
	public static final String ROOT_TAGNAME = "_root";

	/** default constructor */
	public RootElement(){
		super(ROOT_TAGNAME);
	}
	/**
	 * Parses XML into a RootElement, which is an extension of the Element
	 * class.
	 * @param xml XML document as a String
	 * @return A RootElement whose children are top level of text and tags from
	 * the provided XML
	 * @throws IllegalArgumentException Thrown if there was a syntax error in
	 * the XML.
	 */
	public static RootElement parseXML(String xml) throws IllegalArgumentException{
		String document = XMLFormatter.removeComments(xml);
		RootElement root = new RootElement();
		root = (RootElement) parseContent(root, document);
		return root;
	}
	/**
	 * Loads the text from the <code>xmlSource</code> and then parses it as XML.
	 * @param xmlSource A <code>java.io.Reader</code> that can read text from 
	 * a source (e.g. <code>parseXML(new FileReader(xmlFile))</code> ). This
	 * reader will be buffered automatically, so providing a BufferedReader
	 * would be redundant.
	 * @return A RootElement whose children are top level of text and tags from
	 * the provided XML
	 * @throws IllegalArgumentException Thrown if there was a syntax error in
	 * the XML.
	 * @throws IOException Thrown if the reader's InputStream could not be read
	 */
	public static RootElement parseXML(java.io.Reader xmlSource)
			throws IllegalArgumentException, IOException{
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(xmlSource);
		char[] buffer = new char[1024];
		while(reader.ready()){
			int length = reader.read(buffer);
			if(length < 0) break;
			sb.append(buffer, 0, length);
		}
		reader.close();
		return parseXML(sb.toString());
	}
	/**
	 * Parses the content between the openning and closing tags of an element.
	 * This is a recursive function that calls itself to parse the contents of
	 * the elements within this content
	 * @param root The root element who is the owner of this content
	 * @param content XML from between <code>root</code>'s opening and closing
	 * tags.
	 * @return Returns <code>root</code>, after parsing its content.
	 * @throws IllegalArgumentException Thrown if there was a syntax error in
	 * the XML.
	 */
	protected static Element parseContent(Element root, String content) throws IllegalArgumentException{
		int lastTagEnd = 0;
		int scanpos = 0;
		try {
			while (scanpos >= 0 && scanpos < content.length()) {
				boolean noMoreTags = false;
				scanpos = content.indexOf("<", scanpos); // find next tag
				if (scanpos < 0) { // hit end of content (no more tags after this text)
					scanpos = content.length();
					noMoreTags = true;
				}
				String text = content.substring(lastTagEnd, scanpos).trim();
				// add text between tags as content
				if (text.length() > 0) {
					root.addChild(XMLFormatter.XMLToString(text));
				}
				if (noMoreTags) {
					break;
				}
				// make a string that holds the name and attributes of this tag
				lastTagEnd = content.indexOf(">", scanpos) + 1; // next closing bracket
				if (lastTagEnd <= 0) {// problem! Hit end of content too early (syntax error)
					throw new IllegalArgumentException("Malformed XML! < and > mismatch!\n\n" + content);
				}
				String tagDef = content.substring(scanpos, lastTagEnd);
				Element e = makeTag(tagDef); // turn that string into a tag
				if (tagDef.endsWith("/>")) {
					// this is an empty tag
					root.addChild(e);
					scanpos = lastTagEnd;
				} else {
					// this is not an empty tag, and it's own contents need to be parsed
					scanpos = content.indexOf("</" + e.getTagName(), lastTagEnd);
					String subcontent = content.substring(lastTagEnd, scanpos);
					root.addChild(parseContent(e, subcontent)); // parse contents into tag and then add it to root element
					lastTagEnd = content.indexOf(">", scanpos) + 1; // jump forward to end of that tag we just parsed
					if (lastTagEnd <= 0) {// problem!
						throw new IllegalArgumentException("Malformed XML! < and > mismatch!\n\n" + content);
					}
					scanpos = lastTagEnd;
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Malformed XML! Unable to parse '" + content + "'");
		}

		return root;
	}
	/**
	 * Makes an element from the (opening) tag from the XML text
	 * @param tag e.g. <code>&lt;hippy name="fred"&gt;</code>
	 * @return The element described by the XML, including attributes, but
	 * excluding children (no content yet).
	 */
	private static Element makeTag(String tag){
		// first, remove < and >
		String nakedtag = tag.replace("/>", "");
		nakedtag = nakedtag.replace(">", "");
		nakedtag = nakedtag.replace("<", "");
		String[] params = splitStringByWhiteSpace(nakedtag);
		if(params.length == 0){
			return null;
		} else {
			Element e = new Element(params[0]);
			for(int i = 1; i < params.length; i++){
				// add attributes
				if(params[i].length() > 0){
					// only bother with non-empty Strings
					// 3 possible scenarios: attribute1=value1 attribute2="value2" attribute3
					String attribute = params[i];
					attribute = attribute.replace("\"", "");
					attribute = attribute.replace("'", "");// remove quotes
					int namelength = attribute.indexOf("=");
					boolean hasvalue = true;
					if(namelength < 0){
						namelength = attribute.length();
						hasvalue = false;
					}
					String attname = attribute.substring(0, namelength);
					if(hasvalue){
						String value = attribute.substring(namelength + 1, attribute.length());
						e.setAttribute(attname, XMLFormatter.XMLToString(value));
					} else {
						e.setAttribute(attname, null);
					}
				}
			}
			return e;
		}
	}

	/**
	 * Takes a String and splits it into an array of strings using whitespace as
	 * the delimiter. Whitespace inside quotations will not be split.
	 * @param s A string you want to split
	 * @return A String array of the split strings, in the order they were in
	 * the original string.
	 */
	private static String[] splitStringByWhiteSpace(String s){
		String input = s.trim();
		java.util.ArrayList<String> strs = new java.util.ArrayList<String>();
		int startwsp = 0;
		int endwsp = 0;
		boolean inwhitespace = true;
		boolean inquote = false;
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			if(i + 1 == input.length()){// hit the end
				strs.add(input.substring(endwsp,input.length()));
			}
			if (inquote) {
				if (c == '"' || c == '\'') {
					inquote = false;
					continue;
				} else {
					continue;
				}
			} else {
				if (c == '"' || c == '\'') {
					inquote = true;
					continue;
				}
			}
			if(inwhitespace){
				if(Character.isWhitespace(c) == false){// exiting whitespace
					endwsp = i;
					inwhitespace = false;
				}
			} else {
				if(Character.isWhitespace(c)){// entering whitespace
					startwsp = i;
					strs.add(input.substring(endwsp,startwsp));
					inwhitespace = true;
				}
			}
		}

		return strs.toArray(new String[strs.size()]);
	}

	/**
	 * Rebuilds the XML document
	 * @return A String of the XML representation of this XML tree
	 */
	@Override
	public String toString() {
		String newLine = XMLFormatter.getInstance().getLineTerminator();
		StringBuilder string = new StringBuilder();
		for (int i = 0; i < super.countChildren(); i++) {
			Object child = super.getChild(i);
			if (child instanceof Element) {
				Element e = (Element) child;
				string.append(e.toString(0,newLine));
			} else {
				string.append(XMLFormatter.stringToXML(child.toString()));
			}
			string.append("\n");
		}
		return string.toString();
	}
	
}
