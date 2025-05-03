package org.example.aiplayground.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tensor {

    public double[] data;
    public double[] gradient;
    public ArrayList<Integer> shape;

    public Tensor(double data) {
        this.data = new double[]{data};
        this.gradient = new double[]{0};
        shape = new ArrayList<>(List.of(1));
    }

    public Tensor(double[] data, ArrayList<Integer> shape) {
        int num_elements=1;
        for(int dim:shape)
        {
            num_elements*=dim;
        }
        if(num_elements!=data.length)
        {
            throw new IllegalArgumentException(String.format(
                    "Data array length %d does not match shape %d", data.length, num_elements));
        }
        this.data = data;
        this.gradient = data.clone();
        for (int i=0; i<data.length; i++)
        {
            gradient[i]=0;
        }
        this.shape = shape;
    }
    public static Tensor zeros(ArrayList<Integer> shape) {
        int num_elements=1;
        for(int dim:shape)
        {
            num_elements*=dim;
        }
        double[] data = new double[num_elements];
        for(int i=0; i<data.length; i++)
        {
            data[i]=0;
        }
        return new Tensor(data, shape);
    }
    public static Tensor zerosLike(Tensor other) {
        double[] zeros = new double[other.data.length];
        for(int i=0; i<zeros.length; i++)
        {
            zeros[i]=0;
        }
        return new Tensor(zeros, other.shape);
    }

    public void fill( double value) {
        Arrays.fill(data, value);
    }

    public static Tensor randomVector(ArrayList<Integer> shape, double min, double max) {
        int num_elements=1;
        for(int dim:shape)
        {
            num_elements*=dim;
        }
        double[] vector = new double[num_elements];
        for(int i=0; i<vector.length; i++)
        {
            vector[i]=min+Math.random()*(max-min);
        }
        return new Tensor(vector, shape);
    }

    public static Tensor add(ArrayList<Tensor> addends, ComputationalGraph graph) {
        ArrayList<Integer> shapeOfAddends = addends.get(0).shape;
        for(Tensor addend:addends)
        {
            if(!addend.shape.equals( shapeOfAddends))
            {
                throw new IllegalArgumentException(String.format(
                        "Shape mismatch! a.shape=%s, b.shape=%s", shapeOfAddends, addend.shape));
            }
        }
        Tensor result = zerosLike(addends.get(0));
        for(Tensor addend : addends) {
            for(int i=0; i<addend.data.length; i++)
            {
                result.data[i]+=addend.data[i];
            }
        }

        graph.addNode(result,addends,"+");
        return result;
    }
    public static Tensor add(Tensor a, Tensor b, ComputationalGraph graph) {
        if(!a.shape.equals(b.shape))
        {
            throw new IllegalArgumentException(String.format(
                    "Shape mismatch! a.shape=%s, b.shape=%s", a.shape, b.shape));
        }
        Tensor result = zerosLike(a);
        for(int i=0; i<a.data.length; i++)
        {
            result.data[i]=b.data[i]+a.data[i];
        }
        ArrayList<Tensor> addends = new ArrayList<Tensor>();
        addends.add(a);
        addends.add(b);
        graph.addNode(result,addends,"+");
        return result;
    }
    public static Tensor multiply(ArrayList<Tensor> factors, ComputationalGraph graph) {

        ArrayList<Integer> shapeOfFactors = factors.get(0).shape;
        for(Tensor factor:factors)
        {
            if(!factor.shape.equals(shapeOfFactors))
            {
                throw new IllegalArgumentException(String.format(
                        "Shape mismatch! a.shape=%s, b.shape=%s", shapeOfFactors, factor.shape));
            }
        }

        Tensor result = zerosLike(factors.get(0));
        result.fill(1);
        for(Tensor factor : factors) {
            for(int i=0; i<factor.data.length; i++)
            {
                result.data[i]*=factor.data[i];
            }
        }
        graph.addNode(result,factors,"*");
        return result;
    }

    public static Tensor multiply(Tensor a, Tensor b, ComputationalGraph graph) {
        if (!a.shape.equals(b.shape)) {
            throw new IllegalArgumentException(String.format(
                    "Shape mismatch! a.shape=%s, b.shape=%s", a.shape, b.shape));
        }
        Tensor result = zerosLike(a);
        for (int i = 0; i < a.data.length; i++) {
            result.data[i] = a.data[i] * b.data[i];
        }
        ArrayList<Tensor> factors = new ArrayList<>();
        factors.add(a);
        factors.add(b);
        graph.addNode(result, factors, "*");
        return result;
    }
    public Tensor sum(ComputationalGraph graph)
    {
        ArrayList<Integer> newShape = new ArrayList<>(shape);;

        int len = newShape.get(0);
        newShape.remove(0);
        if(newShape.isEmpty())
        {
            newShape.add(1);
        }
        int num_elements=1;
        for(int dim: newShape)
        {
            num_elements*=dim;
        }
        double[] sum = new double[num_elements];
        for(int i=0; i<sum.length; i++)
        {
            sum[i]=0;
        }
        for(int i=0; i<sum.length; i++)
        {
            for(int j=0; j<len; j++)
            {
                sum[i]+=data[i+num_elements*j];
            }
        }
        Tensor result = new Tensor(sum, newShape);
        ArrayList<Tensor> comps = new ArrayList<>();
        comps.add(this);
        graph.addNode(result,comps,"Sum0");
        return result;
    }

}
