package hall.collin.christopher.xml;
import java.util.*;
/**
 * The SAX XML parser packaged with Java is not thread safe and fails miserably
 * in the true multithreaded application. As such, this class is
 * part of a bare-bones XML writing and reading parser that is fully thread safe.
 * <p/>
 * <b>This class IS thread-safe!</b>
 * <p/>
 * @author Christopher Collin Hall
 */
public class Element implements java.io.Serializable, Cloneable{
	/** Version string for comparing versions */
	public static final String VERSION = "1.0.1";

	/** This is the name of this tag. Ex: &lt;font face="serif"&gt; has a
	 * tagname of "font".
	 */
	private String tagname;
	/** A <i>synchronized</i> map for holding key-value pairs that represent the 
	 * attributes (the key is the attribute name)
	 */
	private Map<String,String> attributeMap;
	/** This list holds the text and elements contained by this tag. If it is
	 * an empty tag (e.g. &lt;br &#47;&gt;), then this list has a size of 0.
	 */
	private List<Object> contents;
	
/** default contsructor, try not to use it if you don't have to */
	public Element(){
		initialize();
	}

	/**
	 * Constructs an Element object with the specified tagname
	 * @param tagname The name of the tag (e.g. &lt;font face="serif"&gt; has a
	 * tagname of "font").
	 * @throws IllegalArgumentException Thrown is <code>tagname</code> is not
	 * a valid name for an XML tag (has innappropriate characters or whitespace).
	 */
	public Element(String tagname){
		initialize();
		if(isXMLTagNameFormatted(tagname)){
			this.tagname = tagname;
		} else {
			// bad name
			throw new IllegalArgumentException(tagname + " is not a valid tagname."
					+ " XML tag names must not contain whitespaces or escape "
					+ "characters.");
		}

	}
	/**
	 * Adds an attribute to this element.
	 * @param name Name of the attribute
	 * @param value Value of the attribute
	 * @deprecated Use <code>setAttribute(name, value)</code> instead;
	 */
	public void addAttribute(String name, String value){
		setAttribute(name, value);
	}
	/**
	 * Retrieves the value of an attribute
	 * @param name The name of the attribute
	 * @return The value of the attribute, or <code>null</code> if the named 
	 * attribute does not exist for this element.
	 */
	public String getAttributeValue(String name){
		return attributeMap.get(name);
	}
	/**
	 * Retrieves the value of an attribute. This method is only for drop-in
	 * replacement of <code>org.w3c.dom.Element</code> as it merely passes the
	 * invokation along to <code>getAttributeValue(String)</code>.
	 * @param name The name of the attribute
	 * @return The value of the attribute, or <code>null</code> if the named
	 * attribute does not exist for this element.
	 */
	public String getAttribute(String name){
		return getAttributeValue(name);
	}
	/**
	 * Sets the value of an attribute for this element If the attribute does not
	 * already exist, it is automatically created.
	 * @param name The name of the attribute
	 * @param Value The (new) value of the attribute
	 * @throws IllegalArgumentException Thrown if <code>name</code> is not
	 * a valid name for an XML attribute (has innappropriate characters or whitespace).
	 */
	public void setAttribute(String name, String Value){
		if(isXMLTagNameFormatted(name)){
			attributeMap.put(name, Value);
		}else {
			// bad name
			throw new IllegalArgumentException(name + " is not a valid attribute name."
					+ " XML atribute names must not contain whitespaces or escape "
					+ "characters.");
		}
	}
	/**
	 * Checks for th eexistance of an attribute
	 * @param name Name of the attribute
	 * @return <code>true</code> if that attribute has been set for this element,
	 * <code>false</code> otherwise.
	 */
	public boolean hasAttribute(String name){
		return attributeMap.containsKey(name);
	}
	/**
	 * Removes the specified attribute from this element.
	 * @param name The name of the attribute to remove
	 */
	public void removeAttribute(String name){
		attributeMap.remove(name);
	}
	/**
	 * Removes all of the attributes from this element
	 */
	public void removeAllAttributes(){
		attributeMap.clear();
	}
	/**
	 * Returns a list of the names of all of the attributes.
	 * @return A copy of the names of all of the attributes of this element,
	 * packed into a List object for convenience.
	 */
	public List<String> getAllAttributeNames(){
		return new ArrayList<String>(attributeMap.keySet());
	}
	/**
	 * Adds a portion of text as a child to this element.<p/>
	 * Ex: <br/><code>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;font size="2"&gt;Greetings &lt;b&gt;friend!&lt;&#47;b&gt;&lt;&#47;font&gt;
	 * </code><br/>
	 * In the above code, the children of the <b><code>font</code></b> element are (in
	 * order) <i>"Greetings "</i> and <b><code>b</code></b>. <i>"Greetings "</i> is a text child and
	 * <b><code>b</code></b> is an element child. <b><code>b</code></b> has <i>"friend!"</i> as its
	 * child (<i>"friend!"</i> is not a child of <b><code>font</code></b>).
	 * @param textChild A string of text to add
	 */
	public void addChild(String textChild){
		contents.add(textChild);
	}
	/**
	 * Adds an element child inside this tag.<p/>
	 * Ex: <br/><code>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;font size="2"&gt;Greetings &lt;b&gt;friend!&lt;&#47;b&gt;&lt;&#47;font&gt;
	 * </code><br/>
	 * In the above code, the children of the <b><code>font</code></b> element are (in
	 * order) <i>"Greetings "</i> and <b><code>b</code></b>. <i>"Greetings "</i> is a text child and
	 * <b><code>b</code></b> is an element child. <b><code>b</code></b> has <i>"friend!"</i> as its
	 * child (<i>"friend!"</i> is not a child of <b><code>font</code></b>).
	 * @param child An element to add as a child to this element
	 */
	public void addChild(Element child){
		contents.add(child);
	}
	/**
	 * Ex: <br/><code>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;font size="2"&gt;Greetings &lt;b&gt;friend!&lt;&#47;b&gt;&lt;&#47;font&gt;
	 * </code><br/>
	 * In the above code, the <b><code>font</code></b> element has 2 children:
	 * <i>"Greetings "</i> and <b><code>b</code></b>. <i>"Greetings "</i> is a
	 * text child and <b><code>b</code></b> is an element child.
	 * <b><code>b</code></b> has one child: <i>"friend!"</i>. <i>"friend!"</i>
	 * is not a child of <b><code>font</code></b>.
	 * @return The number of children that this element has
	 */
	public int countChildren(){
		return contents.size();
	}
	/**
	 * Fetches a child from this element.
	 * @param index Index of the child (first child is index 0).
	 * @return The child, which should either be a String or another element
	 * object.
	 */
	public Object getChild(int index){
		return contents.get(index);
	}

