#Taks

##Elastic Search Integration

ElasticSearch soll Logeinträge indzieren. Hauptgrund hierfür ist die spätere  Verwendung von Kibana zur Visualisierung.

Der Logfileservice soll nach jedem Parselauf die aktuell gefundenen Einträge in eine Queue schreiben (zusätzlich
zum Eintragen in eine DB). Dort horcht jeder mit, der an den Logeinträgen interessiert ist.

* GET Route, die eine Liste von IDs entgegennimmt und die entsprechenden Logs aus der DB liefert
* Nach jedem täglichen Import, die frischen LogIDs in die Queue schreiben