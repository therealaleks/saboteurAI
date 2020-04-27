package student_player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;


public class MyTools {
	
	//historical data on cards we know have been played
	//an array will be used to count how many of each of the 16 playable tiles have been played
	public static int[] history = new int[16];
	//a counter to count how many of each of the 4 playable cards have been played
	public static int mapsUsed = 0;
	public static int destroysUsed = 0;
	public static int bonusUsed = 0;
	public static int malusUsed = 0;
	//all other unidentifiable played cards (cards opponent dropped or maps that they used)
	public static int dropped = 0;
	
	//storing last turns malus numbers
	public static int myNbMalus = 0;
	public static int oppNbMalus = 0;
	
	//storing current data on who is malused
	public static boolean imMalused = false;
	public static boolean oppMalused = false;

	//stored data on if we played a tile last turn and which
	public static boolean playedTile = false;
	public static int[] posPlayed = new int[2];
	
	//most current data on cards that have either been played or that are in our possession, recalculated at every turn
	//(each index counts the number of plays for its card.) 0-15: 16 playable tiles, 16-19: (in this order) destroy, malus, bonus map
	//20: dropped
	public static int[] handIntoAccountTemp;

	//memory of the previous turn's board
	public static SaboteurTile[][] lastBoard;
    
	//update function to be run first thing every turn. This updates our data
    public static void updateData(SaboteurBoardState pBoard) {
    	
    	//checking if anyone's malused or not
    	imMalused = (pBoard.getNbMalus(pBoard.getTurnPlayer()) > 0);
    	oppMalused = (pBoard.getNbMalus((1-pBoard.getTurnPlayer())) > 0);
    	
    	//we init a change counter. this is because each player must do something but the only thing we can't track are drops
    	//so, at every update, we can log (2-changes) drops.
    	int changes = 0;
    	
    	if(pBoard.getTurnNumber() == 1 || pBoard.getTurnNumber()==2) {
    		changes=1;
    		reInit();
    	}
    	
    	//reinitialize since we recalculate at every turn 
    	handIntoAccountTemp = new int [21];
    	
    	//get the current hand
    	ArrayList<SaboteurCard> currentHand = pBoard.getCurrentPlayerCards();
    	//get the current board
    	SaboteurTile[][] currentBoard = pBoard.getHiddenBoard();
    	
    	//if first turn, save current board as last board
    	if (lastBoard == null) {
    		lastBoard = pBoard.getHiddenBoard();
    	}
    	
    	for(int i =0; i < 14; i++) {
    		for(int z = 0; z < 14; z++) {
    			//iterating through the board tiles
    			
    			//if a tile remains null, we move on
    			if(currentBoard[i][z] == null && lastBoard[i][z] == null ) {
					continue;
				}
    			//if a tile remains a tile, we move on unless its one of the first two turns
    			else if(currentBoard[i][z] != null && lastBoard[i][z] != null) {
    				
    				if(pBoard.getTurnNumber() == 1 ||pBoard.getTurnNumber() == 2) {
    		    		if(!(i==5&&z==5)&&!(i==12&&z==3)&&!(i==12&&z==5)&&!(i==12&&z==7)) {
    		    			
    		    			int currentTileIdx =  Integer.parseInt(currentBoard[i][z].getIdx().replaceAll("[^0-9]", ""));
    	    				history[currentTileIdx]++;
    	    				changes++;
    		    		}
    		    	}
    				continue;
    			}
    			//if a null tile becomes not null, a tile has been placed, so we update our data accordingly
    			else if (currentBoard[i][z] != null && lastBoard[i][z] == null ) {
    				
    				int currentTileIdx =  Integer.parseInt(currentBoard[i][z].getIdx().replaceAll("[^0-9]", ""));
    				history[currentTileIdx]++;
    				
    				changes++;	
    			}
    			//if a tile becomes null, a destroy has been used
    			else if (currentBoard[i][z] == null && lastBoard[i][z] != null ) {
    				destroysUsed++;
    				changes++;

    			}
    		}
    	}
    	
    	//we then iterate through our hand to update our temporary "hand into account" data array
    	for( SaboteurCard pCard : currentHand) {
    		
    		String CurrentCardName = (pCard).getName();
    		
    		switch(CurrentCardName) {
    			case "Destroy" :
    				handIntoAccountTemp[16]++;
    				break;
    			case "Malus" :
    				handIntoAccountTemp[17]++;
    				break;
    			case "Bonus" :
    				handIntoAccountTemp[18]++;
    				break;
    			case "Map" :
    				handIntoAccountTemp[19]++;
    				break;
    			case "":
    				break;
    			default:	
    				handIntoAccountTemp[Integer.parseInt(CurrentCardName.replaceAll("[^0-9]", ""))]++;	
    		}
    	}
    	
    	
    	//here we infer if a malus has been used on us
    	if(pBoard.getNbMalus(pBoard.getTurnPlayer()) > myNbMalus) {
    		malusUsed++;
    		changes++;
    	}
    	//save current malus state
    	myNbMalus = pBoard.getNbMalus(pBoard.getTurnPlayer());
    	
    	//here we infer if the opponent has used a bonus
    	if (pBoard.getNbMalus(1- pBoard.getTurnPlayer()) < oppNbMalus) {
    		bonusUsed++;
    		changes++;
    	}
    	//save current oppononent malus state
    	oppNbMalus = pBoard.getNbMalus(1- pBoard.getTurnPlayer());
    	
    	//if less than 2 changes was detected, we mark the difference as dropped
    	System.out.println("CHANGES:"+changes);
    	dropped += 2-changes;
    	
    	//we add historical data to the hand into account data
    	for( int i =0 ; i<16;i++){
    		handIntoAccountTemp[i] += history[i];
    	}
    	handIntoAccountTemp[16] += destroysUsed;
    	handIntoAccountTemp[17] += malusUsed;
    	handIntoAccountTemp[18] += bonusUsed;
    	handIntoAccountTemp[19] += mapsUsed;
    	handIntoAccountTemp[20] += dropped;
    	
    	
    	//save current board as last board
    	lastBoard = currentBoard;
    	
    	//printing out data
    	int totalPlayed = 0;
    	for(int i =0; i<21 ; i++) {
    		totalPlayed +=handIntoAccountTemp[i];
    		System.out.println(i+"'s PLAYED: "+ handIntoAccountTemp[i]);
    	}
    	System.out.println("total known:"+totalPlayed);
    }
    
