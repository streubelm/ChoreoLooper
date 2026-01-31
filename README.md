# ChoreoLooper #

*ChoreoLooper* ist eine Audiowiedergabe-App, die beim Üben von Freestyle-Küren unterstützen
soll. Inspiriert von [ReChoreo](https://app.goos.de/rechoreo.html)-App für iOS, entwickelt von Nina Herzog und Jonas Goos, erlaubt *ChoreoLooper* das 
wiederholte Abspielen von Ausschnitten aus der Kürmusik, mit anpassbaren Pausen vor und 
zwischen den Wiederholungen.

Außerdem ist es möglich, auch während des Abspielens Musikstellen zu markieren und zu diesen 
Markierungen oder zu ganzen Abschnitten eigene Notizen hinzuzufügen. Durch Funktionen zur
Navigation zwischen markierten Stellen wird es erleichtert, während des Trainings die gewünschte
Musikstelle zu finden.

Alle Musikausschnitte einschließlich der gewünschten Zahl an Wiederholungen und der Länge der Pausen,
alle Markierungen sowie die Notizen können in eine JSON-Datei exportiert werden, um das Training
später reibungslos an den gleichen Stellen fortzusetzen. Außerdem können die Kürdateien an 
andere Fahrer weitergegeben werden.


## Installation ##

Die App wird zeitnah im Google Play Store veröffentlicht. Bis dahin steht die jeweils aktuelle Version als APK-Paket unter [Releases](https://github.com/Maximilian57/ChoreoLooper/releases) zum Download bereit.


## Verwendung ##

Eine Übersicht der Oberläche mit Erklärungen zu den einzelnen Funktionen findet sich in der 
beiliegenden [Bedienungsanleitung](file://app/src/main/assets/manual.index), welche auch über die App abrufbar ist.


## Unterstützen ##

Diese App wird als reines Hobbyprojekt entwickelt, Fehler sind deshalb wahrscheinlich. Wenn Du einen Fehler gefunden hast, melde ihn gerne als Issue auf GitHub oder per E-Mail an [streubelmaximilian@gmail.com](mailto:streubelmaximilian@gmail.com). Außerdem freue ich mich über Feedback oder Verbesserungsvorschläge jeder Art.

Wenn Du bei der Entwicklung unterstützen willst, sei es mit Code, Übersetzungen, Icons oder Anderem, nimm gerne under obiger Adresse Kontakt auf. Auch Pull Requests sind gerne willkommen.


## Geplante Features ##

* In-App Speicher für Küren
* Nativer `MediaSessionService` zur Steuerung vom Sperrbildschirm aus
* Optisches Signal bei Wiedergabe einer markierten Stelle
* Nutzerdefinierte Farbmarkierungen / Tags für Sequenzen und Marken
* Filtern von Marken nach Farben / Tags


## Versionsverlauf ##

* **1.02.07**: Erstes öffentliches Release
  - Umstrukturierung des Hauptbereichs in mehrere Fragmente
  - Einstellungsdialoge verwenden intern Millisekunden
  - Kein String-parsing in Einstellungsdialogen
  - Verhindern von automatischem Standby
  - Reaktive Farben zur Unterstützung von Dark Modes
* **1.02.06**: Erstes (internes) Release
  - Richtiges Verhalten des Zurück-Buttons in HTML-Fragmenten
* **0.02.5**: Umstrukturierung in mehrere Fragmente
  - Menüleiste hinzugefügt
  - Anleitung und Über diese App hinzugefügt
  - Behebung von Layoutfehlern in Android >=15
* **0.02.4**: Visuelle Verbesserungen für intuitivere Nutzerführung
  - Inaktive und gedrückte Schaltflächen werden gekenzeichnet
  - Autogenerierte Sequenz wird markiert
  - Verbessertes Verhalten beim Bearbeiten der autogenerierten Sequenz
* **0.02.3**: Umgestaltung der Oberfläche für kürzere Interaktionspfade
  - Entfernen der Startseite
  - Integration der Modussteuerung und Informationsanzeige in die Wiedergabeleiste
  - Modussensitive Statusanzeige
* **0.02.2**: Fehlerbehebungen, intuitiveres Verhalten
  - Auswählen einer Markierung navigiert nur, wenn keine Wiedergabe aktiv ist
  - Möglicher Absturz beim Entfernen von Sequenzen behoben
* **0.02.1**: Mehrere Sequenzen, Markierungen
  - Vollständige Unstrukturierung der Oberfläche mit mehrseitigem Layout
  - Unterstützung für mehrere Sequenzen
  - Markierungen
  - Namen und Notizen für Sequenzen und Markierungen
  - Erweiterung des Dateiformats um die neuen Features aufzunehmen
  - Verbesserte Behandlung von feherhaften Audiolinks
  - Zusätzliche Navigationselemente zum Springen zwischen POIs
  - Darstellung von Bereichen und Marken in der Suchleiste
* **0.01.1**: Erste funktionale Version
  - Eine Sequenz
  - Dateiimport und -Export
