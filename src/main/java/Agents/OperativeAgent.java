    package Agents;

import Model.Protocol;
import Model.Sudoku;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.awt.Point;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dericop
 */
public class OperativeAgent extends Agent {

    private Sudoku sudoku;
    private Point quadrantPivot;
    private Point backtrackingPivot;
    private int[][] lastSolution;
    


    @Override
    protected void setup() {
        OperativeBehaviour operativeBehaviour = new OperativeBehaviour();
        int px = (int) this.getArguments()[0];
        int py = (int) this.getArguments()[1];

        quadrantPivot = new Point(px, py);
        backtrackingPivot = new Point(quadrantPivot.x+2,quadrantPivot.y+2);
        this.addBehaviour(operativeBehaviour);
    }

    private void resolveTask() {

    }

    private void notifySolution() {

    }

    private void notifyNonSolution() {

    }

    class OperativeBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage message = receive();
            

            if (message != null) {

                if (Protocol.WORK.equals(message.getContent())) {
                    if (sudoku != null) {
                        System.out.println("Agent: " + this.getAgent().getLocalName() + " pivote: " + backtrackingPivot);
                        boolean response = sudoku.solve(getPivot(), backtrackingPivot, lastSolution);
                        System.out.println("pivote: " + backtrackingPivot);
                        if (response) {
                            lastSolution = sudoku.getmSudoku();
                            reply(message, 'S', sudoku);
                        } else {
                            lastSolution = null;
                            reply(message, 'W', Protocol.SOLUTION_NOT_FOUND);

                        }
                    }
                } else {
                    Sudoku su;
                    try {
                        su = ((Sudoku) message.getContentObject());
                        sudoku = su;

                        reply(message, 'W', Protocol.NOTIFY_SUDOKU_UPDATE);

                    } catch (UnreadableException ex) {
                        Logger.getLogger(OperativeAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }

        private void reply(ACLMessage message, char c, Object rep) {
            ACLMessage answer = message.createReply();
            answer.setPerformative(ACLMessage.INFORM);

            if (c == 'S') {
               
                try {
                    answer.setContentObject((Sudoku) rep);
                    
                } catch (IOException ex) {
                    Logger.getLogger(OperativeAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            } else {

                answer.setContent(rep + "");
            }

            send(answer);
        }
    }

    /**
     * @return the pivot
     */
    public Point getPivot() {
        return quadrantPivot;
    }
    
    public Point getBacktrackingPivot() {
        return backtrackingPivot;
    }
    
    public void setBacktrackingPivot(Point backtrackingPivot) {
        this.backtrackingPivot = backtrackingPivot;
    }
    
    
}
