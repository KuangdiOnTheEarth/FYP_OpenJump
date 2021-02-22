/*
 * (C) 2011 michael.michaud@free.fr
 */
 
package org.openjump.core.ui.plugin.match.util;

import java.io.UnsupportedEncodingException;

import static org.openjump.core.ui.plugin.match.util.CharUtil.*;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * StringUtil is a utility class including static methods to check nullity
 * or equality of Strings and to do some String manipulation like
 * simplification, normalization or splitting.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
public final class StringUtil {
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();
    
    /**
     * Check if an Object or a String is null or empty (the length is null).
     *
     * @param o the string to check
     * @return true if it is null or empty
     */
    public static boolean isNullOrEmpty(Object o) {
        return o == null || o.toString().trim().length() == 0;
    }
    
    /**
     * Check if two strings are equal.
     * Here, null equals null and null equals empty.
     *
     * @param a the first value
     * @param b the second value
     * @return true if both are null or empty or both are equal
     */
    public static boolean equals(String a, String b) {
        if (a == null) {
            return isNullOrEmpty(b);
        }
        else if (b == null) {
            return isNullOrEmpty(a);
        }
        else return a.equals(b);
    }
    
    
    /**
     * Returns a canonical form of the String, i.e. a non-null trimmed String
     * without double-spaces.
     * Only normal spaces (0x20) are processed. 0x09, 0x10 and 0x13 are 
     * considered as normal characters.
     * Using normalize is about 5x faster than using an equivalent compiled
     * regex ([ ]+) and trim(). 
     * @param s the string to normalize
     * @return a normalized String
     */
     public static String normalize(String s) {
         if ( s == null ) {
            return "";
         }
         else {
            s = s.trim();
            int len = s.length();
            if (len < 4) return s;
            StringBuilder b = new StringBuilder( len - 1 );
            boolean removeSpaces = false;
            for ( int i = 0; i < len; i++ ) {
                char c = s.charAt( i );
                if ( c != ' ') {
                    b.append( c );
                    removeSpaces = false;
                }
                else {
                    if (!removeSpaces ) {// first space
                        b.append( c );
                        removeSpaces = true;
                    }
                }
            }
            return b.toString();
        }
     }
     
