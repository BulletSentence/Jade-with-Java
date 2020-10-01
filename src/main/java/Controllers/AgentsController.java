/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import Model.Protocol;
import Model.Sudoku;
import View.SudokuGUI;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

public class AgentsController extends GuiAgent {

    public Sudoku sudoku;
    private LinkedList<String> operativeAgents;
    private HashMap<String, Point> pivotHash;
    private LinkedList<JLabel> labels;
    SudokuGUI gui;
    boolean isRunning = false;
    ContainerController cc;

    @Override
    protected void setup() {
        gui = new SudokuGUI();
        gui.setVisible(true);
        ControllerBehaviour controllerBehaviour = new ControllerBehaviour();
        addBehaviour(controllerBehaviour);
        gui.execute.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                guiSolve();
                gui.txtConsole.setText("Procesando ...");
                isRunning = true;
                gui.execute.setEnabled(false);
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

    }

    public void guiSolve() {
        int size = 9; // DEBE SER UN PARAMETRO ejjeje
        this.sudoku = new Sudoku(size, 3);
        gui.log.append("Sudoku inicial \n");
        sudoku.showSudoku(sudoku.getmSudoku(), gui.log);
        getLabels();
        paintStaticNumbers();
        //sudoku.showSudoku(sudoku.getmSudoku());
        this.operativeAgents = new LinkedList<>();
        this.pivotHash = new HashMap();
        this.createOperativeAgents();
    }

