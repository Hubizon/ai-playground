package pl.edu.uj.tcs.aiplayground.core;

import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class  Dataset {

    public ArrayList<Integer> inputShape;
    public ArrayList<Integer> outputShape;
    public int size;
    public ArrayList<Pair<Tensor, Tensor>> trainData;
    public ArrayList<Pair<Tensor, Tensor>> testData;
    private Map<String, Integer> labelMap;
    private int nextLabel = 0;

    public Dataset() {
        inputShape = new ArrayList<>();
        outputShape = new ArrayList<>();
        labelMap = new HashMap<>();
    }

    public void load(String filename, float trainTestSplit) {
        trainData = new ArrayList<>();
        testData = new ArrayList<>();

        ArrayList<double[]> rawInputs = new ArrayList<>();
        ArrayList<double[]> rawOutputs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String inLine = br.readLine();
            if (inLine == null || !inLine.startsWith("IN: ")) {
                throw new IOException("Invalid file format: Missing or malformed IN: line.");
            }
            String inDimStr = inLine.substring(4).replace(",", "").trim();
            inputShape.clear();
            inputShape.add(Integer.parseInt(inDimStr));

            String outLine = br.readLine();
            if (outLine == null || !outLine.startsWith("OUT: ")) {
                throw new IOException("Invalid file format: Missing or malformed OUT: line.");
            }
            String outDimStr = outLine.substring(5).replace(",", "").trim();
            outputShape.clear();
            outputShape.add(Integer.parseInt(outDimStr));

            if (inputShape.isEmpty() || outputShape.isEmpty()) {
                throw new IOException("Input or output shape could not be determined from the file header.");
            }

            int inputLen = inputShape.get(0);
            int outputLen = outputShape.get(0);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] tokens = line.split(",");
                if (tokens.length != inputLen + outputLen) {
                    System.err.println("Warning: Skipping malformed line (incorrect number of tokens): " + line);
                    continue;
                }

                double[] inputValues = new double[inputLen];
                double[] outputValues = new double[outputLen];

                try {
                    for (int i = 0; i < inputLen; i++) {
                        inputValues[i] = Double.parseDouble(tokens[i].trim());
                    }
                    for (int i = 0; i < outputLen; i++) {
                        outputValues[i] = Double.parseDouble(tokens[i + inputLen].trim());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Skipping malformed line (number format error): " + line);
                    continue;
                }

                rawInputs.add(inputValues);
                rawOutputs.add(outputValues);
            }

            // === Compute normalization stats ===
            double[] mean = new double[inputShape.get(0)];
            double[] std = new double[inputShape.get(0)];

            for (double[] input : rawInputs) {
                for (int i = 0; i < input.length; i++) {
                    mean[i] += input[i];
                }
            }
            for (int i = 0; i < mean.length; i++) {
                mean[i] /= rawInputs.size();
            }

            for (double[] input : rawInputs) {
                for (int i = 0; i < input.length; i++) {
                    std[i] += Math.pow(input[i] - mean[i], 2);
                }
            }
            for (int i = 0; i < std.length; i++) {
                std[i] = Math.sqrt(std[i] / rawInputs.size());
                if (std[i] == 0.0) std[i] = 1e-8; // avoid division by zero
            }

            // === Normalize and create tensors ===
            ArrayList<Pair<Tensor, Tensor>> allData = new ArrayList<>();
            for (int idx = 0; idx < rawInputs.size(); idx++) {
                double[] normalized = new double[inputShape.get(0)];
                for (int i = 0; i < normalized.length; i++) {
                    normalized[i] = (rawInputs.get(idx)[i] - mean[i]) / std[i];
                }

                Tensor inputTensor = new Tensor(new double[][]{normalized}, 1, normalized.length);
                Tensor outputTensor = new Tensor(new double[][]{rawOutputs.get(idx)}, 1, outputShape.get(0));
                allData.add(new Pair<>(inputTensor, outputTensor));
            }

            if (!allData.isEmpty()) {
                Collections.shuffle(allData);
                int splitIndex = (int) (allData.size() * trainTestSplit);
                trainData = new ArrayList<>(allData.subList(0, splitIndex));
                testData = new ArrayList<>(allData.subList(splitIndex, allData.size()));
                size = allData.size();
            } else {
                size = 0;
                System.err.println("Warning: No data loaded into the dataset. Train and test sets will be empty.");
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading dataset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shuffle() {
        Collections.shuffle(trainData);
    }

    public DataLoader getDataLoader(String type,int batchSize) {
        if(type.equals("train")) shuffle();
        return new DataLoader(type,batchSize);
    }


    public class DataLoader implements Iterator<ArrayList<Pair<Tensor, Tensor>>>
    {
        int cursor=0;
        int lastRet = -1;
        int batchSize;
        ArrayList<Pair<Tensor, Tensor>> data;
        String type;

        public DataLoader(String type, int batchSize) {
            this.type = type;
            this.batchSize = batchSize;
            if(type.equals("train"))
                data = trainData;
            else
                data = testData;
        }

        public boolean hasNext()
        {
                return cursor+batchSize < data.size();
        }

        public ArrayList<Pair<Tensor, Tensor>> next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            cursor+=batchSize;
            return new ArrayList<>(data.subList(cursor-batchSize,cursor));
        }

    }

}
