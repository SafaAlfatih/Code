package com.MW.chatServer.jabber.xml;

import com.MW.chatServer.log.Log;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
*
* @author Siddiq Hamed
* @Title : Formatter
* @version : 1.0
* this class is used to format text sent through XML to PayChat client.
*/
public class Formatter {
	  Hashtable<String , String> specialCharacters ;

	    public Formatter()
	    {
	        /*
	&#09;	Horizontal tab	    non-printing
	&#10;	Line feed	        non-printing
	&#13;	Carriage Return	    non-printing
	&#32;	Space	            non-printing
	&#33;	Exclamation mark	!
	&#34;	Quotation mark	    "
	&#35;	Number sign	        #
	&#36;	Dollar sign	        $
	&#37;	Percent sign	    %
	&#38;	Ampersand	        &
	&#39;	Apostrophe	        '
	&#40;	Left parenthesis    (
	&#41;	Right parenthesis	)
	&#42;	Asterisk	        *
	&#43;	Plus sign	        +
	&#44;	Comma	            ,
	&#45;	Hyphen	            -
	&#46;	Period	            .
	&#47;	Slash	            /
	&#58;	Colon	            :
	&#59;	Semi-colon	        ;
	&#60;	Less than	        <
	&#61;	Equals sign	        =
	&#62;	Greater than	    >
	&#63;	Question mark	    ?
	&#64;	At	                @
	&#91;	Left square bracket	[
	&#92;	Bbackslash	        \
	&#93;	Right square bracket]
	&#94;	Caret	            ^
	&#95;	Underscore	        _
	&#96;	Acute accent	    `
	&#123;	Left curly brace	{
	&#124;	Vertical bar	    |
	&#125;	Right curly brace	}
	&#126;	Tilde               ~
	                */
	        specialCharacters = new Hashtable<String, String>();
	        specialCharacters.put("~","&#126;");
	        specialCharacters.put("}","&#125;");
	        specialCharacters.put("|","&#124;");
	        specialCharacters.put("{","&#123;");
	        specialCharacters.put("`","&#96;");
	        specialCharacters.put("_","&#95;");
	        specialCharacters.put("^","&#94;");
	        specialCharacters.put("]","&#93;");
	        specialCharacters.put("\\","&#92;"); // backslash is escaped in String
	        specialCharacters.put("[","&#91;");
	        specialCharacters.put("@","&#64;");
	        specialCharacters.put("?","&#63;");
	        specialCharacters.put(">","&#62;");
	        specialCharacters.put("=","&#61;");
	        specialCharacters.put("<","&#60;");
	        specialCharacters.put(";","&#59;");
	        specialCharacters.put(":","&#58;");
	        specialCharacters.put("/","&#47;");
	        specialCharacters.put(".","&#46;");
	        specialCharacters.put("-","&#45;");
	        specialCharacters.put(",","&#44;");
	        specialCharacters.put("+","&#43;");
	        specialCharacters.put("*","&#42;");
	        specialCharacters.put(")","&#41;");
	        specialCharacters.put("(","&#40;");
	        specialCharacters.put("'","&#39;");
	        specialCharacters.put("&","&#38;");
	        specialCharacters.put("%","&#37;");
	        specialCharacters.put("$","&#36;");
	        specialCharacters.put("#","&#35;");
	        specialCharacters.put("\"","&#34;"); // this is escaped in String
	        specialCharacters.put("!","&#33;");
	     // specialCharacters.put(" " ,"&#32;"); not necessary
	        specialCharacters.put("\r","&#13;");
	        specialCharacters.put("\f","&#10;");
	        specialCharacters.put("\t","&#9;");

	    }

	    // formats strings into XML friendly text that can be used safely with XML
	    // parsers and transferred through network without crashing the parser
	    // you can add all special chars to special characters hashtable above
	    public  String formatToXMLFriendly(String rawText)
	    {
	        // first : we replace every special character with it's numeric reference
	         LinkedHashMap<Integer,XMLchar> indexes=new LinkedHashMap<Integer,XMLchar>();
	        Set <String> specialCharsKeys = specialCharacters.keySet();
	        String current = "";
	        for(int i = 0 ; i<rawText.length() ; i++)
	        {
	             Iterator<String> specialCharsKeysIterator = specialCharsKeys.iterator();
	            current = rawText.charAt(i)+"";
	            boolean found = false ;
	            boolean normalChar = true ;
	            String  xmlchar ="";
	            while(specialCharsKeysIterator.hasNext())
	            {
	                String next = specialCharsKeysIterator.next();
	                if(current.equalsIgnoreCase(next))
	                {
	                    xmlchar = specialCharacters.get(next);
	                    found = true ;
	                    break;
	                }
	            }
	              if(found)
	              {
	                   indexes.put(i, new XMLchar(current, xmlchar));
	                  found = false ;
	              }
	              else
	              {
	                  indexes.put(i, new XMLchar(current,current));
	              }
	        }

	        String formattedText = "";
	            for (Map.Entry temp:indexes.entrySet())
	            {
	                    formattedText = formattedText +((XMLchar)temp.getValue()).numericReference;

	            }
	        // second : we trim the text
	            Log.trace(formattedText);
	        return formattedText.trim() ;
	    }

}
class XMLchar
{
    String Char ;
    String numericReference;

    public XMLchar(String sp , String nr) {
        Char = sp ;
        numericReference = nr ;
    }
}

