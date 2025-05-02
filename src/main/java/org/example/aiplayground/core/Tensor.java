package org.example.aiplayground.core;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Tensor {
    public double data;
    public double gradient;
    public int parentNode;
    public Tensor(double data) {
        this.data = data;
        parentNode = -1;
    }
    public static Tensor add(ArrayList<Tensor> addends, ComputationalGraph graph) {
        Tensor result = new Tensor(0);
        result.gradient=0;
        for(Tensor addend : addends) {
            result.data+=addend.data;
        }
        result.parentNode = graph.addNode(result,addends,"+");
        return result;
    }
    public static Tensor add(Tensor a, Tensor b, ComputationalGraph graph) {
        Tensor result = new Tensor(0);
        result.gradient=0;
        result.data=a.data+b.data;
        ArrayList<Tensor> addends = new ArrayList<Tensor>();
        addends.add(a);
        addends.add(b);
        result.parentNode = graph.addNode(result,addends,"+");
        return result;
    }
    public static Tensor multiply(ArrayList<Tensor> factors, ComputationalGraph graph) {
        Tensor result = new Tensor(1);
        result.gradient=0;
        for(Tensor factor : factors) {
            result.data*=factor.data;
        }
        result.parentNode = graph.addNode(result,factors,"*");
        return result;
    }
    public static Tensor multiply(Tensor a, Tensor b, ComputationalGraph graph) {
        Tensor result = new Tensor(0);
        result.gradient=0;
        result.data=a.data*b.data;
        ArrayList<Tensor> factors = new ArrayList<Tensor>();
        factors.add(a);
        factors.add(b);
        result.parentNode = graph.addNode(result,factors,"*");
        return result;
    }
}
