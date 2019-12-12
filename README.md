Ergänzung P6:
Für kleine Instanzen n<=30 reichen bereits wenig Iterationen als Abbruch-
kriterium aus um eine hinreichend zufriedenstellende Lösung zu finden. Für 
größere Instanzen liefert hohe Laufzeit (erwartungsgemäß) tendenziell 
bessere Ergebnisse (n>=100). Falls hier auch unzulässige Lösungen generiert 
werden, ist das Endergebnis eher schlechter i.V. zu nur zulässigen Lösungen 
(wahrscheinlich werden mehr Iterationen benötigt). Das Speichern von ganzen 
Lösungen in der TabuListe verbraucht sehr viel Speicher und sollte vermieden
werden (hier Speichern von "geflippten" Bits des Lösungsvektors). Für die
Parameter gilt: Cooldown f abhängig von Instanz wählen, je größer die 
Instanz desto größer der cooldown (Richtwert f=n/10 aber nicht f<3). 
