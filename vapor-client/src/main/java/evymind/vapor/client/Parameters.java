package evymind.vapor.client;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import evyframework.common.Assert;

public class Parameters implements Iterable<Parameter> {

	private List<Parameter> parameterList = Lists.newArrayList();
	
	public Parameters() {
	}

	public Parameters(Object[] args) {
		for (Object arg : args) {
			setParameter(null, arg);
		}
	}
	
	public void setParameter(String name, Object value) {
		parameterList.add(new Parameter(name, value));
	}
	
	public <T> void setParameter(String name, Class<T> type, T value) {
		parameterList.add(new Parameter(name, type, value));
	}
	
	public Parameter getParameter(String name) {
		Assert.notNull(name, "'name' must not be null");
		for (Parameter parameter : parameterList) {
			if (name.equals(parameter.getName())) {
				return parameter;
			}
		}
		return null;
	}
	
	public Object getParameterValue(String name) {
		Parameter parameter = getParameter(name);
		return parameter == null ? null : parameter.getValue();
	}
	
	public boolean isEmpty() {
		return parameterList.isEmpty();
	}
	
	public int size() {
		return parameterList.size();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return parameterList.iterator();
	}
}
