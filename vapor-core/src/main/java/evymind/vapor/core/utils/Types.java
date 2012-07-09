package evymind.vapor.core.utils;

import java.util.HashMap;

public class Types {

	private static final HashMap<String, Class<?>> name2Class = new HashMap<String, Class<?>>();
	static {
		name2Class.put("boolean", java.lang.Boolean.TYPE);
		name2Class.put("byte", java.lang.Byte.TYPE);
		name2Class.put("char", java.lang.Character.TYPE);
		name2Class.put("double", java.lang.Double.TYPE);
		name2Class.put("float", java.lang.Float.TYPE);
		name2Class.put("int", java.lang.Integer.TYPE);
		name2Class.put("long", java.lang.Long.TYPE);
		name2Class.put("short", java.lang.Short.TYPE);
		name2Class.put("void", java.lang.Void.TYPE);

		name2Class.put("java.lang.Boolean.TYPE", java.lang.Boolean.TYPE);
		name2Class.put("java.lang.Byte.TYPE", java.lang.Byte.TYPE);
		name2Class.put("java.lang.Character.TYPE", java.lang.Character.TYPE);
		name2Class.put("java.lang.Double.TYPE", java.lang.Double.TYPE);
		name2Class.put("java.lang.Float.TYPE", java.lang.Float.TYPE);
		name2Class.put("java.lang.Integer.TYPE", java.lang.Integer.TYPE);
		name2Class.put("java.lang.Long.TYPE", java.lang.Long.TYPE);
		name2Class.put("java.lang.Short.TYPE", java.lang.Short.TYPE);
		name2Class.put("java.lang.Void.TYPE", java.lang.Void.TYPE);

		name2Class.put("java.lang.Boolean", java.lang.Boolean.class);
		name2Class.put("java.lang.Byte", java.lang.Byte.class);
		name2Class.put("java.lang.Character", java.lang.Character.class);
		name2Class.put("java.lang.Double", java.lang.Double.class);
		name2Class.put("java.lang.Float", java.lang.Float.class);
		name2Class.put("java.lang.Integer", java.lang.Integer.class);
		name2Class.put("java.lang.Long", java.lang.Long.class);
		name2Class.put("java.lang.Short", java.lang.Short.class);

		name2Class.put("Boolean", java.lang.Boolean.class);
		name2Class.put("Byte", java.lang.Byte.class);
		name2Class.put("Character", java.lang.Character.class);
		name2Class.put("Double", java.lang.Double.class);
		name2Class.put("Float", java.lang.Float.class);
		name2Class.put("Integer", java.lang.Integer.class);
		name2Class.put("Long", java.lang.Long.class);
		name2Class.put("Short", java.lang.Short.class);

		name2Class.put(null, java.lang.Void.TYPE);
		name2Class.put("string", java.lang.String.class);
		name2Class.put("String", java.lang.String.class);
		name2Class.put("java.lang.String", java.lang.String.class);
	}

	private static final HashMap<Class<?>, String> class2Name = new HashMap<Class<?>, String>();
	static {
		class2Name.put(java.lang.Boolean.TYPE, "boolean");
		class2Name.put(java.lang.Byte.TYPE, "byte");
		class2Name.put(java.lang.Character.TYPE, "char");
		class2Name.put(java.lang.Double.TYPE, "double");
		class2Name.put(java.lang.Float.TYPE, "float");
		class2Name.put(java.lang.Integer.TYPE, "int");
		class2Name.put(java.lang.Long.TYPE, "long");
		class2Name.put(java.lang.Short.TYPE, "short");
		class2Name.put(java.lang.Void.TYPE, "void");

		class2Name.put(java.lang.Boolean.class, "java.lang.Boolean");
		class2Name.put(java.lang.Byte.class, "java.lang.Byte");
		class2Name.put(java.lang.Character.class, "java.lang.Character");
		class2Name.put(java.lang.Double.class, "java.lang.Double");
		class2Name.put(java.lang.Float.class, "java.lang.Float");
		class2Name.put(java.lang.Integer.class, "java.lang.Integer");
		class2Name.put(java.lang.Long.class, "java.lang.Long");
		class2Name.put(java.lang.Short.class, "java.lang.Short");

		class2Name.put(null, "void");
		class2Name.put(java.lang.String.class, "java.lang.String");
	}

	/* ------------------------------------------------------------ */
	/**
	 * Class from a canonical name for a type.
	 * 
	 * @param name
	 *            A class or type name.
	 * @return A class , which may be a primitive TYPE field..
	 */
	public static Class<?> fromTypeName(String name) {
		return name2Class.get(name);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Canonical name for a type.
	 * 
	 * @param type
	 *            A class , which may be a primitive TYPE field.
	 * @return Canonical name.
	 */
	public static String toTypeName(Class<?> type) {
		return class2Name.get(type);
	}
}
