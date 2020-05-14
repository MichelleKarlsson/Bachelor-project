package android.mnah;

import android.util.Log;

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
            Log.e("SimpleNLG","Error creating lexicon, " + e);
        }

        this.lexicon = new XMLLexicon(prop.getProperty("XML_FILENAME"));
        factory = new NLGFactory(lexicon);
        realiser = new Realiser(lexicon);

    }

    public SPhraseSpec getTypeBrandString(String type, String brand, String color, String model) {
        SPhraseSpec s1 = this.factory.createClause();
        s1.setFeature(Feature.TENSE, Tense.PRESENT);
        s1.setSubject("This");
        s1.setVerb("be");

        if (type == null || brand == null || color == null) {
            SPhraseSpec s2 = this.factory.createClause();
            s2.setComplement("Insufficient information");
            return s2;
        }

        WordElement wordBrand = this.lexicon.getWord(brand);
        WordElement wordType = this.lexicon.getWord(type);
        WordElement wordColor = this.lexicon.getWord(color);
        WordElement wordModel = this.lexicon.getWord(model);
        String brandReal = this.realiser.realise(wordBrand).getRealisation();
        String brandCap = brandReal.substring(0, 1).toUpperCase() + brandReal.substring(1);
        String typeReal = this.realiser.realise(wordType).getRealisation();
        String colorReal = this.realiser.realise(wordColor).getRealisation();
        String wordReal = this.realiser.realise(wordModel).getRealisation();

        NPPhraseSpec item;
        if (model.equals("None")) {
            item = this.factory.createNounPhrase(colorReal + " " + brandCap + " " + typeReal);
        } else {
            if (brand.equals("android")) {
                item = this.factory.createNounPhrase(colorReal + " " + wordReal + " " + brandCap + " " + typeReal);
            } else {
                item = this.factory.createNounPhrase(colorReal + " " + brandCap + " " + wordReal + " " + typeReal);
            }
        }
        item.setDeterminer("a");
        s1.addComplement(item);
        return s1;

    }

    public SPhraseSpec getConditionString(String condition) {
        PPPhraseSpec pp = this.factory.createPrepositionPhrase();
        pp.addComplement(this.lexicon.getWord(condition.toLowerCase()));
        pp.setPreposition("in");

        //"it is in x condition"..
        SPhraseSpec s2 = this.factory.createClause();
        s2.setFeature(Feature.TENSE, Tense.PRESENT);
        s2.setSubject("it");
        s2.setVerb("be");
        s2.addComplement(pp);
        s2.addComplement("condition");
        return s2;
    }

    public SPhraseSpec getPriceString(int price, String currency) {
        SPhraseSpec s3 = this.factory.createClause();
        s3.setFeature(Feature.TENSE, Tense.PRESENT);
        if (price == 0) { //If no lowest price has been set, add generic text
            s3.addComplement("bids are welcome");
        } else {
            s3.setSubject("it");
            s3.setVerb("cost");
            s3.addComplement(price + " " + currency);
        }
        return s3;
    }

    public String getFullDescription(String type, String brand, String color, String model, String condition, int price, String currency) {

        SPhraseSpec s1 = getTypeBrandString(type, brand, color, model);
        SPhraseSpec s2 = getConditionString(condition);
        SPhraseSpec s3 = getPriceString(price, currency);

        CoordinatedPhraseElement cc = this.factory.createCoordinatedPhrase();
        cc.addCoordinate(s2);
        cc.addCoordinate(s3);


        //Putting it together
        CoordinatedPhraseElement c = this.factory.createCoordinatedPhrase();
        c.addCoordinate(s1); //first part of the description with type, color and brand
        c.addCoordinate(cc); //second part with condition and price
        c.setConjunction(",");
        return this.realiser.realiseSentence(c);
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
