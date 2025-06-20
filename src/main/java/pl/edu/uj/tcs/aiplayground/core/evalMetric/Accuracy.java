package pl.edu.uj.tcs.aiplayground.core.evalMetric;

import javafx.util.Pair;
import pl.edu.uj.tcs.aiplayground.core.Dataset;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;

import java.util.ArrayList;

public class Accuracy {
    public static AccAndLoss eval(NeuralNet neuralNet, Dataset.DataLoader dataLoader, LossFunc lossFunc) {
        int correct = 0;
        int all = 0;
        double loss = 0;
        ArrayList<Pair<Tensor, Tensor>> datapoints;
        while (dataLoader.hasNext()) {
            datapoints = dataLoader.next();
            Tensor output;
            for (Pair<Tensor, Tensor> pair : datapoints) {
                output = neuralNet.forward(pair.getKey().transpose(), null);
                double max = Double.NEGATIVE_INFINITY;
                int maxIndex1 = -1, maxIndex2 = -1;
                for (int i = 0; i < output.rows; i++) {
                    for (int j = 0; j < output.cols; j++) {
                        if (output.data[i][j] > max) {
                            max = output.data[i][j];
                            maxIndex1 = i;
                            maxIndex2 = j;
                        }
                    }
                }

                if (pair.getValue().transpose().data[maxIndex1][maxIndex2] == 1) {
                    correct++;
                }
                all++;

                loss += lossFunc.loss(output, pair.getValue().transpose());
            }
        }
        return new AccAndLoss((double) correct / (double) all * 100, loss / all);
    }

    public record AccAndLoss(double accuracy, double loss) {
    }
}
