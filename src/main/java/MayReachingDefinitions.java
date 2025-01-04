import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class MayReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet> {

    public MayReachingDefinitions(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet in, Unit unit, FlowSet out) {
        // Copy the input set to the output set
        in.copy(out);

        // If the statement is an assignment, process it
        if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) unit;

            // Get the left-hand side (variable being defined)
            Value leftOp = assignStmt.getLeftOp();

            // Remove previous definitions of this variable from out
            if (leftOp instanceof JimpleLocal) {
                out.remove(new Definition(leftOp, unit));
            }

            // Add the new definition to the out set
            out.add(new Definition(leftOp, unit));
        }
    }

    @Override
    protected FlowSet newInitialFlow() {
        return new ArraySparseSet();
    }

    @Override
    protected FlowSet entryInitialFlow() {
        return new ArraySparseSet();
    }

    @Override
    protected void merge(FlowSet in1, FlowSet in2, FlowSet out) {
        // Union of two sets
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet source, FlowSet dest) {
        // Copy the contents of the source set to the destination
        source.copy(dest);
    }

    // Helper class to represent a definition (variable and statement)
    private static class Definition {
        private final Value variable;
        private final Unit statement;

        public Definition(Value variable, Unit statement) {
            this.variable = variable;
            this.statement = statement;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Definition that = (Definition) obj;
            return variable.equals(that.variable) && statement.equals(that.statement);
        }

        @Override
        public int hashCode() {
            return variable.hashCode() * 31 + statement.hashCode();
        }

        @Override
        public String toString() {
            return "Definition{" + "variable=" + variable + ", statement=" + statement + '}';
        }
    }
}
