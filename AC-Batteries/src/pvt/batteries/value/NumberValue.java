package pvt.batteries.value;

import arjunasdk.enums.ValueType;
import arjunasdk.interfaces.Value;

public class NumberValue extends AbstractValue {

	public NumberValue(Number numberObject) {
		super(ValueType.NUMBER, numberObject);
	}

	protected NumberValue(ValueType type, Number numberObject) {
		super(type, numberObject);
	}

	@Override
	public Value clone() {
		return new NumberValue(getRawObject());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private Number getRawObject() {
		return (Number) this.object();
	}

	@Override
	public Number asNumber() {
		return (Number) this.object();
	}
}