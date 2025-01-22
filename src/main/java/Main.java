import soot.*;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;

public class Main {
    public static void main(String[] args) {
        // Configure Soot
        configureSoot();

        // Specify the class and method to analyze
        String targetClass = "Factorial"; // Replace with your target class
        String targetMethod = "int fact(int)"; // Replace with your target method signature

        // Load the class
        SootClass sootClass = Scene.v().loadClassAndSupport(targetClass);
        sootClass.setApplicationClass();

        // Find the method to analyze
        SootMethod method = sootClass.getMethod(targetMethod);

        if (method != null) {
            // Retrieve the method's body
            Body body = method.retrieveActiveBody();

            // Create a UnitGraph for the method
            UnitGraph graph = new BriefUnitGraph(body);
//            System.out.println(graph);
            // Run Reaching Definitions Analysis
            System.out.println("\n---May Reaching Definitions Analysis ---");
            MayReachingDefinitions mayreachingDefinitions = new MayReachingDefinitions(graph , method);
            for (Unit unit : graph) {
                System.out.println("At " + unit + ":");
                System.out.println("  MayReaching Definitions Before: " + mayreachingDefinitions.getFlowBefore(unit));
                System.out.println("  MayReaching Definitions After: " + mayreachingDefinitions.getFlowAfter(unit));
            }

//            System.out.println("\n---Must Reaching Definitions Analysis ---");
//            MustReachingDefinitions mustreachingDefinitions = new MustReachingDefinitions(graph);
//            for (Unit unit : graph) {
//                System.out.println("At " + unit + ":");
//                System.out.println("  MustReaching Definitions Before: " + mustreachingDefinitions.getFlowBefore(unit));
//                System.out.println("  MustReaching Definitions After: " + mustreachingDefinitions.getFlowAfter(unit));
//            }

//             Run Live Variable Analysis
//            System.out.println("\n--- Possible Live Variable Analysis ---");
//            PossibleLiveVariableAnalysis possibleLiveVariableAnalysis = new PossibleLiveVariableAnalysis(graph);
//            for (Unit unit : graph) {
//                System.out.println("At " + unit + ":");
//                System.out.println("  Live Variables Before: " + possibleLiveVariableAnalysis.getFlowBefore(unit));
//                System.out.println("  Live Variables After: " + possibleLiveVariableAnalysis.getFlowAfter(unit));
//            }

//            System.out.println("\n--- Definite Live Variable Analysis ---");
//            DefiniteLiveVariableAnalysis definiteLiveVariableAnalysis = new DefiniteLiveVariableAnalysis(graph);
//            for (Unit unit : graph) {
//                System.out.println("At " + unit + ":");
//                System.out.println("  Live Variables Before: " + definiteLiveVariableAnalysis.getFlowBefore(unit));
//                System.out.println("  Live Variables After: " + definiteLiveVariableAnalysis.getFlowAfter(unit));
//            }

//            // Run Pointer Analysis
//            System.out.println("\n--- Pointer Analysis ---");
//            PointerAnalysis pointerAnalysis = new PointerAnalysis(graph);
//            for (Unit unit : graph) {
//                System.out.println("At " + unit + ":");
//                System.out.println("  Aliases Before: " + pointerAnalysis.getFlowBefore(unit));
//                System.out.println("  Aliases After: " + pointerAnalysis.getFlowAfter(unit));
//            }
//
//            // Additional: Points-to Sets for Pointer Analysis
//            System.out.println("\n--- Points-to Sets ---");
//            Map<Value, FlowSet<Value>> pointsToSets = pointerAnalysis.getPointsToSets();
//            pointsToSets.forEach((key, value) -> System.out.println(key + " -> " + value));

        } else {
            System.out.println("Method not found: " + targetMethod);
        }
    }

    private static void configureSoot() {
        // Reset Soot
        G.reset();

        // Set options
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);

        // Set input classpath (directory containing compiled .class files)
        Options.v().set_process_dir(java.util.Collections.singletonList("D:/AINSHAMS_SEMESTERS/semester9/Program analysis/projects/proj1/target/classes")); // Replace with the path to your compiled classes

        // Set output format (none, since we're doing static analysis only)
        Options.v().set_output_format(Options.output_format_none);

        // Load necessary classes
        Scene.v().loadNecessaryClasses();
    }
}
