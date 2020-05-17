-- Cancellazione dell'utente usato per popolare se esiste
DO $$
	DECLARE 
		count int;
	BEGIN
		SELECT count(*) INTO count FROM pg_roles WHERE rolname = 'tonino';
		IF count > 0 THEN
			EXECUTE 'REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM tonino;';
			EXECUTE 'DROP USER  IF EXISTS tonino';
		END IF;
END $$;

-- Cancellazione le tabelle se esistono
DROP TABLE IF EXISTS Massaggio 				CASCADE;
DROP TABLE IF EXISTS RecapitoMassaggiatore 	CASCADE;
DROP TABLE IF EXISTS RecapitoCliente 		CASCADE;
DROP TABLE IF EXISTS RecapitoReceptionist 	CASCADE;
DROP TABLE IF EXISTS Specializzazione 		CASCADE;
DROP TABLE IF EXISTS TipoMassaggio 			CASCADE;
DROP TABLE IF EXISTS Cliente 				CASCADE;
DROP TABLE IF EXISTS Sala 					CASCADE;
DROP TABLE IF EXISTS Macchinario 			CASCADE;
DROP TABLE IF EXISTS Receptionist 			CASCADE;
DROP TABLE IF EXISTS Massaggiatore 			CASCADE;

-- Creazione tabelle
CREATE TABLE Cliente (
	CodiceFiscale 	CHAR(16) 	PRIMARY KEY CHECK( CHAR_LENGTH(CodiceFiscale) = 16 ),
	Cognome 		VARCHAR(20) NOT NULL,
	Nome 			VARCHAR(20) NOT NULL
);


CREATE TABLE Sala(
	NumeroSala 		VARCHAR(20) PRIMARY KEY,
	NumeroLettini 	SMALLINT 	NOT NULL	CHECK( NumeroLettini >= 0 )
);


CREATE TABLE Macchinario(
	Tipo 		VARCHAR(20) 	PRIMARY KEY,
	Quantita 	SMALLINT 		NOT NULL 	CHECK( Quantita >= 0 )
);


CREATE TABLE Receptionist(
	CodiceFiscale 	CHAR(16) 		PRIMARY KEY    CHECK( CHAR_LENGTH(CodiceFiscale) = 16 ),
	Cognome 		VARCHAR(20) 	NOT NULL,
	Nome 			VARCHAR(20) 	NOT NULL,
	via 			VARCHAR(20) 	NOT NULL,
	CAP 			CHAR(5) 		NOT NULL 		CHECK( CHAR_LENGTH(CAP) = 5 ),
	Citta 			VARCHAR(20) 	NOT NULL,
	Stipendio 		DECIMAL(8,2) 	NOT NULL
);


CREATE TABLE Massaggiatore(
	CodiceFiscale 	CHAR(16) 		PRIMARY KEY    CHECK( CHAR_LENGTH(CodiceFiscale) = 16 ),
	Cognome 		VARCHAR(20) 	NOT NULL,
	Nome 			VARCHAR(20) 	NOT NULL,
	Via 			VARCHAR(20) 	NOT NULL,
	CAP 			CHAR(5) 		NOT NULL 		CHECK( CHAR_LENGTH(CAP) = 5 ),
	Citta 			VARCHAR(20) 	NOT NULL,
	StipendioBase 	DECIMAL(8,2)	NOT NULL
);


CREATE TABLE TipoMassaggio(
	Tipo 			VARCHAR(20) 	PRIMARY KEY,
	Prezzo 			DECIMAL(8,2) 	NOT NULL,
	Durata 			SMALLINT 		NOT NULL,
	Macchinario 	VARCHAR(20) 	NULL,
	
	FOREIGN KEY (Macchinario) 	   REFERENCES Macchinario(Tipo)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
);


CREATE TABLE Specializzazione(
	Massaggiatore CHAR(16) 		NOT NULL 		CHECK( CHAR_LENGTH(Massaggiatore) = 16 ),
	TipoMassaggio VARCHAR(20) 	NOT NULL,
	
	PRIMARY KEY (Massaggiatore, TipoMassaggio),
	FOREIGN KEY (Massaggiatore) 	REFERENCES Massaggiatore(CodiceFiscale)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
		DEFERRABLE INITIALLY DEFERRED,
	FOREIGN KEY (TipoMassaggio) 	REFERENCES TipoMassaggio(Tipo)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
);


