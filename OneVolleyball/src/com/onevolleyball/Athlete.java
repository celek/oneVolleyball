package com.onevolleyball;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OneVolleyball Athlete
 * @author xtofv
 *
 */
class Athlete {
	
	private static final Logger log = Logger.getLogger(GenerateLeagueAwards.class.getName());
	
	public static final int ATTACK_LIMIT=19;
	public static final int PASS_LIMIT=19;
	
	private static Map<String, Athlete> map = new HashMap<String, Athlete>();
	
	private String name; // the key name|team
	private String theTeam;
	private int shirt_num;
	
	
	private int theAces;
	private int theSerVErr;
	private int theTotalServe;
	
	// points
	private int thePoints;
	private int theBlocks;
	//private BigDecimal theAttackPercent; //not USED , we compute on the fly
	
	//ATTACK
	private int theKill;
	private int theAttackErr;
	private int theAttackBlocked;
	private int theAttackTotal;	
	
	//BLOCK
	//theBlocks defined above
	private int theBlockErr;
	private int theBlockTotal;
	
	//DIGS
	private int theDigs;
	
	//SETS
	private int theSets;
	
	//PASSES
	private int thePasses; 
	private int thePassErr;
	private int thePassTotal;
	
	public static Athlete getAthlete(String s3) {
		// an athlete is the name AND the team as the short may change
		String theName = s3.trim().replaceAll(" +", " ");;
		Athlete theAthlete = map.get(theName);
		if (theAthlete == null){
			log.log(Level.INFO,String.format("Athlete %s not found in the database. Creating...", theName));
			theAthlete = new Athlete();
			theAthlete.setName(theName); //name + team, not shirt
			theAthlete.setTeam(theName.substring(theName.indexOf('|')+1));
			map.put(theName, theAthlete);
		}
		return theAthlete;
	}

	private void setTeam(String ateam) {
		theTeam = ateam;
	}

	private void setName(String theName) {
		name = theName;
	}


	public void setShirt(String s2) {
		int shirt = 0;
		if (s2.isEmpty()){
			log.log(Level.SEVERE,String.format("The athlete %s does not seem to have a shirt number. Setting shirt to 0", name));
		} else {
			shirt = Integer.parseInt(s2);
		}
		if (shirt_num != 0 && shirt_num != shirt){
			log.log(Level.SEVERE,String.format("Shirt for %s already set to %d. Now attempting to set to %d",name,shirt_num,shirt));
		}
		shirt_num = shirt;
	}


	public void addServe(String aces, String SE, String totServe) {
		theAces += (aces == null || aces.isEmpty())?0:Integer.parseInt(aces);
		theSerVErr += (SE == null || SE.isEmpty())?0:Integer.parseInt(SE);
		theTotalServe += (totServe == null || totServe.isEmpty())?0:Integer.parseInt(totServe);
	}
	
	public void addAttack(String kill, String err, String blk, String tot) throws ParseException {
		theKill += (kill == null || kill.isEmpty())?0:Integer.parseInt(kill);
		theAttackErr += (err == null || err.isEmpty())?0:Integer.parseInt(err);
		theAttackBlocked += (blk == null ||blk.isEmpty())?0:Integer.parseInt(blk);
		theAttackTotal += (tot == null || tot.isEmpty())?0:Integer.parseInt(tot);	
	}
	
	public void addBlock(String block, String blkErr, String tot) throws ParseException {
		theBlocks += (block == null || block.isEmpty())?0:Integer.parseInt(block); 
		theBlockErr += (blkErr == null || blkErr.isEmpty())?0:Integer.parseInt(blkErr);
		theBlockTotal += (tot == null || tot.isEmpty())?0:Integer.parseInt(tot);	
	}
	
	public void addPassing(String perfect, String passErr, String tot) throws ParseException {
		thePasses += (perfect == null || perfect.isEmpty())?0:Integer.parseInt(perfect); 
		thePassErr += (passErr == null || passErr.isEmpty())?0:Integer.parseInt(passErr);
		thePassTotal += (tot == null || tot.isEmpty())?0:Integer.parseInt(tot);	
	}
	
	public void addSet(String set) throws ParseException {
		theSets += (set == null || set.isEmpty())?0:Integer.parseInt(set);	
	}
	
	public void addDig(String dig) throws ParseException {
		theDigs += (dig == null || dig.isEmpty())?0:Integer.parseInt(dig);	
	}


	public void addPoints(int points) {
		thePoints += points;
	}


	public int getAces() {
		return theAces;
	}


	public int getServeError() {
		return theSerVErr;
	}


	public int getTotalServe() {
		return theTotalServe;
	}

	public String getName() { // the key
		return name;
	}
	
	public String getShortName() { // the key
		return name.substring(0, name.indexOf('|'));
	}
	
	public String getTeamName() { // the key
		return theTeam;
	}
	
	public int getShirt() {
		return shirt_num;
	}
	
	public int getPoints(){
		return thePoints;
	}
	
	public int getDigs(){
		return theDigs;
	}
	
	public int getSets(){
		return theSets;
	}
	
	public int getBlock(){
		return theBlocks;
	}
	
	public int getBlockError(){
		return theBlockErr;
	}
	
	public int getTotalBlock(){
		return theBlockTotal;
	}
	
	public int getKill(){
		return theKill;
	}
	
