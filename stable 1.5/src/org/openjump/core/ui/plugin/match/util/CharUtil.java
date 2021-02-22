/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util;

import java.io.UnsupportedEncodingException;
import java.lang.Character;

/**
 * CharUtil offers some static methods and constants to transpose strings
 * from a character set to another.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
public final class CharUtil {

    /**
     * Transpose a String using characters taken from the specified encoding
     * into a new String defined by a mapping from this encoding new character
     * strings.
     * Example : the character œ may be coded as follows :
     * <ul>
     * <li> ISO 8859-1    : œ does not exist in this character table </li>
     * <li> ISO 8859-15   : hexadecimal value is 'BD' which is the 189th character among 256</li>
     * <li> UTF-16 (java) : hexadecimal value is '0153' which is the 339th character among 65636</li>
     * <li> windows-1252  : hexadecimal value is '9C' which is the 156th character among 256</li>
     * </ul>
     * With "windows-1252" as encoding parameter and CP1252_TO_ASCII as 
     * conversion table, transpose will first get 156, the integer value of 'œ'
     * in the windows-1252 codepage, then it will transpose this value into
     * "oe" (o character followed by e character), ie CP1252_TO_ASCII[156].
     *
     * The function swallows UnsupportedEncodingException. It means that
     * characters of input string that are unknown the encoding parameter are
     * simply ignored.
     *
     * TODO : compare this approach with CharsetEncoder/CharsetDecoder implementation.
     *
     * @param input input String to be transposed
     * @param encoding the encoding that following table transpose from
     * @param table array used to map encoded characters to new strings
     * @return the transposed String
     * @throws UnsupportedEncodingException if the named charset is not supported
     */
    public static final String transpose(String input, String encoding,
                           String[] table) throws UnsupportedEncodingException {
        if (input == null || table == null || input.length() == 0) return input;
        byte[] bytes = null;
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++){
            char c = input.charAt(i);
            // get the windows-1252 representation of this char (or ?)
            bytes = new String(new char[]{c}).getBytes(encoding);
            sb.append(table[bytes[bytes.length-1] & 0xFF]);
        }
        return sb.toString();
    }

    /**
     * Prints the current code page to the standard output stream in a human
     * readable format.
     */
    public static final void currentCodePageCharacters() {
        int max = 0;
        for (int i = 0 ; i < 256 ; i++) {
            String s = new String(new byte[]{(byte)i});
            int code = s.codePointAt(0);
            max = code > max ? code : max;
            int type = Character.getType(code);
            String unicodeType = "??????";
            // C = control
            // L = letter
            // M = mark
            // N = number
            // P = punctuation
            // S = symbol
            // Z = separator
            switch(type) {
                case(Character.COMBINING_SPACING_MARK) : {unicodeType = "COMBINING_SPACING_MARK (Mc)"; break;}
                case(Character.CONNECTOR_PUNCTUATION)  : {unicodeType = "CONNECTOR_PUNCTUATION  (Pc)"; break;}
                case(Character.CONTROL)                : {unicodeType = "CONTROL (Cc)"; break;}
                case(Character.CURRENCY_SYMBOL)        : {unicodeType = "CURRENCY_SYMBOL (Sc)"; break;}
                case(Character.DASH_PUNCTUATION)       : {unicodeType = "DASH_PUNCTUATION (Pd)"; break;}
                case(Character.DECIMAL_DIGIT_NUMBER)   : {unicodeType = "DECIMAL_DIGIT_NUMBER (Nd)"; break;}
                case(Character.ENCLOSING_MARK)         : {unicodeType = "ENCLOSING_MARK (Me)"; break;}
                case(Character.END_PUNCTUATION)        : {unicodeType = "END_PUNCTUATION (Pe)"; break;}
                case(Character.FINAL_QUOTE_PUNCTUATION): {unicodeType = "FINAL_QUOTE_PUNCTUATION  (Pf)"; break;}
                case(Character.FORMAT )                : {unicodeType = "FORMAT (Cf)"; break;}
                case(Character.INITIAL_QUOTE_PUNCTUATION) : {unicodeType = "INITIAL_QUOTE_PUNCTUATION (Pi)"; break;}
                case(Character.LETTER_NUMBER)          : {unicodeType = "LETTER_NUMBER (Nl)"; break;}
                case(Character.LINE_SEPARATOR)         : {unicodeType = "LINE_SEPARATOR (Zl)"; break;}
                case(Character.LOWERCASE_LETTER)       : {unicodeType = "LOWERCASE_LETTER (Ll)"; break;}
                case(Character.MATH_SYMBOL)            : {unicodeType = "MATH_SYMBOL (Sm)"; break;}
                case(Character.MODIFIER_LETTER)        : {unicodeType = "MODIFIER_LETTER (Lm)"; break;}
                case(Character.MODIFIER_SYMBOL)        : {unicodeType = "MODIFIER_SYMBOL (Sk)"; break;}
                case(Character.NON_SPACING_MARK)       : {unicodeType = "NON_SPACING_MARK (Mn)"; break;}
                case(Character.OTHER_LETTER)           : {unicodeType = "OTHER_LETTER (Lo)"; break;}
                case(Character.OTHER_NUMBER)           : {unicodeType = "OTHER_NUMBER (No)"; break;}
                case(Character.OTHER_PUNCTUATION)      : {unicodeType = "OTHER_PUNCTUATION (Po)"; break;}
                case(Character.OTHER_SYMBOL)           : {unicodeType = "OTHER_SYMBOL (So)"; break;}
                case(Character.PARAGRAPH_SEPARATOR)    : {unicodeType = "PARAGRAPH_SEPARATOR (Zp)"; break;}
                case(Character.PRIVATE_USE)            : {unicodeType = "PRIVATE_USE (Co)"; break;}
                case(Character.SPACE_SEPARATOR)        : {unicodeType = "SPACE_SEPARATOR (Zs)"; break;}
                case(Character.START_PUNCTUATION)      : {unicodeType = "START_PUNCTUATION (Ps)"; break;}
                case(Character.SURROGATE)              : {unicodeType = "SURROGATE (Cs)"; break;}
                case(Character.TITLECASE_LETTER)       : {unicodeType = "TITLECASE_LETTER (Lt)"; break;}
                case(Character.UNASSIGNED)             : {unicodeType = "UNASSIGNED (Cn)"; break;}
                case(Character.UPPERCASE_LETTER)       : {unicodeType = "UPPERCASE_LETTER (Lu)"; break;}
                default : break;
            }
            String cat = "??????";
            if (!Character.isDefined(code)) cat = "Undefined";
            else if (Character.isWhitespace(code)) cat = "Whitespace";
            else if (Character.isLetter(code)) cat = "Letter";
            else if (Character.isDigit(code)) cat = "Digit";
            else if (Character.isISOControl(code)) cat = "ISOControl";
            else if (Character.isMirrored(code)) cat = "Mirrored";
            else if (Character.isJavaIdentifierPart(code)) cat = "JavaIdentifierPart";
            else cat = "???????";
            System.out.println("//" + i + " " + s + " " + String.format("%#06X", new Object[]{code}) + " " + unicodeType + " " + cat);
        }
        System.out.println("max unicode values used by this charset is : " + max);
    }
    
    /**
     * Prints a 16 x 16 String[] table showing each transposition from the
     * initial charset the table is supposed to convert from to the new
     * character strings.
     * 
     * @param charset the charset this table is supposed to transpose from
     * @param table the transposition table
     */
    public static final void printTable(String charset, String[] table)
                                            throws UnsupportedEncodingException{
        for (int i = 0 ; i < 16 ; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0 ; j < 16 ; j++) {
                String s1 = new String(new byte[]{(byte)(16*i+j)}, charset);
                String s2 = table[16*i+j];
                sb.append(format(s1, "/", s2, 6));
            }
            System.out.println(sb.toString());
        }
    }
    
    private static String format(String s1, String sep, String s2, int width) {
        String s = "            " + s1 + sep + s2;
        return s.substring(s.length()-width, s.length());
    }
    
    /**
     * Creates a new transposition table doing nothing for strings encoded in
     * this character set.
     * Elements of this table can be individually changed with the
     * setTableElement static method.
     *
     * @param charset the character set this table is supposed to transform from
     */
    public static final String[] createTable(String charset) throws UnsupportedEncodingException {
        String[] table = new String[256];
        for (int i = 0 ; i < 256 ; i++) {
            table[i] = new String(new byte[]{(byte)i}, charset);
        }
        return table;
    }
    
    /**
     * Changes one element of a transposition table.
     *
     * @param table the table to change
     * @param i the index of the element to change
     * @param newString the new string for the element at index i
     * @return the modified table
     */
    public static final String[] setTableElement(String[] table, int i, String newString) {
        assert table.length == 256;
        assert i < 256;
        table[i] = newString;
        return table;
    }

    /**
     * Each character of the cp1252 codepage expressed as a printable ascii
     * character string (0x20-0x7E) or the empty string.
     * Resulting strings match [\u0020-\u007E]*
     */
    public static final String[] CP1252_TO_ASCII = new String[] {
        "",    //0    \u0000
        "",    //1    \u0001
        "",    //2    \u0002
        "",    //3    \u0003
        "",    //4    \u0004
        "",    //5    \u0005
        "",    //6    \u0006
        "",    //7    \u0007
        "",    //8    \u0008
        " ",   //9    \u0009 tabulation
        " ",   //10   saut de ligne (LF)
        "",    //11   \u000B
        "",    //12   \u000C
        " ",   //13   retour chariot (CR)
        "",    //14   \u000E
        "",    //15   \u000F
        "",    //16   \u0010
        "",    //17   \u0011
        "",    //18   \u0012
        "",    //19   \u0013
        "",    //20   \u0014
        "",    //21   \u0015
        "",    //22   \u0016
        "",    //23   \u0017
        "",    //24   \u0018
        "",    //25   \u0019
        "",    //26   \u001A
        "",    //27   \u001B
        "",    //28   \u001C
        "",    //29   \u001D
        "",    //30   \u001E
        "",    //31   \u001F
        " ",   //32   \u0020 espace (SP)
        "!",   //33 ! \u0021
        "\"",  //34 " \u0022
        "#",   //35 # \u0023
        "$",   //36 $ \u0024
        "%",   //37 % \u0025
        "&",   //38 & \u0026
        "'",   //39 ' \u0027
        "(",   //40 ( \u0028
        ")",   //41 ) \u0029
        "*",   //42 * \u002A
        "+",   //43 + \u002B
        ",",   //44 , \u002C
        "-",   //45 - \u002D
        ".",   //46 . \u002E
        "/",   //47 / \u002F
        "0",   //48 0 \u0030
        "1",   //49 1 \u0031
        "2",   //50 2 \u0032
        "3",   //51 3 \u0033
        "4",   //52 4 \u0034
        "5",   //53 5 \u0035
        "6",   //54 6 \u0036
        "7",   //55 7 \u0037
        "8",   //56 8 \u0038
        "9",   //57 9 \u0039
        ":",   //58 : \u003A
        ";",   //59 ; \u003B
        "<",   //60 < \u003C
        "=",   //61 = \u003D
        ">",   //62 > \u003E
        "?",   //63 ? \u003F
        "@",   //64 @ \u0040
        "A",   //65 A \u0041
        "B",   //66 B \u0042
        "C",   //67 C \u0043
        "D",   //68 D \u0044
        "E",   //69 E \u0045
        "F",   //70 F \u0046
        "G",   //71 G \u0047
        "H",   //72 H \u0048
        "I",   //73 I \u0049
        "J",   //74 J \u004A
        "K",   //75 K \u004B
        "L",   //76 L \u004C
        "M",   //77 M \u004D
        "N",   //78 N \u004E
        "O",   //79 O \u004F
        "P",   //80 P \u0050
        "Q",   //81 Q \u0051
        "R",   //82 R \u0052
        "S",   //83 S \u0053
        "T",   //84 T \u0054
        "U",   //85 U \u0055
        "V",   //86 V \u0056
        "W",   //87 W \u0057
        "X",   //88 X \u0058
        "Y",   //89 Y \u0059
        "Z",   //90 Z \u005A
        "[",   //91 [ \u005B
        "\\",  //92 \ \u005C
        "]",   //93 ] \u005D
        "^",   //94 ^ \u005E Accent circonflexe
        "_",   //95 _ \u005F
        "`",   //96 ` \u0060 Accent grave
        "a",   //97 a \u0061
        "b",   //98 b \u0062
        "c",   //99 c \u0063
        "d",   //100 d \u0064
        "e",   //101 e \u0065
        "f",   //102 f \u0066
        "g",   //103 g \u0067
        "h",   //104 h \u0068
        "i",   //105 i \u0069
        "j",   //106 j \u006A
        "k",   //107 k \u006B
        "l",   //108 l \u006C
        "m",   //109 m \u006D
        "n",   //110 n \u006E
        "o",   //111 o \u006F
        "p",   //112 p \u0070
        "q",   //113 q \u0071
        "r",   //114 r \u0072
        "s",   //115 s \u0073
        "t",   //116 t \u0074
        "u",   //117 u \u0075
        "v",   //118 v \u0076
        "w",   //119 w \u0077
        "x",   //120 x \u0078
        "y",   //121 y \u0079
        "z",   //122 z \u007A
        "{",   //123 { \u007B
        "|",   //124 | \u007C
        "}",   //125 } \u007D
        "~",   //126 ~ \u007E Tilde
        "",    //127   \u007F Touche supprimer (DEL)
        "EUR", //128 € \u20AC
        "",    //129   \uFFFD Caractère de contrôle
        ",",   //130 ‚  \u201A Apostrophe anglaise basse (traduite par une , suivant son aspect)
        "f",   //131 ƒ \u0192 florin, forte musical
        ",,",  //132 „ \u201E guillemet anglais bas
        "...", //133 …\u2026 points de suspension
        "*",   //134 †  \u2020 obèle, dague, croix (traduit par une astérisque)
        "*",   //135 ‡ \u2021 double croix
        "^",   //136 ˆ \u02C6 accent circonflexe traduit par le caractère \u005E
        " ",   //137 ‰ \u2030 pour mille
        "S",   //138 š \u0160 S majuscule avec caron
        "<",   //139 ‹ \u2039 guillemet simple allemand et suisse, parenthÃ¨se angulaire ouvrante
        "OE",  //140 Œ \u0152 Ligature o-e majuscule
        "",    //141   \uFFFD Caractère de contrôle
        "Z",   //142 Ž \u017D Z majuscule avec caron
        "",    //143   \uFFFD Caractère de contrôle
        "",    //144   \uFFFD Caractère de contrôle
        "'",   //145 ‘ \u2018 guillemet anglais simple ouvrant
        "'",   //146 ’ \u2019 guillemet anglais simple fermant
        "\"",  //147 “ \u201C guillemets anglais doubles ouvrants
        "\"",  //148 � \u201D guillemets anglais doubles fermants
        " ",   //149 • \u2022 boulet, utiliser plutôt des listes à puces
        "-",   //150 – \u2013 tiret demi-cadratin (incise)
        "-",   //151 — \u2014 tiret cadratin (dialogue)
        "~",   //152 ˜ \u02DC tilde (traduit par le tilde ASCII \u007E)
        "TM",  //153 ™ \u2122 marque déposée
        "s",   //154 š \u0161 s minuscule avec caron
        ">",   //155 › \u203A guillemet simple allemand et suisse, parenthÃ¨se angulaire fermante
        "oe",  //156 œ \u0153 Ligature o-e minuscule (absente de la norme ISO-8859-1 pour une raison aberranteâ€¦)
        "",    //157   \uFFFD Caractère de contrôle
        "z",   //158 ž \u017E z minuscule avec caron
        "Y",   //159 Ÿ \u0178 Y majuscule avec trema
        " ",   //160   \u00A0 Espace insécable
        "!",   //161 ¡ \u00A1 Point d'exclamation (inversé)
        "c",   //162 ¢ \u00A2 cent (monnaie)
        "GBP", //163 £ \u00A3 livre (monnaie)
        " ",   //164 ¤ \u00A4 symbole monétaire
        "Y",   //165 ¥ \u00A5 yen (monnaie)
        "|",   //166 ¦ \u00A6 Barre verticale (pipe)
        " ",   //167 § \u00A7 Paragraphe
        " ",   //168 ¨ \u00A8 Tréma
        "(c)", //169 © \u00A9 Copyright
        " ",   //170 ª \u00AA indicateur ordinal
        "\"",  //171 « \u00AB Guillemets ouvrants
        "-",   //172 ¬ \u00AC Négation logique
        " ",   //173 ­ \u00AD Coupure de mot (SHY = soft hyphen)
        "(r)", //174 ® \u00AE Marque déposée
        " ",   //175 ¯ \u00AF Macron (signe diacritique non utilisé en français)
        " ",   //176 ° \u00B0 Degré
        "+/-", //177 ± \u00B1 Signe plus ou moins
        " 2",  //178 ² \u00B2 Carré
        " 3",  //179 ³ \u00B3 Cube
        "'",   //180 ´ \u00B4 Accent aigu
        "u",   //181 µ \u00B5 Micro
        " ",   //182 ¶ \u00B6 Pied de mouche (marque de paragraphe)
        ".",   //183 · \u00B7 Point médian
        "",    //184 ¸ \u00B8 Cédille
        " 1",  //185 ¹ \u00B9 Exposant un
        " ",   //186 º \u00BA Indicateur ordinal
        "\"",  //187 » \u00BB Guillemets fermants
        " 1/4",//188 ¼ \u00BC Un quart
        " 1/2",//189 ½ \u00BD Un demi
        " 3/4",//190 ¾ \u00BE Trois quarts
        "?",   //191 ¿ \u00BF Point d'interrogation
        "A",   //192 À \u00C0
        "A",   //193 � \u00C1
        "A",   //194 Â \u00C2
        "A",   //195 Ã \u00C3
        "A",   //196 Ä \u00C4
        "A",   //197 Å \u00C5
        "AE",  //198 Æ \u00C6
        "C",   //199 Ç \u00C7
        "E",   //200 È \u00C8
        "E",   //201 É \u00C9
        "E",   //202 Ê \u00CA
        "E",   //203 Ë \u00CB
        "I",   //204 Ì \u00CC
        "I",   //205 � \u00CD
        "I",   //206 Î \u00CE
        "I",   //207 � \u00CF
        "D",   //208 � \u00D0
        "N",   //209 Ñ \u00D1
        "O",   //210 Ò \u00D2
        "O",   //211 Ó \u00D3
        "O",   //212 Ô \u00D4
        "O",   //213 Õ \u00D5
        "O",   //214 Ö \u00D6
        "x",   //215 × \u00D7 Croix de multiplication
        "O",   //216 Ø \u00D8 O barrÃ©
        "U",   //217 Ù \u00D9
        "U",   //218 Ú \u00DA
        "U",   //219 Û \u00DB
        "U",   //220 Ü \u00DC
        "Y",   //221 � \u00DD
        "T",   //222 Þ \u00DE Thorn (lettre utilisÃ©e en Islande)
        "ss",  //223 ß \u00DF
        "a",   //224 à \u00E0
        "a",   //225 á \u00E1
        "a",   //226 â \u00E2
        "a",   //227 ã \u00E3
        "a",   //228 ä \u00E4
        "a",   //229 å \u00E5
        "ae",  //230 æ \u00E6
        "c",   //231 ç \u00E7
        "e",   //232 è \u00E8
        "e",   //233 é \u00E9
        "e",   //234 ê \u00EA
        "e",   //235 ë \u00EB
        "i",   //236 ì \u00EC
        "i",   //237 í \u00ED
        "i",   //238 î \u00EE
        "i",   //239 ï \u00EF
        "d",   //240 ð \u00F0
        "n",   //241 ñ \u00F1
        "o",   //242 ò \u00F2
        "o",   //243 ó \u00F3
        "o",   //244 ô \u00F4
        "o",   //245 õ \u00F5
        "o",   //246 ö \u00F6
        "/",   //247 ÷ \u00F7
        "o",   //248 ø \u00F8
        "u",   //249 ù \u00F9
        "u",   //250 ú \u00FA
        "u",   //251 û \u00FB
        "u",   //252 ü \u00FC
        "y",   //253 ý \u00FD
        "t",   //254 þ \u00FE
        "y"    //255 ÿ \u00FF
    };

    /**
     * Each character of the cp1252 codepage expressed as one of the 38
     * following printable ascii characters : [ 0-9A-Z/].
     * Conversion rules are <ul>
     * <li>Control characters are transposed into the empty string</li>
     * <li>Cedille is transposed into the empty string</li>
     * <li>Digits are not transposed</li>
     * <li>Letters are transposed into the range [A-Z]</li>
     * <li>Space, apostroph, hyphen, quotes, double quotes and underscore are transposed into spaces</li>
     * <li>Various symbols like #,$,%,+,*,&lt;,&gt;,=,~,â‚¬,@ are transposed into spaces</li>
     * <li>Accents are transposed into spaces</li>
     * <li>Separators (tab, LF, CR, &, parenthesis, brackets) are transposed into the '/' symbol<li>
     * <li>Punctuation marks like .,:;... are transposed into the '/' symbol<li>
     * </ul>
     */
    public static final String[] CP1252_TO_ASCII38 = new String[] {
        "",    //0    \u0000
        "",    //1    \u0001
        "",    //2    \u0002
        "",    //3    \u0003
        "",    //4    \u0004
        "",    //5    \u0005
        "",    //6    \u0006
        "",    //7    \u0007
        "",    //8    \u0008
        "/",   //9    \u0009 tabulation
        "/",   //10   saut de ligne (LF)
        "",    //11   \u000B
        "",    //12   \u000C
        "/",   //13   retour chariot (CR)
        "",    //14   \u000E
        "",    //15   \u000F
        "",    //16   \u0010
        "",    //17   \u0011
        "",    //18   \u0012
        "",    //19   \u0013
        "",    //20   \u0014
        "",    //21   \u0015
        "",    //22   \u0016
        "",    //23   \u0017
        "",    //24   \u0018
        "",    //25   \u0019
        "",    //26   \u001A
        "",    //27   \u001B
        "",    //28   \u001C
        "",    //29   \u001D
        "",    //30   \u001E
        "",    //31   \u001F
        " ",   //32   \u0020 espace (SP)
        "/",   //33 ! \u0021
        " ",   //34 " \u0022
        " ",   //35 # \u0023
        " ",   //36 $ \u0024
        " ",   //37 % \u0025
        "/",   //38 & \u0026
        " ",   //39 ' \u0027
        "/",   //40 ( \u0028
        "/",   //41 ) \u0029
        " ",   //42 * \u002A
        " ",   //43 + \u002B
        "/",   //44 , \u002C
        " ",   //45 - \u002D
        "/",   //46 . \u002E
        "/",   //47 / \u002F
        "0",   //48 0 \u0030
        "1",   //49 1 \u0031
        "2",   //50 2 \u0032
        "3",   //51 3 \u0033
        "4",   //52 4 \u0034
        "5",   //53 5 \u0035
        "6",   //54 6 \u0036
        "7",   //55 7 \u0037
        "8",   //56 8 \u0038
        "9",   //57 9 \u0039
        "/",   //58 : \u003A
        "/",   //59 ; \u003B
        " ",   //60 < \u003C
        " ",   //61 = \u003D
        " ",   //62 > \u003E
        "/",   //63 ? \u003F
        " ",   //64 @ \u0040
        "A",   //65 A \u0041
        "B",   //66 B \u0042
        "C",   //67 C \u0043
        "D",   //68 D \u0044
        "E",   //69 E \u0045
        "F",   //70 F \u0046
        "G",   //71 G \u0047
        "H",   //72 H \u0048
        "I",   //73 I \u0049
        "J",   //74 J \u004A
        "K",   //75 K \u004B
        "L",   //76 L \u004C
        "M",   //77 M \u004D
        "N",   //78 N \u004E
        "O",   //79 O \u004F
        "P",   //80 P \u0050
        "Q",   //81 Q \u0051
        "R",   //82 R \u0052
        "S",   //83 S \u0053
        "T",   //84 T \u0054
        "U",   //85 U \u0055
        "V",   //86 V \u0056
        "W",   //87 W \u0057
        "X",   //88 X \u0058
        "Y",   //89 Y \u0059
        "Z",   //90 Z \u005A
        "/",   //91 [ \u005B
        "/",   //92 \ \u005C
        "/",   //93 ] \u005D
        " ",   //94 ^ \u005E Accent circonflexe
        " ",   //95 _ \u005F
        " ",   //96 ` \u0060 Accent grave
        "A",   //97 a \u0061
        "B",   //98 b \u0062
        "C",   //99 c \u0063
        "D",   //100 d \u0064
        "E",   //101 e \u0065
        "F",   //102 f \u0066
        "G",   //103 g \u0067
        "H",   //104 h \u0068
        "I",   //105 i \u0069
        "J",   //106 j \u006A
        "K",   //107 k \u006B
        "L",   //108 l \u006C
        "M",   //109 m \u006D
        "N",   //110 n \u006E
        "O",   //111 o \u006F
        "P",   //112 p \u0070
        "Q",   //113 q \u0071
        "R",   //114 r \u0072
        "S",   //115 s \u0073
        "T",   //116 t \u0074
        "U",   //117 u \u0075
        "V",   //118 v \u0076
        "W",   //119 w \u0077
        "X",   //120 x \u0078
        "Y",   //121 y \u0079
        "Z",   //122 z \u007A
        "/",   //123 { \u007B
        "/",   //124 | \u007C
        "/",   //125 } \u007D
        " ",   //126 ~ \u007E Tilde
        "",    //127   \u007F Touche supprimer (DEL)
        " ",   //128  \u20AC
        "",    //129  \uFFFD Caractère de contrôle
        " ",   //130  \u201A Apostrophe anglaise basse (traduite par une , suivant son aspect)
        " ",   //131  \u0192 florin, forte musical
        " ",   //132  \u201E guillemet anglais bas
        "/",   //133  \u2026 	points de suspension
        " ",   //134  \u2020 obèle, dague, croix (traduit par une astérisque)
        " ",   //135  \u2021 double croix
        " ",   //136  \u02C6 accent circonflexe traduit par le caractère \u005E
        " ",   //137  \u2030 	pour mille
        "S",   //138  \u0160 S majuscule avec caron
        " ",   //139  \u2039 guillemet simple allemand et suisse, parenthèse angulaire ouvrante
        "OE",  //140  \u0152 Ligature o-e majuscule
        "",    //141    \uFFFD Caractère de contrôle
        "Z",   //142  \u017D Z majuscule avec caron
        "",    //143    \uFFFD Caractère de contrôle
        "",    //144    \uFFFD Caractère de contrôle
        " ",   //145  \u2018 guillemet anglais simple ouvrant
        " ",   //146  \u2019 guillemet anglais simple fermant
        " ",   //147  \u201C guillemets anglais doubles ouvrants
        " ",   //148  \u201D guillemets anglais doubles fermants
        " ",   //149  \u2022 boulet, utiliser plutôt des listes à puces
        " ",   //150  \u2013 tiret demi-cadratin (incise)
        " ",   //151  \u2014 tiret cadratin (dialogue)
        " ",   //152  \u02DC tilde (traduit par le tilde ASCII \u007E)
        "TM",  //153  \u2122 marque déposée
        "S",   //154  \u0161 s minuscule avec caron
        " ",   //155  \u203A guillemet simple allemand et suisse, parenthèse angulaire fermante
        "OE",  //156  \u0153 Ligature o-e minscule (absente de la norme ISO-8859-1 pour une raison aberrante)
        "",    //157   \uFFFD Caractère de contrôle
        "Z",   //158  \u017E z minuscule avec caron
        "Y",   //159  \u0178 Y majuscule avec trema
        " ",   //160   \u00A0 Espace insécable
        "/",   //161 ¡ \u00A1 Point d'exclamation (inversé)
        "C",   //162 ¢ \u00A2 cent (monnaie)
        "GBP", //163 £ \u00A3 livre (monnaie)
        " ",   //164 ¤ \u00A4 symbole monétaire
        "Y",   //165 ¥ \u00A5 yen (monnaie)
        "/",   //166 ¦ \u00A6 Barre verticale (pipe)
        "/",   //167 § \u00A7 Paragraphe
        " ",   //168 ¨ \u00A8 Tréma
        " ",   //169 © \u00A9 Copyright
        " ",   //170 ª \u00AA indicateur ordinal
        " ",   //171 « \u00AB Guillemets ouvrants
        " ",   //172 ¬ \u00AC Négation logique
        " ",   //173 ­ \u00AD Coupure de mot (SHY = soft hyphen)
        " ",   //174 ® \u00AE Marque déposée
        "",    //175 ¯ \u00AF Macron (signe diacritique non utilisé en français)
        " ",   //176 ° \u00B0 Degré
        " ",   //177 ± \u00B1 Signe plus ou moins
        " 2",  //178 ² \u00B2 Carré
        " 3",  //179 ³ \u00B3 Cube
        " ",   //180 ´ \u00B4 Accent aigu
        "U",   //181 µ \u00B5 Micro
        "/",   //182 ¶ \u00B6 Pied de mouche (marque de paragraphe)
        "/",   //183 · \u00B7 Point médian
        "",    //184 ¸ \u00B8 Cédille
        " 1",  //185 ¹ \u00B9 Exposant un
        " ",   //186 º \u00BA Indicateur ordinal
        " ",  //187 » \u00BB Guillemets fermants
        " 1/4",//188 ¼ \u00BC Un quart
        " 1/2",//189 ½ \u00BD Un demi
        " 3/4",//190 ¾ \u00BE Trois quarts
        "/",   //191 ¿ \u00BF Point d'interrogation
        "A",   //192 À \u00C0
        "A",   //193 � \u00C1
        "A",   //194 Â \u00C2
        "A",   //195 Ã \u00C3
        "A",   //196 Ä \u00C4
        "A",   //197 Å \u00C5
        "AE",  //198 Æ \u00C6
        "C",   //199 Ç \u00C7
        "E",   //200 È \u00C8
        "E",   //201 É \u00C9
        "E",   //202 Ê \u00CA
        "E",   //203 Ë \u00CB
        "I",   //204 Ì \u00CC
        "I",   //205 � \u00CD
        "I",   //206 Î \u00CE
        "I",   //207 � \u00CF
        "D",   //208 � \u00D0
        "N",   //209 Ñ \u00D1
        "O",   //210 Ò \u00D2
        "O",   //211 Ó \u00D3
        "O",   //212 Ô \u00D4
        "O",   //213 Õ \u00D5
        "O",   //214 Ö \u00D6
        "X",   //215 × \u00D7 Croix de multiplication
        "O",   //216 Ø \u00D8 O barré
        "U",   //217 Ù \u00D9
        "U",   //218 Ú \u00DA
        "U",   //219 Û \u00DB
        "U",   //220 Ü \u00DC
        "Y",   //221 � \u00DD
        "T",   //222 Þ \u00DE Thorn (lettre utilisée en Islande)
        "SS",  //223 ß \u00DF
        "A",   //224 à \u00E0
        "A",   //225 á \u00E1
        "A",   //226 â \u00E2
        "A",   //227 ã \u00E3
        "A",   //228 ä \u00E4
        "A",   //229 å \u00E5
        "AE",  //230 æ \u00E6
        "C",   //231 ç \u00E7
        "E",   //232 è \u00E8
        "E",   //233 é \u00E9
        "E",   //234 ê \u00EA
        "E",   //235 ë \u00EB
        "I",   //236 ì \u00EC
        "I",   //237 í \u00ED
        "I",   //238 î \u00EE
        "I",   //239 ï \u00EF
        "D",   //240 ð \u00F0
        "N",   //241 ñ \u00F1
        "O",   //242 ò \u00F2
        "O",   //243 ó \u00F3
        "O",   //244 ô \u00F4
        "O",   //245 õ \u00F5
        "O",   //246 ö \u00F6
        "/",   //247 ÷ \u00F7
        "O",   //248 ø \u00F8
        "U",   //249 ù \u00F9
        "U",   //250 ú \u00FA
        "U",   //251 û \u00FB
        "U",   //252 ü \u00FC
        "U",   //253 ý \u00FD
        "T",   //254 þ \u00FE
        "Y"    //255 ÿ \u00FF
    };

}