CREATE TABLE RecapitoCliente(
	Cliente 	CHAR(16) 		NOT NULL 		CHECK( CHAR_LENGTH(Cliente) = 16 ),
	Telefono 	VARCHAR(13) 	NOT NULL,
	
	PRIMARY KEY (Cliente, Telefono),
	FOREIGN KEY (Cliente) 		REFERENCES Cliente(CodiceFiscale)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
		DEFERRABLE INITIALLY DEFERRED
);


CREATE TABLE RecapitoMassaggiatore(
	Massaggiatore 	CHAR(16) 		 NOT NULL 		CHECK( CHAR_LENGTH(Massaggiatore) = 16 ),
	Telefono 		VARCHAR(13) 	 NOT NULL,
	
	PRIMARY KEY (Massaggiatore, Telefono),
	FOREIGN KEY (Massaggiatore)		REFERENCES Massaggiatore(CodiceFiscale)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
		DEFERRABLE INITIALLY DEFERRED
);


CREATE TABLE RecapitoReceptionist(
	Receptionist 	CHAR(16) 		NOT NULL 	CHECK( CHAR_LENGTH(Receptionist) = 16 ),
	Telefono 		VARCHAR(13) 	NOT NULL,
	
	PRIMARY KEY (Receptionist, Telefono),
	FOREIGN KEY (Receptionist)		REFERENCES Receptionist(CodiceFiscale)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
		DEFERRABLE INITIALLY DEFERRED
);


CREATE TABLE Massaggio(
	Cliente 			CHAR(16) 		NOT NULL 	CHECK( CHAR_LENGTH(Cliente) = 16 ),
	DataMassaggio 		DATE 			NOT NULL    CHECK( DataMassaggio > CURRENT_DATE OR DataMassaggio = CURRENT_DATE AND OraInizio > CURRENT_TIME ),
	OraInizio 			TIME 			NOT NULL	CHECK( OraInizio >=  TIME '09:00:00' ),
	OraFine 			TIME 			NOT NULL	CHECK( OraFine <= TIME '21:00:00' ),
	DataPrenotazione 	TIMESTAMP 		NOT NULL,
	Massaggiatore 		CHAR(16) 		NOT NULL 	CHECK( CHAR_LENGTH(Massaggiatore) = 16 ),
	Sala 				VARCHAR(20) 	NOT NULL,
	TipoMassaggio 		VARCHAR(20) 	NOT NULL,
	
	PRIMARY KEY (Cliente, DataMassaggio, OraInizio),
	FOREIGN KEY (Massaggiatore)		REFERENCES Massaggiatore(CodiceFiscale)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (Sala)				REFERENCES Sala(NumeroSala)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (TipoMassaggio)		REFERENCES TipoMassaggio(Tipo)
		ON DELETE CASCADE 
		ON UPDATE CASCADE
);


-- Creazione View per la gestione della tabella massaggio
CREATE OR REPLACE VIEW Prenotazione AS 
SELECT Cliente, DataMassaggio, OraInizio, TipoMassaggio
FROM Massaggio;



/*
	Prima di inserire un nuovo cliente, massaggiatore o receptionist verifica che gli sia già stato attribuito un recapito telefonico.
	Nel caso di un massaggiatore verifica inoltre che gli sia stata attribuita almeno una specializzazione.
	Se la verifica non va a buon fine impedisce l'inserimento.
*/
CREATE OR REPLACE FUNCTION inserimento_persona() RETURNS TRIGGER AS $$
    BEGIN    
        IF TG_TABLE_NAME = 'cliente' THEN
            IF (SELECT count(*) FROM recapitoCliente R where NEW.CodiceFiscale = R.Cliente) = 0 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il cliente deve avere almeno un recapito telefonico';
            END IF;
            
        ELSIF TG_TABLE_NAME = 'massaggiatore' THEN
            IF (SELECT count(*) FROM recapitoMassaggiatore R where NEW.CodiceFiscale = R.Massaggiatore) = 0 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il massaggiatore deve avere almeno un recapito telefonico';
            END IF;
			IF (SELECT count(*) FROM specializzazione S where NEW.CodiceFiscale = S.Massaggiatore) = 0 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il massaggiatore deve avere almeno una specializzazione';
            END IF;
            
        ELSE 
            IF (SELECT count(*) FROM recapitoReceptionist R WHERE NEW.CodiceFiscale = R.Receptionist) = 0 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il receptionist deve avere almeno un recapito telefonico';
            END IF;
            
        END IF;
		RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER inserisci_cliente
BEFORE INSERT ON cliente
FOR EACH ROW
EXECUTE PROCEDURE inserimento_persona();

