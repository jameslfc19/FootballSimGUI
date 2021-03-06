package com.james.footballsim.Simulator;

import com.badlogic.gdx.Gdx;
import com.james.footballsim.FootballSim;

import java.io.Serializable;
import java.util.*;

public class Team implements Serializable {
	
	public int id;
	public String name;
	public String shortName;
	public double attack;
	public double defence;

	public double penalites = 0.7;
	public double freekicks = 0.7;

	public int winStreak = 0;
	public int loseStreak = 0;
	public int trophiesWon = 0;
	public double averagePosition = 0;
	public double totalPositions = 0;
	public double leaguesPlayed = 0;

	public Players players;
	public Players injured;

	private List<Player> goalkeepers;
	private List<Player> defenders;
	private List<Player> midfielders;
	private List<Player> forwards;

	List<TeamUpdate> updates;

	public int rating;
	public float attackRating;
	public float defenceRating;
	public float form;
	private LinkedList<Double> formList;

	public float averageAttackRating;
	public float averageDefenceRating;

	final int FOURTHREETHREE = 433;
	final int FOURFOURTWO = 442;
	final int THREEFOURTHREE = 343;

	int formation;
	float bestRating;

	int nDefenders;
	int nMidfielders;
	int nForwards;

//	final static float FORWARD_ATTACK = 0.15f;
//	final static float MID_ATTACK = 0.14f;
//	final static float CB_ATTACK = 0.02f;
//	final static float GK_ATTACK = 0.022f;
//
//	final static float FORWARD_DEF = 0.04f;
//	final static float MID_DEF = 0.11f;
//	final static float CB_DEF = 0.14f;
//	final static float GK_DEF = 0.15f;

	Random rand;

	public boolean chosenTeam = false;

	public Team(int id, String name, String shortName, float averageRating){
		this(id,name,shortName,averageRating,averageRating);
	}

	public Team(int id, String name, float averageRating){
		this(id,name,averageRating,averageRating);
	}

	public Team(int id, String name, String shortName, float averageAttackRating, float averageDefenceRating){
		this(id,name,averageAttackRating,averageDefenceRating);
		this.shortName = shortName;
	}

	public Team(int id, String name, float averageAttackRating, float averageDefenceRating){
		Gdx.app.log(name," Initliasing team");
		rand = new Random();
		this.id = id;
		this.name = name;
		this.shortName = name;
		this.averageAttackRating = (float) (averageAttackRating+rand.nextGaussian()*2);
		this.averageDefenceRating = (float) (averageDefenceRating+rand.nextGaussian()*2);
		this.formList = new LinkedList<>();
		generateSquad();
		genOverallRating();
		bestFormation();
		calculateStats();
		//System.out.println(name+" - "+Math.round(bestRating)+"| Attack: "+attackRating+" Defence: "+defenceRating+" Formation: "+formation);
		//System.out.println("Generated "+name+". Attack: "+this.attack+" Defence: "+this.defence);
	}

	public Team(){

	}

	public Team notInitliased(){
		Gdx.app.log(name,"not initalised team");
		this.id = -1;
		this.name = "Non Initialised Screen";
		players = new Players();
		injured = new Players();
		return this;
	}

