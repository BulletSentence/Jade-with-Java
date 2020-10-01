/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedList;
import javax.swing.JTextArea;

/**
 *
 * @author Dericop
 */
public class Sudoku implements Serializable {

    private int[][] mSudoku;
    private final long serialVersionUID = 12345L;
    private static final int sizeOfQuadrant = 3;
    private LinkedList initialNumbersPositions;
    

    public Sudoku(int size, int initNumbers) {
        this.mSudoku = new int[size][size];
        initialNumbersPositions = new LinkedList();
        //.out.println(getNumberOfQuadrantsForRow() + " cuadrantes por fila de tamaño " + this.sizeOfQuadrant);

        init(initNumbers);
    }

    public int[][] getmSudoku() {
        return mSudoku;
    }

    private void init(int initNumbers) {
        int row, column, number;
        while (initNumbers > 0) {
            row = getRandomPosition();
            column = getRandomPosition();

            if (this.getmSudoku()[row][column] == 0) {
                number = getRandomNumber() + 1;
                while (!isValidNumberInRow(number, row, mSudoku) || !isValidNumberInColumn(number, column, mSudoku) || !isValiNumberInQuadrant(number, row, column, mSudoku)) {
                    number = getRandomNumber() + 1;
                }
                this.getmSudoku()[row][column] = number;
                this.getInitialNumbersPositions().add(new Point(row, column));
                initNumbers--;
            }
        }

        //showSudoku(this.mSudoku,);
    }

    private boolean isValid(int row, int column, int number, int[][] s) {
        return isValidNumberInRow(number, row, s) && isValidNumberInColumn(number, column, s) && isValiNumberInQuadrant(number, row, column, s);
    }

    private int getRandomNumber() {
        return (int) (Math.random() * ((sizeOfQuadrant * sizeOfQuadrant)));
    }

    private int getRandomPosition() {
        return (int) (Math.random() * ((mSudoku.length)));
    }

    private boolean isValidNumberInRow(int number, int row, int[][] s) {
        boolean valid = true;
        int i = 0;
        while (i < s.length && valid) {
            if (s[row][i] == number) {
                valid = false;
            } else {
                i++;
            }
        }
        return valid;
    }

    private boolean isValidNumberInColumn(int number, int column, int[][] s) {
        boolean valid = true;
        int i = 0;
        while (i < s.length && valid) {
            if (s[i][column] == number) {
                valid = false;
            } else {
                i++;
            }
        }
        return valid;
    }

    private boolean isValiNumberInQuadrant(int number, int row, int column, int[][] s) {
        Point quadrant = this.getQuadrant(row, column);
        boolean valid = true;
        int i = quadrant.x;
        int j = quadrant.y;

        while (valid && i < quadrant.x + this.getSizeOfQuadrant()) {

            if (s[i][j] == number) {
                valid = false;
            } else {
                if (j + 1 < quadrant.y + this.getSizeOfQuadrant()) {
                    j++;
                } else {
                    j = quadrant.y;
                    i++;
                }
            }
        }
        return valid;
    }

    private Point getQuadrant(int row, int column) {
        Point quadrant = new Point();
        quadrant.x = (Math.round(row / this.getSizeOfQuadrant())) * this.getSizeOfQuadrant();
        quadrant.y = (Math.round(column / this.getSizeOfQuadrant())) * this.getSizeOfQuadrant();

        //System.out.println("(" + row + "," + column + ") : " + quadrant.toString());
        return quadrant;
    }

    private int getMaxDivisorOfNumber(int limit, int number) {
        int divisor = 0;
        int i = 0;
        while (i < limit) {
            if (i != 0 && number % i == 0) {
                divisor = i;
            }
            i++;
        }
        return divisor;
    }

    public int getNumberOfQuadrantsForRow() {
        return (this.getmSudoku().length / this.getSizeOfQuadrant());
    }

    public int counterAllQuadrants() {
        return (this.getmSudoku().length / this.getSizeOfQuadrant()) * getNumberOfQuadrantsForRow();
    }

    public void showSudoku(int[][] s, JTextArea log) {
        for (int[] mSudoku1 : s) {
            for (int j = 0; j < s.length; j++) {
                log.append(mSudoku1[j] + " ");
                System.out.print(mSudoku1[j] + " ");
            }
            log.append("\n");
            System.out.println();
        }
        log.append("\n");
        System.out.println();
    }

    /**
     * @return the sizeOfQuadrant
     */
    public int getSizeOfQuadrant() {
        return sizeOfQuadrant;
    }

