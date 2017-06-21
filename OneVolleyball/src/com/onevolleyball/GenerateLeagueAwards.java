/**
 * Reads the AllPlayer CSV file from Datavolley and construct the following files
 *  Best Points
 *  Best Attack
 *  Best Digs
 *  Best Setter
 *  Best Passing
 *  
 *  Runs the code in the same directory where the CSV files are
 */
package com.onevolleyball;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * @author xtofv
 *
 */
public class GenerateLeagueAwards {
	
	private static final Logger log = Logger.getLogger(GenerateLeagueAwards.class.getName());
		
	//private static String PATH = "C:\\Users\\xtofv\\Documents\\OneVolleyball\\Generate Overall Awards\\Input\\Men\\";
	//private static String PATH = "C:\\Users\\xtofv\\Documents\\OneVolleyball\\Generate Overall Awards\\Input\\Women\\";
	private static String PATH;
	private static String GENERATED_PATH;
	private static String FTP_SERVER;
	private static String USERID;
	private static String PASSWORD;
	private static String REMOTE_PATH;
	
	private static final int LIMIT = 10; // list top 10 only
	
	// Files to generate
	private static  String POINTS_FILE = "generated\\oneLeague_Points.csv";
	private static  String SERVES_FILE = "generated\\oneLeague_Serve.csv";
	private static  String KILL_FILE = "generated\\oneLeague_Kill.csv";
	private static  String DIGS_FILE = "generated\\oneLeague_Dig.csv";
	private static  String BLOCKS_FILE = "generated\\oneLeague_Block.csv";
	private static  String SETS_FILE = "generated\\oneLeague_Set.csv";
	private static  String PASS_FILE = "generated\\oneLeague_Passing.csv";

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws java.text.ParseException {
		
		log.setLevel(Level.ALL);
		
		// Verify all parameters
		parse(args);
		
		// obtain all Files of all skills
		List<File> allCSV = null;
		try {
			allCSV = getAllCSV();
		} catch (IOException e) {
			log.log(Level.WARNING,"Unable to read the files *.csv in the current directory.");
			e.printStackTrace();
		}
		
		//Parse all files into a static SET
		// name|team, shirt #, ...
		if (allCSV != null){
			generateAllAthletes(allCSV);

		
			// Generate all Files from the SET
			generatePoints();
			generateServe();
			generatePassing();
			generateDig();
			generateSet();
			generateBlock();
			generateAttacks();
			
			//ftp transfer
			transferFiles();
			
			log.log(Level.INFO," ******** ALL DONE ********");
		} else {
			log.log(Level.WARNING,"No CSV found. Nothing generated.");
		}

	}
	
	/*
	 * Parses command line
	 * PATH - of the CSV files
	 * LIMIT - top 10 players ?
	 */
	private static void parse(String[] args) {

		Options options = new Options();
		options.addOption( "help", "Print this message" );
		options.addOption( "p", "path", true, "Path where the CSV files are generated. The name of each file must be week<number>_<Team name>." );
		options.addOption( "f", "ftp", true, "FTP server to connect to" );
		options.addOption( "u", "uid", true, "Userid for the FTP site" );
		options.addOption( "w", "pwd", true, "Password for the FTP site" );
		options.addOption( "x", "fpath", true, "Path on the FTP server to push the files to." );
		try {
		    // parse the command line arguments
			CommandLineParser parser = new DefaultParser();
		    CommandLine line = parser.parse( options, args );
		    
		    if (line.hasOption('h')){
		    	log.log(Level.FINER,"-p PATH of CSV files -f FTP server -u userid -w password -x remote PATH on FTP server");
		    	help(options);
		    }
		    
		    if (line.hasOption('p')){
		    	log.log(Level.INFO, "Reading files from Path -p=" + line.getOptionValue("p"));
		    	PATH = line.getOptionValue("p");
		    	GENERATED_PATH = PATH + File.separator + "generated"+File.separator;
		    	File generateLocation = new File(GENERATED_PATH);
		    	if (!generateLocation.exists()){
		    		generateLocation.mkdir();
		    	}
		    	POINTS_FILE = GENERATED_PATH+"oneLeague_Points.csv";
		    	SERVES_FILE = GENERATED_PATH+"oneLeague_Serve.csv";
		    	KILL_FILE = GENERATED_PATH+"oneLeague_Kill.csv";
		    	DIGS_FILE = GENERATED_PATH+"oneLeague_Dig.csv";
		    	BLOCKS_FILE = GENERATED_PATH+"oneLeague_Block.csv";
		    	SETS_FILE = GENERATED_PATH+"oneLeague_Set.csv";
		    	PASS_FILE = GENERATED_PATH+"oneLeague_Passing.csv";
		    } else {
		    	log.log(Level.SEVERE,"Path to CSV files not recognized");
		    	help(options);
		    }
		    
		    if (line.hasOption('f')){
		    	log.log(Level.INFO, "FTP Server -f=" + line.getOptionValue("f"));
		    	FTP_SERVER = line.getOptionValue("f");
		    }
		    
		    if (line.hasOption('u')){
		    	log.log(Level.INFO, "Userid -u=" + line.getOptionValue("u"));
		    	USERID = line.getOptionValue("u");
		    }
		    
		    if (line.hasOption('w')){
		    	log.log(Level.INFO, "Password read");
		    	PASSWORD = line.getOptionValue("w");
		    }
		    
		    if (line.hasOption('x')){
		    	log.log(Level.INFO, "Remote PATH on FTP SERVER xp=" + line.getOptionValue("x"));
		    	REMOTE_PATH = line.getOptionValue("x");
		    }
		    
		    
		}
		catch( org.apache.commons.cli.ParseException exp ) {
		    log.log(Level.SEVERE, "Unexpected exception:" + exp.getMessage() );
		}
	}
	
