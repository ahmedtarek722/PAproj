import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class MustReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet<MustReachingDefinitions.Definition>> {

    private final soot.SootMethod method;

    public MustReachingDefinitions(UnitGraph graph, soot.SootMethod method) {
        super(graph);
        this.method = method;
        doAnalysis();
    }


@Override
protected void flowThrough(FlowSet<Definition> in, Unit unit, FlowSet<Definition> out) {
    // Copy the input set to the output set
    in.copy(out);

    // Process assignments
    if (unit instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) unit;

        // Get the left-hand side (variable being defined)
        Value leftOp = assignStmt.getLeftOp();

        if (leftOp instanceof JimpleLocal) {
            // Remove previous definitions of this variable
            FlowSet<Definition> temp = new ArraySparseSet<>();
            for (Definition def : out) {
                if (!def.variable.equals(leftOp)) {
                    temp.add(def);
                }
            }
            out.clear();
            out.union(temp);

            // Add the new definition to the out set
            out.add(new Definition(leftOp, unit));
        }
    }

    // Ensure parameter definitions are preserved
    for (Local parameter : method.getActiveBody().getParameterLocals()) {
        for (Definition def : in) {
            if (def.variable.equals(parameter)) {
                out.add(def);
            }
        }
    }
}


    @Override
    protected FlowSet<Definition> newInitialFlow() {
        // The initial flow set is empty for must analysis
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Definition> entryInitialFlow() {
        FlowSet<Definition> initialFlow = new ArraySparseSet<>();
        int parameterCount = method.getParameterCount();
        for (int i = 0; i < parameterCount; i++) {
            Local parameterLocal = method.getActiveBody().getParameterLocal(i);
            initialFlow.add(new Definition(parameterLocal, graph.getHeads().get(0)));
        }
        return initialFlow;
}
    @Override
    protected void merge(FlowSet<Definition> in1, FlowSet<Definition> in2, FlowSet<Definition> out) {
        FlowSet<Definition> temp = new ArraySparseSet<>();
        in1.intersection(in2, temp);

        // Ensure parameter definitions are preserved
        for (Local parameter : method.getActiveBody().getParameterLocals()) {
            for (Definition def : in1) {
                if (def.variable.equals(parameter)) {
                    temp.add(def);
                }
            }
        }

        out.clear();
        out.union(temp);
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