    private void getLabels() {
        labels = new LinkedList<>();
        Container container = gui.s00.getParent();
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel) {
                labels.add((JLabel) child);
            }

        }
    }

    private void createOperativeAgents() {

        int numbQueadrants = this.sudoku.getNumberOfQuadrantsForRow();
        int sizequadrants = this.sudoku.getSizeOfQuadrant();
        int row = 0, column = 0;

        Runtime rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        
        cc = rt.createAgentContainer(p);
        AgentController agent;

        for (int i = 0; i < numbQueadrants; i++) {
            for (int j = 0; j < numbQueadrants; j++) {
                Point pivot = new Point();
                pivot.x = row;
                pivot.y = column;

                Object args[] = new Object[2];
                args[0] = pivot.x;
                args[1] = pivot.y;
                String name = "agent" + row + "" + column;

                try {
                    agent = cc.createNewAgent(name, "Agents.OperativeAgent", args);
                    operativeAgents.addLast(name);
                    this.pivotHash.put(name, pivot);

                    agent.start();

                } catch (StaleProxyException ex) {
                    Logger.getLogger(AgentsController.class.getName()).log(Level.SEVERE, null, ex);
                }

                column += sizequadrants;

            }
            row += sizequadrants;
            column = 0;
        }
        //System.out.println("");
    }

    public void sendMessage(String localName, char type, Object message) {
        AID idReceptor = new AID();
        //AgentController agent = operativeAgents.getFirst();
        idReceptor.setLocalName(localName);
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.setSender(getAID());
        aclMessage.addReceiver(idReceptor);
        if (type == 'S') {
            try {
                aclMessage.setContentObject((Sudoku) message);
            } catch (IOException ex) {
                Logger.getLogger(AgentsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            aclMessage.setContent(message + "");

        }
        send(aclMessage);
    }

    public void assignWork() {
        //System.out.println(pivotHash.get(operativeAgents.getFirst()).toString());
        gui.log.append("Agente: " + operativeAgents.getFirst() + " Pivote: " + pivotHash.get(operativeAgents.getFirst()) + "\n");
        sendMessage(operativeAgents.getFirst(), 'W', Protocol.WORK);

    }

    public void newSolution() {
        String operativeAgent = operativeAgents.removeLast();
        operativeAgents.addFirst(operativeAgent);
        
        gui.log.append("Vuelve a agente: " + operativeAgents.getFirst() + "\n");
        sendMessage(operativeAgents.getFirst(), 'W', Protocol.WORK);
    }

    public void applySolution(int[][] solution, Point pivot) {
        this.sudoku.setmSudoku(solution);

        if (!isSudokuSolved()) {
            operativeAgents.addLast(operativeAgents.removeFirst());
            sendMessage(operativeAgents.getFirst(), 'S', sudoku);
            //assignWork();
        } else {
            isRunning = false;
            gui.txtConsole.setText("¡ Solución encontrada !");
            //gui.execute.setEnabled(true);
            
            sudoku.showSudoku(this.sudoku.getmSudoku(), gui.log);
            //gui.txtConsole.setText("¡ Presione resolver para iniciar !");
           
        }
    }

    private boolean isSudokuSolved() {
        boolean solved = true;
        int i = 0;
        int j = 0;
        while (i < sudoku.getmSudoku().length && solved) {
            if (sudoku.getmSudoku()[i][j] == 0) {
                solved = false;
            } else {
                if (j + 1 < sudoku.getmSudoku().length) {
                    j++;
                } else {
                    i++;
                    j = 0;
                }
            }
        }
        return solved;
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {

    }

    private void paintStaticNumbers() {
        for (int i = 0; i < sudoku.getmSudoku().length; i++) {
            for (int j = 0; j < sudoku.getmSudoku().length; j++) {
                for (int k = 0; k < labels.size(); k++) {
                    JLabel currentLabel = labels.get(k);
                    if (currentLabel.getAccessibleContext().getAccessibleName().equals("s" + i + "" + j)
                            && sudoku.getInitialNumbersPositions().contains(new Point(i, j))) {
                        currentLabel.setBackground(new Color(10, 10, 10));

                    }
                }
            }
        }
    }

    private void paintQuadrant(Point quadrant, Color color) {
        for (int i = quadrant.x; i < quadrant.x + 3; i++) {
            for (int j = quadrant.y; j < quadrant.y + 3; j++) {
                for (int k = 0; k < labels.size(); k++) {
                    JLabel currentLabel = labels.get(k);
                    if (currentLabel.getAccessibleContext().getAccessibleName().equals("s" + i + "" + j)
                            && !sudoku.getInitialNumbersPositions().contains(new Point(i, j))) {
                        currentLabel.setBackground(color);

                    }
                }
            }
        }
    }

    public void updateGUI(Point quadrant) {

        paintQuadrant(quadrant, new Color(153, 204, 255));

        for (int i = 0; i < sudoku.getmSudoku().length; i++) {
            for (int j = 0; j < sudoku.getmSudoku().length; j++) {
                for (int k = 0; k < labels.size(); k++) {
                    JLabel currentLabel = labels.get(k);
                    if (currentLabel.getAccessibleContext().getAccessibleName().equals("s" + i + "" + j)) {
                        currentLabel.setText(sudoku.getmSudoku()[i][j] + "");

                    }
                }
            }
        }
        try {

            Thread.sleep(100);
            paintQuadrant(quadrant, new Color(51, 51, 51));

        } catch (InterruptedException ex) {
            Logger.getLogger(AgentsController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ControllerBehaviour extends CyclicBehaviour {

        boolean isFirstTime = true;

        @Override
        public void action() {

            if (isRunning) {

                if (isFirstTime) {
                    sendMessage(operativeAgents.getFirst(), 'S', sudoku);
                    //assignWork();

                    isFirstTime = false;
                }

                ACLMessage answer = blockingReceive();

                if (answer != null && answer.getContent() != null) {
                    switch (answer.getContent()) {
                        case Protocol.SOLUTION_NOT_FOUND:
                            System.out.println("Solución no encontrada");
                            gui.log.append("Solucion no encontrada \n");
                            
                            newSolution();
                            //sudoku.showSudoku(sudoku.getmSudoku());
                            break;

                        case Protocol.NOTIFY_SUDOKU_UPDATE:
                            assignWork();
                            break;
                        default:

                            try {
                                Sudoku sudoku = (Sudoku) (answer.getContentObject());
                                applySolution(sudoku.getmSudoku(), pivotHash.get(operativeAgents.getFirst()));
                                sudoku.showSudoku(sudoku.getmSudoku(), gui.log);
                                updateGUI(pivotHash.get(operativeAgents.getFirst()));

                            } catch (UnreadableException ex) {
                                Logger.getLogger(AgentsController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                    }
                }
            }
        }
    }
}