    //method that player calls to communicate the move it decided to play at each turn
    public static void movePlayed(SaboteurMove pMove, ArrayList<SaboteurCard> pHand) {
    	//get the name of the card that we decided to play
    	String name = pMove.getCardPlayed().getName();
    	
    	//get a boolean to indicate if we played a tile (used for identifying the tile opponent played)
    	playedTile = false;
    	switch (name.split(":")[0]){
        	case "Tile":
        		playedTile = true;
        		posPlayed = pMove.getPosPlayed();
        		break;
        	case "Map": 
        		//The code in the update function wont detect any use of a map by the player, so itll be marked as dropped. Here, correct that inacurracy
        		mapsUsed++;
        		dropped--;
        		break;
        	case "Drop":
        		//we are primarily interested in knowing specifically which cards are taken out of the game. 
        		//So for all the cards that we dropped, since we know what they are, we'll instead mark them as used
        		
        		//we move the increment from drop to one of the cases below
        		dropped--;
        		String droppedName = pHand.get((pMove.getPosPlayed())[0]).getName();
        	
        		switch(droppedName.split(":")[0]) {
        			case "Tile":
        				history[Integer.parseInt(droppedName.replaceAll("[^0-9]", ""))]++;
        				break;
        			case "Map":
        				mapsUsed++;
        				break;
        			case "Malus":
        				malusUsed++;
        				break;
        			case "Bonus":
        				bonusUsed++;
        				break;
        			case "Destroy":
        				destroysUsed++;
        				break;
        		}
        		break;
        	case "Malus":
        		//same as map
        		malusUsed++;
        		dropped--;
        		break;
        	case "Bonus":
        		//same as map
        		bonusUsed++;
        		dropped--;
        		break;
    	}

    }
    
