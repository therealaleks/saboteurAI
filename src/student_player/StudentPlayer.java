package student_player;

import Saboteur.SaboteurBoard;
import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurMove;
import Saboteur.SaboteurBoardState;
import Saboteur.cardClasses.*;
import java.io.*;
import java.util.*;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("SaboteurAI");
    }

    SaboteurBoardState oldboard;

    /**
    Given an instance of our custom state obect, and a list of tile cards, this
    method iterates through the entire list, determines all possible moves involving
    those cards, and of those moves, returns that with the highest yield.
    */
    public float lookAhead(StudentState ss, ArrayList<SaboteurTile> i){
        float max=0;
        SaboteurCard hello=null;
        for (SaboteurMove move : getTileMoves(ss, i)) {
            int[] coords = move.getPosPlayed();
            SaboteurCard card = move.getCardPlayed();
            ss.board[coords[0]][coords[1]] = (SaboteurTile) card;
            ss.getReachable();
            float tmp=StudentState.properValue(ss);
            if (tmp > max){
                max = tmp;
                hello=card;
            }
            ss.board[coords[0]][coords[1]] = null;
        }
        return max;
    }

    /**
     * From all possible moves, get moves involving some tile card, 
     * create a list containing all those tiles, and return it.
     * Helper function of sorts for lookAhead.
    */
    public ArrayList<SaboteurTile> getTileCards(ArrayList<SaboteurMove> i){
        ArrayList<SaboteurTile> r = new ArrayList<SaboteurTile>();
        for (SaboteurMove j : i){
            SaboteurCard card = j.getCardPlayed();
            String cardName = card.getName().split(":")[0];
            if (cardName.equals("Tile")){
                String cardType = card.getName().split(":")[1];
                r.add(new SaboteurTile(cardType));
            }
        }
        return r;
    }

    /**
     * From all possible tile cards get all possible moves we can make with those
     * cards given a certain board state.
     * Helper function of sorts for lookAhead.
    */
    public ArrayList<SaboteurMove> getTileMoves(StudentState ss, ArrayList<SaboteurTile> i){
        ArrayList<SaboteurMove> r = new ArrayList<SaboteurMove>();
        for (SaboteurTile j : i){
            String cardType = j.getName().split(":")[1];
            for (int[] k : ss.possiblePositions(j)){
                r.add(new SaboteurMove(j, k[0], k[1], ss.turnplayer));
            }
        }
        return r;
    }

    /**
     * The method that puts everything together.
    */
    public Move moveHelper(float bench, SaboteurBoardState s){
        float maxTile=0;
        SaboteurMove maxMove=null;
        //Iterate through all legal moves...
        for (SaboteurMove move : s.getAllLegalMoves()){
            StudentState ss = new StudentState(s);
            SaboteurCard card = move.getCardPlayed();
            String cardName = card.getName().split(":")[0];
            //If we don't know where the nugget is, finding it is a priority.
            //Hence, use the map at all costs if we can and need to.
            if (cardName.equals("Map")){
                if (ss.numKnown<3){
                    return move;
                }
                //If we know where the nugget is, then the map is useless.
                //Set maps to be droppable.
            }
            //If we are blocked by a Malus, play bonus if possible.
            else if (cardName.equals("Bonus") && ss.myMalus>0){
                return move;
            }
            //It is most likely beneficial to have a blocked opponent,
            //especially with this player's strategy.
            //Hence, play Malus on opponent if possible and if
            //the above two conditions don't apply.
            else if (cardName.equals("Malus")){
                return move;
            }
            else if (cardName.equals("Tile")){
                //If we are testing a tile-related move, apply it to our custom board state.
                String cardType = card.getName().split(":")[1];
                int[] coords = move.getPosPlayed();
                ss.board[coords[0]][coords[1]] = (SaboteurTile) card;
                ss.getReachable();
                for (int[] i: ss.Reachableint){
                    //If we can reach a tile one away from the goal state and it is
                    //unlike;y that the opponent can steal the game...
                    if (coords[0]==i[0] && coords[1]==i[1]){
                        if (coords[0]==12 && (coords[1]==4 && ss.prizeProb[0]>0 || coords[1]==6 && ss.prizeProb[1]>0 ||  coords[1]==8 && ss.prizeProb[2]>0 )){
                            if (cardType.equals("0") || cardType.equals("6_flip") ||
                                    cardType.equals("8") || cardType.equals("6")){
                                    }
                                if ((MyTools.getRemainingCard("0")>4 || MyTools.getRemainingCard("6")>4 || MyTools.getRemainingCard("6_flip")>4 || MyTools.getRemainingCard("8")>4) && MyTools.getRemainingDeckSize()<10 && MyTools.getNbDropedCards()<2){
                                    continue;
                                }
                        }
                        //If we can reach a goal state this move, make sure we play a tile that will complete a path (eg favor "-+" or "L+" over "+L").
                        if (coords[0]==11 && (coords[1]==3 && ss.prizeProb[0]>0 || coords[1]==5 && ss.prizeProb[1]>0 || coords[1]==7 && ss.prizeProb[2]>0)){
                            if (cardType.equals("7_flip") || cardType.equals("5") || cardType.equals("0") || cardType.equals("6") || cardType.equals("6_flip") ||
                                    cardType.equals("8") || cardType.equals("9")){
                                return move;
                                    }
                        }
                        if (coords[0]==13 && (coords[1]==3 && ss.prizeProb[0]>0 || coords[1]==5 && ss.prizeProb[1]>0 || coords[1]==7 && ss.prizeProb[2]>0)){
                            if (cardType.equals("7") || cardType.equals("5_flip") || cardType.equals("0") || cardType.equals("6") || cardType.equals("6_flip") ||
                                    cardType.equals("8") || cardType.equals("9_flip")){
                                return move;
                                    }
                        }
                        if (coords[0]==12 && (coords[1]==2 && ss.prizeProb[0]>0 || coords[1]==4 && ss.prizeProb[1]>0 ||  coords[1]==6 && ss.prizeProb[2]>0 )){
                            if (cardType.equals("10") || cardType.equals("5") || cardType.equals("6_flip") || cardType.equals("9_flip") ||
                                    cardType.equals("8") || cardType.equals("9") ||  cardType.equals("7")){
                                return move;
                                    }
                        }
                        if (coords[0]==12 && (coords[1]==4 && ss.prizeProb[0]>0 || coords[1]==6 && ss.prizeProb[1]>0 ||  coords[1]==8 && ss.prizeProb[2]>0 )){
                            if (cardType.equals("10") || cardType.equals("5_flip") || cardType.equals("6") || cardType.equals("9_flip") ||
                                    cardType.equals("8") || cardType.equals("9") ||  cardType.equals("7_flip")){
                                return move;
                                    }
                        }
                    }
                }
                //If we cannot reach the goal state with a correct path...
                //Determine the move that is most likely to yield the highest board state
                //two moves from it.
                float tmp=lookAhead(ss, getTileCards(s.getAllLegalMoves()));
                if (maxTile < tmp){
                    maxTile=tmp;
                    maxMove=move;
                }
            }
            //Do the same with the Destroy function...
            //It is less likely for a Destroy move to decrease the yield of the board.
            //It is, however quite likely for a destroy move to alter the board state in such a way that
            //it is more likely to get increase its yield the next turn.
            else if (cardName.equals("Destroy")){
                int[] coords = move.getPosPlayed();
                ss.board[coords[0]][coords[1]] = null;
                ss.getReachable();
                float tmp=lookAhead(ss, getTileCards(s.getAllLegalMoves()));
                if (maxTile < tmp){
                    maxTile=tmp;
                    maxMove=move;
                }
            }
            //Only drop the least valuable cards (ie trap tiles which are not part of the strategy, or Map cards if we know where the nugget is located)
            else if (cardName.equals("Drop")){
                int x=move.getPosPlayed()[0];
                if (s.getPlayerCardsForDisplay(ss.turnplayer).get(x).getName().split(":")[0].equals("Map")){
                    return move;
                }
                if (s.getPlayerCardsForDisplay(ss.turnplayer).get(x).getName().split(":")[0].equals("Tile")){
                    String type=s.getPlayerCardsForDisplay(ss.turnplayer).get(x).getName().split(":")[1];
                    if (type.equals("1") || type.equals("11") || type.equals("11_flip") || type.equals("12") || type.equals("12_flip") || type.equals("13") ||  type.equals("14_flip") || type.equals("14") ||  type.equals("15") || type.equals("2") ||  type.equals("2_flip") || type.equals("3") ||  type.equals("3_flip") || type.equals("4") ||  type.equals("4_flip") ){
                        return move;
                    }
                }
            }
        }
        //return move which will yield the highest board state in two moves (only if that board state is more than the current state.
        if (maxTile>bench) {
            return maxMove;
        }
        //turns out random IS the best we can do :(
        return null;
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */

    public Move chooseMove(SaboteurBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        //MyTools.();
        MyTools.updateData(boardState);
        StudentState sss= new StudentState(boardState);
        float bench=StudentState.properValue(sss);
        Move myMove = moveHelper(bench, boardState);
        if (myMove==null){
            myMove=boardState.getRandomMove();
        }
        // Is random the best you can do?
        // Return your move to be processed by the server.
        MyTools.movePlayed((SaboteurMove) myMove, boardState.getCurrentPlayerCards());
        return myMove;
    }
}
