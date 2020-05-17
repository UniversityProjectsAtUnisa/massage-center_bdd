package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;


public class Macchinario extends Utils{
    
    String tipo;
    int quantita;

    public Macchinario(Random R) {
        tipo = randomString(1 + R.nextInt(20), R);
        
        quantita = 8 + R.nextInt(5);  // Circa 10 macchinari per tipo
    }
    
    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into macchinario(tipo,quantita)");
        temp.append("values(");
        temp.append("'").append(tipo).append("',");
        temp.append(quantita);
        temp.append(")");
        return temp.toString();
    }

    public String getTipo() {
        return tipo;
    }
    
    @Override
    public String toString() {
        return tipo + " " + quantita;
    } 
    
}
