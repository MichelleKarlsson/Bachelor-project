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
    public void getTypeBrandStringGivenModelNone() {
        String type = "laptop";
        String brand = "Apple";
        String color = "white";
        String model = "None";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, brand, color, model));
        assertEquals(String.format("This is a %s %s %s.", color, brand, type), ans);
    }

    @Test
    public void getTypeBrandStringGivenModel() {
        String type = "laptop";
        String brand = "Dell";
        String color = "black";
        String model = "Latitude";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, brand, color, model));
        //"This is a black Dell Latitude laptop."
        assertEquals(String.format("This is a %s %s %s %s.", color, brand, model, type), ans);
    }

    @Test
    public void getTypeBrandStringGivenTypeNullFails() {
        String brand = "Lenovo";
        String color = "silver";
        String model = "None";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(null,brand,color,model));
        assertEquals("Insufficient information.", ans);
    }

    @Test
    public void getTypeBrandStringGivenBrandNullFails() {
        String color = "black";
        String type = "phone";
        String model = "None";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, null, color, model));
        assertEquals("Insufficient information.", ans);
    }

    @Test
    public void getTypeBrandStringGivenColorNullFails() {
        String brand = "Acer";
        String type = "laptop";
        String model = "None";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, brand, null, model));
        assertEquals("Insufficient information.", ans);
    }

    @Test
    public void getTypeBrandStringGivenAndroid() {
        String brand = "android";
        String confirmBrand = "Android";
        String type = "phone";
        String color = "black";
        String model = "Huawei";
        String ans = simpleNLG.getRealiser().realiseSentence(simpleNLG.getTypeBrandString(type, brand, color, model));
        //"This is a black Huawei android phone."
        assertEquals(String.format("This is a %s %s %s %s.", color, model, confirmBrand, type), ans);
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
    public void getFullDescriptionReturnsFullDescriptionAndroid() {
        String type = "phone";
        String brand = "android";
        String confirmBrand = "Android";
        String color = "black";
        String model = "Oneplus";
        String condition = "like new";
        int price = 1000;
        String currency = "DKK";

        String ans = simpleNLG.getFullDescription(type, brand, color, model, condition, price, currency);
        assertEquals(String.format("This is a %s %s %s %s, it is in %s condition and it costs %s %s.",color,model,confirmBrand,type,condition,price,currency),ans);
    }

    @Test
    public void getFullDescriptionReturnsFullDescription() {
        String type = "laptop";
        String brand = "Apple";
        String color = "silver";
        String model = "MacBook Air";
        String condition = "refurbished";
        int price = 3000;
        String currency = "DKK";

        String ans = simpleNLG.getFullDescription(type, brand, color, model, condition, price, currency);
        assertEquals(String.format("This is a %s %s %s %s, it is in %s condition and it costs %s %s.", color, brand, model, type, condition, price, currency), ans);
    }
}
