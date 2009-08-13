package org.kawane.filebox.json;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class JSONObjectHandler implements JSONHandler {

	private Stack<Object> objects = new Stack<Object>();

	private Stack<Field> fields = new Stack<Field>();

	public JSONObjectHandler(Object root) {
		objects.push(root);
	}

	public void beginDocument() {
	}

	public void beginObject() {
		// create instance
		Field field = fields.peek();
		Type type = field.getGenericType();
		if(type instanceof ParameterizedType) {
			if(Collection.class.isAssignableFrom((Class<?>)((ParameterizedType) type).getRawType())) {
				Type parameterizedType = ((ParameterizedType) type).getActualTypeArguments()[0];
				createInstance((Class<?>)parameterizedType);
			} else {
				createInstance((Class<?>)((ParameterizedType) type).getRawType());
			}
		} else {
			createInstance((Class<?>)type);
		}
	}

	private void createInstance(Class<?> type) {
		if (type.isArray()) {
			createInstance(type.getComponentType());
		} else {
			try {
				objects.push(type.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void beginArray() {
		objects.push(new ArrayList<Object>());
	}

	@SuppressWarnings("unchecked")
	public void endArray() {
		List list = (List) objects.pop();
		Object o = objects.peek();

		Field field = fields.peek();
		Class<?> type = field.getType();
		try {
			if (o instanceof List) {
				if (type.isArray()) {
					((List) o).add(getValueArray(list, getRecComponentType(type.getComponentType())));
				} else {
					((List) o).add(list);
				}
			} else if (type.isArray()) {
				fields.pop();
				field.set(o, getValueArray(list, type.getComponentType()));
			} else if (Collection.class.isAssignableFrom(type)) {
				fields.pop();
				((Collection) field.get(o)).addAll(list);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private Object getValueArray(List list, Class<?> type) {
		int cpt;
		Object value;
		int size = list.size();
		if (Integer.TYPE == type) {
			int[] tab = new int[size];
			cpt = 0;
			for (Integer l : (List<Integer>) list) {
				tab[cpt++] = l.intValue();
			}
			value = tab;
		} else if (Long.TYPE == type) {
			long[] tab = new long[size];
			cpt = 0;
			for (Long l : (List<Long>) list) {
				tab[cpt++] = l.longValue();
			}
			value = tab;
		} else if (Byte.TYPE == type) {
			byte[] tab = new byte[size];
			cpt = 0;
			for (Byte b : (List<Byte>) list) {
				tab[cpt++] = b.byteValue();
			}
			value = tab;
		} else if (Short.TYPE == type) {
			short[] tab = new short[size];
			cpt = 0;
			for (Short s : (List<Short>) list) {
				tab[cpt++] = s.shortValue();
			}
			value = tab;
		} else if (Double.TYPE == type) {
			double[] tab = new double[size];
			cpt = 0;
			for (Double d : (List<Double>) list) {
				tab[cpt++] = d.doubleValue();
			}
			value = tab;
		} else if (Float.TYPE == type) {
			float[] tab = new float[size];
			cpt = 0;
			for (Float f : (List<Float>) list) {
				tab[cpt++] = f.floatValue();
			}
			value = tab;
		} else if (Character.TYPE == type) {
			char[] tab = new char[size];
			cpt = 0;
			for (Character c : (List<Character>) list) {
				tab[cpt++] = c.charValue();
			}
			value = tab;
		} else if (Boolean.TYPE == type) {
			boolean[] tab = new boolean[size];
			cpt = 0;
			for (Boolean b : (List<Boolean>) list) {
				tab[cpt++] = b.booleanValue();
			}
			value = tab;
		} else {
			value = list.toArray((Object[]) Array.newInstance(type, size));
		}
		return value;
	}

	public void endDocument() {
		objects.pop();
	}

	@SuppressWarnings("unchecked")
	public void endObject() {
		Object object = objects.pop();
		Object o = objects.peek();
		Field field = fields.peek();
		try {
			if (o instanceof List) {
				((List) o).add(object);
			} else {
				fields.pop();
				field.set(o, object);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void member(String name) {
		Object o = objects.peek();
		Class<? extends Object> clazz = o.getClass();
		try {
			Field field = clazz.getDeclaredField(name);
			fields.push(field);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void nullValue() {
		Object o = objects.peek();
		Field field = fields.pop();
		try {
			field.set(o, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void numberValue(String value) {
		Object o = objects.peek();
		Field field = fields.peek();
		Class<?> type = field.getType();
		try {
			if (o instanceof List) {
				type = getRecComponentType(type.getComponentType());
				if (Integer.TYPE == type || Integer.class == type) {
					((List) o).add(Integer.valueOf(value));
				} else if (Long.TYPE == type || Long.class == type) {
					((List) o).add(Long.valueOf(value));
				} else if (Byte.TYPE == type || Byte.class == type) {
					((List) o).add(Byte.valueOf(value));
				} else if (Short.TYPE == type || Short.class == type) {
					((List) o).add(Short.valueOf(value));
				} else if (Double.TYPE == type || Double.class == type) {
					((List) o).add(Double.valueOf(value));
				} else if (Float.TYPE == type || Float.class == type) {
					((List) o).add(Float.valueOf(value));
				}
			} else {
				fields.pop();
				if (Integer.TYPE == type) {
					field.setInt(o, Integer.parseInt(value));
				} else if (Long.TYPE == type) {
					field.setLong(o, Long.parseLong(value));
				} else if (Byte.TYPE == type) {
					field.setByte(o, Byte.parseByte(value));
				} else if (Short.TYPE == type) {
					field.setShort(o, Short.parseShort(value));
				} else if (Double.TYPE == type) {
					field.setDouble(o, Double.parseDouble(value));
				} else if (Float.TYPE == type) {
					field.setFloat(o, Float.parseFloat(value));
				} else if (Integer.class == type) {
					field.set(o, Integer.valueOf(value));
				} else if (Long.class == type) {
					field.set(o, Long.valueOf(value));
				} else if (Byte.class == type) {
					field.set(o, Byte.valueOf(value));
				} else if (Short.class == type) {
					field.set(o, Short.valueOf(value));
				} else if (Double.class == type) {
					field.set(o, Double.valueOf(value));
				} else if (Float.class == type) {
					field.set(o, Float.valueOf(value));
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private Class<?> getRecComponentType(Class<?> type) {
		if (type.isArray()) {
			return getRecComponentType(type.getComponentType());
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	public void stringValue(String value) {
		Object o = objects.peek();
		Field field = fields.peek();
		Class type = field.getType();
		Object v = value;
		if (o instanceof List) {
			((List) o).add(value);
		} else if (Enum.class.isAssignableFrom(type)) {
			fields.pop();
			v = Enum.valueOf(type, value);
			try {
				field.set(o, v);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			fields.pop();
			try {
				field.set(o, v);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void booleanValue(boolean bool) {
		Object o = objects.peek();
		Field field = fields.peek();
		Class type = field.getType();
		try {
			if (o instanceof List) {
				((List) o).add(bool);
			} else if (Boolean.TYPE == type || Boolean.class == type) {
				fields.pop();
				field.setBoolean(o, bool);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public void value(String value) {
		Object o = objects.peek();
		Field field = fields.peek();
		Class type = field.getType();
		try {
			if (o instanceof List) {
				((List) o).add(value);
			} else if (Boolean.TYPE == type || Boolean.class == type) {
				fields.pop();
				field.setBoolean(o, Boolean.parseBoolean(value));
			} else {
				fields.pop();
				field.set(o, value);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	static final String test = "{ \"coucou\": \"coucou\", \"i\": [0, 1, 2],\"ii\": [[3, 4, 5], [6, 7, 8]], \"titis\": [{"
			+ "	\"coucou\": \"coucou\", 	\"i\": [0, 1, 2], 	\"ii\": [[3, 4, 5], [6, 7, 8]] } , { 	\"coucou\": \"coucou\","
			+ "	\"i\": [0, 1, 2], 	\"ii\": [[3, 4, 5], [6, 7, 8]] } ] }";

	//	static final String test = "{\"ii\": [[3, 4, 5], [6, 7, 8]]}";

	//	static final String test = "{\"ii\": [[3, 4, 5]]}";

	//	static final String test = "{ \"coucou\": \"coucou\", \"i\": [0, 1, 2, 3], \"s\" : [\"coucou1\"], \"titis\": [{"
	//			+ "	\"coucou\": \"coucou2\", 	\"i\": [0, 1, 2]} , { 	\"coucou\": \"coucou3\"," + "	\"i\": [0, 1, 2]} ] }";

	static class Toto {
		String coucou;
		int[] i;
		int[][] ii;
		List<Titi> titis = new ArrayList<Titi>();
	}

	static class Titi {
		String coucou;
		int[] i;
		int[][] ii;
	}

	public static void main(String[] args) {
		JSONObjectStream objectStream = new JSONObjectStream();
		try {
			Toto toto = objectStream.deserialize(new StringReader(test), Toto.class);
			System.out.println(toto.coucou);
			for (int i = 0; i < toto.i.length; i++) {
				System.out.println(toto.i[i]);
			}
			for (int i = 0; i < toto.ii.length; i++) {
				for (int j = 0; j < toto.ii[i].length; j++) {
					System.out.println(toto.ii[i][j]);
				}
			}
			//			for (int i = 0; i < toto.titis.length; i++) {
			//				System.out.println(toto.titis[i].coucou);
			//				for (int j = 0; j < toto.titis[i].ii.length; j++) {
			//					for (int j2 = 0; j2 < toto.titis[i].ii[j].length; j2++) {
			//						System.out.println(toto.titis[i].ii[j][j2]);
			//					}
			//
			//				}
			//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
