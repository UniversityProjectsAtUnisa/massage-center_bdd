package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;


public class Receptionist extends Utils{
    
    String codiceFiscale;
    String cognome;
    String nome;
    String via;
    String cap;
    String citta;
    double stipendio;
    

    public Receptionist(Random R, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        cognome = randomString(1 + R.nextInt(20), R);
        nome = randomString(1 + R.nextInt(20), R);
        via = randomString(1 + R.nextInt(20), R);
        cap = randomString(5, R);
        citta = randomString(1 + R.nextInt(20), R);
        stipendio = 900 + R.nextDouble() * 700;
    }
    
    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into receptionist(codicefiscale,cognome,nome, via, cap, citta, stipendio)");
        temp.append("values(");
        temp.append("'").append(codiceFiscale).append("',");
        temp.append("'").append(cognome).append("',");
        temp.append("'").append(nome).append("',");
        temp.append("'").append(via).append("',");
        temp.append("'").append(cap).append("',");
        temp.append("'").append(citta).append("',");
        temp.append(stipendio);
        temp.append(")");
        return temp.toString();
    }
    
    @Override
    public String toString() {
        return codiceFiscale + " " + cognome + " " + nome + " " + via + " " + cap + " " + citta + " " + stipendio;
    } 

}
