package student_player;
import java.io.*;
import java.util.*;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;

public class StudentState {
    //how many bonuses will I need to get unblocked?
    public static int myMalus;
    //how many bonuses will they need to get unblocked?
    public static int enemyMalus;
    //likelihood of the nugget being at tile n (originally 1/3 for all 3)
    public static float [] prizeProb={1/3, 1/3, 1/3};
    //for how many tiles are we sure the nugget is/isn't located there?
    //(either 1 (if we know it isn't at tile n) or 3)
    public static int numKnown=0;
    //2d array of saboteur tiles or null
    public SaboteurTile[][] board;
    //which turn is it??
    public int turn;
    public int turnplayer;
    //All tile reachable from the entrance
    public ArrayList<SaboteurTile> Reachable;
    //And their respective coordinates
    public ArrayList<int[]> Reachableint;

    StudentState(SaboteurBoardState pbs){
        this.turnplayer = pbs.getTurnPlayer();
        this.board = pbs.getHiddenBoard();
        this.getReachable();
        this.myMalus=pbs.getNbMalus(pbs.getTurnPlayer());
        this.enemyMalus=pbs.getNbMalus((pbs.getTurnPlayer()+1) % 2);
        this.turn=pbs.getTurnPlayer();
        for (int i=0; i<3; i++){
            if (board[12][2*i+3].getIdx().equals("nugget")){
                prizeProb[i]=1;
                prizeProb[(i+2) % 3]=0;
                prizeProb[(i+1) % 3]=0;
                numKnown=3;
                break;
            }
            if (board[12][2*i+3].getIdx().equals("hidden1") || board[12][2*i+3].getIdx().equals("hidden2")){
                numKnown++;
                prizeProb[i]=0;
            } else{
                prizeProb[i]=-1;
            }
        }
        if (numKnown>=2){
            numKnown=3;
            for (int i=0; i<3; i++){
                if (prizeProb[i]!=0){
                    prizeProb[i]=1;
                } else{
                    prizeProb[i]=0;
                }
            }
        } else{
            for (int i=0; i<3; i++){
                if (prizeProb[i]!=0){
                    prizeProb[i]= (float) 1/(3-numKnown);
                } else{
                    prizeProb[i]=0;
                }
            }
        }
    }

    //Modified version of the one from Saboteur.boardState
    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < 14 && 0 <= pos[1] && pos[1] < 14)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (prizeProb[i]!=0){
                objHiddenList.add(this.board[12][2*i+3]);
            }
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<14-1) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = this.board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<14-1) {
            SaboteurTile neighborCard = this.board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }

    //Modified version of the one from Saboteur.boardState
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
        ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 14; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < 14 && 0 <= j+moves[m][1] && j+moves[m][1] < 14) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
    }

    //Heuristic for determining yield of the current board state
    //Returns the average of the reachable tiles to the closest 
    //end tile that still could contain the nugget
    static float properValue(StudentState s){
        float sum=0;
        int counter=0;
        for (int[] i : s.Reachableint){
            SaboteurTile t = s.board[i[0]][i[1]];
            String cardType=t.getName().split(":")[1];
            if (cardType.equals("4_flip") || cardType.equals("4") || cardType.equals("15") || cardType.equals("1") || cardType.equals("12") || cardType.equals("12_flip")){
                sum -=getDist(i);
            }else if (cardType.equals("2") || cardType.equals("2_flip") || cardType.equals("11") || cardType.equals("11_flip")){
                sum -=3*getDist(i);
            }else if (cardType.equals("3") || cardType.equals("3_flip") || cardType.equals("14") || cardType.equals("14_flip")){
                sum -=2*getDist(i);
            }else if (cardType.equals("13")){
                sum -=4*getDist(i);
            } else{
                sum+=getDist(i);
            }
            counter++;
        }
        return (sum/(s.Reachableint.size()));
    }

    //returns the distance between a coordinate on the board and the nerest
    //end tile that still could contain the nugget
    static float getDist(int[] coord){
        float sum=0;
        for (int i=0; i<3; i++){
            double j=prizeProb[i];
            if (j==0){
                continue;
            }
            float h= (float) (12-Math.abs(coord[0]-12)+(14-Math.abs(i*2+3-coord[1])));
            if (h>sum) {
                sum=h;
            }
        }
        return sum;
    }

    //sets the Reachable 
    //and the Reachableint values for this class
    void getReachable(){
        ArrayList<int[]> queue = new ArrayList<int[]>();
        ArrayList<SaboteurTile> visited = new ArrayList<SaboteurTile>();
        ArrayList<int[]> visitedint = new ArrayList<int[]>();
        queue.add(new int[]{5,5});
        float sum=0;
        float closest=0;

        while (queue.size() > 0){
            int[] i = queue.remove(0);
            SaboteurTile j = board[i[0]][i[1]];
            SaboteurTile jd;
            SaboteurTile jl;
            SaboteurTile jr;
            SaboteurTile ju;
            if (i[0]+1 >= 0){
                jd = this.board[i[0]+1][i[1]];
            } else {
                jd = null;
            }
            if (i[0]-1 <14){
                ju = this.board[i[0]-1][i[1]];
            } else {
                ju = null;
            }
            if (i[1]-1 >= 0){
                jl = this.board[i[0]][i[1]-1];
            } else {
                jl = null;
            }
            if (i[1]+1 <14){
                jr = this.board[i[0]][i[1]+1];
            } else {
                jr = null;
            }
            if (j.getIdx().equals("entrance")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("0")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
            }
            else if (j.getIdx().equals("5")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("5_flip")){
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
            }
            else if (j.getIdx().equals("6")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
            }
            else if (j.getIdx().equals("6_flip")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("7")){
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("7_flip")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
            }
            else if (j.getIdx().equals("8") || j.getIdx().equals("hidden1") || j.getIdx().equals("hidden2")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("9")){
                if (jd!=null && !(visited.contains(jd))){
                    queue.add(new int[]{i[0]+1, i[1]});
                    visited.add(jd);
                    visitedint.add(new int[]{i[0]+1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("9_flip")){
                if (ju!=null && !(visited.contains(ju))){
                    queue.add(new int[]{i[0]-1, i[1]});
                    visited.add(ju);
                    visitedint.add(new int[]{i[0]-1, i[1]});
                }
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else if (j.getIdx().equals("10")){
                if (jl!=null && !(visited.contains(jl))){
                    queue.add(new int[]{i[0], i[1]-1});
                    visited.add(jl);
                    visitedint.add(new int[]{i[0], i[1]-1});
                }
                if (jr!=null && !(visited.contains(jr))){
                    queue.add(new int[]{i[0], i[1]+1});
                    visited.add(jr);
                    visitedint.add(new int[]{i[0], i[1]+1});
                }
            }
            else {
                continue;
            }
        }
        this.Reachable=visited;
        this.Reachableint=visitedint;
    }
}
