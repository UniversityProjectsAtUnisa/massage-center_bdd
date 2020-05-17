package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;

public class TipoMassaggio extends Utils {

    String tipo;
    double prezzo;
    String macchinario;
    int durata;

    public TipoMassaggio(Random R, String macchinario) {
        tipo = randomString(1 + R.nextInt(20), R);
        this.macchinario = macchinario;
        prezzo = 10 + R.nextDouble() * 90; //double tra 10 e 100
        durata = 30 + R.nextInt(7) * 15;
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into tipomassaggio(tipo,prezzo,macchinario,durata)");
        temp.append("values(");
        temp.append("'").append(tipo).append("',");
        temp.append(prezzo).append(",");
        if (macchinario != null) {
            temp.append("'").append(macchinario).append("',");
        } else {
            temp.append("null").append(",");
        }
        temp.append(durata);
        temp.append(")");
        return temp.toString();
    }

    public String getTipo() {
        return tipo;
    }
    
    @Override
    public String toString() {
        return tipo + " " + prezzo + " " + macchinario + " " + durata;
    }

}
