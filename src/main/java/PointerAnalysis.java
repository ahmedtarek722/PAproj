import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.Map;

public class PointerAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<PointerAnalysis.Alias>> {

    private final Map<Value, FlowSet<Value>> pointsToSets;

    public PointerAnalysis(UnitGraph graph) {
        super(graph);
        this.pointsToSets = new HashMap<>();
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Alias> in, Unit unit, FlowSet<Alias> out) {
        // Copy the input set to the output set
        in.copy(out);

        // Process assignment statements
        if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) unit;

            Value leftOp = assignStmt.getLeftOp();
            Value rightOp = assignStmt.getRightOp();

            // Handle pointer assignment: left = right
            if (leftOp instanceof JimpleLocal && rightOp instanceof JimpleLocal) {
                // Remove previous points-to relations of left
                out.remove(new Alias(leftOp, null));

                // Add new alias relation
                FlowSet<Value> pointsToSet = pointsToSets.computeIfAbsent(rightOp, k -> new ArraySparseSet<>());
                pointsToSet.add(leftOp);
                out.add(new Alias(leftOp, rightOp));
            }

            // Handle heap allocations: left = new Object()
            if (leftOp instanceof JimpleLocal && rightOp.getType().toString().startsWith("new ")) {
                out.add(new Alias(leftOp, rightOp));
            }
        }
    }

    @Override
    protected FlowSet<Alias> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Alias> entryInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<Alias> in1, FlowSet<Alias> in2, FlowSet<Alias> out) {
        // Union of two sets
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<Alias> source, FlowSet<Alias> dest) {
        // Copy the contents of the source set to the destination
        source.copy(dest);
    }

    // Class to represent alias relationships
    public static class Alias {
        private final Value variable;
        private final Value pointsTo;

        public Alias(Value variable, Value pointsTo) {
            this.variable = variable;
            this.pointsTo = pointsTo;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Alias that = (Alias) obj;
            return variable.equals(that.variable) &&
                    (pointsTo == null ? that.pointsTo == null : pointsTo.equals(that.pointsTo));
        }

        @Override
        public int hashCode() {
            return variable.hashCode() * 31 + (pointsTo != null ? pointsTo.hashCode() : 0);
        }

        @Override
        public String toString() {
            return variable + " -> " + pointsTo;
        }
    }

    // Method to retrieve points-to sets for debugging
    public Map<Value, FlowSet<Value>> getPointsToSets() {
        return pointsToSets;
    }
}
