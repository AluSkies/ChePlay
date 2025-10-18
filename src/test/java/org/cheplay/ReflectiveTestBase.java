package org.cheplay;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectiveTestBase {
    public static void runClass(String className) throws Exception {
        Class<?> cls = Class.forName(className);

        // try public static void main(String[])
        try {
            Method main = cls.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            return;
        } catch (NoSuchMethodException ignored) {
        }

        // try common no-arg methods
        String[] candidates = {"runExamples", "run", "execute", "demo", "examples", "start"};
        for (String name : candidates) {
            try {
                Method m = cls.getMethod(name);
                if (Modifier.isStatic(m.getModifiers())) {
                    m.invoke(null);
                } else {
                    Object inst = cls.getDeclaredConstructor().newInstance();
                    m.invoke(inst);
                }
                return;
            } catch (NoSuchMethodException ignored) {
            }
        }

        // last resort: instantiate to detect ctor issues
        cls.getDeclaredConstructor().newInstance();
    }
}
