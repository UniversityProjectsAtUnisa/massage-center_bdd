package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;


public class Cliente extends Utils{
    
    String codiceFiscale;
    String cognome;
    String nome;
    

    public Cliente(Random R, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        cognome = randomString(1 + R.nextInt(20), R);
        nome = randomString(1 + R.nextInt(20), R);
    }
    
    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into cliente(codicefiscale,cognome,nome)");
        temp.append("values(");
        temp.append("'").append(codiceFiscale).append("',");
        temp.append("'").append(cognome).append("',");
        temp.append("'").append(nome).append("'");
        temp.append(")");
        return temp.toString();
    }
    
    @Override
    public String toString() {
        return codiceFiscale + " " + cognome + " " + nome;
    } 
}