# Progetto centromassaggi

Repository pubblica per il progetto di basi di dati del gruppo 07 sulla realtà di un centro massaggi.

### Caro prof,

se sta visitando questa repository vuol probabilmente dire che sta apprezzando il nostro lavoro e ci fa molto piacere :).

## Installazione

Creare un database locale con PostgreSQL chiamato "centromassaggi".

Avviare lo script di creazione presente nel file nella root del progetto chiamato "scriptcreazione.sql".

Clonare il progetto direttamente su netbeans come mostrato di seguito

Cliccare su Team -> Git -> Clone...

![](/images/Primo.png)

Inserire il link di questa repository come di seguito.
NOTA: Non è necessario effettuare il login.

![](images/Secondo.png)

Cliccare su next.

![](images/Terzo.png)

Scegliere un percorso nel quale salvare il progetto di NetBeans o lasciare quello di default.
Cliccare su Finish.

![](images/Quarto.png)

Cliccare su Open Project.

![](images/Quinto.png)

Verrà mostrato un errore in quanto il collegamento al DriverManager non risulta valido.
Cliccare su Resolve Problems...

![](images/Sesto.png)

Poi ancora su Resolve...

![](images/Settimo.png)

Selezionare il Driver per Postgres tramite l'explorer e confermare.

![](images/Ottavo.png)

A questo punto il problema dovrebbe essere risolto.
Cliccare su close.

![](images/Nono.png)

## Source tree

Il source tree si presenta come segue.

![](images/Decimo.png)

Nel package principale "centromassaggi" sono presenti due file .java eseguibili che si interfacceranno con il database postgres.
Le configurazioni del database possono essere modificate nel file CustomManager.java in "centromassaggi.common".

Qui è possibile specificare il link di connessione a Postgres, l'utente e la password.

Per semplicità è sufficiente creare sulla macchina locale un database chiamato "centromassaggi" ed inizializzarlo con lo script che si trova nel file "scriptcreazione.sql" nella root di questa repository.

Successivamente è possibile eseguire il file PopolaDatabase.java per eseguire il popolamento o Menu.java per provare le query implementate.

Buon divertimento!


