import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class MustReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet<MustReachingDefinitions.Definition>> {

    public MustReachingDefinitions(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Definition> in, Unit unit, FlowSet<Definition> out) {
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
    protected FlowSet<Definition> newInitialFlow() {
        // The initial flow set is empty for must analysis
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Definition> entryInitialFlow() {
        // The entry flow set is empty for must analysis
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<Definition> in1, FlowSet<Definition> in2, FlowSet<Definition> out) {
        // Perform intersection of in1 and in2 for must analysis
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<Definition> source, FlowSet<Definition> dest) {
        // Copy the contents of the source set to the destination
        source.copy(dest);
    }

    // Helper class to represent a definition (variable and statement)
    public static class Definition {
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
