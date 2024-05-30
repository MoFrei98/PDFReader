# Aufgabe 20
## PDF Reader mit Filteroptionen 

### Beschreibung 
Grafischer PDF Reader mit Filteroptionen. 
Der Benutzer kann ein PDF Dokument auswählen, den Text des Dokuments extrahieren und diesen Filtern
#### Filteroptionen:
- Text (>= 2 Zeichen) + Case Sensitive (ja/Nein)
- Datum (Pattern: "\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}\\b")
- Uhrzeit (Pattern: "\\b\\d{1,2}:\\d{2}\\b")
- Preise (Pattern: "\\b\\d+([.,]\\d{2})?\\s*[€$]")

### Vorbereitung
Fügen Sie pdfbox-app-3.02.jar als Bibliothek hinzu (IntelliJ IDEA): 
1. File -> Project Structure -> Modules -> Dependencies -> + -> JARs or directories -> pdfbox-app-3.02.jar
2. Apply
3. OK
4. Build -> Build Project
5. Run

### Hinweise
Das Programm wird immer als erstes versuchen, die (alphabetisch) erste PDF Datei aus dem Order "files-to-read" (im Wurzelverzeichnis des Projekts) zu laden.
In diesem Ordner befinden sich bereits zwei PDF Dateien
- 1_TestPDF.pdf: Diese Datei enthält Text, Datum, Uhrzeit und Preise
- 20_Aufgabestellung (PDF Filter).pdf: Diese Datei enthält die Aufgabestellung für diese Arbeit

Über den Button "Select PDF" kann man allerdings jede beliebige PDF Datei laden