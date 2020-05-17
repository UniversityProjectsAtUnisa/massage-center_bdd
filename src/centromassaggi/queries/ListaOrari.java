package centromassaggi.queries;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/*
    Struttura dati per la gestione delle fasce orarie.
*/
public class ListaOrari extends ArrayList<Orario> {

    /*
        Minimo di preavviso prima di prenotare un massaggio, in minuti.
    */
    private static final int MINIMO_PREAVVISO = 10;

    /*
        Restituisce una lista contenente gli orari di apertura.
        Se diOggi è true restituisce una lista orari relativo all'orario corrente in quanto saranno poi usati
        per comunicare all'utente per quale orario è possibile prenotare
    */
    public static ListaOrari orariDiApertura(boolean diOggi) {
        if(diOggi) return new ListaOrari().add(LocalTime.now().plusMinutes(MINIMO_PREAVVISO).truncatedTo(ChronoUnit.MINUTES), LocalTime.of(21, 0));
        return new ListaOrari().add(LocalTime.of(9, 0), LocalTime.of(21, 0));
    }

    public ListaOrari add(LocalTime oraInizio, LocalTime oraFine) {
        if (oraFine.compareTo(oraInizio) > 0) {
            for (Orario ora : this) {
                if (ora.getOraFine().compareTo(oraInizio) == 0) {
                    ora.setOraFine(oraFine);
                    return this;
                }
            }
            this.add(new Orario(oraInizio, oraFine));
        }
        return this;
    }

    /* 
        Classe di appoggio per l'elaborazione degli orari nel metodo sottraiOrari
    */
    private class OrarioCaratterizzato implements Comparable<OrarioCaratterizzato> {
        LocalTime ora;
        String tipo;
        int valore;

        public OrarioCaratterizzato(LocalTime ora, String tipo, int valore){
            this.ora = ora;
            this.tipo = tipo;
            this.valore = valore;
        }

        @Override
        public int compareTo(OrarioCaratterizzato o) {
            return this.ora.compareTo(o.ora);
        }
    }

    /*
        Date due liste di fasce orarie (this ed l), restituisce una lista di orari che 
        rappresenta la sottrazione delle fasce orarie di l da quelle di this.
    */
    public ListaOrari sottraiOrari(ListaOrari l) {
        ListaOrari result = new ListaOrari();
        ArrayList<OrarioCaratterizzato> temp = new ArrayList<>();
        for(Orario o: this){
            temp.add(new OrarioCaratterizzato(o.getOraInizio(), "libero", 1));
            temp.add(new OrarioCaratterizzato(o.getOraFine(), "libero", -1));
        }
        for(Orario o: l){
            temp.add(new OrarioCaratterizzato(o.getOraInizio(), "occupato", 1));
            temp.add(new OrarioCaratterizzato(o.getOraFine(), "occupato", -1));
        }
        temp.sort(null);

        int libero = 0, occupato = 0;
        LocalTime oraInizio = null, oraFine;
        for(OrarioCaratterizzato o: temp){
            if ("libero".equals(o.tipo)){
                libero += o.valore;
            } else {
                occupato += o.valore;
            }
            if(oraInizio != null) {
                oraFine = o.ora;
                result.add(oraInizio, oraFine);
                oraInizio = null;
            } else if (libero == 1 && occupato == 0){
                oraInizio = o.ora;
            }
        }

        return result;
    }

    @Override
    public String toString() {

        String s = "-------------------";
        s += String.format("\n%-9s %9s", "OraInizio", "OraFine");
        s += "\n-------------------";
        for (Orario ora : this) {
            s += "\n" + ora;
        }
        s += "\n-------------------";
        return s;
    }

}