    /**
     * Splits a string into an array of strings using the given separator. A null
     * string will result in a null array, and an empty string in a zero element
     * array.
     * Notes :
     * split("", ' ', true) or split("", ' ', true) returns an empty array.
     * split("A   B") returns {"A","","","B"}.
     * To split a String with a single character, this method is about 2.5 times
     * faster than using String.split(String regex)
     *
     * @param s the string to split
     * @param delimiter the delimiter character
     * @param trim whether each element should be trimmed
     * @return the array list
     */
    public static String[] split(String s, char delimiter, boolean trim) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return new String[]{s};
        }
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder buff = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == delimiter) {
                String e = buff.toString();
                list.add(trim ? e.trim() : e);
                buff.setLength(0);
            } else {
                buff.append(c);
            }
        }
        String e = buff.toString();
        list.add(trim ? e.trim() : e);
        return list.toArray(new String[list.size()]);
    }
    
    /**
     * Transpose a string using windows-1252 characters into an pure ASCII String.
     * Warning : output string length may be different from input String length.
     *
     * @param input input String
     * @return the same String with non ASCII characters converted to ASCII.
     */
     public static final String cp1252toASCII(String input) {
        String output = null;
        try {
            output = transpose(input, "windows-1252", CP1252_TO_ASCII);
        }
        catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return output;
    }
    
    /**
     * Transpose a string using windows-1252 characters into an ASCII String
     * using only upper case letters, digits whitespace and slash.
     * Warning : output string length may be different from input String length.
     *
     * @param input input String
     * @return the same String with non ASCII characters converted to ASCII.
     */
    public static final String cp1252toASCII38(String input) {
        String output = null;
        try {
            output = transpose(input, "windows-1252", CP1252_TO_ASCII38);
        }
        catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return output;
    }


   /**
    * Comparator to compare length of strings and order them from longest to shortest.
    * Useful to do several search/replace, as it is always recommended to search
    * longest strings first.
    */
    public static Comparator<String> INVERSE_STRING_LENGTH = new Comparator<String>(){
       public int compare(String o1, String o2) {
          int comp = -1*new Integer(o1.length()).compareTo(new Integer(o2.length()));
          if (comp == 0) return -1*o1.compareTo(o2);
          else return comp;
       }
       public boolean equals(Object obj) {return this == obj;}
    };

    /** Constructor. */
    private StringUtil() {
    }

   /**
    * Remove the part of the string within parenthesis.
    * If the string contains several nested or successive parenthesis, only
    * the first interior parenthesis is removed.
    * The method can be applied several times to remove several successive or
    * nested parenthesis.
    * If parenthesis are not well balanced, result is undefined.
    */
    public static String removeParenthesis(String in) {
        int d = in.lastIndexOf('('); // last opening parenthesis
        int f = in.indexOf(')', d);  // first closing parenthesis after the last opening one 
        StringBuilder sb = new StringBuilder();
        if (d >= 0 && f > d) {
            sb.append(in.substring(0, d)).append(in.substring(f+1));
            if (sb.indexOf("(") >= 0) return removeParenthesis(sb.toString());
            return sb.toString();
        }
        else return in;
    }
    
    /**
     * Build a random String from a String containing all the authorized characters.
     * To increase/decrease characters frequency, just duplicate them
     * in the authorizedCharacters parameter.
     * Example "aaaabbcd" will make 'a' happens with a 0.5 probability, b with a
     * 0.25 probability and c and d with 0.125 probability.
     *
     * @param size size of the random string to build
     * @param authorizedCharacters String containing all authorized characters
     * @return a random string
     */
    public static String randomString(int size, String authorizedCharacters) {
        int nbc = authorizedCharacters.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < size ; i++) {
            sb.append(authorizedCharacters.charAt((int)(Math.random()*nbc)));
        }
        return sb.toString();
    }


    //public static void main(String[] args) {
    //    System.out.println(checkPhonetic("CHAUMIERE","homier"));
    //    System.out.println(checkPhonetic("CHAUVEROCHE","hoveroh"));
    //    System.out.println(checkPhonetic("GOUTTE DES FORGE","gut de forj"));
    //    System.out.println(checkPhonetic("SCIERIE","siri"));
    //    System.out.println(checkPhonetic("HAUTS PRES","o pre"));
    //    System.out.println(checkPhonetic("PRES MOUILLETS","pre muye"));
    //    System.out.println(checkPhonetic("RUISSEAU DE LA GOUTTE","rÃ¼iso de la gut"));
    //    System.out.println(checkPhonetic("SOUS LES BOIS","su le boa"));
    //    System.out.println(checkPhonetic("PONT","pÃ²"));
    //    System.out.println(checkPhonetic("GENTIANE","jÃ sian"));
    //    System.out.println(checkPhonetic("FONDERIE","fÃ²deri"));
    //    System.out.println(checkPhonetic("GOUTTE DU BON POMMIER","gut dÃ¼ bÃ² pomie"));
    //    System.out.println(checkPhonetic("ROCHE DU SARAZIN","roh dÃ¼ sarasÃ¬"));
    //    System.out.println(checkPhonetic("PLATEAU DES SOMBRES","plato de sÃ²br"));
    //    System.out.println(checkPhonetic("MALVAUX","malvo"));
    //    System.out.println(checkPhonetic("ROSEES","rose"));
    //    System.out.println(checkPhonetic("SOMBRES MOUSSEUX","sÃ²br muse"));
    //    System.out.println(checkPhonetic("ROND","rÃ²"));
    //    System.out.println(checkPhonetic("GUENEZ","genes"));
    //    System.out.println(checkPhonetic("LESAING","lesÃ¬"));
    //    System.out.println(checkPhonetic("GONFLE","gÃ²fl"));
    //    System.out.println(checkPhonetic("PHANITOR","fanitor"));
    //    System.out.println(checkPhonetic("MONT JEAN","mÃ² jÃ "));
    //    System.out.println(checkPhonetic("BELFORT","belfor"));
    //    System.out.println(checkPhonetic("EGLISE","eglis"));
    //    System.out.println(checkPhonetic("BEUCINIERE","besinier"));
    //    System.out.println(checkPhonetic("CHARRIERE","harier"));
    //    System.out.println(checkPhonetic("NOIE","noa"));
    //    System.out.println(checkPhonetic("PAPETERIE","papeteri"));
    //    System.out.println(checkPhonetic("FOUILLOTTES","fuyot"));
    //    System.out.println(checkPhonetic("SAVOUREUSE","savures"));
    //    System.out.println(checkPhonetic("MINES","min"));
    //    System.out.println(checkPhonetic("PRES CORBEAUX","pre korbo"));
    //    System.out.println(checkPhonetic("MOULIN","mulÃ¬"));
    //    System.out.println(checkPhonetic("QUERTY","kerti"));
    //    System.out.println(checkPhonetic("SAINT PIERRE","sÃ¬ pier"));
    //    System.out.println(checkPhonetic("BALLON D ALSACE","balÃ² d alsas"));
    //    System.out.println(checkPhonetic("TOURNANT PAULIN","turnÃ  polÃ¬"));
    //    System.out.println(checkPhonetic("SOUS LES BOIS","su le boa"));
    //
    //    System.out.println(checkPhonetic("COMBE","kÃ²b"));
    //    System.out.println(checkPhonetic("NOUE","nu"));
    //    System.out.println(checkPhonetic("BOIS ZELIN","boa selÃ¬"));
    //    System.out.println(checkPhonetic("SOUS LE FAHYS","su le fais"));
    //
    //    System.out.println(checkPhonetic("FLEURS","fler"));
    //    System.out.println(checkPhonetic("CENTRE DE LOISIRS","cÃ tr de loasir"));
    //    System.out.println(checkPhonetic("LA NOYE","la noi"));
    //    System.out.println(checkPhonetic("EGUENIGUE","egenig"));
    //    System.out.println(checkPhonetic("ETUEFFONT","etÃ¼fÃ²"));
    //    System.out.println(checkPhonetic("ERRUES","erÃ¼"));
    //    System.out.println(checkPhonetic("RAYMOND BURAIS","remÃ² bÃ¼re"));
    //
    //    System.out.println(checkPhonetic("BOURGUIGNONS","burgiÃ±Ã²"));
    //    System.out.println(checkPhonetic("MONTS CORON","mÃ² korÃ²"));
    //    System.out.println(checkPhonetic("PAIX","pe"));
    //    System.out.println(checkPhonetic("GIROMAGNY","jiromaÃ±i"));
    //    System.out.println(checkPhonetic("PEUPLIERS","peplie"));
    //    System.out.println(checkPhonetic("BREUILS","brey"));
    //    System.out.println(checkPhonetic("ESSERTS","eser"));
    //    System.out.println(checkPhonetic("PAQUIS","paki"));
    //
    //    System.out.println(checkPhonetic("ANJOUTEY","Ã jute"));
    //    System.out.println(checkPhonetic("PRES BREULETS","pre brele"));
    //    System.out.println(checkPhonetic("VELLESCOT","velesko"));
    //    System.out.println(checkPhonetic("JONCHEREY","jÃ²here"));
    //    System.out.println(checkPhonetic("GROSNE","gron"));
    //    System.out.println(checkPhonetic("GRABEUSETS","grabese"));
    //    System.out.println(checkPhonetic("CHALEMBERT","halÃ ber"));
    //    System.out.println(checkPhonetic("PAQUIS","paki"));
    //
    //
    //
    //    System.out.println(checkPhonetic("MARSEILLE","marsey"));
    //    System.out.println(checkPhonetic("PARIS","pari"));
    //    System.out.println(checkPhonetic("LYON","liÃ²"));
    //    System.out.println(checkPhonetic("CALACUCCIA","kalakuthia"));
    //
    //    System.out.println(checkPhonetic("CHAMBERLAIN","hÃ berlÃ¬"));
    //    System.out.println(checkPhonetic("CLEMANCEAU","klemÃ so"));
    //    System.out.println(checkPhonetic("KOELHER","kele"));
    //    System.out.println(checkPhonetic("KOEHLER","kele"));
    //    System.out.println(checkPhonetic("ZAMENHOF","samenof"));
    //
    //    //System.out.println("Distance de Jaro-Winkler entre FAVRE et FABRE = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("FAVRE","FABRE"));
    //    //System.out.println("Distance de Jaro-Winkler entre MARTHA et MARHTA = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("MARTHA","MARHTA"));
    //    //System.out.println("Distance de Jaro-Winkler entre DWAYNE et DUANE = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("DWAYNE","DUANE"));
    //    //System.out.println("Distance de Jaro-Winkler entre DIXON et DICKSONX = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("DIXON","DICKSONX"));
    //    //System.out.println("Distance de Jaro-Winkler entre MITTERAND et MITERANDD = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("MITTERAND","MITERANDD"));
    //    //System.out.println("Distance de Jaro-Winkler entre PIERRES et PIERRES RANGEES = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("PIERRES","PIERRES RANGEES"));
    //    //System.out.println("Distance de Jaro-Winkler entre VALFLEURY et VAL FLEURY = " + JaroWinklerAlgorithm.jaroWinklerSimilarity("VALFLEURY","VAL FLEURY"));
    //
    //    //try {
    //    //java.io.BufferedReader br = new java.io.BufferedReader(new FileReader("E:/jeocode-pb/44190-Nantes.csv"));
    //    //String line = "";
    //    //while (null != (line = br.readLine())) {
    //    //    AbstractToponyme top = new AbstractToponyme(line);
    //    //    System.out.println(top + " | " + top.getPartieGeneriqueSimplifiee() + " | " + top.getPartieSpecifique(0) +
    //    //    " | " + top.getPartieSpecifique(1) + " | " + top.getPartieSpecifique(2) + " | " + top.getPartieSpecifique(3));
    //    //}
    //    //}
    //    //catch(java.io.FileNotFoundException e) {e.printStackTrace();}
    //    //catch(java.io.IOException e) {e.printStackTrace();}
    //
    //
    //}

}