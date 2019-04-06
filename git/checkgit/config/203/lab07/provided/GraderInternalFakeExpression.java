

public class GraderInternalFakeExpression extends BinaryExpression {

    public GraderInternalFakeExpression() {
	super((Expression) null, (Expression) null);
    }

    @Override public String getOperatorName() {
	return null;
    }

    @Override public double applyOperator(double left, double right) {
        return 0.0;
    }

    

}
