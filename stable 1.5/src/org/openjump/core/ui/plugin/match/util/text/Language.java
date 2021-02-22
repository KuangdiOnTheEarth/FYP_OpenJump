/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A language in use in France or in a french territory.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-08-13)
 */
// History
// 0.1 (2011-05-01)
public class Language {
    
    public final static Language UNKNOWN             = new Language("Unknown");

    // Familles
    public final static Language INDO_EUROPEEN       = new Language("Indo-europ�en");
    public final static Language BASQUE              = new Language("Basque");
    public final static Language CREOLE              = new Language("Cr�ole");
    public final static Language AMERINDIEN          = new Language("Am�rindien");
    public final static Language PACIFIQUE           = new Language("Pacifique");
    
    // Branches
    public final static Language CELTE               = new Language("Celte",      INDO_EUROPEEN);
    public final static Language GERMANIQUE          = new Language("Germanique", INDO_EUROPEEN);
    public final static Language ROMAN               = new Language("Roman",      INDO_EUROPEEN);
    
    public final static Language CREOLE_ANTILLAIS    = new Language("Cr�ole antillais", CREOLE);
    public final static Language CREOLE_MASCARIN     = new Language("Cr�ole mascarin",  CREOLE);
    public final static Language CREOLE_PACIFIQUE    = new Language("Cr�ole pacifique", CREOLE);
    
    public final static Language CARIBE              = new Language("Caribe", AMERINDIEN);
    
    // Groupe
    public final static Language BRITTONIQUE         = new Language("Brittonique",        CELTE);
    public final static Language CELTE_CONTINENTAL   = new Language("Celte continental",  CELTE);
    
    public final static Language GERMANO_NEERLANDAIS = new Language("Germano-n�erlandais", GERMANIQUE);
    
    public final static Language OCCITANO_ROMAN      = new Language("Occitano-roman", ROMAN);
    public final static Language FRANCO_PROVENCAL    = new Language("Franco-proven�al (Arpitan)", ROMAN);
    public final static Language GALLO_ROMAN         = new Language("Gallo-roman", ROMAN);
    public final static Language ITALO_ROMAN         = new Language("Italo-roman", ROMAN);
    public final static Language CARIBE_GUYANAIS     = new Language("Caribe guyanais", CARIBE);
    
    // Langues
    public final static Language BRETON              = new Language("Breton",   BRITTONIQUE);
    public final static Language GAULOIS             = new Language("Gaulois",  CELTE_CONTINENTAL);
    
    public final static Language BAS_FRANCIQUE       = new Language("Bas-francique",      GERMANO_NEERLANDAIS);
    public final static Language MOYEN_FRANCIQUE     = new Language("Moyen-francique",    GERMANO_NEERLANDAIS); // franciques luxembourgeois, mosellan, rh�nan
    public final static Language ALLEMAND_SUP        = new Language("Allemand sup�rieur", GERMANO_NEERLANDAIS);
    
    public final static Language OCCITAN             = new Language("Occitan",  OCCITANO_ROMAN);
    public final static Language CATALAN             = new Language("Catalan",  OCCITANO_ROMAN);
    public final static Language FRANCAIS            = new Language("Fran�ais", GALLO_ROMAN);
    public final static Language WALLON              = new Language("Wallon",   GALLO_ROMAN);
    public final static Language LORRAIN             = new Language("Lorrain",  GALLO_ROMAN); // diff�rent du lorrain francique, d'origine germanique
    public final static Language CORSE               = new Language("Corse",    ITALO_ROMAN);
    
    
    // Dialectes
    public final static Language FLAMAND          = new Language("Flamand",   BAS_FRANCIQUE);
    public final static Language ALSACIEN         = new Language("Alsacien",  ALLEMAND_SUP);
    
    public final static Language GASCON           = new Language("Gascon",    OCCITAN);
    public final static Language PROVENCAL        = new Language("Proven�al", OCCITAN);
    public final static Language LANGUEDOCIEN     = new Language("Languedocien", OCCITAN);
    
