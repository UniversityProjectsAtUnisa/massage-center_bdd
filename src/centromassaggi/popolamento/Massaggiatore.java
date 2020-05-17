package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;


public class Massaggiatore extends Utils{
    
    String codiceFiscale;
    String cognome;
    String nome;
    String via;
    String cap;
    String citta;
    double stipendioBase;
    

    public Massaggiatore(Random R, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        cognome = randomString(1 + R.nextInt(20), R);
        nome = randomString(1 + R.nextInt(20), R);
        via = randomString(1 + R.nextInt(20), R);
        cap = randomString(5, R);
        citta = randomString(1 + R.nextInt(20), R);
        stipendioBase = 600 + R.nextDouble() * 400;
    }
    
    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into massaggiatore(codicefiscale,cognome,nome, via, cap, citta, stipendiobase)");
        temp.append("values(");
        temp.append("'").append(codiceFiscale).append("',");
        temp.append("'").append(cognome).append("',");
        temp.append("'").append(nome).append("',");
        temp.append("'").append(via).append("',");
        temp.append("'").append(cap).append("',");
        temp.append("'").append(citta).append("',");
        temp.append(stipendioBase);
        temp.append(")");
        return temp.toString();
    }
    
    @Override
    public String toString() {
        return codiceFiscale + " " + cognome + " " + nome + " " + via + " " + cap + " " + citta + " " + stipendioBase;
    } 
}