package pl.edu.uj.tcs.aiplayground.core;

import javafx.util.Pair;
import org.jooq.SelectSeekLimitStep;

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

    public Dataset(ArrayList<Integer> inputShape, ArrayList<Integer> outputShape) {
        this.inputShape = inputShape;
        this.outputShape = outputShape;
        this.size = 0;
    }

    public void load(String filename, float trainTestSplit) {
        trainData = new ArrayList<>();
        testData = new ArrayList<>();
        labelMap = new HashMap<>();

        ArrayList<Pair<Tensor, Tensor>> allData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                int inputLen = inputShape.get(0);
                int outputLen = outputShape.get(0);

                double[][] inputValues = new double[1][inputLen];
                double[][] outputValues = new double[1][outputLen];

                for (int i = 0; i < inputLen; i++) {
                    inputValues[0][i] = Double.parseDouble(tokens[i]);
                }

                for (int i = 0; i < outputLen; i++) {
                    outputValues[0][i] = Double.parseDouble(tokens[i + inputLen]);
                }

                Tensor input = new Tensor(inputValues, 1, inputLen);
                Tensor label = new Tensor(outputValues, 1, outputLen);

                allData.add(new Pair<>(input, label));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.shuffle(allData);
        int splitIndex = (int)(allData.size() * trainTestSplit);
        trainData = new ArrayList<>(allData.subList(0, splitIndex));
        testData = new ArrayList<>(allData.subList(splitIndex, allData.size()));
        size = allData.size();
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
