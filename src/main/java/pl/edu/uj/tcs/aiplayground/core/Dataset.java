package pl.edu.uj.tcs.aiplayground.core;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class  Dataset {
    public ArrayList<Integer> shape;
    public int size;
    public int batchSize;
    public ArrayList<Pair<Tensor, Double>> trainData;
    public ArrayList<Pair<Tensor, Double>> testData;
    private Map<String, Integer> labelMap;
    private int nextLabel = 0;
    public Dataset(ArrayList<Integer> shape, int size, int batchSize) {
        this.shape = shape;
        this.size = size;
        this.batchSize = batchSize;
    }
    public void load(String filename, float trainTestSplit) {
        List<Pair<Tensor, Double>> allData = new ArrayList<>();

        int expectedNumFeatures = this.shape.get(1);

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < expectedNumFeatures + 1) {
                    System.err.println("Skipping line due to insufficient columns for expected features + label: " + line);
                    continue;
                }

                double[] featuresArray = new double[expectedNumFeatures];
                int featureStartIndex = parts.length - 1 - expectedNumFeatures;

                try {
                    for (int i = 0; i < expectedNumFeatures; i++) {
                        featuresArray[i] = Double.parseDouble(parts[featureStartIndex + i].trim());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line due to invalid number format in features: " + line);
                    continue;
                }

                String labelString = parts[parts.length - 1].trim();

                int labelInt;
                if (labelMap.containsKey(labelString)) {
                    labelInt = labelMap.get(labelString);
                } else {
                    labelInt = nextLabel++;
                    labelMap.put(labelString, labelInt);
                }

                double[][] tensorData = new double[][]{featuresArray};

                Tensor dataTensor = new Tensor(tensorData, this.shape.get(0), this.shape.get(1));

                allData.add(new Pair<>(dataTensor, (double) labelInt));
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Collections.shuffle(allData);
        int trainSize = (int) (allData.size() * trainTestSplit);
        this.trainData = new ArrayList<>(allData.subList(0, trainSize));
        this.testData = new ArrayList<>(allData.subList(trainSize, allData.size()));
        this.size = allData.size();
        System.out.println("Dataset loaded successfully. Total samples: " + this.size);
        System.out.println("Training samples: " + this.trainData.size());
        System.out.println("Testing samples: " + this.testData.size());
        System.out.println("Label mapping: " + labelMap);
    }

    public void shuffle() {
        Collections.shuffle(trainData);
    }


    public class TrainDataLoader implements Iterator<ArrayList<Pair<Tensor, Double>>>
    {
        int cursor=0;
        int lastRet = -1;

        public boolean hasNext()
        {
            return cursor+batchSize<size;
        }
        public ArrayList<Pair<Tensor, Double>> next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            cursor+=batchSize;
            return new ArrayList<>(trainData.subList(cursor-batchSize,cursor));
        }

    }

    public class TestDataLoader implements Iterator<ArrayList<Pair<Tensor, Double>>>
    {
        int cursor=0;
        int lastRet = -1;

        public boolean hasNext()
        {
            return cursor+batchSize<size;
        }
        public ArrayList<Pair<Tensor, Double>> next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            cursor+=batchSize;
            return new ArrayList<>(testData.subList(cursor-batchSize,cursor));
        }

    }

}
