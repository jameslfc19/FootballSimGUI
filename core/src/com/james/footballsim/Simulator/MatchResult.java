package com.james.footballsim.Simulator;

import com.badlogic.gdx.Gdx;

public class MatchResult {
	
	private int homeGoals = 0;
	private int awayGoals = 0;
	private int homePoints = 0;
	private int awayPoints = 0;

	//Used for two legged ties mostly.
	private boolean homeTeamWon;
	private boolean cupGame;
	private int homePens;
	private int awayPens;

	private Team homeTeam;
	private Team awayTeam;

	public MatchResult(){

	}

	//Use only for two legged up cup tie games.
	public MatchResult(Team home, Team away, int homeGoals, int awayGoals, boolean homeTeamWon){
		this(home,away,homeGoals,awayGoals);
		this.homeTeamWon = homeTeamWon;
		this.cupGame = true;
	}

	//Use only for two legged up cup tie games. With Penalty shootout.
	public MatchResult(Team home, Team away, int homeGoals, int awayGoals, int homePens, int awayPens, boolean homeTeamWon){
		this(home,away,homeGoals,awayGoals,homeTeamWon);
		this.homePens = homePens;
		this.awayPens = awayPens;
	}
	
	public MatchResult(Team home, Team away, int homeGoals, int awayGoals){
		//Gdx.app.log("MatchResult", home.name+" "+homeGoals+"-"+awayGoals+" "+away.name);
		this.homeGoals = homeGoals;
		this.awayGoals = awayGoals;
		this.homeTeam = home;
		this.awayTeam = away;
		if(homeGoals > awayGoals){
			homePoints = 3;
			home.winStreak++;
			away.winStreak = 0;
			home.loseStreak = 0;
			away.loseStreak++;
			home.teamForm(1,away.getFormRating()-home.getFormRating());
			away.teamForm(-1,home.getFormRating()-away.getFormRating());
		}
		else if(homeGoals < awayGoals){
			awayPoints = 3;
			away.winStreak++;
			home.winStreak = 0;
			away.loseStreak = 0;
			home.loseStreak++;
			home.teamForm(-1,away.getFormRating()-home.getFormRating());
			away.teamForm(1,home.getFormRating()-away.getFormRating());
		}
		else {
			homePoints = 1; 
			awayPoints = 1;
			home.loseStreak = 0;
			away.loseStreak = 0;
			away.teamForm(0,home.getFormRating()-away.getFormRating());
			home.teamForm(0,away.getFormRating()-home.getFormRating());

		}
//		Team[] teams = {home,away};
//		for(Team team : teams){
//			if((team.winStreak >= 1) && (homePoints!=1) ){
////				double oldAttack = team.attack;
////				double oldDefence = team.defence;
////				team.attack = 0.025*(Team.attackRatio(1)-team.attack)+team.attack;
////				team.defence = 0.0125*(Team.defenceRatio(1)-team.defence)+team.defence;
//				//System.out.println(team.name+" on a "+team.winStreak+" match unbeaten streak increased attack from "+oldAttack+" to "+team.attack+". Difference = "+(team.attack-oldAttack)+" increased defence from "+oldDefence+" to "+team.defence+". Difference = "+(team.defence-oldDefence));
//			}
//			if(team.loseStreak >= 2){
////				double oldAttack = team.attack;
////				double oldDefence = team.defence;
////				if(team.attack > 0.1) team.attack = team.attack-0.025*(team.attack);
////				if(team.defence > 0.85) team.defence = team.defence-0.004*(team.defence);
//				//System.out.println(team.name+" on a "+team.loseStreak+" loss streak decreased attack from "+oldAttack+" to "+team.attack+". Difference = "+(team.attack-oldAttack)+" decreased defence from "+oldDefence+" to "+team.defence+". Difference = "+(team.defence-oldDefence));
//			} else if(team.loseStreak >= 1) {
////				double oldAttack = team.attack;
////				double oldDefence = team.defence;
////				if(team.attack > 0.16) team.attack = team.attack-0.025*(team.attack);
//				//System.out.println(team.name+" on a "+team.loseStreak+" loss streak decreased attack from "+oldAttack+" to "+team.attack+". Difference = "+(team.attack-oldAttack)+" decreased defence from "+oldDefence+" to "+team.defence+". Difference = "+(team.defence-oldDefence));
//
//			}
//		}
		
	}
	
	public int getHomeGoals(){
		return homeGoals;
	}
	
	public int getAwayGoals(){
		return awayGoals;
	}
	
	public int getHomePoints(){
		return homePoints;
	}
	
	public int getAwayPoints(){
		return awayPoints;
	}
	
	public Team getHomeTeam() {
		return homeTeam;
	}
	
	public Team getAwayTeam() {
		return awayTeam;
	}

	public boolean hasHomeTeamWon() {
		return homeTeamWon;
	}

	public boolean isCupGame() {
		return cupGame;
	}

	public int getHomePens() {
		return homePens;
	}

	public int getAwayPens() {
		return awayPens;
	}
}
