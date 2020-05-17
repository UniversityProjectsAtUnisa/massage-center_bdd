package centromassaggi.queries;

import java.time.LocalTime;

/* 
    Classe che rappresenta una fascia oraria sulla quale sono stati 
    definiti getter, setter e toString.
*/
public class Orario {

        private LocalTime oraInizio, oraFine;


        public Orario(LocalTime oraInizio, LocalTime oraFine) {
            this.oraInizio = oraInizio;
            this.oraFine = oraFine;
        }


        public LocalTime getOraInizio() {
            return this.oraInizio;
        }


        public LocalTime getOraFine() {
            return this.oraFine;
        }
        

        public void setOraInizio(LocalTime oraInizio) {
            this.oraInizio = oraInizio;
        }


        public void setOraFine(LocalTime oraFine) {
            this.oraFine = oraFine;
        }

        
        @Override
        public String toString() {
            return String.format("%-9s %9s", this.oraInizio, this.oraFine);
        }
    }
