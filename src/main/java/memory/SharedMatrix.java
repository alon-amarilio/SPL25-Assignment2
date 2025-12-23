package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        SharedVector[] oldVectors = this.vectors;

        if (oldVectors != null) {
            acquireAllVectorWriteLocks(oldVectors);
        }
        
        try{
            if(matrix.length != 0)
            {
                vectors = new SharedVector[matrix.length];
                for(int row = 0; row < matrix.length; row++){
                    double[] temp = new double[matrix[0].length];
                    for(int col = 0; col < matrix[0].length; col++){
                        temp[col] = matrix[row][col];
                    }
                    vectors[row] = new SharedVector(temp, VectorOrientation.ROW_MAJOR);
                }
            }
            else {
                vectors = new SharedVector[0];
            }
        }
        finally{
            if (oldVectors != null) {
                releaseAllVectorWriteLocks(oldVectors);
            }
        }
        
    }

    public void loadColumnMajor(double[][] matrix) {
        SharedVector[] oldVectors = this.vectors;

        if (oldVectors != null) {
            acquireAllVectorWriteLocks(oldVectors);
        }

        try{
            if(matrix.length != 0)
            {
                vectors = new SharedVector[matrix[0].length];
                for(int col = 0; col < matrix[0].length; col++){
                    double[] temp = new double[matrix.length];
                    for(int row = 0; row < matrix.length; row++){
                        temp[row] = matrix[row][col];
                    }
                    vectors[col] = new SharedVector(temp, VectorOrientation.COLUMN_MAJOR);
                }
            }
            else {
                vectors = new SharedVector[0];
            }
        }
        finally{
            if (oldVectors != null) {
                releaseAllVectorWriteLocks(oldVectors);
            }
        }
        
    }

    public double[][] readRowMajor() {
        if(vectors.length != 0)
        {
            acquireAllVectorReadLocks(vectors);
            try{
                if(vectors[0].getOrientation().equals(VectorOrientation.ROW_MAJOR)){
                    double[][] result = new double[vectors.length][vectors[0].length()];
                    for(int row = 0; row < vectors.length; row++){
                        for(int col = 0; col < vectors[0].length(); col++){
                            result[row][col] = vectors[row].get(col);
                        }
                    }
                    return result;
                }
                else{
                    double[][] result = new double[vectors[0].length()][vectors.length];
                    for(int col = 0; col < vectors.length; col++){
                        for(int row = 0; row < vectors[0].length(); row++){
                            result[row][col] = vectors[col].get(row);
                        }
                    }
                    return result;
                }
            }
            finally{
                releaseAllVectorReadLocks(vectors);
            }

            
        }
        return new double[0][0];
    }

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if(vectors.length == 0) return VectorOrientation.ROW_MAJOR;
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector v : vecs) {
            v.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector v : vecs) {
            v.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector v : vecs) {
            v.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector v : vecs) {
            v.writeUnlock();
        }
    }
}
