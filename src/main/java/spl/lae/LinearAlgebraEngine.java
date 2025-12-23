package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        computationRoot.associativeNesting();
        ComputationNode resolvable;
        while ((resolvable = computationRoot.findResolvable()) != null) {
            loadAndCompute(resolvable);
        }
        try{
            if (executor != null) {
                executor.shutdown();
            }
        }
        catch(Exception e){
            Thread.currentThread().interrupt();
        }

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<ComputationNode> children = node.getChildren();
        this.leftMatrix = new SharedMatrix(children.get(0).getMatrix());

        if (children.size() > 1) {
            this.rightMatrix = new SharedMatrix(children.get(1).getMatrix());
        }

        List<Runnable> tasks = null;
        switch (node.getNodeType()) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                break;
        }

        if(tasks!=null){
            executor.submitAll(tasks);
        }
        node.resolve(leftMatrix.readRowMajor());

    }

    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();

        int numVectors = leftMatrix.length(); 

        for (int i = 0; i < numVectors; i++) {
            final int index = i;
            tasks.add(() -> {
                SharedVector vLeft = leftMatrix.get(index);
                SharedVector vRight = rightMatrix.get(index);
                vLeft.add(vRight);
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();

        int numVectors = leftMatrix.length(); 

        for (int i = 0; i < numVectors; i++) {
            final int index = i;
            tasks.add(() -> {
                SharedVector vLeft = leftMatrix.get(index);
                vLeft.vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
    
        int numVectors = leftMatrix.length();

        for (int i = 0; i < numVectors; i++) {
            final int index = i; 
            
            tasks.add(() -> {
                leftMatrix.get(index).negate();
            });
        }
    
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
    
        int numVectors = leftMatrix.length();

        for (int i = 0; i < numVectors; i++) {
            final int index = i; 
            
            tasks.add(() -> {
                leftMatrix.get(index).transpose();
            });
        }
    
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }


}
