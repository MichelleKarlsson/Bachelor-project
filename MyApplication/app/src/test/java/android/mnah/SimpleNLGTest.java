package android.mnah;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class SimpleNLGTest {

    SimpleNLG simpleNLG = new SimpleNLG();

    @Test
    public void constructionInitializesComponents() {
        SimpleNLG simpleNLG = new SimpleNLG();
        assert(simpleNLG.getLexicon() != null);
        assert(simpleNLG.getRealiser() != null);
        assert(simpleNLG.getFactory() != null);
    }

    @Test
    public void getTypeBrandStringGivenCorrectInput() {
        String type = "laptop";
        String brand = "Apple";
        String color = "white";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, brand, color));
        assertEquals(String.format("This is a %s %s %s.", color, brand, type), ans);
    }

    @Test
    public void getTypeBrandStringGivenIncorrectInputFails() {
        String brand = "Lenovo";
        String color = "silver";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(null,brand,color));
        assertEquals(String.format("Insufficient information."), ans);
    }

    @Test
    public void getConditionStringGivenCondition() {
        String condition = "refurbished";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getConditionString(condition));
        assertEquals(String.format("It is in %s condition.", condition), ans);
    }

    @Test
    public void getPriceStringGivenCorrectInput() {
        int price = 2000;
        String currency = "DKK";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getPriceString(price,currency));
        assertEquals(String.format("It costs %s %s.", price, currency),ans);
    }

    @Test
    public void getPriceGiven0(){
        int price = 0;
        String currency = "DKK";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getPriceString(price,currency));
        assertEquals("Bids are welcome.", ans);
    }

    @Test
    public void getFullDescriptionReturnsFullDescription() {
        String type = "phone";
        String brand = "Android";
        String color = "black";
        String condition = "like new";
        int price = 1000;
        String currency = "DKK";

        String ans = simpleNLG.getFullDescription(type, brand, color, condition, price, currency);
        assertEquals(String.format("This is a %s %s %s, it is in %s condition and it costs %s %s.",color,brand,type,condition,price,currency),ans);
    }
}
