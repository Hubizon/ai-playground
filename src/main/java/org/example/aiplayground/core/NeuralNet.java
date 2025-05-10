package org.example.aiplayground.core;


import java.util.ArrayList;
import java.util.List;

public class NeuralNet {

    class Layer {
        String type;
        ArrayList<Object> params;
        public Layer(String type, ArrayList<Object> params) {
            this.type = type;
            this.params = params;
        }

        public Tensor forwadPass(Tensor input, ComputationalGraph graph) {
            if(type.equals("Linear")) {
                return Tensor.matMul((Tensor) params.get(2),input,graph);
            }
            if(type.equals("Bias")) {
                return Tensor.add((Tensor) params.get(0),input,graph);
            }
            if(type.equals("Sigmoid")) {
                return Tensor.Sigmoid(input,graph);
            }
            if(type.equals("Relu")) {
                return Tensor.Relu((Tensor) params.get(0),graph);
            }
            return input;
        }
    }

    ArrayList<Layer> layers = new ArrayList<>();

    public void loadFromJson(String json) {
        layers.add(new Layer("Linear",new ArrayList<>(List.of(2,10, Tensor.randomMatrix(10,2, -1, 1)))));
    }
    public String toJson() {
        return "{Linear{2,10}}";
    }

    public Tensor pass(Tensor input, ComputationalGraph graph) {
        Tensor forwardTensor = input;
        for(Layer layer : layers) {
            forwardTensor = layer.forwadPass(forwardTensor, graph);
        }
        return forwardTensor;
    }

    public Tensor train(ArrayList<Tensor> inputs, ArrayList<Tensor> labels, int epochs, FunctionalInterface LossFunction) {
        System.out.println("Training");
        return inputs.get(0);
    }
}
