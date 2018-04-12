package com.james.footballsim;

import java.util.*;

public class Team {
	
	int id;
	public String name;
	double attack;
	double defence;
	
	double penalites;
	double freekicks;
	
	int winStreak = 0;
	int loseStreak = 0;
	int trophiesWon = 0;
	double averagePosition = 0;
	double totalPositions = 0;
	double leaguesPlayed = 0;

	Players players;
	Players injured;

	List<Player> goalkeepers;
	List<Player> defenders;
	List<Player> midfielders;
	List<Player> forwards;

	int rating;
	float attackRating;
	float defenceRating;

	final int FOURTHREETHREE = 433;
	final int FOURFOURTWO = 442;
	final int THREEFOURTHREE = 343;

	int formation;
	float bestRating;

	int nDefenders;
	int nMidfielders;
	int nForwards;

	Random rand;

	boolean chosenTeam = false;
	
	public Team(int id, String name, double pens, double freekicks){
		rand = new Random();
		this.id = id;
		this.name = name;
		this.penalites = pens;
		this.freekicks = freekicks;
		generateSquad();
		genOverallRating();
		bestFormation();
		calculateStats();
		//System.out.println(name+" - "+Math.round(bestRating)+"| Attack: "+attackRating+" Defence: "+defenceRating+" Formation: "+formation);
		//System.out.println("Generated "+name+". Attack: "+this.attack+" Defence: "+this.defence);
	}

	public void update(){
		//Store old rating
		int previousRating = getRating();

		playerGrowth();
		playerUpdate();
		genOverallRating();
		bestFormation();
		calculateStats();

		//if(chosenTeam)
			checkRating(previousRating);
	}


	public static double defenceRatio(double ratio){
		return Math.exp(0.4*ratio)-0.45;
	}
	
	public static double attackRatio(double ratio){
		return (Math.exp(ratio)-1.5)/2.5;
	}

	public void generateSquad(){

		float defAverageRating = (float) (84f+rand.nextGaussian()*4);
		float attackAverageRating = (float) (84f+rand.nextGaussian()*4);
		players = new Players();
		injured = new Players();
		//Keepers
		for(int i = 1; i <= 3; i++){
			players.add(new Player(Player.GOALKEEPER,defAverageRating-(i-1)*5));
		}
		//Defenders
		for(int i = 1; i <= 8; i++){
			players.add(new Player(Player.DEFENDER, (float) (defAverageRating-((i-1)*2))));
		}
		//Midfielders
		for(int i = 1; i <= 8; i++){
			players.add(new Player(Player.MIDFIELDER, (float) ((defAverageRating+attackAverageRating)/2-((i-1)*1.5))));
		}
		//Forwards
		for(int i = 1; i <= 6; i++){
			players.add(new Player(Player.FORWARD, (float) (attackAverageRating-(i-1)*2.5)));
		}

	}

	private void genOverallRating(){

		goalkeepers = players.getPlayers(Player.GOALKEEPER);
		defenders = players.getPlayers(Player.DEFENDER);
		midfielders = players.getPlayers(Player.MIDFIELDER);
		forwards = players.getPlayers(Player.FORWARD);

		if(goalkeepers.size()<=0) genPlayer(Player.GOALKEEPER);
		if(defenders.size() <= 4) for(int i = 0; i <= (4-defenders.size()); i++) genPlayer(Player.DEFENDER);
		if(midfielders.size() <= 4) for(int i = 0; i <= (4-midfielders.size()); i++) genPlayer(Player.MIDFIELDER);
		if(forwards.size() <= 3) for(int i = 0; i <= (3-forwards.size()); i++) genPlayer(Player.FORWARD);

		int i = 0;
		for(Player player : players.getList()){
			i += player.rating;
		}
		rating = i/players.getList().size();
	}

