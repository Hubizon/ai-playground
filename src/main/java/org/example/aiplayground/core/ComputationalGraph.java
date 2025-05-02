package org.example.aiplayground.core;

import java.util.ArrayList;

public class ComputationalGraph {
    private static class CompGraphNode{
        Tensor result;
        ArrayList<Tensor> components;
        String operation;
        public CompGraphNode(Tensor result, ArrayList<Tensor> components, String operation) {
            this.result = result;
            this.components = components;
            this.operation = operation;
        }
        public void propagateGradient() {
            if(operation.equals("+")) {
                for(Tensor component : components) {
                    component.gradient += result.gradient/components.size();
                }
            }
            else if(operation.equals("*")) {
                double product= 1;
                for(Tensor component : components) {
                    product *= component.data;
                }
                for(Tensor component : components) {
                    component.gradient += result.gradient * product / component.data;
                }
            }
        }
    }

    ArrayList<CompGraphNode> nodes = new ArrayList<CompGraphNode>();
    ArrayList<ArrayList<Integer>> graph = new ArrayList<>();

    public int addNode(Tensor result, ArrayList<Tensor> components, String operation)
    {
        nodes.add(new CompGraphNode(result, components, operation));
        graph.add(new ArrayList<Integer>());
        for(Tensor component : components) {
            graph.get(graph.size() - 1).add(component.parentNode);
        }
        return graph.size()-1;
    }
    public void propagate() {
        for(int i = graph.size() - 1; i >= 0; i--) {
            nodes.get(i).propagateGradient();
        }
    }
    public void clear()
    {
        nodes.clear();
        graph.clear();
    }
}
