package com.zyc.dc.dao;
import java.util.List;

public class DataGpio{
	private List<GpioPinValue> values;
	public List<GpioPinValue> getValues() {
		return values;
	}
	public void setValues(List<GpioPinValue> values) {
		this.values = values;
	}
	public static class GpioPinValue {
    	private int pinValue;
    	private int pinMode;
    	private GpioPinType pinType;
    	private int pin;
    	private GpioValueType valueType;
		public GpioValueType getValueType() {
			return valueType;
		}
		public void setValueType(GpioValueType valueType) {
			this.valueType = valueType;
		}
		public int getPinValue() {
			return pinValue;
		}
		public void setPinValue(int pinValue) {
			this.pinValue = pinValue;
		}
		public int getPinMode() {
			return pinMode;
		}
		public void setPinMode(int pinMode) {
			this.pinMode = pinMode;
		}
		public GpioPinType getPinType() {
			return pinType;
		}
		public void setPinType(GpioPinType pinType) {
			this.pinType = pinType;
		}
		public int getPin() {
			return pin;
		}
		public void setPin(int pin) {
			this.pin = pin;
		}
    }
    public enum GpioPinType{
    	ESP,
    	EXT
    }
    public enum GpioValueType{
    	ANALOG,
    	DIGITAL
    }
}