	private void genPlayer(int type) {
		Player player = null;
		if((type == Player.GOALKEEPER) || (type == Player.DEFENDER)) {
			player = new Player(type, defenceRating*0.8f);
		}
		if((type == Player.MIDFIELDER)) {
			player = new Player(type, 0.8f*(defenceRating+attackRating)/2);
		}
		if((type == Player.FORWARD)) {
			player = new Player(type, 0.8f*(attackRating));
		}
		players.add(player);
		System.out.println("Generated new player at "+name+" :"+player.getMatchName()+" R:"+player.rating+" P:"+(player.rating+player.growth));
		Utils.promptEnterKey2(Game.reader);
	}

	private void playerGrowth() {
		for(Player player : players.getList()){
			if(rand.nextDouble() <= ((double) player.growth/300)){
				if(player.growth>0) {
					player.rating++;
					player.growth--;
					if(chosenTeam) {
						System.out.println(player.getMatchName() + " just grew!");
						System.out.println("New rating: " + player.rating);
						System.out.println("New potential: " + (player.rating + player.growth));

					}
				}
			} else {

			}
		}
	}

	private void checkRating(int previous){
		if(previous < getRating()){
			System.out.println(name+" Current squad rating: "+getRating()+" (+"+(getRating()-previous)+")");
		}
		if(previous > getRating()){
			System.out.println(name+ "Current squad rating: "+getRating()+" ("+(getRating()-previous)+")");
		}
	}

	private void playerUpdate(){
		for(Player player : new ArrayList<>(injured.getList())){
			player.injuryLength--;
			if(player.injuryLength <= 0){
				injured.remove(player);
				player.injured = false;
				players.add(player);
				if(chosenTeam) System.out.println(player.getMatchName()+" is no longer injured.");
			}
		}
		for(Player player : new ArrayList<>(players.getList())) {
			if (rand.nextDouble() <= 0.005) {
				players.remove(player);
				player.injured = true;
				double length = (rand.nextGaussian()*4+5);
				if(length<1) length = 1;
				player.injuryLength = (int) Math.round(length);
				injured.add(player);
				if(chosenTeam) System.out.println(player.getMatchName()+" is injured for "+player.injuryLength+" week[s].");
			}
		}



	}

	public int getAttackRating(){
		return Math.round(attackRating);
	}

	public int getDefenceRating(){
		return Math.round(defenceRating);
	}

	public int getRating(){
		return Math.round(bestRating);
	}

