package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {

      if (args.length < 3) {
        System.out.println("Usage: java -jar LAE.jar <input_path> <output_path> <num_threads>");
        return;
      }

      int numThreads = Integer.parseInt(args[0]); 
      String inputPath = args[1];                
      String outputPath = args[2];

      LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);

      try {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(inputPath);

        root = engine.run(root);

        OutputWriter.write(root.getMatrix(), outputPath);

        System.out.println("Computation completed successfully.");
        System.out.println("--- Worker Activity Report ---");
        System.out.println(engine.getWorkerReport());
      }
      catch (ParseException e) {
        System.err.println("Error parsing input: " + e.getMessage());
        OutputWriter.write("Parse error: " + e.getMessage(), outputPath);
      }
      catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
        e.printStackTrace();
        OutputWriter.write("Runtime error: " + e.getMessage(), outputPath);
      }
    }
}