CREATE TRIGGER inserisci_massaggiatore
BEFORE INSERT ON massaggiatore
FOR EACH ROW
EXECUTE PROCEDURE inserimento_persona();

CREATE TRIGGER inserisci_receptionist
BEFORE INSERT ON receptionist
FOR EACH ROW
EXECUTE PROCEDURE inserimento_persona();


/*
	Alla modifica (o cancellazione) di un recapito telefonico, se l'oggetto della modifica è il proprietario del recapito, 
	verifica che tale proprietario possieda almeno un altro recapito telefonico, altrimenti impedisce l'operazione.
*/
CREATE OR REPLACE FUNCTION modifica_recapiti() RETURNS TRIGGER AS $$
    BEGIN    
        IF TG_TABLE_NAME = 'recapitocliente' THEN
            IF (SELECT count(*) FROM recapitoCliente R where OLD.Cliente = R.Cliente) = 1 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il cliente deve avere almeno un recapito telefonico';
            END IF;
            
        ELSIF TG_TABLE_NAME = 'recapitomassaggiatore' THEN
            IF (SELECT count(*) FROM recapitoMassaggiatore R where OLD.Massaggiatore = R.Massaggiatore) = 1 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il massaggiatore deve avere almeno un recapito telefonico';
            END IF;
            
        ELSE 
            IF (SELECT count(*) FROM recapitoReceptionist R WHERE OLD.Receptionist = R.Receptionist) = 1 THEN
                RAISE EXCEPTION 'Impossibile eseguire l operazione: il receptionist deve avere almeno un recapito telefonico';
            END IF;
            
        END IF;
		IF TG_OP = 'DELETE' THEN
        	RETURN OLD;
		ELSE
			RETURN NEW;
		END IF;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER modifica_recapito_cliente
BEFORE UPDATE OF cliente OR DELETE ON recapitocliente
FOR EACH ROW
EXECUTE PROCEDURE modifica_recapiti();

CREATE TRIGGER modifica_recapito_massaggiatore
BEFORE UPDATE OF massaggiatore OR DELETE ON recapitomassaggiatore
FOR EACH ROW
EXECUTE PROCEDURE modifica_recapiti();

CREATE TRIGGER modifica_recapito_receptionist
BEFORE UPDATE OF receptionist OR DELETE ON recapitoreceptionist
FOR EACH ROW
EXECUTE PROCEDURE modifica_recapiti();


/*
	Alla modifica della durata di un tipo massaggio verifica che la durata non venga aumentata.
	Se la durata viene aumentata verifica che non siano stati prenotati, per date future, altri massaggi di quel tipo.
	Se la verifica precedente non va a buon fine, per semplicità di implementazione, impedisce la modifica della durata.
	Se invece la modifica della durata consiste in una riduzione, aggiorna l'ora di fine di tutti i massaggi
	futuri con quel tipo di massaggio.
*/
CREATE OR REPLACE FUNCTION modifica_durata_tipomassaggio() RETURNS TRIGGER AS $$
    BEGIN    
        IF NEW.durata > OLD.durata THEN
            IF NEW.Tipo IN (
                SELECT TipoMassaggio FROM Massaggio WHERE
                DataMassaggio > current_date OR 
                DataMassaggio = current_date AND 
                OraInizio > current_time)
            THEN 
            	RAISE EXCEPTION 'Impossibile aumentare la durata del tipo di massaggio perchè 
sono stati prenotati altri massaggi per lo stesso tipo di massaggio';
            END IF;
        ELSE
            UPDATE Massaggio SET OraFine = OraInizio + interval '1m' * NEW.durata
            WHERE TipoMassaggio = NEW.Tipo AND (
            DataMassaggio > current_date OR 
            DataMassaggio = current_date AND 
            OraInizio > current_time);
        END IF;        
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;
				
CREATE TRIGGER modifica_durata_massaggio 
BEFORE UPDATE OF durata ON tipomassaggio
FOR EACH ROW
EXECUTE PROCEDURE modifica_durata_tipomassaggio();

