package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        readLock();
        try{
            return vector[index];
        }
        finally{
            readUnlock();
        }
        
    }

    public int length() {
        readLock();
        try{
            return vector.length;
        }
        finally{
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        readLock();
        try{
            return this.orientation;
        }
        finally{
            readUnlock();
        }
        
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try{
            if (this.orientation == VectorOrientation.ROW_MAJOR) {
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            } 
            else {
                this.orientation = VectorOrientation.ROW_MAJOR;
            }
        }
        finally{
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        writeLock();
        other.readLock();
        try{
            if(length() == other.length() && getOrientation().equals(other.orientation)){
                for(int i = 0; i < length();i++){
                    vector[i] += other.vector[i];
                }
            }
            else{
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }
        }
        finally{
            writeUnlock();
            other.readUnlock();
        }
    }

    public void negate() {
        writeLock();
        try{
            for(int i = 0; i < length();i++){
                vector[i] = -vector[i];
            }
        }
        finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        double result = 0;
        readLock();
        other.readLock();
        try{
            if(length() == other.length() && getOrientation().equals(other.orientation)){
                for(int i = 0; i < length();i++){
                    result += vector[i] * other.vector[i];
                }
            }
            else{
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }    
        }
        finally{
            readUnlock();
            other.readUnlock();
        }
        return result;
    }

    public void vecMatMul(SharedMatrix matrix) {
        if(getOrientation().equals(VectorOrientation.COLUMN_MAJOR))
            throw new IllegalArgumentException("Illegal Orientation: Must Be a Row Vector");

        writeLock();
        try{
            int matrixRows;
            int matrixCols;
            boolean isCol = false;

            if (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) {
                matrixCols = matrix.length();
                matrixRows = matrix.get(0).length();
                isCol = true;
            } 
            else {
                matrixRows = matrix.length(); 
                matrixCols = matrix.get(0).length();
            }

            if (this.vector.length != matrixRows) {
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }

            double[] result = new double[matrixCols];

            if(isCol){
                for(int i = 0; i < matrixCols; i++){
                    result[i] = dot(matrix.get(i));
                }
            }
            else{
                for (int col = 0; col < matrixCols; col++) {
                    double sum = 0;
                    for (int row = 0; row < matrixRows; row++) {
                        sum += this.vector[row] * matrix.get(row).get(col);
                    }
                    result[col] = sum;
                }
            }

            this.vector = result;
        }
        finally{
            writeUnlock();
        }
    }
}