	public int getAttackTotal(){
		return theAttackTotal;
	}

	public int getAttackError(){
		return theAttackErr;
	}
	
	public int getAttackBlocked(){
		return theAttackBlocked;
	}
	
	public int getPassTotal(){
		return thePassTotal;
	}

	public int getPassError(){
		return thePassErr;
	}
	
	public int getPasses(){
		return thePasses;
	}
	
	
	
	/**
	 * Kill - Error / Total
	 * @return
	 */
	public BigDecimal getAttackPercentage(){
		// do not add percentage - attack percentage = K-err-Blocked/Total
		//return theAttackPercent==null?null:theAttackPercent.setScale(2, BigDecimal.ROUND_HALF_UP);
		if (getAttackTotal()==0) return new BigDecimal(0);
		double val = ((float)(getKill()-getAttackError()-getAttackBlocked()))/getAttackTotal();
		return new BigDecimal(val).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * Perfect - Error / Total
	 * @return
	 */
	public BigDecimal getPassPercentage(){
		// do not add percentage - attack percentage = #-err/Total
		if (getPassTotal()==0) return new BigDecimal(0);
		double val = ((float)(getPasses()-getPassError()))/getPassTotal();
		return new BigDecimal(val).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	
	/*
	 * returns a list of limit Athletes sorted by Aces (may return more than limit)
	 */
	public static List<Athlete> sortServe(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
			      return o2.getAces() - o1.getAces();
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getAces() == list.get(limit).getAces()){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of aces as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}

	/*
	 * returns a list of limit Athletes sorted by Digs (may return more than limit)
	 */
	public static List<Athlete> sortDigs(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
			      return o2.getDigs() - o1.getDigs();
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getDigs() == list.get(limit).getDigs()){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of digs as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}
	
	
	/*
	 * returns a list of limit Athletes sorted by Block (may return more than limit)
	 */
	public static List<Athlete> sortBlocks(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
			      return o2.getBlock() - o1.getBlock();
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getBlock() == list.get(limit).getBlock()){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of block as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}

	
	/*
	 * returns a list of limit Athletes sorted by Set (may return more than limit)
	 */
	public static List<Athlete> sortSets(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
			      return o2.getSets() - o1.getSets();
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getSets() == list.get(limit).getSets()){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of sets as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}


	/*
	 * returns a list of limit Athletes sorted by Points (may return more than limit)
	 */
	public static List<Athlete> sortPoints(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
			      return o2.getPoints() - o1.getPoints();
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getPoints() == list.get(limit).getPoints()){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of points as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}


	
	/*
	 * returns a list of limit Athletes sorted by Kill Percentage (may return more than limit)
	 */
	public static List<Athlete> sortKillPercentage(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
				   // if o1 attacked more than 10 times, and o2 less, o1 has better percent
				   // this is to avoid 100% kill on a 1 attempt
				  if (o1.getAttackTotal()< ATTACK_LIMIT && o2.getAttackTotal() > ATTACK_LIMIT) return 1;
				  if (o2.getAttackTotal()< ATTACK_LIMIT && o1.getAttackTotal() > ATTACK_LIMIT) return -1;
				 // if (o1.getAttackPercentage() == null && o2.getAttackPercentage() == null) return 0;
				 // if (o1.getAttackPercentage()==null) return 1;
				 // if (o2.getAttackPercentage()==null) return -1;
			      return o2.getAttackPercentage().compareTo(o1.getAttackPercentage());
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getAttackPercentage()==null && list.get(limit).getAttackPercentage()==null){
					done = true;
					// we are done when one AttackPercent is not defined
				}else if (list.get(limit).getAttackPercentage().equals(list.get(limit+1).getAttackPercentage())){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of kill percent as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}

	
	/*
	 * returns a list of limit Athletes sorted by Pass Percentage (may return more than limit)
	 */
	public static List<Athlete> sortPassPercentage(int limit) {
		List<Athlete> list = new ArrayList<Athlete>(map.values());
		
		Collections.sort(list, new Comparator<Athlete>(){
			   public int compare(Athlete o1, Athlete o2){
				   // if o1 attacked more than 10 times, and o2 less, o1 has better percent
				   // this is to avoid 100% kill on a 1 attempt
				  if (o1.getPasses()< PASS_LIMIT && o2.getPasses() > PASS_LIMIT) return 1;
				  if (o2.getPasses()< PASS_LIMIT && o1.getPasses() > PASS_LIMIT) return -1;
			      return o2.getPassPercentage().compareTo(o1.getPassPercentage());
			   }
			}); 
		
		// now check if limit++ has same point as limit
		if (limit != -1 && limit < list.size()){
			boolean done = false;
			while (!done){
				if (list.get(limit-1).getAttackPercentage()==null && list.get(limit).getAttackPercentage()==null){
					done = true;
					// we are done when one AttackPercent is not defined
				}else if (list.get(limit).getAttackPercentage().equals(list.get(limit+1).getAttackPercentage())){
					log.log(Level.FINER,list.get(limit).getName()+" has same number of kill percent as # "+(limit+1)+" "+list.get(limit+1).getName());
					limit++;
					if (limit==list.size()-1) done=true;
				} else {
					done = true;
				}
			}
			list = new ArrayList<Athlete>(list.subList(0,limit));
		} 
		return list;
	}




}