/*
	Alla modifica (o cancellazione) di una specializzazione, 
	siccome per ogni massaggiatore deve esistere almeno una specializzazione,
	verifica che il vecchio massaggiatore abbia almeno un'altra specializzazione,
	altrimenti impedisce l'operazione.
	Se il massaggiatore in questione possiede almeno un'altra specializzazione si prova a sostituire
	tutti i massaggi futuri che coinvolgevano quel massaggiatore e di quel tipo di massaggio con un altro
	massaggiatore libero ed in grado di effettuare il tipo di massaggio necessario.
	Per ogni massaggio per cui non risulta disponibile nessun massaggiatore adatto verrà 
	mostrata una nota ed il massaggio sarà cancellato.
*/
CREATE OR REPLACE FUNCTION modifica_specializzazione() RETURNS TRIGGER AS $$
    DECLARE
        r massaggio%rowtype;
        MassaggiatoreLibero varchar;
    BEGIN 
		IF (SELECT count(*) FROM specializzazione S where OLD.massaggiatore = S.massaggiatore) = 0 THEN
        	RAISE EXCEPTION 'Impossibile eseguire l operazione, il massaggiatore deve avere almeno una specializzazione';
        END IF;
        
        FOR r IN
            SELECT * FROM Massaggio 
            WHERE Massaggiatore = OLD.massaggiatore
            AND TipoMassaggio = OLD.TipoMassaggio AND (
            datamassaggio > current_date
            OR datamassaggio = current_date
            AND oraInizio > current_time)
        LOOP
            SELECT Massaggiatore INTO MassaggiatoreLibero FROM (
                SELECT S.Massaggiatore FROM Specializzazione S
                WHERE S.TipoMassaggio = r.TipoMassaggio
                EXCEPT
                SELECT M.Massaggiatore FROM Massaggio M
                WHERE M.datamassaggio = r.datamassaggio 
				AND M.oraFine>r.oraInizio AND M.oraInizio<r.oraFine ) AS foo
            ORDER BY random() LIMIT 1;

            IF FOUND THEN
                UPDATE Massaggio SET Massaggiatore = MassaggiatoreLibero
                WHERE DataMassaggio = r.DataMassaggio AND
                OraInizio = r.OraInizio AND
                Cliente = r.Cliente;
            ELSE
                RAISE NOTICE 'Non è stato possibile sostituire il massaggiatore. La prenotazione del cliente % per il giorno % alle ore %
sarà cancellata', r.cliente, r.datamassaggio, r.orainizio;
                DELETE FROM Massaggio M 
                WHERE M.DataMassaggio = r.DataMassaggio 
                AND M.OraInizio = r.OraInizio 
                AND M.Cliente = r.Cliente;
            END IF;
        END LOOP;
        IF TG_OP = 'DELETE' THEN
        	RETURN OLD;
		ELSE
			RETURN NEW;
		END IF;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER modifica_specializzazione
AFTER UPDATE OR DELETE ON specializzazione
FOR EACH ROW 
EXECUTE PROCEDURE modifica_specializzazione();


/*
	Alla cancellazione del massaggiatore prova a sostituire tutti i massaggi futuri che questo massaggiatore 
	avrebbe dovuto eseguire con un altro massaggiatore libero ed in grado di effettuare il tipo di massaggio necessario.
	Per ogni massaggio per cui non risulta disponibile nessun massaggiatore adatto verrà 
	mostrata una nota ed il massaggio sarà cancellato.
*/
CREATE OR REPLACE FUNCTION cancellazione_massaggiatore() RETURNS TRIGGER AS $$
    DECLARE
        r massaggio%rowtype;
        MassaggiatoreLibero varchar;
    BEGIN
		
        FOR r IN
            SELECT * FROM Massaggio 
            WHERE Massaggiatore = OLD.codicefiscale 
				  AND (DataMassaggio > current_date OR 
                  DataMassaggio = current_date AND 
                  OraInizio > current_time)
        LOOP
            SELECT Massaggiatore INTO MassaggiatoreLibero FROM (
                SELECT S.Massaggiatore FROM Specializzazione S
                WHERE S.TipoMassaggio = r.TipoMassaggio
                EXCEPT
                SELECT M.Massaggiatore FROM Massaggio M
                WHERE M.datamassaggio = r.datamassaggio 
				AND M.oraFine>r.oraInizio AND M.oraInizio<r.oraFine ) AS foo
            ORDER BY random() LIMIT 1;

            IF FOUND THEN
                UPDATE Massaggio SET Massaggiatore = MassaggiatoreLibero
                WHERE DataMassaggio = r.DataMassaggio AND
                OraInizio = r.OraInizio AND
                Cliente = r.Cliente;
            ELSE
                RAISE NOTICE 'Non è stato possibile sostituire il massaggiatore. La prenotazione del cliente % per il giorno % alle ore %
sarà cancellata', r.cliente, r.datamassaggio, r.orainizio;
            END IF;
        END LOOP;
        
        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cancella_massaggiatore 
