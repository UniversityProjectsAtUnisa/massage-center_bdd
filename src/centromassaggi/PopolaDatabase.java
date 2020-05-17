package centromassaggi;

import centromassaggi.common.CustomManager;
import centromassaggi.popolamento.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.time.LocalDate;

public class PopolaDatabase {

    /*
        Script per il popolamento del database.
        Vengono sfruttati ArrayList di stringhe in modo da riutilizzare le stesse chiavi primarie
        senza riottenerle in un secondo momento con query che potrebbero rallentare troppo il popolamento.
    */
    public static void main(String[] args) {
        Random R = new Random(123);

        ArrayList<String> macchinari = new ArrayList<>();
        ArrayList<String> tipimassaggio = new ArrayList<>();
        ArrayList<String> clienti = new ArrayList<>();
        ArrayList<String> receptionists = new ArrayList<>();
        ArrayList<String> massaggiatori = new ArrayList<>();

        try (Connection conn = CustomManager.getConnection()) {
            Statement stm = conn.createStatement();
            // Popolamento Sala
            System.out.println("Popolamento Sala");
            for (int i = 0; i < 5; i++) {
                stm.executeUpdate(new Sala(R).getInsertQuery());
            }
            // Popolamento Macchinario
            System.out.println("Popolamento Macchinario");
            for (int i = 0; i < 3; i++) {
                Macchinario mac = new Macchinario(R);
                stm.executeUpdate(mac.getInsertQuery());
                macchinari.add(mac.getTipo());
            }

            // Popolamento TipoMassaggio
            System.out.println("Popolamento TipoMassaggio");
            for (int i = 0; i < 5; i++) {
                if (R.nextInt(101) < 80) {
                    TipoMassaggio tipomassaggio = new TipoMassaggio(R, macchinari.get(R.nextInt(macchinari.size())));
                    tipimassaggio.add(tipomassaggio.getTipo());
                    stm.executeUpdate(tipomassaggio.getInsertQuery());
                } else {
                    TipoMassaggio tipomassaggio = new TipoMassaggio(R, null);
                    tipimassaggio.add(tipomassaggio.getTipo());
                    stm.executeUpdate(tipomassaggio.getInsertQuery());
                }
            }

            conn.setAutoCommit(false);
            // Popolamento Recapito Cliente
            System.out.println("Popolamento Recapito Cliente");
            for (int i = 0; i < 375; i++) { 
                // Circa 500 clienti considerando che in un terzo dei casi vengono creati due clienti al posto di 1 (vedi case 1)
                switch (R.nextInt(3)) {
                    case 0:
                        // Un cliente due telefoni
                        RecapitoCliente recapitoCliente = new RecapitoCliente(R);
                        clienti.add(recapitoCliente.getCliente());
                        stm.executeUpdate(recapitoCliente.getInsertQuery());
                        stm.executeUpdate(new RecapitoCliente(R, recapitoCliente.getCliente()).getInsertQuery());
                        break;

                    case 1:
                        // Due clienti un telefono
                        RecapitoCliente recapitoCliente1 = new RecapitoCliente(R);
                        clienti.add(recapitoCliente1.getCliente());
                        stm.executeUpdate(recapitoCliente1.getInsertQuery());

                        // Secondo cliente con lo stesso numero di telefono del primo
                        RecapitoCliente recapitoCliente2 = new RecapitoCliente(recapitoCliente1.getTelefono(), R);
                        clienti.add(recapitoCliente2.getCliente());
                        stm.executeUpdate(recapitoCliente2.getInsertQuery());
                        break;

                    default:
                        // Un cliente un telefono
                        RecapitoCliente recapitoCliente3 = new RecapitoCliente(R);
                        clienti.add(recapitoCliente3.getCliente());
                        stm.executeUpdate(recapitoCliente3.getInsertQuery());
                }
            }

            // Popolamento Cliente
            System.out.println("Popolamento Cliente");
            for (String cliente : clienti) {
                stm.executeUpdate(new Cliente(R, cliente).getInsertQuery());
            }
            conn.commit();

            // Popolamento Recapito Receptionist
            System.out.println("Popolamento Recapito Receptionist");
            for (int i = 0; i < 2; i++) {
                // Circa 3 receptionists considerando che in un terzo dei casi vengono creati due receptionists al posto di 1 (vedi case 1)
                switch (R.nextInt(3)) {
                    case 0:
                        // Un receptionist due telefoni
                        RecapitoReceptionist recapitoReceptionist = new RecapitoReceptionist(R);
                        receptionists.add(recapitoReceptionist.getReceptionist());
                        stm.executeUpdate(recapitoReceptionist.getInsertQuery());
                        stm.executeUpdate(
                                new RecapitoReceptionist(R, recapitoReceptionist.getReceptionist()).getInsertQuery());
                        break;

                    case 1:
                        // Due receptionists un telefono
                        RecapitoReceptionist recapitoReceptionist1 = new RecapitoReceptionist(R);
                        receptionists.add(recapitoReceptionist1.getReceptionist());
                        stm.executeUpdate(recapitoReceptionist1.getInsertQuery());

                        // Secondo receptionist con lo stesso numero di telefono del primo
                        RecapitoReceptionist recapitoReceptionist2 = new RecapitoReceptionist(
                                recapitoReceptionist1.getTelefono(), R);
                        receptionists.add(recapitoReceptionist2.getReceptionist());
                        stm.executeUpdate(recapitoReceptionist2.getInsertQuery());
                        break;

                    default:
                        // Un receptionist un telefono
                        RecapitoReceptionist recapitoReceptionist3 = new RecapitoReceptionist(R);
                        receptionists.add(recapitoReceptionist3.getReceptionist());
                        stm.executeUpdate(recapitoReceptionist3.getInsertQuery());
                }
            }
            // Popolamento Receptionist
            System.out.println("Popolamento Receptionist");
            for (String receptionist : receptionists) {
                stm.executeUpdate(new Receptionist(R, receptionist).getInsertQuery());
            }
            conn.commit();

            // Popolamento Recapito Massaggiatore
            System.out.println("Popolamento Recapito Massaggiatore");
            for (int i = 0; i < 12; i++) {
                // Circa 17 massaggiatori considerando che in un terzo dei casi vengono creati due massaggiatori al posto di 1 (vedi case 1)
                switch (R.nextInt(3)) {
                    case 0:
                        // Un massaggiatore due telefoni
                        RecapitoMassaggiatore recapitoMassaggiatore = new RecapitoMassaggiatore(R);
                        massaggiatori.add(recapitoMassaggiatore.getMassaggiatore());
                        stm.executeUpdate(recapitoMassaggiatore.getInsertQuery());
                        stm.executeUpdate(new RecapitoMassaggiatore(R, recapitoMassaggiatore.getMassaggiatore())
                                .getInsertQuery());
                        break;

                    case 1:
                        // Due massaggiatori un telefono
                        RecapitoMassaggiatore recapitoMassaggiatore1 = new RecapitoMassaggiatore(R);
                        massaggiatori.add(recapitoMassaggiatore1.getMassaggiatore());
                        stm.executeUpdate(recapitoMassaggiatore1.getInsertQuery());

                        // Secondo Massaggiatore con lo stesso numero di telefono del primo
                        RecapitoMassaggiatore recapitoMassaggiatore2 = new RecapitoMassaggiatore(
                                recapitoMassaggiatore1.getTelefono(), R);
                        massaggiatori.add(recapitoMassaggiatore2.getMassaggiatore());
                        stm.executeUpdate(recapitoMassaggiatore2.getInsertQuery());
                        break;

                    default:
                        // Un Massaggiatore un telefono
                        RecapitoMassaggiatore recapitoMassaggiatore3 = new RecapitoMassaggiatore(R);
                        massaggiatori.add(recapitoMassaggiatore3.getMassaggiatore());
                        stm.executeUpdate(recapitoMassaggiatore3.getInsertQuery());
                }
            }

            // Popolamento Specializzazione
            System.out.println("Popolamento Specializzazione");
            for (String massaggiatore : massaggiatori) {
                tipimassaggio.sort((a, b) -> 1 - R.nextInt(2) * 2);
                for (int i = 0; i < 2 + R.nextInt(4); i++) {
                    Specializzazione specializzazione = new Specializzazione(massaggiatore, tipimassaggio.get(i));
                    stm.executeUpdate(specializzazione.getInsertQuery());
                }
            }

            // Popolamento Massaggiatore
            System.out.println("Popolamento Massaggiatore");
            for (String massaggiatore : massaggiatori) {
                stm.executeUpdate(new Massaggiatore(R, massaggiatore).getInsertQuery());
            }
            conn.commit();

            conn.setAutoCommit(true);

            // Popolamento Massaggio
            System.out.println("Popolamento Massaggio");
            int totalCounter = 0;
            for (String cliente : clienti) {
                // Circa 10 massaggi a cliente
                int massaggiACliente = 8 + R.nextInt(5);
                for (int j = 0; j < massaggiACliente; j++) {
                    Massaggio massaggio = new Massaggio(R, cliente,
                            tipimassaggio.get(totalCounter % tipimassaggio.size()),
                            LocalDate.now().plusDays((totalCounter % 365) + 1));
                    totalCounter++;
                    stm.executeUpdate(massaggio.getInsertQuery());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
