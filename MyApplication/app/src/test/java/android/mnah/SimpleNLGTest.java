package android.mnah;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.realiser.english.Realiser;

public class SimpleNLGTest {

    @Test
    public void lookupCustomWords() {
        Properties prop = new Properties();

        try {
            prop.load(getClass().getClassLoader().getResourceAsStream("lexicon.properties"));
        } catch (IOException e) {
            System.out.println("Error creating lexicon, " + e);
        }

        Lexicon lexicon = new XMLLexicon(prop.getProperty("XML_FILENAME"));
        NLGFactory factory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);


        Assert.assertEquals("laptop", lexicon.lookupWord("laptop", LexicalCategory.NOUN).getBaseForm());

    }
}