	private static void help(Options options) {
		HelpFormatter formater = new HelpFormatter();
			formater.printHelp("Main", options);
			System.exit(0);
		}

	/*
	 * Create SERVE results CSV file
	 */
	private static void generateServe() {
		List<Athlete> sortedList = Athlete.sortServe(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(SERVES_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Aces","Error","Total"});
			for (int i = 0; i < sortedList.size() ; i++) {
				list = new ArrayList<String>();
				list.add(String.valueOf(sortedList.get(i).getShirt()));
				list.add(sortedList.get(i).getShortName());
				list.add(sortedList.get(i).getTeamName());
				list.add(String.valueOf(sortedList.get(i).getAces()));
				list.add(String.valueOf(sortedList.get(i).getServeError()));
				list.add(String.valueOf(sortedList.get(i).getTotalServe()));
				writer.writeNext(list.toArray(new String[0]));
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create DIGS results CSV file
	 */
	private static void generateDig() {
		List<Athlete> sortedList = Athlete.sortDigs(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(DIGS_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Digs"});
			for (int i = 0; i < sortedList.size() ; i++) {
				list = new ArrayList<String>();
				list.add(String.valueOf(sortedList.get(i).getShirt()));
				list.add(sortedList.get(i).getShortName());
				list.add(sortedList.get(i).getTeamName());
				
				list.add(String.valueOf(sortedList.get(i).getDigs()));
				writer.writeNext(list.toArray(new String[0]));
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create SETS results CSV file
	 */
	private static void generateSet() {
		List<Athlete> sortedList = Athlete.sortSets(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(SETS_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Perfect Sets"});
			for (int i = 0; i < sortedList.size() ; i++) {
				if (sortedList.get(i).getSets()!=0){ //TODO hack, need better way of removing players with 0
					list = new ArrayList<String>();
					list.add(String.valueOf(sortedList.get(i).getShirt()));
					list.add(sortedList.get(i).getShortName());
					list.add(sortedList.get(i).getTeamName());
					list.add(String.valueOf(sortedList.get(i).getSets()));
					writer.writeNext(list.toArray(new String[0]));
				}
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create DIGS results CSV file
	 */
	private static void generateBlock() {
		List<Athlete> sortedList = Athlete.sortBlocks(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(BLOCKS_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Block","Block Error","Total Block"});
			for (int i = 0; i < sortedList.size() ; i++) {
				list = new ArrayList<String>();
				list.add(String.valueOf(sortedList.get(i).getShirt()));
				list.add(sortedList.get(i).getShortName());
				list.add(sortedList.get(i).getTeamName());
				
				list.add(String.valueOf(sortedList.get(i).getBlock()));
				list.add(String.valueOf(sortedList.get(i).getBlockError()));
				list.add(String.valueOf(sortedList.get(i).getTotalBlock()));
				writer.writeNext(list.toArray(new String[0]));
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create POINTS results CSV file
	 */
	private static void generatePoints() {
		List<Athlete> sortedList = Athlete.sortPoints(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(POINTS_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Points","Serve","Block","Kill"});
			for (int i = 0; i < sortedList.size() ; i++) {
				list = new ArrayList<String>();
				list.add(String.valueOf(sortedList.get(i).getShirt()));
				list.add(sortedList.get(i).getShortName());
				list.add(sortedList.get(i).getTeamName());
				list.add(String.valueOf(sortedList.get(i).getPoints()));
				list.add(String.valueOf(sortedList.get(i).getAces()));
				list.add(String.valueOf(sortedList.get(i).getBlock()));
				list.add(String.valueOf(sortedList.get(i).getKill()));
				writer.writeNext(list.toArray(new String[0]));
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create KILLS results CSV file
	 */
	private static void generateAttacks() {
		List<Athlete> sortedList = Athlete.sortKillPercentage(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(KILL_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Percent","Kill","Error","Blocked","Total Attack"});
			for (int i = 0; i < sortedList.size() ; i++) {
				list = new ArrayList<String>();
				list.add(String.valueOf(sortedList.get(i).getShirt()));
				list.add(sortedList.get(i).getShortName());
				list.add(sortedList.get(i).getTeamName());
				
				list.add(String.valueOf(sortedList.get(i).getAttackPercentage()));
				list.add(String.valueOf(sortedList.get(i).getKill()));
				list.add(String.valueOf(sortedList.get(i).getAttackError()));
				list.add(String.valueOf(sortedList.get(i).getAttackBlocked()));
				list.add(String.valueOf(sortedList.get(i).getAttackTotal()));
				writer.writeNext(list.toArray(new String[0]));
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Create PASS results CSV file
	 */
	private static void generatePassing() {
		List<Athlete> sortedList = Athlete.sortPassPercentage(LIMIT); // return top 10 ( or 11 if 11 has same point)
		
		CSVWriter writer;
		List<String> list = null;
		try {
			writer = new CSVWriter(new FileWriter(PASS_FILE), ',');
			writer.writeNext(new String[]{"Jersey#","Name","Team","Percent Perfect","Error","Total Passes"});
			for (int i = 0; i < sortedList.size() ; i++) {
				if (sortedList.get(i).getPassTotal() > Athlete.PASS_LIMIT){ //TODO hack, need better way of removing players with pass < pass limit
					list = new ArrayList<String>();
					list.add(String.valueOf(sortedList.get(i).getShirt()));
					list.add(sortedList.get(i).getShortName());
					list.add(sortedList.get(i).getTeamName());
					
					list.add(String.valueOf(sortedList.get(i).getPassPercentage()));
					list.add(String.valueOf(sortedList.get(i).getPassError()));
					list.add(String.valueOf(sortedList.get(i).getPassTotal()));
					writer.writeNext(list.toArray(new String[0]));
				}
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Generates all athletes from the CSV files
	 * The Athlete Class maintains a static HashMap with all Athletes
	 */
	private static void generateAllAthletes(List<File> allCSV) throws java.text.ParseException {

		Iterator<File> allCSVIterator = allCSV.iterator();
		File csvFile= null;
		while (allCSVIterator.hasNext()){
	        CSVReader reader;
	        String teamName;
	        csvFile= allCSVIterator.next();
			try {
				reader = new CSVReader(new FileReader(csvFile));
				teamName = csvFile.getName().substring(csvFile.getName().indexOf('_')+1, csvFile.getName().indexOf('.'));
				try {
					String [] nextLine = reader.readNext();
					while (nextLine != null) {
				        if(nextLine[0].matches("^[0-9].*$")){
				        	// We found an Athlete, parse the data until we find a new athlete
				        	int points = 0;
				        	String s2 = nextLine[0].substring(0, nextLine[0].indexOf(' '));
				        	String s3 = nextLine[0].substring(nextLine[0].indexOf(' ')+1); //Index of first space+1 to end.
				        	log.log(Level.FINER,"Athlete Name: ["+s3+"]  Shirt "+s2);
				        	Athlete ath = Athlete.getAthlete(s3.trim()+"|"+teamName); // a key is name + team - need to find if we have 2 same last name in a team
				        	ath.setShirt(s2);
				        	do {
				              switch (nextLine[1].toLowerCase()){
				                case "serve":
				                	log.log(Level.FINER," ->"+nextLine[1]+" Aces:"+nextLine[30]+" err:"+nextLine[11]+" total:"+nextLine[9]);
				                	if (!nextLine[30].isEmpty()) points = points + new Integer(nextLine[30]).intValue();
				                	ath.addServe(nextLine[30],nextLine[11],nextLine[9]);
				                	break;
				                case "attack":
				                	log.log(Level.FINER," ->"+nextLine[1]+" %:"+nextLine[7]+" Kill:"+nextLine[30]+" err:"+nextLine[11]+" blocked:"+nextLine[16]+" total:"+nextLine[9]);
				                	if (!nextLine[30].isEmpty()) points = points + new Integer(nextLine[30]).intValue();
				                	ath.addAttack(nextLine[30],nextLine[11],nextLine[16],nextLine[9]);
				                	break;
				                case "reception":
				                	log.log(Level.FINER," ->"+nextLine[1]+" Perfect:"+nextLine[30]+" err:"+nextLine[11]+" total:"+nextLine[9]);
				                	ath.addPassing(nextLine[30],nextLine[11],nextLine[9]);
				                	break;
				                case "block":
				                	log.log(Level.FINER," ->"+nextLine[1]+" block:"+nextLine[30]+" err:"+nextLine[11]+" total:"+nextLine[9]);
				                	if (!nextLine[30].isEmpty()) points = points + new Integer(nextLine[30]).intValue();
				                	ath.addBlock(nextLine[30],nextLine[11],nextLine[9]);
				                	break;
				                case "dig":
				               		log.log(Level.FINER," ->"+nextLine[1]+" dig:"+nextLine[30]+" err:"+nextLine[11]+" total:"+nextLine[9]);
				               		ath.addDig(nextLine[30]);
				               		break;
				                case "set":
				                	log.log(Level.FINER," ->"+nextLine[1]+" set:"+nextLine[30]+" err:"+nextLine[11]+" total:"+nextLine[9]);
				                	ath.addSet(nextLine[30]);
				                	break;
				                }
				              // we read the line, get next one. If new athlete, exit to create new athlete
				              nextLine = reader.readNext();
				        	} while (nextLine!=null && !nextLine[0].matches("^[0-9].*$"));
				        	log.log(Level.FINER," Total Points: "+points);
				        	log.log(Level.FINER," ");
				        	ath.addPoints(points);
						} else {
							// not an Athlete, read next line
							nextLine = reader.readNext();
						}
					}

				} catch (IOException e) {
					log.log(Level.SEVERE,"Error reading file "+csvFile+". Skipping.");
				}	
			} catch (FileNotFoundException e) {
				log.log(Level.SEVERE,"The file "+csvFile.getName()+" was not found in the local directory. Skipping file");
			}
		}

	}

	/*
	 * Reads all the CSV files in the directory PATH
	 */
	private static List<File> getAllCSV() throws IOException {
		File dir = new File(PATH);
		String[] extensions = new String[] { "csv"};
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);
		for (File file : files) {
			log.log(Level.FINER,"Will parse file: " + file.getCanonicalPath());
		}
		return files;
	}
	
	/*
	 * 
	 */
	public static void transferFiles(){
		   FTPClient ftp = new FTPClient();
		    FTPClientConfig config = new FTPClientConfig();
		    //config.setXXX(YYY); // change required options
		    ftp.configure(config);
		    boolean error = false;
		    try {
		      int reply;
		      String server = FTP_SERVER;
		      ftp.connect(server);
		      ftp.login(USERID, PASSWORD);
		      log.log(Level.INFO,"Connected to " + server + ".");
		      log.log(Level.INFO,ftp.getReplyString());

		      // After connection attempt, you should check the reply code to verify
		      // success.
		      reply = ftp.getReplyCode();

		      if(!FTPReply.isPositiveCompletion(reply)) {
		        ftp.disconnect();
		        log.log(Level.SEVERE,"FTP server refused connection.");
		        return;
		      }
		      
		     //transfer files
		     boolean cd= ftp.changeWorkingDirectory(REMOTE_PATH);
			if (cd) {
				File files[] = new File(GENERATED_PATH).listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".csv");
					}
				});

				InputStream inputStream;
				boolean done;
				for (int i = 0; i < files.length; i++) {
					inputStream = new FileInputStream(files[i]);
					 done = ftp.storeFile(files[i].getName(), inputStream);
			         inputStream.close();
			         log.log(Level.INFO, files[i].getName()+" transfered to "+REMOTE_PATH);
				}

			} else {
				log.log(Level.SEVERE,
						"Unable to go to path:" + REMOTE_PATH + " on FTP server:" + FTP_SERVER + " as user:" + USERID);
			}
		      ftp.logout();
		    } catch(IOException e) {
		      error = true;
		      e.printStackTrace();
		    } finally {
		      if(ftp.isConnected()) {
		        try {
		          ftp.disconnect();
		        } catch(IOException ioe) {
		          // do nothing
		        }
		      }
		    }
	}
}
