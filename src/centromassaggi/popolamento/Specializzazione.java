package centromassaggi.popolamento;

import centromassaggi.common.Utils;

public class Specializzazione extends Utils {

    String massaggiatore;
    String tipoMassaggio;

    public Specializzazione(String massaggiatore, String tipoMassaggio) {
        this.massaggiatore = massaggiatore;
        this.tipoMassaggio = tipoMassaggio;
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into specializzazione(massaggiatore, tipoMassaggio)");
        temp.append("values(");
        temp.append("'").append(massaggiatore).append("',");
        temp.append("'").append(tipoMassaggio).append("'");
        temp.append(")");
        return temp.toString();
    }

    @Override
    public String toString() {
        return massaggiatore + " " + tipoMassaggio;
    }

}
