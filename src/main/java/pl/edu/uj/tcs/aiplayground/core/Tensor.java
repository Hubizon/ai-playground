package pl.edu.uj.tcs.aiplayground.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tensor {

    public double[][] data;
    public double[][] gradient;
    public int rows;
    public int cols;

    public Tensor(double data) {
        this.data = new double[][]{{data}};
        this.gradient = new double[][]{{0}};
        rows = 1;
        cols = 1;
    }

    public Tensor(double[][] data, int rows, int cols) {

        if (rows * cols != data.length * data[0].length) {
            throw new IllegalArgumentException(String.format("Data array length %d does not match shape %d, %d", data.length, rows, cols));
        }
        this.data = data;
        this.gradient = new double[rows][cols];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                this.gradient[i][j] = 0;
            }
        }
        this.rows = rows;
        this.cols = cols;
    }

    public static Tensor zeros(int rows, int cols) {
        double[][] data = new double[rows][cols];
        return new Tensor(data, rows, cols);
    }

    public static Tensor zerosLike(Tensor other) {
        double[][] zeros = new double[other.rows][other.cols];
        return new Tensor(zeros, other.rows, other.cols);
    }

    public static Tensor randomMatrix(int rows, int cols, double min, double max) {
        double[][] data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = min + Math.random() * (max - min);
            }
        }
        return new Tensor(data, rows, cols);

    }

    public static Tensor add(Tensor a, Tensor b, ComputationalGraph graph) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException(String.format("Shape mismatch! a=%d, %d, b= %d, %d", a.rows, a.cols, b.rows, b.cols));
        }
        Tensor result = zerosLike(a);
        for (int i = 0; i < result.rows; i++) {
            for (int j = 0; j < result.cols; j++) {
                result.data[i][j] = b.data[i][j] + a.data[i][j];
            }

        }
        ArrayList<Tensor> addends = new ArrayList<Tensor>();
        addends.add(a);
        addends.add(b);
        if(graph!=null){graph.addNode(result, addends, "+");}
        return result;
    }

    public static Tensor multiply(Tensor a, Tensor b, ComputationalGraph graph) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException(String.format("Shape mismatch! a=%d, %d, b= %d, %d", a.rows, a.cols, b.rows, b.cols));
        }
        Tensor result = zerosLike(a);
        for (int i = 0; i < result.rows; i++) {
            for (int j = 0; j < result.cols; j++) {
                result.data[i][j] = b.data[i][j] * a.data[i][j];
            }

        }
        ArrayList<Tensor> factors = new ArrayList<>();
        factors.add(a);
        factors.add(b);
        if(graph!=null) {graph.addNode(result, factors, "*");}
        return result;
    }

    public static Tensor matMul(Tensor a, Tensor b, ComputationalGraph graph) {
        if (a.cols != b.rows) {
            throw new IllegalArgumentException(String.format("Shape mismatch for matMul! a=%d, %d, b= %d, %d", a.rows, a.cols, b.rows, b.cols));
        }
        Tensor result = zeros(a.rows, b.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < b.cols; j++) {
                for (int k = 0; k < a.cols; k++) {
                    result.data[i][j] += a.data[i][k] * b.data[k][j];
                }
            }
        }

        ArrayList<Tensor> factors = new ArrayList<>();
        factors.add(a);
        factors.add(b);
        if(graph!=null) {graph.addNode(result, factors, "matMul");}
        return result;
    }

    public static Tensor Relu(Tensor a, ComputationalGraph graph) {
        Tensor result = Tensor.zerosLike(a);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                if (a.data[i][j] <= 0) result.data[i][j] = 0;
                else result.data[i][j] = a.data[i][j];
            }

        }
        if(graph!=null){graph.addNode(result, new ArrayList<>(List.of(a)), "relu");}
        return result;
    }

    public static Tensor Sigmoid(Tensor a, ComputationalGraph graph) {
        Tensor result = Tensor.zerosLike(a);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = 1 / (1 + Math.exp(-a.data[i][j]));
            }
        }
        if(graph!=null){ graph.addNode(result, new ArrayList<>(List.of(a)), "sigmoid");}
        return result;
    }

    public void fill(double value) {
        for (int i = 0; i < rows; i++) {
            Arrays.fill(data[i], value);
        }

    }

    public Tensor sumRows(ComputationalGraph graph) {
        double[][] sum = new double[1][cols];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                sum[0][i] += data[j][i];
            }
        }
        Tensor result = new Tensor(sum, 1, cols);
        ArrayList<Tensor> comps = new ArrayList<>();
        comps.add(this);
        if(graph!=null){graph.addNode(result, comps, "sumRows");}
        return result;
    }

    public Tensor sumCols(ComputationalGraph graph) {
        double[][] sum = new double[rows][1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum[i][0] += data[i][j];
            }
        }
        Tensor result = new Tensor(sum, rows, 1);
        ArrayList<Tensor> comps = new ArrayList<>();
        comps.add(this);
        if(graph!=null){graph.addNode(result, comps, "sumCols");}
        return result;
    }

    public Tensor transpose() {

        double[][] transposedData = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedData[j][i] = data[i][j];
            }
        }
        return new Tensor(transposedData, cols, rows);
    }
}