	private void bestFormation(){
		//Check if 4-3-3 or 4-4-2 or 3-4-3 is best formation

		//Best Goal Keeper
		Collections.sort(goalkeepers, sortPlayers);
		Collections.sort(defenders, sortPlayers);
		Collections.sort(midfielders, sortPlayers);
		Collections.sort(forwards, sortPlayers);

		//433
			float r = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=4)&&(midfielders.size()>=3)&&(forwards.size()>=3)) {
			r += goalkeepers.get(0).rating;

			//4 Defenders
			for (int i = 0; i <= 3; i++) {
				r += defenders.get(i).rating;
			}

			//3 Mids
			for (int i = 0; i <= 2; i++) {
				r += midfielders.get(i).rating;
			}

			//3 Forwards
			for (int i = 0; i <= 2; i++) {
				r += forwards.get(i).rating;
			}

			float r433 = (r / 11);
			if (r433 > bestRating) {
				bestRating = r433;
				formation = FOURTHREETHREE;
				//System.out.println("New Formation! "+formation);
			}
		}


		//442
		r = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=4)&&(midfielders.size()>=4)&&(forwards.size()>=2)) {
			r += goalkeepers.get(0).rating;

			//4 Defenders
			for (int i = 0; i <= 3; i++) {
				r += defenders.get(i).rating;
			}

			//4 Mids
			for (int i = 0; i <= 3; i++) {
				r += midfielders.get(i).rating;
			}

			//2 Forwards
			for (int i = 0; i <= 1; i++) {
				r += forwards.get(i).rating;
			}

			float r442 = (r / 11);
			if (r442 > bestRating) {
				bestRating = r442;
				formation = FOURFOURTWO;
				//System.out.println("New Formation! "+formation);
			}
		}

		//343
		r = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=3)&&(midfielders.size()>=4)&&(forwards.size()>=3)) {
			r += goalkeepers.get(0).rating;

			//3 Defenders
			for (int i = 0; i <= 2; i++) {
				r += defenders.get(i).rating;
			}

			//4 Mids
			for (int i = 0; i <= 3; i++) {
				r += midfielders.get(i).rating;
			}

			//3 Forwards
			for (int i = 0; i <= 2; i++) {
				r += forwards.get(i).rating;
			}

			float r343 = (r / 11);

			if (r343 > bestRating) {
				bestRating = r343;
				formation = THREEFOURTHREE;
				//System.out.println("New Formation! "+formation);
			}
		}

		switch(formation) {
			case FOURTHREETHREE:
				nDefenders = 4;
				nMidfielders = 3;
				nForwards = 3;
				break;
			case FOURFOURTWO:
				nDefenders = 4;
				nMidfielders = 4;
				nForwards = 2;
				break;
			case THREEFOURTHREE:
				nDefenders = 3;
				nMidfielders = 4;
				nForwards = 3;
				break;
		}

	}

	public void calculateStats(){
		//Defence
		float defence = 0;
		defence += goalkeepers.get(0).rating;
		for (int i = 0; i < nDefenders; i++){
			if(i<defenders.size()) defence += defenders.get(i).rating;
		}
		for (int i = 0; i < nMidfielders; i++) {
			if(i<midfielders.size()) defence += midfielders.get(i).rating;
		}
		defenceRating = defence/(1+nDefenders+nMidfielders);
		this.defence = defenceRatio(0.01*defenceRating);

		//Attack
		float attack = 0;
		for (int i = 0; i < nMidfielders; i++){
			if(i<midfielders.size()) attack += midfielders.get(i).rating;
		}
		for (int i = 0; i < nForwards; i++){
			if(i<forwards.size()) attack += forwards.get(i).rating;
		}
		attackRating = attack/(nForwards+nMidfielders);
		this.attack = attackRatio(0.01*attackRating);
	}

	public void listPlayers(){
		Collections.sort(players.getList(),sortPlayers);
		for(Player player : players.getList()){
			System.out.println(player.firstName+" "+player.lastName+" - "+player.getPosition()+" - Rating: "+player.rating+" - Potential:"+(player.rating+player.growth));
		}
	}

	public List<Player> getBestSquad(){
		List<Player> bestTeam = new ArrayList<>();
		bestTeam.add(goalkeepers.get(0));
		for(int i = 0; i<nDefenders;i++) bestTeam.add(defenders.get(i));
		for(int i = 0; i<nMidfielders;i++) bestTeam.add(midfielders.get(i));
		for(int i = 0; i<nForwards;i++) bestTeam.add(forwards.get(i));
		return bestTeam;
	}

	public void listBestSquad(){
		for(Player player : getBestSquad()){
			System.out.println(player.firstName+" "+player.lastName+" - "+player.getPosition()+" - Rating: "+player.rating+" - Potential:"+(player.rating+player.growth));
		}
	}

	public static Comparator<Player> sortPlayers = (p1, p2) -> {
        if(p1.rating>p2.rating) return -1;
        else if(p1.rating<p2.rating) return 1;
        else if(p1.growth>p2.growth) return -1;
        else return 0;
    };

	public Player getGoalscorer(){
		double i = rand.nextDouble();
		if(i <= (1.5*attackRating)/(attackRating+defenceRating)){
			i = rand.nextDouble();
			if(i<=0.75){
				int p = rand.nextInt(nForwards-1);
				return forwards.get(p);
			} else {
				int p = rand.nextInt(nMidfielders-1);
				return midfielders.get(p);
			}
		} else {
			int p = rand.nextInt(nDefenders-1);
			return defenders.get(p);
		}
	}

}