	public void update(){
		//Remove old updates
		updates = new ArrayList<>();

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


	private float defenceRatio(float rating){
		return -((rating-80)/20);
	}
	
	private float attackRatio(float rating){
		return (rating-80)/20;
	}

	private void generateSquad(){

		this.penalites = averageAttackRating/120;
		this.freekicks = averageAttackRating/120;

//		if(defAverageRating>99) defAverageRating = 99;
//		if(attackAverageRating>99) attackAverageRating = 99;

//		if(id == 1){
//			defAverageRating = 90;
//			attackAverageRating = 92;
//		}

		players = new Players();
		injured = new Players();
		//Keepers
		for(int i = 1; i <= 3; i++){
			players.add(new Player(Player.GOALKEEPER, (averageAttackRating-(i-2)*5),averageDefenceRating-(i-2)*5));
		}
		//Defenders
		for(int i = 1; i <= 8; i++){
			players.add(new Player(Player.DEFENDER, (averageAttackRating-(i-2)*1.5f) , (averageDefenceRating-((i-2)*1.5f))));
		}
		//Midfielders
		for(int i = 1; i <= 8; i++){
			players.add(new Player(Player.MIDFIELDER, (averageAttackRating-(i-2)*1.5f), (averageDefenceRating-(i-2)*1.5f)));
		}
		//Forwards
		for(int i = 1; i <= 6; i++){
			players.add(new Player(Player.FORWARD, (averageAttackRating-(i-2)*1.5f), (averageDefenceRating-(i-2)*1.5f)));
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

		goalkeepers = players.getPlayers(Player.GOALKEEPER);
		defenders = players.getPlayers(Player.DEFENDER);
		midfielders = players.getPlayers(Player.MIDFIELDER);
		forwards = players.getPlayers(Player.FORWARD);

		int i = 0;
		for(Player player : players.getList()){
			i += player.rating;
		}
		rating = i/players.getList().size();
	}

	private void genPlayer(int type) {
		Player player = new Player(type, 0.7f*attackRating, 0.7f*defenceRating);
		players.add(player);
		updates.add(new TeamUpdate().youthPromotion(player));
	}

	private void playerGrowth() {
		for(Player player : players.getList()){
			if(rand.nextDouble() <= ((double) player.growth/300)){
				if(player.growth>0) {
					player.grow();
					updates.add(new TeamUpdate().playerGrowth(player));
				}
			} else {

			}
		}
	}

	private void checkRating(int previous){
		if(previous != getRating()) updates.add(new TeamUpdate().teamRating(this,previous));
	}

	private void playerUpdate(){
		for(Player player : new ArrayList<>(injured.getList())){
			player.injuryLength--;
			if(player.injuryLength <= 0){
				injured.remove(player);
				player.injured = false;
				players.add(player);
				updates.add(new TeamUpdate().backFromInjury(player));
				if(chosenTeam) System.out.println("Back from injury "+player.getMatchName());
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
				updates.add(new TeamUpdate().injury(player));
				if(chosenTeam) System.out.println("injury "+player.getMatchName());
			}
		}
	}

	public int getAttackRating(){
		return (int) Math.rint(attackRating);
	}

	public int getDefenceRating(){
		return (int) Math.rint(defenceRating);
	}

	public int getRating(){
		return (int) Math.rint(bestRating);
	}

	public List<TeamUpdate> getUpdates() {
		return updates;
	}

	private void bestFormation(){
		//Check if 4-3-3 or 4-4-2 or 3-4-3 is best formation

		//Best Goal Keeper
		Collections.sort(goalkeepers, sortPlayers);
		Collections.sort(defenders, sortPlayers);
		Collections.sort(midfielders, sortPlayers);
		Collections.sort(forwards, sortPlayers);

		//433
			float rAttack = 0;
			float rDefence = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=4)&&(midfielders.size()>=3)&&(forwards.size()>=3)) {
			rDefence += goalkeepers.get(0).defense;
			//4 Defenders
			for (int i = 0; i <= 3; i++) {
				rDefence += defenders.get(i).defense;
			}

			//3 Mids
			for (int i = 0; i <= 2; i++) {
				rAttack += midfielders.get(i).attack;
				rDefence += midfielders.get(i).defense;
			}

			//3 Forwards
			for (int i = 0; i <= 2; i++) {
				rAttack += forwards.get(i).attack;
			}

			rAttack /= (3+3);
			rDefence /= (4+3+1);

			float r433 = (rAttack+rDefence)/2;
			if (r433 > bestRating) {
				bestRating = r433;
				formation = FOURTHREETHREE;
				System.out.println("A: "+rAttack+" D: "+rDefence+" O:"+bestRating);
			}
		}


		//442
		rAttack = 0;
		rDefence = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=4)&&(midfielders.size()>=4)&&(forwards.size()>=2)) {
			rDefence += goalkeepers.get(0).defense;
			//4 Defenders
			for (int i = 0; i <= 3; i++) {
				rDefence += defenders.get(i).defense;
			}

			//4 Mids
			for (int i = 0; i <= 3; i++) {
				rAttack += midfielders.get(i).attack;
				rDefence += midfielders.get(i).defense;
			}

			//2 Forwards
			for (int i = 0; i <= 1; i++) {
				rAttack += forwards.get(i).attack;
			}

			rAttack /= (2+4);
			rDefence /= (4+4+1);
			float r442 = (rAttack+rDefence)/2;
			if (r442 > bestRating) {
				bestRating = r442;
				formation = FOURFOURTWO;
				//System.out.println("New Formation! "+formation);
			}
		}

		//343
		rAttack = 0;
		rDefence = 0;
		if((goalkeepers.size()>=1)&&(defenders.size()>=3)&&(midfielders.size()>=4)&&(forwards.size()>=3)) {
			rDefence += goalkeepers.get(0).defense;
			//3 Defenders
			for (int i = 0; i <= 2; i++) {
				rDefence += defenders.get(i).defense;
			}

			//4 Mids
			for (int i = 0; i <= 3; i++) {
				rAttack += midfielders.get(i).attack;
				rDefence += midfielders.get(i).defense;
			}

			//3 Forwards
			for (int i = 0; i <= 2; i++) {
				rAttack += forwards.get(i).attack;
			}

			rAttack /= (3+4);
			rDefence /= (4+3+1);

			float r343 = (rAttack+rDefence)/2;
			if (r343 > bestRating) {
				bestRating = r343;
				formation = THREEFOURTHREE;
				//System.out.println("New Formation! "+formation);
			}
		}

		//System.out.println(name+": Formation: "+formation);

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
		defence += goalkeepers.get(0).defense;
		for (int i = 0; i < nDefenders; i++){
			if(i<defenders.size()) defence += defenders.get(i).defense;
		}
		for (int i = 0; i < nMidfielders; i++) {
			if(i<midfielders.size()) defence += midfielders.get(i).defense;
		}
		defenceRating = defence/(nDefenders+nMidfielders+1);
		this.defence = defenceRatio(defenceRating+form);

		//Attack
		float attack = 0;
		for (int i = 0; i < nMidfielders; i++) {
			if(i<midfielders.size()) attack += midfielders.get(i).attack;
		}
		for (int i = 0; i < nForwards; i++) {
			if(i<forwards.size()) attack += forwards.get(i).attack;
		}
		attackRating = attack/(nMidfielders+nForwards);
		this.attack = attackRatio(attackRating+form);

		bestRating = (attackRating+defenceRating)/2;
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

	public List<Player> getReserves(){
		List<Player> reserveTeam = new ArrayList<>();
		for(int i = 1; i<goalkeepers.size();i++) reserveTeam.add(goalkeepers.get(i));
		for(int i = nDefenders; i<defenders.size();i++) reserveTeam.add(defenders.get(i));
		for(int i = nMidfielders; i<midfielders.size();i++) reserveTeam.add(midfielders.get(i));
		for(int i = nForwards; i<forwards.size();i++) reserveTeam.add(forwards.get(i));
		return reserveTeam;
	}


	public void listBestSquad(){
		for(Player player : getBestSquad()){
			System.out.println(player.firstName+" "+player.lastName+" - "+player.getPosition()+" - Rating: "+player.rating+" - Potential:"+(player.rating+player.growth));
		}
	}

	public static Comparator<Player> sortPlayers = (p1, p2) -> {
        if(p1.getWeightedRating()>p2.getWeightedRating()) return -1;
        else if(p1.getWeightedRating()<p2.getWeightedRating()) return 1;
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

	//-1 lose, 0 draw, 1 win
	public void teamForm(int result, float ratingDifference){
		switch (result){
			case 1:
				formList.add(0,0.5*Math.exp(ratingDifference*0.15));
				break;
			case 0:
				formList.add(0,0.0);
				break;
			case -1:
				formList.add(0,-0.5*Math.exp(-ratingDifference*0.15));
				break;
		}
		if(formList.size()>10) formList.remove(10);
		form = 0;
		for(int i = 0; i<formList.size(); i++){
			double d = formList.get(i);
			form += Math.exp(-i/2.5)*d;
		}
		if(id==FootballSim.info.teamId) {
			System.out.println(name+" Form: "+form);
		}
	}

	public float getFormRating(){
		return rating+form;
	}

	public double getForm(){
		return Math.round(form*10.0)/10.0;
	}

}