    //method to check if player is malused
    public static boolean amIMalused() {
    	return imMalused;
    }
    //method to check if opponent is malused
    public static boolean isOppMalused() {
    	return oppMalused;
    }
    
    //method to reinit our memory for a new game
    public static void reInit() {
    	history = new int[16];
    	mapsUsed = 0;
    	destroysUsed = 0;
    	bonusUsed = 0;
    	malusUsed = 0;
    	dropped = 0;
    	
    	myNbMalus = 0;
    	oppNbMalus = 0;
    	playedTile = false;
 
    	lastBoard = null;
    }
    
    //pass arguments as strings "0" to "15" for each of the 16 tiles. "destroy", "malus", "bonus" and "map" for the cards of which they are the name of
    //returns the number of remaining cards corresponding to the argument
    public static int getRemainingCard(String card) {
    	int remaining=0;
    	//we use the default deck configuration
    	Map<String,Integer> compo =new HashMap<String, Integer>();
        compo.put("0",4);
        compo.put("1",1);
        compo.put("2",1);
        compo.put("3",1);
        compo.put("4",1);
        compo.put("5",4);
        compo.put("6",5);
        compo.put("7",5);
        compo.put("8",5);
        compo.put("9",5);
        compo.put("10",3);
        compo.put("11",1);
        compo.put("12",1);
        compo.put("13",1);
        compo.put("14",1);
        compo.put("15",1);
        compo.put("destroy",3);
        compo.put("malus",2);
        compo.put("bonus",4);
        compo.put("map",6);
        //based on the argument, we substract what we know so far from the starting amount
        if(card.equals("destroy")) {
        	remaining = compo.get(card) - handIntoAccountTemp[16];
        }else if(card.equals("malus")) {
        	remaining = compo.get(card) - handIntoAccountTemp[17];
        }else if(card.equals("bonus")) {
        	remaining = compo.get(card) - handIntoAccountTemp[18];
        }else if(card.equals("map")) {
        	remaining = compo.get(card) - handIntoAccountTemp[19];
        }else if(Integer.parseInt(card)>= 0 && Integer.parseInt(card)<= 15 ) {
        	remaining = compo.get(card) - handIntoAccountTemp[Integer.parseInt(card)];
        }else {
        	return 100;
        }
        
        return remaining;
    }
    //to get the number of times a specific card has been used or is in our hand(to the best of our knowledge)
    public static int getNumberPlayed(String card) {
    	int played=0;

    	
        //based on the argument, we just return what we know so far
        if(card.equals("destroy")) {
        	played = handIntoAccountTemp[16];
        }else if(card.equals("malus")) {
        	played = handIntoAccountTemp[17];
        }else if(card.equals("bonus")) {
        	played = handIntoAccountTemp[18];
        }else if(card.equals("map")) {
        	played = handIntoAccountTemp[19];
        }else if(Integer.parseInt(card)>= 0 && Integer.parseInt(card)<= 15 ) {
        	played = handIntoAccountTemp[Integer.parseInt(card)];
        }else {
        	return 100;
        }
        
        return played;
    }
    
    public static int[] getDataArray() {
    	return handIntoAccountTemp.clone();
    }
    
    //to get the number of cards not played yet
    public static int getRemainingDeckSize() {
    	int played = 0;
    	//we loop and sum up all the data we have so far (handIntoAccountTemp)
    	for(int i =0; i<21 ; i++) {
    		played += handIntoAccountTemp[i];
    	}
    	
    	return 55- played;
    }
    //obtain the number of cards played or dropped for which we have no knowledge of their name
    public static int getNbDropedCards() {
    	return handIntoAccountTemp[20];
    }
    
    //methods that might be used to calculate probabilities
    public static int choose(int N, int K) {
    	return factorial(N)/(factorial(N-K)*factorial(K));
    }
    
    public static int factorial(int n) {
    	if(n == 0) return 1;
    	else return n*factorial(n-1);
    }
}