	/**
	 * Scans through the children of this element and returns those of the type
	 * specified.
	 * @param tagname Tag name of the elements you want.
	 * @param caseSensitive Whether or not to use case-sensitive matching for
	 * <code>tagname</code>
	 * @return A list of Elements whose tag names match
	 */
	public List<Element> getChildElementsByTagName(String tagname, boolean caseSensitive){
		List<Element> list = new ArrayList<Element>();
		for (Object child : contents) {
			if (child instanceof Element) {
				Element e = (Element) child;
				if (caseSensitive) {
					if (e.getTagName().equals(tagname)) {
						list.add(e);
					}
				} else {
					if (e.getTagName().equalsIgnoreCase(tagname)) {
						list.add(e);
					}
				}
				list.addAll(e.getChildElementsByTagName(tagname, caseSensitive));
			}
		}
		return list;
	}
	/**
	 * Scans through the children of this element and returns those of the type
	 * specified.
	 * @param tagname Tag name of the elements you want. This is not case
	 * sensitive.
	 * @return A list of Elements whose tag names match
	 */
	public List<Element> getChildElementsByName(String tagname){
		return getChildElementsByTagName(tagname, false);
	}
	/**
	 * 
	 * @return A list holding all of the children (text children and elements) 
	 * of this Element
	 */
	public List<Object> getAllChildren(){
		List<Object> copy = new ArrayList<Object>(contents.size());
	//	Collections.copy(copy, contents);
		for(Object o : contents){
			copy.add(o);
		}
		return copy;
	}
	/**
	 *
	 * @return A list of all of the text shildren of this Element
	 */
	public List<String> getAllTextChildren(){
		List<String> copy = new ArrayList<String>();
		for(Object o : contents){
			if(o instanceof String){
				copy.add((String)o);
			}
		}
		return copy;
	}
	/**
	 *
	 * @return A list of all of the non-text children of this Element
	 */
	public List<Element> getAllElementChildren(){
		List<Element> copy = new ArrayList<Element>();
		for(Object o : contents){
			if(o instanceof Element){
				copy.add((Element)o);
			}
		}
		return copy;
	}
	/**
	 * Removes the specified child from this element.
	 * @param index
	 */
	public void removeChild(int index){
		contents.remove(index);
	}
	/**
	 * Removes the specified child from this element.
	 * @param child The object to be removed
	 */
	public void removeChild(Object child){
		contents.remove(child);
	}
	/**
	 * Removes all of ht echildren from this element
	 */
	public void removeAllChildren(){
		contents.clear();
	}
	/**
	 * Checks to see if the specified object is a child of this element
	 * @param obj An object that may or may not already be a child of this
	 * element.
	 * @return <code>true</code> if this element has <code>obj</code> as one
	 * of its children.
	 */
	public boolean containsChild(Object obj){
		return contents.contains(obj);
	}
	/**
	 * Determines if this element is empty. An XML element is said to be empty
	 * if it has no children (nothing to put between opening and closing tags).
	 * @return
	 */
	public boolean isEmpty(){
		if(countChildren() == 0){
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 
	 * @return The name of this tag.
	 */
	public String getTagName(){
		return this.tagname;
	}
	/**
	 * Sets the name of this tag.
	 * @param tagname The new name for this tag.
	 */
	public void setTagName(String tagname){
		this.tagname = tagname;
	}
	/**
	 * This will convert this element into its XML representation, returned as a
	 * String.
	 * @return The XML representation of this element and all of its children.
	 * This is equivalent to invoking <code>toString(0)</code>.
	 */
	@Override
	public String toString(){
		String newLine = XMLFormatter.getInstance().getLineTerminator();
		return toString(0,newLine);
	}
	/**
	 * Converts this element and all of its children into their XML
	 * representation, with the indecated amount of extra indentation.
	 * @param indentNumber How many extra tabs to indent
	 * @param newLine What character(s) to use as the liner terminator
	 * @return The XML representation of this element and all of its children.
	 */
	public String toString(int indentNumber, String newLine){
		StringBuilder string = new StringBuilder();
		// first indent
		for(int i = 0; i < indentNumber; i++){
			string.append(XMLFormatter.INDENT);
		}
		string.append("<");
		string.append(this.tagname); // write the tag
		// then write its attributes
		List<String> attributeNames = this.getAllAttributeNames();
		for(String attributeName : attributeNames){
			string.append(" ");
			string.append(attributeName);
			if(this.getAttributeValue(attributeName) != null){ // attributes without values return null
				// attribute with value
				string.append("=\"");
				string.append(XMLFormatter.stringToXML(this.getAttributeValue(attributeName)));
				string.append("\"");
			}
		}
		if(this.isEmpty()){ // if this is an empty tag, no need for closing tags
			string.append("/>");
		} else { // not empty, so write out all of the content
			string.append(">");
			string.append(newLine);
			int newIndent = indentNumber + 1;
			for(Object child : this.contents){
				if(child instanceof Element){
					Element e = (Element)child;
					string.append(e.toString(newIndent,newLine));
				} else {
					// add the new indent distance
					for(int i = 0; i < newIndent; i++){
						string.append(XMLFormatter.INDENT);
					}
					// add the object's toString() String
					string.append(XMLFormatter.stringToXML(child.toString()));
				}
				string.append(newLine);
			}
			// done writing the content of this element, now add the closing tag
			for(int i = 0; i < indentNumber; i++){
					string.append(XMLFormatter.INDENT);
			}
			string.append("</");
			string.append(this.tagname);
			string.append(">");
			string.append(newLine);
		}
		return string.toString();
	}

	/** Used by contructors to initialize the fields to their default values */
	private void initialize() {
		tagname = null;
		attributeMap = Collections.synchronizedMap(new HashMap<String,String>());
		contents = Collections.synchronizedList(new LinkedList<Object>());
	}

	/**
	 *
	 * @return Returns a deep copy of this object
	 */
	@Override
	public Element clone(){
		Element copy = new Element();
		copy.tagname = tagname;
		copy.attributeMap = Collections.synchronizedMap(new HashMap<String,String>(attributeMap));
		copy.contents = Collections.synchronizedList(deepCopyList(contents));

		return copy;
	}


	/**
	 * Tests if the given string is safe for  an XML tag name. By safe, I mean
	 * that it contains no characters that need to be escaped and it has no
	 * whitespaces.<br/>
	 * The characters that need to be escaped (and their corresponding escapes)
	 * are:<br/>
	 * <code>
	 * "&nbsp;&nbsp;(&amp;quot;)<br/>
	 * '&nbsp;&nbsp;(&amp;apos;)<br/>
	 * &lt;&nbsp;&nbsp;(&amp;lt;)<br/>
	 * &gt;&nbsp;&nbsp;(&amp;gt;)<br/>
	 * &amp;&nbsp;&nbsp;(&amp;amp;)<br/>
	 * </code>
	 * @param s A String you want to check
	 * @return <code>true</code> if and only if there are no problematic
	 * characters in the string
	 */
	private static boolean isXMLTagNameFormatted(String s){
		Character[] illegalchars = {'<','>','\'','"','&','/'};
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if(Character.isWhitespace(c)){
				return false;
			}
			for(Character bad : illegalchars){
				if(c == bad){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Performs a custom deep copy operation on the given list where all
	 * Elements in this list are deep-copied and everything else is shallow
	 * copied. Note that everything should be either an Element (which gets deep-
	 * copied) or a String (which is immutable, so it behaves like a deep-copy).
	 * @param list List to be deep-copied
	 * @return A list that is a deep copy of <code>list</code>.
	 */
	private List<Object> deepCopyList(List<Object> list){
		LinkedList<Object> copy = new LinkedList<Object>();
		for(Object o : list){
			if(o instanceof Element){
				Element e = (Element)o;
				copy.add(e.clone());
			} else {
				copy.add(o);
			}
		}
		return copy;
	}

	/** testing only
	 * @deprecated
	 */
	public static void main(String[] args){
		Element e1 = new Element("root");
		e1.setAttribute("name", "Bob");
		e1.setAttribute("soil", "moist");
		Random r = new Random();
		e1.addChild("I was'a growin' an' a diggin' ");
		Element bold = new Element("b");
		bold.addChild("real \"deep & dirty\"");
		e1.addChild(bold);
		System.out.println(e1);

		String test = "Hi <!-- \ncomments\n\t boooooooooogy-->there!";
		System.out.println(test.replaceAll("<!--[\\s\\S]*?-->", ""));
		System.err.println(e1.getAllText());
	}
	/**
	 * 
	 * @return All of the text in this tag as a single String, including text in 
	 * child Elements.
	 */
	public String getAllText() {
		StringBuilder allText = new StringBuilder();
		List<Object> children = this.getAllChildren();
		for(Object child : children){
			if(child instanceof String){
				allText.append((String)child);
			} else if(child instanceof Element){
				allText.append(((Element)child).getAllText());
			}
		}
		return allText.toString();
	}
}