    public boolean solve(int[][] s, Point quadrant) {

        for (int i = quadrant.x; i < quadrant.x + 3; i++) {
            for (int j = quadrant.y; j < quadrant.y + 3; j++) {
                if (s[i][j] != 0) {
                    continue;
                }
                for (int num = 1; num <= 9; num++) {
                    if (isValid(i, j, num, s)) {
                        s[i][j] = num;
                        if (solve(s, quadrant)) {
                            return true;
                        } else {
                            s[i][j] = 0;
                        }
                    }
                }
                return false;
            }

        }

        this.setmSudoku(s);
        return true;
    }

    private int[][] clone(int[][] s) {
        int[][] copia = new int[s.length][s.length];
        for (int i = 0; i < s.length; i++) {
            copia[i] = s[i].clone();
        }
        return copia;
    }

    public boolean solve(Point quadrantPivot, Point backtrackingPivot, int[][] lastSolution) {
        boolean solutionWasFounded = false;
        if (lastSolution != null) {
            boolean isConfigured = configureSudokuForNextSolution(quadrantPivot, backtrackingPivot, lastSolution);
            if(isConfigured)
                solutionWasFounded = solve(lastSolution, quadrantPivot);
            else
                return false;
        } else {
            solutionWasFounded = solve(this.getmSudoku(), quadrantPivot);
        }
        return solutionWasFounded;
    }

    public boolean configureSudokuForNextSolution(Point quadrantPivot, Point backtrackingPivot, int[][] lastSolution) {
        boolean configured = false;
        if (isPosibleIncrementNumberInCell(quadrantPivot, backtrackingPivot, lastSolution)) {
            restartSolutionFromBacktrackingPivot(quadrantPivot, backtrackingPivot, lastSolution);
            configured = true;
        } else {
            if (quadrantPivot.x == backtrackingPivot.x && quadrantPivot.y == backtrackingPivot.y) {
                
                return false;
                
            } else {
                Point copyBack = (Point)backtrackingPivot.clone();
                previousCell(quadrantPivot,copyBack);
                restartSolutionFromBacktrackingPivot(quadrantPivot, copyBack, lastSolution);//se esta reiniciando valores estataicos 
                configureSudokuForNextSolution(quadrantPivot, copyBack, lastSolution);
            }
        }
        return configured;

    }


    private boolean isPosibleIncrementNumberInCell(Point quadrantPivot, Point backtrackingPivot, int[][] lastSolution) {
        boolean isValid = false;
        int currentNumber = lastSolution[backtrackingPivot.x][backtrackingPivot.y];
        if (currentNumber < 9 && !this.initialNumbersPositions.contains(backtrackingPivot)) {
            int num = currentNumber + 1;
            while (num <= 9 && !isValid) {
                if (isValid(backtrackingPivot.x, backtrackingPivot.y, num, lastSolution)) {
                    lastSolution[backtrackingPivot.x][backtrackingPivot.y] = num;
                    isValid = true;
                }
                num++;
            }
        }
        return isValid;
    }

    public void restartSolutionFromBacktrackingPivot(Point quadrantPivot, Point backtrackingPivot, int[][] lastSolution) {
        Point restartReference = new Point(backtrackingPivot);

        nextCell(quadrantPivot, restartReference);
        if (restartReference.x < lastSolution.length && restartReference.y < lastSolution.length) {
            int x = restartReference.x, y = restartReference.y;
            while (x < quadrantPivot.x + 3) {
                
                if(!this.initialNumbersPositions.contains(new Point(x,y))){
                    lastSolution[x][y] = 0;
                }
                
                if (y + 1 < quadrantPivot.y + 3) {
                    y++;
                } else {
                    x++;
                    y = quadrantPivot.y;
                }
            }
        }

    }

    public void previousCell(Point quadrantPivot, Point bactrackingPivot) {

        if (bactrackingPivot.y - 1 >= quadrantPivot.y) {
            bactrackingPivot.y--;
        } else if (bactrackingPivot.x - 1 >= quadrantPivot.x) {
            bactrackingPivot.y = quadrantPivot.y + 2;
            bactrackingPivot.x--;
        }

    }

    public void nextCell(Point quadrantPivot, Point bactrackingPivot) {

        if (bactrackingPivot.y + 1 < quadrantPivot.y + 3) {
            bactrackingPivot.y++;
        } else if (bactrackingPivot.x + 1 < quadrantPivot.x + 3) {
            bactrackingPivot.y = quadrantPivot.y;
            bactrackingPivot.x++;
        }

    }

    /**
     * @param mSudoku the mSudoku to set
     */
    public void setmSudoku(int[][] mSudoku) {
        this.mSudoku = mSudoku;
    }

    /**
     * @return the initialNumbersPositions
     */
    public LinkedList getInitialNumbersPositions() {
        return initialNumbersPositions;
    }
}
