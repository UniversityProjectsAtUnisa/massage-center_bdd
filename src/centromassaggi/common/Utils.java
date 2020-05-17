package centromassaggi.common;

import java.util.Random;

/* 
    Classe astratta per generare stringhe di lettere o numeri casuali di lunghezza prefissata.
    Dichiara inoltre il metodo getInsertQuery.
*/
public abstract class Utils {

    static String lowerLetters = "abcdefghilmnopqrstuvz";
    static String numbers = "0123456789";
    public abstract String getInsertQuery();
    
    public final String randomString(int len, Random R) {
        StringBuilder temp = new StringBuilder();
        temp.setLength(0);
        while (temp.length() < len) {
          temp.append(lowerLetters.charAt(R.nextInt(lowerLetters.length())));
        }
        return temp.toString();
    }
    
    public final String randomNumeralString(int len, Random R) {
        StringBuilder temp = new StringBuilder();
        temp.setLength(0);
        while (temp.length() < len) {
          temp.append(numbers.charAt(R.nextInt(numbers.length())));
        }
        return temp.toString();
    }
  
}
