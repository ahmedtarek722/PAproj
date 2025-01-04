import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class PossibleLiveVariableAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<Value>> {

    public PossibleLiveVariableAnalysis(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Value> in, Unit unit, FlowSet<Value> out) {
        in.copy(out);

        if (unit.toString().startsWith("return")) {
            for (ValueBox useBox : unit.getUseBoxes()) {
                Value use = useBox.getValue();
                if (use instanceof soot.Local) {
                    out.add(use);
                }
            }
            return;
        }

        if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) unit;
            Value leftOp = assignStmt.getLeftOp();
            if (leftOp instanceof soot.Local) {
                out.remove(leftOp);
            }
            for (ValueBox useBox : assignStmt.getUseBoxes()) {
                Value use = useBox.getValue();
                if (use instanceof soot.Local) {
                    out.add(use);
                }
            }
        } else if (unit instanceof soot.jimple.IfStmt) {
            soot.jimple.IfStmt ifStmt = (soot.jimple.IfStmt) unit;
            Value condition = ifStmt.getCondition();
            for (ValueBox useBox : condition.getUseBoxes()) {
                Value use = useBox.getValue();
                if (use instanceof soot.Local) {
                    out.add(use);
                }
            }
        }
    }

    @Override
    protected FlowSet<Value> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Value> entryInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<Value> in1, FlowSet<Value> in2, FlowSet<Value> out) {
        // Union of two sets
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<Value> source, FlowSet<Value> dest) {
        // Copy the contents of the source set to the destination
        source.copy(dest);
    }
}
