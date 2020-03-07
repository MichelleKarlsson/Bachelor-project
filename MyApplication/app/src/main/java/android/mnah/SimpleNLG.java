package android.mnah;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;

public class SimpleNLG {

    private Lexicon lexicon;
    private NLGFactory factory;
    private Realiser realiser;

    public SimpleNLG() {

        Properties prop = new Properties();

        try {
            prop.load(getClass().getClassLoader().getResourceAsStream("lexicon.properties"));
        } catch (IOException e) {
            System.out.println("Error creating lexicon, " + e);
        }

        this.lexicon = new XMLLexicon(prop.getProperty("XML_FILENAME"));
        factory = new NLGFactory(lexicon);
        realiser = new Realiser(lexicon);

    }

    public NLGFactory getFactory() {
        return this.factory;
    }

    public Realiser getRealiser() {
        return this.realiser;
    }

    public Lexicon getLexicon() {
        return this.lexicon;
    }
}