AFTER DELETE ON massaggiatore 
FOR EACH ROW 
EXECUTE PROCEDURE cancellazione_massaggiatore();


/*
	Alla cancellazione di una sala prova a sostituire tutte i massaggi futuri previsti in quella sala 
	e prova a sostituirla con un'altra sala in cui ci sono lettini liberi.
	Per ogni massaggio per cui non risulta disponibile nessun lettino (e quindi nessuna sala)
	verrà mostrata una nota ed il massaggio sarà cancellato.
*/
CREATE OR REPLACE FUNCTION cancellazione_sala() RETURNS TRIGGER AS $$
    DECLARE
        r massaggio%rowtype;
        SalaLibera varchar;
    BEGIN    
        
        FOR r IN
            SELECT * FROM Massaggio 
            WHERE Sala = OLD.numerosala
				  AND (DataMassaggio > current_date OR 
                  DataMassaggio = current_date AND 
                  OraInizio > current_time)
        LOOP
            SELECT S.NumeroSala INTO SalaLibera FROM Sala S
            WHERE S.NumeroLettini > 0 AND S.numeroSala NOT IN (SELECT DISTINCT sala FROM Massaggio)
				  OR S.NumeroLettini > (
				  SELECT count(*) FROM Massaggio M
				  WHERE M.datamassaggio = r.datamassaggio 
				  AND M.orafine > r.orainizio AND M.orainizio < r.oraFine
				  AND M.Sala = S.numeroSala)
			ORDER BY random() LIMIT 1;

            IF FOUND THEN
                UPDATE Massaggio SET Sala = SalaLibera
                WHERE DataMassaggio = r.DataMassaggio AND
                OraInizio = r.OraInizio AND
                Cliente = r.Cliente;
			ELSE
				RAISE NOTICE 'Non è stato possibile sostituire la sala. La prenotazione del cliente % per il giorno % alle ore %
sarà cancellata', r.cliente, r.datamassaggio, r.orainizio;
            END IF;
        END LOOP;
        
        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cancella_sala 
AFTER DELETE ON sala 
FOR EACH ROW 
EXECUTE PROCEDURE cancellazione_sala();