    public final static int ROOT     = 0;
    public final static int FAMILY   = 1; // ex. indo-europ�en
    public final static int BRANCH   = 2; // ex. celte/germanique/roman
    public final static int GROUP    = 3; // ex. 
    public final static int LANGUAGE = 4; // ex. breton/Occitan
    public final static int DIALECT  = 5; // ex.
    
    private String name;
    private Language parent = null;
    private int level = 0;
    
    public Language(String name) {
        this.name = name;
    }
    
    public Language(String name, Language parent) {
        this.name = name;
        this.parent = parent;
    }
    
    public String getName() {
        return name;
    }
    
    private Language getParent() {
        return parent;
    }
    
    public int getLevel() {
        int lev = 1;
        Language lang = this;
        while (null != (lang = lang.getParent())) lev++;
        return lev;
    }
    
    public static boolean areComparable(Language l1, Language l2) {
        if (l1 == l2) return true;
        // case l1 level > l2 level : reverse the code
        if (l1.getLevel() > l2.getLevel()) return areComparable(l2, l1);
        // case l1 level = l2 level : two different language of same level
        else if (l1.getLevel() == l2.getLevel()) return false;
        else return areComparable(l1, l2.getParent());
    }
    
    public String toString() {
        List<String> list = new ArrayList<String>();
        list.add(name);
        Language lang = this;
        while (null != (lang = lang.getParent())) list.add(lang.getName());
        Collections.reverse(list);
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < list.size() ; i++) {
            if (i>0) sb.append("/");
            sb.append(list.get(i));
        }
        return sb.toString();
    }
    
    //public static class LanguageLevelException extends Exception {
    //    public LanguageLevelException(String message) {
    //        super(message);
    //    }
    //}
    
    // aecker
    // bach    = ruisseau
    // berg    = montagne
    // brunnen = source
    // burg    = bourg
    // feld
    // garten  = jardin
    // heim    = hameau
    // holz    = for�t (holtz = forme francique)
    // ingen
    // kopf    = t�te
    // viller  = 
    private static Pattern GERMANIQUE_1 = Pattern.compile("\\p{L}(aecker|bach|berg|brunnen|burg|feld|garten|heim|holt?z|ingen|kopf|[vw]e?iller|wald|weg)\\b", Pattern.CASE_INSENSITIVE);
    // dorf    = ville
    // eck
    // heid    = lande, pelouse
    // kir (kirche, kirsche...) : �glise...
    // opf     = kopf (t�te) ou hopf ()
    // schl (ex. schloss, ch�teau) - 2 exceptions en Bretagne
    // stadt   = ville
    // stein   = pierre
    // was (wasen=tourbi�re...)
    private static Pattern GERMANIQUE_2 = Pattern.compile("dorf|eck|heid|kir([ceklnrstv]|b[ael])|opf|schl|stadt|stein(?!s\\b|u)|was", Pattern.CASE_INSENSITIVE);
    private static Pattern GERMANIQUE_3 = Pattern.compile("\\bim\\b", Pattern.CASE_INSENSITIVE);
    private static Pattern GERMANIQUE_4 = Pattern.compile("(?<!c)ae(?!u)", Pattern.CASE_INSENSITIVE); //ae
    private static Pattern GERMANIQUE_5 = Pattern.compile("(?<!pl)oe(?!([uiy]|dic))", Pattern.CASE_INSENSITIVE); //oe sauf ploe, oeu, oei, oey et oedic
    private static Pattern GERMANIQUE_6 = Pattern.compile("(\\b|[rl])sch|sch(\\b|[blvw])", Pattern.CASE_INSENSITIVE); //sch initial ou pr�c�d� d'une consonne
    private static Pattern GERMANIQUE_7 = Pattern.compile("(?<!\\b)berg(en)?\\b", Pattern.CASE_INSENSITIVE); // berg/bergen
    private static Pattern GERMANIQUE_8 = Pattern.compile("([bd-km-su-z]|zel|mul|st)house\\b", Pattern.CASE_INSENSITIVE); // ..house (maison)
    private static Pattern GERMANIQUE_9 = Pattern.compile("m[iu�]nster\\b", Pattern.CASE_INSENSITIVE); // monast�re
    private static Pattern GERMANIQUE_10 = Pattern.compile("(?<!(\\b|�)c)hoff?(en)?", Pattern.CASE_INSENSITIVE); // exploitation agricole
    private static Pattern GERMANIQUE_11 = Pattern.compile("(?<!ar|[aeiouy�]|\\b)matt(?!es\\b)", Pattern.CASE_INSENSITIVE); // alpage
    
    private static Pattern ROMAN_1      = Pattern.compile("castel", Pattern.CASE_INSENSITIVE); // castel
    
    private static Pattern OCCITAN_1    = Pattern.compile("(?<!\\b\\p{L})ac\\b", Pattern.CASE_INSENSITIVE); // fin en ac sauf 'bac' et 'lac'
    private static Pattern OCCITAN_2    = Pattern.compile("[e�]oux\\b", Pattern.CASE_INSENSITIVE); // eoux
    private static Pattern OCCITAN_3    = Pattern.compile("(?<!b)osc\\b", Pattern.CASE_INSENSITIVE); // osc sauf bosc
    private static Pattern OCCITAN_4    = Pattern.compile("[e�]one\\b", Pattern.CASE_INSENSITIVE); // �one (proven�al)
    private static Pattern OCCITAN_5    = Pattern.compile("\\bpey|ranc|sagne\\b", Pattern.CASE_INSENSITIVE); // colline, rocher, marais
    
    private static Pattern LANGUEDOCIEN_1     = Pattern.compile("\\bplo|pioch\\b", Pattern.CASE_INSENSITIVE); // castel
    
    private static Pattern FRANCO_PROVENCAL_1 = Pattern.compile("\\brif\\b", Pattern.CASE_INSENSITIVE); // ruisseau
    
    private static Pattern CORSE_1      = Pattern.compile("(cc|gg)(h?i(?!eu)|e\\b)", Pattern.CASE_INSENSITIVE); // cce, ccia, cchi, ccio, cciu...
    private static Pattern CORSE_2      = Pattern.compile("zz([aiou]|e\\b)", Pattern.CASE_INSENSITIVE); // zza, zzi, zzo
    private static Pattern CORSE_3      = Pattern.compile("\\bdi\\b", Pattern.CASE_INSENSITIVE);
    private static Pattern CORSE_4      = Pattern.compile("\\bsan(t[ao]?)?\\b", Pattern.CASE_INSENSITIVE);
    //private static Pattern CORSE_5      = Pattern.compile("(?<!uel)one\\b", Pattern.CASE_INSENSITIVE);
    // termes terminant en one ou ona
    private static Pattern CORSE_5      = Pattern.compile("(a|(ia|c|[clz]i|([io]|[il]a)n|f[ou]r)c|le|(f|t[au])f|((tr|s)a|[tu]an)g|([cprz]|bb|gg|(cc|g)h|[aiu]gl)i|j|(((i|st)e|[tv]a)l|[iz]a|[tuz]i)l|(lia|ci)m|((ta|n)|(\\b(st)?ag))n|[ai]tt|([bg]ra|chi[ou])v)on[ae]\\b", Pattern.CASE_INSENSITIVE);
    private static Pattern CORSE_6      = Pattern.compile("iano", Pattern.CASE_INSENSITIVE);
    private static Pattern CORSE_7      = Pattern.compile("\\bpie(tr|di)", Pattern.CASE_INSENSITIVE);
    private static Pattern CORSE_8      = Pattern.compile(".\\bpetra\\b|\\bpetra\\b.", Pattern.CASE_INSENSITIVE); // rocher
    private static Pattern CORSE_9      = Pattern.compile("^cima\\b|\\ba cima\\b", Pattern.CASE_INSENSITIVE); // sommet
    
    
    private static Pattern BRETON_1     = Pattern.compile("(?<![bs]|ruff|aur|gn|tr)ec\\b", Pattern.CASE_INSENSITIVE); // fin en ec
    private static Pattern BRETON_2     = Pattern.compile("c[' ]h\\b", Pattern.CASE_INSENSITIVE); // fin en c'h (inclut crec'h, roc'h)
    private static Pattern BRETON_3     = Pattern.compile("\\bker", Pattern.CASE_INSENSITIVE); // ker attention, moins fort que GERMANIQUE_1
    private static Pattern BRETON_4     = Pattern.compile("\\bplo[eu](?!\\b)", Pattern.CASE_INSENSITIVE); //d�but en plou ou ploe
    private static Pattern BRETON_5     = Pattern.compile("[cg]o[e�]t", Pattern.CASE_INSENSITIVE); // co�t ou go�t (bois)
    private static Pattern BRETON_6     = Pattern.compile("^an ([nthaeiouy�]|d(?!er))", Pattern.CASE_INSENSITIVE); // an (article d�fini breton) mais pas suivi de der (germanique)
    private static Pattern BRETON_7     = Pattern.compile("^ar [^ntdhlaeiouy���]", Pattern.CASE_INSENSITIVE); // ar (article breton) // 21 occurences
    private static Pattern BRETON_8     = Pattern.compile("^al l", Pattern.CASE_INSENSITIVE); // al (article d�fini breton) - une exception dans le 47 : al Liot 
    private static Pattern BRETON_9     = Pattern.compile("^(goas|roz)\\b", Pattern.CASE_INSENSITIVE); // ruisseau, colline
    private static Pattern BRETON_10    = Pattern.compile("porz", Pattern.CASE_INSENSITIVE); // maison, manoir (une exception dans le 79)
    private static Pattern BRETON_11    = Pattern.compile("\\bm[e�]n[e�]z ", Pattern.CASE_INSENSITIVE); // mont (attention, un m�n� trouv� dans le 32)
    private static Pattern BRETON_12    = Pattern.compile("\\b(beg|g[ow]u?a[hz]|kastell|milin|traon)\\b", Pattern.CASE_INSENSITIVE); // pointe, ruisseau, ch�teau, moulin, partie basse
    
    
    
    private static Pattern CATALAN_1    = Pattern.compile("\\bll", Pattern.CASE_INSENSITIVE); // Ll
    private static Pattern CATALAN_2    = Pattern.compile("nya\\b", Pattern.CASE_INSENSITIVE); // nya
    private static Pattern CATALAN_3    = Pattern.compile("aixas\\b", Pattern.CASE_INSENSITIVE); // aixas
    private static Pattern CATALAN_4    = Pattern.compile("\\coma|serrat\\b", Pattern.CASE_INSENSITIVE); // serrat (cr�te, colline �lev�e)
    
    private static Pattern FRANCAIS_1   = Pattern.compile("\\bl�s\\b", Pattern.CASE_INSENSITIVE); // -l�s-
    private static Pattern FRANCAIS_2   = Pattern.compile("bourg", Pattern.CASE_INSENSITIVE); // bourg
    private static Pattern FRANCAIS_3   = Pattern.compile("vill(e|iers)", Pattern.CASE_INSENSITIVE); // ville/villiers
    private static Pattern FRANCAIS_4   = Pattern.compile("fay(e(s|t|l|tte)?|s)?\\b", Pattern.CASE_INSENSITIVE); // fay le h�tre
    private static Pattern FRANCAIS_5   = Pattern.compile("court\\b", Pattern.CASE_INSENSITIVE); // fin en court
    private static Pattern FRANCAIS_6   = Pattern.compile("(o|ill|gn)y\\b", Pattern.CASE_INSENSITIVE); // fin en oy/illy/gny
    private static Pattern FRANCAIS_7   = Pattern.compile("\\b(bordes?|champagne)\\b", Pattern.CASE_INSENSITIVE); // m�taierie, �tendue plate, cultivable
    private static Pattern FRANCAIS_8   = Pattern.compile("pierre", Pattern.CASE_INSENSITIVE); // pierre
    
    // bronn, brunn : source
    // prich        : montagne
    // thal         : vallon
    // troff        : ville
    private static Pattern M_FRANCIQUE_1  = Pattern.compile("(br[ou]nn|prich|thal|troff)\\b", Pattern.CASE_INSENSITIVE); // source (moins for que ker : kerbrunn = breton)
    // stett        : ville
    private static Pattern M_FRANCIQUE_2  = Pattern.compile("stett(?![aiou]|es?\\b)", Pattern.CASE_INSENSITIVE); // ville
    private static Pattern M_FRANCIQUE_3  = Pattern.compile("kastel(?!l)", Pattern.CASE_INSENSITIVE); // ch�teau (attention, kastell = breton)
    
    
    private static Pattern BASQUE_1  = Pattern.compile("[aeiou��]ko", Pattern.CASE_INSENSITIVE);  // env. 960 (3 exceptions hors pays basque)
    private static Pattern BASQUE_2  = Pattern.compile("[e�]rr?[e�]ka", Pattern.CASE_INSENSITIVE); // env. 460
    private static Pattern BASQUE_3  = Pattern.compile("[aeiou��]zki", Pattern.CASE_INSENSITIVE); // env. 
    private static Pattern BASQUE_4  = Pattern.compile("tx(?!\\b)", Pattern.CASE_INSENSITIVE); // env. 320
    private static Pattern BASQUE_5  = Pattern.compile("tz[aeiou��]k", Pattern.CASE_INSENSITIVE); // env. 65
    
    
    public static Language guessLanguage(String s) {
        if (GERMANIQUE_1.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_2.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_3.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_4.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_5.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_6.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_7.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_8.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_9.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_10.matcher(s).find()) return GERMANIQUE;
        if (GERMANIQUE_11.matcher(s).find()) return GERMANIQUE;
        
        if (M_FRANCIQUE_1.matcher(s).find()) return MOYEN_FRANCIQUE;
        if (M_FRANCIQUE_2.matcher(s).find()) return MOYEN_FRANCIQUE;
        if (M_FRANCIQUE_3.matcher(s).find()) return MOYEN_FRANCIQUE;
        
        if (OCCITAN_1.matcher(s).find()) return OCCITAN;
        if (OCCITAN_2.matcher(s).find()) return OCCITAN;
        if (OCCITAN_3.matcher(s).find()) return OCCITAN;
        if (OCCITAN_4.matcher(s).find()) return OCCITAN;
        if (OCCITAN_5.matcher(s).find()) return OCCITAN;
        
        if (CORSE_1.matcher(s).find()) return CORSE;
        if (CORSE_2.matcher(s).find()) return CORSE;
        if (CORSE_3.matcher(s).find()) return CORSE;
        if (CORSE_4.matcher(s).find()) return CORSE;
        if (CORSE_5.matcher(s).find()) return CORSE;
        if (CORSE_6.matcher(s).find()) return CORSE;
        if (CORSE_7.matcher(s).find()) return CORSE;
        if (CORSE_8.matcher(s).find()) return CORSE;
        if (CORSE_9.matcher(s).find()) return CORSE;
        
        if (ROMAN_1.matcher(s).find()) return ROMAN;
        if (LANGUEDOCIEN_1.matcher(s).find()) return LANGUEDOCIEN;
        if (FRANCO_PROVENCAL_1.matcher(s).find()) return FRANCO_PROVENCAL;
        
        if (BRETON_1.matcher(s).find()) return BRETON;
        if (BRETON_2.matcher(s).find()) return BRETON;
        if (BRETON_3.matcher(s).find()) return BRETON;
        if (BRETON_4.matcher(s).find()) return BRETON;
        if (BRETON_5.matcher(s).find()) return BRETON;
        if (BRETON_6.matcher(s).find()) return BRETON;
        if (BRETON_7.matcher(s).find()) return BRETON;
        if (BRETON_8.matcher(s).find()) return BRETON;
        if (BRETON_9.matcher(s).find()) return BRETON;
        if (BRETON_10.matcher(s).find()) return BRETON;
        if (BRETON_11.matcher(s).find()) return BRETON;
        if (BRETON_12.matcher(s).find()) return BRETON;
        
        if (CATALAN_1.matcher(s).find()) return CATALAN;
        if (CATALAN_2.matcher(s).find()) return CATALAN;
        if (CATALAN_3.matcher(s).find()) return CATALAN;
        if (CATALAN_4.matcher(s).find()) return CATALAN;
        
        if (FRANCAIS_1.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_2.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_3.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_4.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_5.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_6.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_7.matcher(s).find()) return FRANCAIS;
        if (FRANCAIS_8.matcher(s).find()) return FRANCAIS;
        
        else return UNKNOWN;
    
    }

}