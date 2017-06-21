Library used for CSV parsing:
http://opencsv.sourceforge.net/

 CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
 String [] nextLine;
 while ((nextLine = reader.readNext()) != null) {
    // nextLine[] is an array of values from the line
    System.out.println(nextLine[0] + nextLine[1] + "etc...");
 }
     
CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
List myEntries = reader.readAll();  

     CSVWriter writer = new CSVWriter(new FileWriter("yourfile.csv"), '\t');
     // feed in your array (or convert your data to an array)
     String[] entries = "first#second#third".split("#");
     writer.writeNext(entries);
	 writer.close();
	 
	    