/*
	Alla modifica della quantità dei lettini di una sala verifica che la modifica non consista in una riduzione.
	Se la modifica consiste in una riduzione del numero dei lettini di una sala cerca di riposizionare i massaggi
	in altre sale libere.
	Qualora non fosse possibile impedisce la modifica.
*/
CREATE OR REPLACE FUNCTION modifica_quantita_lettini() RETURNS TRIGGER AS $$
    DECLARE
        counter int;
        r RECORD;
        t RECORD;
        SalaLibera varchar;
    BEGIN    
        IF NEW.numerolettini < OLD.numerolettini THEN
            EXECUTE format(
                'CREATE TABLE IF NOT EXISTS MASSAGGI_CONTEMPORANEI (
                    cliente CHAR(16),
                    "data" DATE,
                    orainizio TIME,
                    orafine TIME,
                    PRIMARY KEY(cliente, data, orainizio, orafine)
                );'
            );
			
			/*
				DATAMASSAGGIO, ORAINIZIO, ORAFINE
				DATA,           ORA,                 TIPO
				DATAMASSAGGIO,  ORAINIZIO o ORAFINE, -1(ORAINIZIO) o +1(ORAFINE)
				ORDINO PER DATA e se è uguale
				ORDINO PER ORA
				SE è un'ora di inizio sottraggo 1, altrimenti sommo 1
				Se vado sotto 0 ritorno un'eccezione

				In questo modo individuo per ogni istante il numero di macchinari/lettini/massaggiatori liberi
				a seconda del filtro applicato ai massaggi selezionati.
			*/
            counter := NEW.numerolettini;
            FOR r IN 
                SELECT cliente, datamassaggio "data", OraInizio AS Ora, OI AS Orainizio, OraFine, tipo FROM (
                    SELECT Cliente, DataMassaggio, OraInizio, OraInizio AS OI, OraFine, -1 tipo FROM Massaggio 
                    WHERE Sala = NEW.NumeroSala 
						  AND (DataMassaggio > current_date OR 
						  DataMassaggio = current_date AND 
					      OraInizio > current_time)
                    UNION ALL
                    SELECT Cliente, DataMassaggio, OraFine, OraInizio, OraFine, 1 tipo FROM Massaggio
                    WHERE Sala = NEW.NumeroSala
						  AND (DataMassaggio > current_date OR 
                  	      DataMassaggio = current_date AND 
                  		  OraInizio > current_time)
                    ) AS O
                ORDER BY "data", Ora
            LOOP
                counter := counter + r.Tipo;
                IF r.Tipo = -1 THEN
                    INSERT INTO MASSAGGI_CONTEMPORANEI VALUES (r.cliente, r.data, r.orainizio, r.orafine);
                ELSE
                    DELETE FROM MASSAGGI_CONTEMPORANEI MC WHERE MC.cliente = r.cliente 
                    AND MC.data = r.data
                    AND MC.orainizio = r.oraInizio
                    AND MC.orafine = r.oraFine;
                END IF;
				
                IF counter < 0 AND r.Tipo = -1 THEN
                    FOR t IN SELECT * FROM MASSAGGI_CONTEMPORANEI
                    LOOP
                        SELECT S.NumeroSala INTO SalaLibera FROM Sala S
                        WHERE S.numeroSala <> NEW.numerosala AND 
							  S.NumeroLettini > (
							  SELECT count(*) FROM Massaggio M
							  WHERE M.datamassaggio = t.data
							  AND M.orafine > t.orainizio AND M.orainizio < t.oraFine
							  AND M.Sala = S.numeroSala)
                        ORDER BY random() LIMIT 1;

                        IF FOUND THEN
                            UPDATE Massaggio SET Sala = SalaLibera
                            WHERE DataMassaggio = t.Data AND
                            OraInizio = t.OraInizio AND
                            Cliente = t.Cliente;
							
							DELETE FROM MASSAGGI_CONTEMPORANEI MC WHERE MC.cliente = r.cliente 
							AND MC.data = r.data
							AND MC.orainizio = r.oraInizio
							AND MC.orafine = r.oraFine;
                            EXIT;
                        END IF;
                    END LOOP;
                    IF SalaLibera IS NULL THEN
                        EXECUTE format(
                            'DROP TABLE IF EXISTS MASSAGGI_CONTEMPORANEI;'
                        );
                
                        RAISE EXCEPTION 'Operazione non valida perché ci sono troppi massaggi prenotati collocati in questa sala.';
                    END IF;
                END IF;
            END LOOP;
        END IF;
        EXECUTE format(
            'DROP TABLE IF EXISTS NUOVA_TABELLA;'
        );
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_update_quantita_lettini
BEFORE UPDATE OF numerolettini ON sala
FOR EACH ROW
EXECUTE PROCEDURE modifica_quantita_lettini();


/*
	Alla modifica della quantità di macchinari per un dato tipo di macchinario verifica che la modifica non consista in una riduzione.
	Se la modifica consiste in una riduzione del numero dei macchinari verifica che questi siano ancora sufficienti
	per effettuare tutti i massaggi. 
	In caso contrario impedisce la modifica.
*/
CREATE OR REPLACE FUNCTION modifica_quantita_macchinari() RETURNS TRIGGER AS $$
    DECLARE
        counter int;
        r RECORD;
    BEGIN
        IF NEW.quantita < OLD.quantita THEN
            counter := NEW.quantita;
            FOR r IN 
                SELECT datamassaggio "data", orainizio ora, tipo FROM (
                    SELECT DataMassaggio, OraInizio, -1 tipo FROM Massaggio 
                        WHERE (DataMassaggio > current_date OR 
                  			  DataMassaggio = current_date AND 
                  	          OraInizio > current_time) AND tipomassaggio IN (
                            SELECT tipo FROM tipomassaggio 
                            WHERE tipomassaggio.macchinario = NEW.tipo)
                    UNION ALL
                    SELECT DataMassaggio, OraFine, 1 tipo FROM Massaggio
                        WHERE (DataMassaggio > current_date OR 
                  			  DataMassaggio = current_date AND 
                  			  OraInizio > current_time) AND tipomassaggio IN (
                            SELECT tipo FROM tipomassaggio 
                            WHERE tipomassaggio.macchinario = NEW.tipo)
                    ) AS O
                ORDER BY "data", ora
            LOOP
                counter := counter + r.Tipo;
                IF counter < 0 THEN
                    RAISE EXCEPTION 'Operazione non valida perché ci sono troppi massaggi prenotati che utilizzano il macchinario.'; 
                END IF;
            END LOOP;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_update_quantita_macchinario
BEFORE UPDATE OF quantita ON macchinario
FOR EACH ROW
EXECUTE PROCEDURE modifica_quantita_macchinari();

