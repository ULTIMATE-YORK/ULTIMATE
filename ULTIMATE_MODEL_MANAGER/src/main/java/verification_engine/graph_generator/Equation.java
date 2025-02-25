package verification_engine.graph_generator;

import Jama.Matrix;
import java.util.List;

public class Equation {
    private Matrix coefficients; // Coefficient matrix (A)
    private Matrix constants;    // Constant terms (B)
    private int numVariables;

    public Equation(double[][] coefficients, double[] constants) {
        if (coefficients.length != constants.length) {
            throw new IllegalArgumentException("Coefficient matrix row count must match constants vector size.");
        }
        
        this.coefficients = new Matrix(coefficients);
        this.constants = new Matrix(constants, constants.length);
        this.numVariables = coefficients[0].length;
    }

    public double[] solve() {
        if (coefficients.getRowDimension() != coefficients.getColumnDimension()) {
            throw new IllegalArgumentException("System must have equal number of equations and variables.");
        }

        // Solve Ax = B -> x = A^(-1) * B (if A is invertible)
        Matrix solution;
        if (coefficients.det() != 0) { // Check if A is invertible
            solution = coefficients.inverse().times(constants);
        } else { // Use LU Decomposition if not directly invertible
            solution = coefficients.solve(constants);
        }

        return solution.getColumnPackedCopy(); // Return solution as an array
    }

    public void printEquation() {
        System.out.println("System of Equations:");
        for (int i = 0; i < coefficients.getRowDimension(); i++) {
            for (int j = 0; j < coefficients.getColumnDimension(); j++) {
                System.out.print(coefficients.get(i, j) + "x" + (j + 1) + " ");
            }
            System.out.println("= " + constants.get(i, 0));
        }
    }

    public static void main(String[] args) {
        double[][] coefficients = { { 2, -1 }, { -4, 6 } };
        double[] constants = { 3, 9 };
        //2x1-x2=3 and -4x1+6x2=9

        Equation eq = new Equation(coefficients, constants);
        eq.printEquation();
        
        double[] solution = eq.solve();
        System.out.println("\nSolution:");
        for (int i = 0; i < solution.length; i++) {
            System.out.println("x" + (i + 1) + " = " + solution[i]);
        }
    }
}