/*
	All'inserimento (o alla modifica del codice fiscale) di un cliente, massaggiatore o receptionist verifica 
	che non esista un altro cliente, massaggiatore o receptionist con lo stesso codice fiscale.
	
	Il vincolo tra massaggiatore e receptionist deriva dalla tecnica utilizzata per tradurre la gerarchia.
	Il vincolo tra cliente e dipendente (quindi massaggiatore e recepitionist) deriva dalla combinazione di
	due ulteriori vincoli aggiuntivi:
		"Se un cliente è anche un dipendente allora non può prenotare un massaggio durante il suo orario di lavoro."
		"Gli orari di lavoro di tutti i dipendenti coincidono con gli orari di apertura del centro massaggi."
*/
CREATE OR REPLACE FUNCTION check_codice_fiscale() RETURNS TRIGGER AS $$
    DECLARE
        temp1 int;
        temp2 int;
        temp3 int;
    BEGIN
        SELECT count(*) INTO temp1 FROM Cliente
        WHERE codicefiscale = NEW.codicefiscale;
        SELECT count(*) INTO temp2 FROM Massaggiatore
        WHERE codicefiscale = new.codicefiscale;
        SELECT count(*) INTO temp3 FROM Receptionist
        WHERE codicefiscale = new.codicefiscale;
        IF (temp1 + temp2 + temp3 > 0) THEN
            RAISE EXCEPTION 'Codice Fiscale già presente';
        END IF;
        RETURN NEW;
    END; 
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_insert_codice_fiscale_cliente
BEFORE INSERT OR UPDATE OF codicefiscale ON cliente
FOR EACH ROW
EXECUTE PROCEDURE check_codice_fiscale();

CREATE TRIGGER check_insert_codice_fiscale_massaggiatore
BEFORE INSERT OR UPDATE OF codicefiscale ON massaggiatore
FOR EACH ROW
EXECUTE PROCEDURE check_codice_fiscale();

CREATE TRIGGER check_insert_codice_fiscale_receptionist
BEFORE INSERT OR UPDATE OF codicefiscale ON receptionist
FOR EACH ROW
EXECUTE PROCEDURE check_codice_fiscale();
	

/*
	All'inserimento di una prenotazione (cioè di soli cliente, dataMassaggio, oraInizio, tipoMassaggio),
	dopo aver verificato la correttezza della richiesta calcola i campi mancanti per l'inserimento in massaggio.
	(Si ricorda che prenotazione è una vista basata su massaggio)
	
	oraFine = oraInizio + durata (ottenuta dalla tabella tipoMassaggio)
	dataPrenotazione = data e ora correnti (timestamp)
	Massaggiatore = un massaggiatore libero nella fascia oraria desiderata in grado di effettuare quel tipo di massaggio richiesto
	Sala = una sala con sufficienti lettini liberi nella fascia oraria desiderata
	Macchinario = un macchinario libero nella fascia oraria desiderata
*/
CREATE OR REPLACE FUNCTION prenota() RETURNS TRIGGER AS $$
    DECLARE
	 -- NEW = cliente, dataMassaggio, oraInizio, tipoMassaggio
		durata int;
        NewOraFine time;
        MassaggiatoreLibero varchar;
        MacchinarioRichiesto varchar;
        SalaLibera varchar;
    BEGIN
        -- Selezione durata del tipoMassaggio
		SELECT tipomassaggio.durata INTO durata FROM tipomassaggio WHERE tipo = NEW.tipomassaggio;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Il tipo massaggio indicato non è disponibile';
        END IF;

        -- Calcola l'ora di fine
        NewOraFine := NEW.orainizio + interval '1m' * durata;


        -- Verifica massaggiatori disponibili per tutta la durata

        -- Seleziona tutti i massaggiatori che sanno fare il tipo massaggio richiesto
        SELECT Risultato.massaggiatore INTO massaggiatoreLibero from (
            SELECT S.massaggiatore FROM Specializzazione S WHERE S.tipomassaggio = NEW.tipomassaggio
            EXCEPT
            -- Seleziona tutti i massaggiatori che sono impegnati in quel giorno in quell intervallo orario
            SELECT M.massaggiatore FROM Massaggio M
            WHERE M.dataMassaggio = NEW.dataMassaggio AND 
            M.orafine > NEW.orainizio AND M.orainizio < NewOraFine) AS foo
        ORDER BY random() LIMIT 1;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Non c è nessun massaggiatore disponibile';
        END IF;
        
        -- Verifica se è richiesto il macchinario per quel tipo massaggio
        SELECT T.macchinario INTO MacchinarioRichiesto FROM TipoMassaggio T
      	WHERE T.tipo = NEW.tipomassaggio;  
        
		
        IF MacchinarioRichiesto IS NOT NULL THEN
            -- Verifica macchinari disponibili per tutta la durata
            -- Prendi tutti i macchinari la cui quantità è maggiore delle volte che sono usati nell'intervallo orario desiderato
            PERFORM macchinario.tipo FROM macchinario 
            WHERE macchinario.tipo = MacchinarioRichiesto 
            AND macchinario.quantita > 
            -- Le volte che lo stesso macchinario è usato
                (select count(*) FROM massaggio M 
                -- Nel giorno scelto
                WHERE M.datamassaggio = NEW.datamassaggio 
                -- È impegnato in quell'ora
                AND M.orafine > NEW.orainizio AND M.orainizio < NewOraFine
                -- Il macchinario utilizzato è quello richiesto
                -- Il tipo massaggio prevede di utilizzare il macchinario richiesto
                AND M.tipomassaggio IN (
                    SELECT tipo FROM tipomassaggio 
                    WHERE tipomassaggio.macchinario = MacchinarioRichiesto)
                );
            IF NOT FOUND THEN
                RAISE EXCEPTION 'Non c è nessun macchinario disponibile';
            END IF;
        END IF;

        -- Verifica sale con lettini disponibili per tutta la durata
        -- Prendi tutte le sale i cui lettini sono maggiori di quelli usati nell'intervallo orario desiderato
        SELECT sala.numeroSala INTO SalaLibera FROM sala
        WHERE sala.numeroLettini > 
        (   -- Sale usate nell'intervallo desiderato
            SELECT count(*) FROM Massaggio M
            WHERE M.datamassaggio = NEW.datamassaggio 
            AND M.orafine > NEW.orainizio AND M.orainizio < NewOraFine
            AND M.Sala = sala.numeroSala
        )
        ORDER BY random() LIMIT 1;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Non c è nessuna sala disponibile';
        END IF;                

        -- Verifica cliente non impegnato in quell'intervallo
        PERFORM M.cliente FROM massaggio M
        WHERE M.cliente = NEW.cliente
        AND M.datamassaggio = NEW.datamassaggio 
        AND M.orafine > NEW.orainizio AND M.orainizio < NewOraFine;

        IF FOUND THEN
            RAISE EXCEPTION 'Il cliente è già impegnato';
        END IF; 

        IF TG_OP = 'INSERT' THEN
		    INSERT INTO Massaggio
            VALUES(NEW.Cliente, NEW.DataMassaggio, NEW.OraInizio, NewOraFine, now(), massaggiatoreLibero, Salalibera, NEW.TipoMassaggio);
        ELSE
            UPDATE Massaggio SET 
                Cliente = NEW.Cliente,
                DataMassaggio = NEW.DataMassaggio,
                OraInizio = NEW.OraInizio,
                OraFine = NewOraFine,
                dataPrenotazione = now(),
                massaggiatore = massaggiatoreLibero,
                Sala = Salalibera,
                TipoMassaggio = NEW.TipoMassaggio
            WHERE 
                Cliente = OLD.Cliente AND
                DataMassaggio = OLD.DataMassaggio AND
                OraInizio = OLD.OraInizio AND
                OraFine = OLD.OraFine;                
		END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER richiesta_massaggio 
INSTEAD OF INSERT OR UPDATE ON prenotazione 
FOR EACH ROW 
EXECUTE PROCEDURE prenota();
	
CREATE USER tonino WITH PASSWORD 'Pippo';
GRANT ALL PRIVILEGES ON Prenotazione 			TO tonino;
GRANT ALL PRIVILEGES ON Massaggio 				TO tonino;
GRANT ALL PRIVILEGES ON RecapitoMassaggiatore 	TO tonino;
GRANT ALL PRIVILEGES ON RecapitoCliente 		TO tonino;
GRANT ALL PRIVILEGES ON RecapitoReceptionist 	TO tonino;
GRANT ALL PRIVILEGES ON Specializzazione 		TO tonino;
GRANT ALL PRIVILEGES ON TipoMassaggio 			TO tonino;
GRANT ALL PRIVILEGES ON Cliente 				TO tonino;
GRANT ALL PRIVILEGES ON Sala 					TO tonino;
GRANT ALL PRIVILEGES ON Macchinario 			TO tonino;
GRANT ALL PRIVILEGES ON Receptionist 			TO tonino;
GRANT ALL PRIVILEGES ON Massaggiatore 			TO tonino;
