package com.tal.autotest.runtime.mock;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;

public class MockFrameWork {
    private static HashMap<Object, Class<?>> mockDict = new HashMap<>();
    private static CaseBuilder onGongingStubing = null;
    private static HashMap<String, Map<String, List<IMockCase>>> mockCaches = new HashMap<>();
    private static boolean active = false;

    public static void registerMockCase(String className, String methodSig, IMockCase result) {
        String key = className;
        Map<String, List<IMockCase>> methodMocks = null;
        if (!mockCaches.containsKey(key)) {
            methodMocks = new HashMap<>();
            mockCaches.put(key, methodMocks);
        } else {
            methodMocks = mockCaches.get(key);
        }

        key = methodSig;
        List<IMockCase> mockCases = null;
        if (!methodMocks.containsKey(key)) {
            mockCases = new ArrayList<>();
            methodMocks.put(methodSig, mockCases);
        } else {
            mockCases = methodMocks.get(key);
        }

        mockCases.add(0, result);
    }

    public static <T> T mock(Class<T> clz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(new MockProxy());
        T res = (T)enhancer.create();
        mockDict.put(res, clz);
        registerMockClass(clz);
        return res;
    }

    public static <T> CaseBuilder when(T object) {
        return onGongingStubing;
    }

    private static void registerMockClass(Class<?> clz) {
        String slashClassName = clz.getName().replace(".", "/");
        if (!mockCaches.containsKey(slashClassName)) {
            mockCaches.put(slashClassName, new HashMap<>());
        }
    }

    public static void active() {
        active = true;
    }

    public static void inActive() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static class CaseBuilder {
        Method method = null;
        Object res = null;
        String methodSig = null;
        Object[] args = null;
        Class<?> clz = null;

        public void setResult(Object res) {
            this.res = res;
        }

        public void setMethodSig(Method method) {
            this.methodSig = method.getName() + processMethodDesc(method);
        }

        public void setMethodParams(Object[] args) {
            this.args = args;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public void setClz(Class<?> clz) {
            this.clz = clz;
        }

        public void thenReturn(Object res) {
            this.res = res;
            IMockCase mockCase = build();
            registerMockCase(clz.getName().replace(".", "/"), methodSig, mockCase);
        }

        public IMockCase build() {
            if (this.args == null) {
                return new DefaultCase(res);
            }
            List<ITerm> terms = new ArrayList<>();
            for (Object param : this.args) {
                terms.add(new RealTerm(param));
            }
            return new ParamCase(terms, res);
        }
    }

    static class MockProxy implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
            if (method.getDeclaringClass() == Object.class
                    || method.getName().equals("hashCode")
                    || method.getName().equals("toString")
                    || method.getName().equals("equals")) {
                try {
                    return proxy.invokeSuper(obj, args);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return getDefault(method.getReturnType());
                }
            }
            Class<?> clz = mockDict.get(obj);
            CaseBuilder caseBuilder = new CaseBuilder();
            caseBuilder.setClz(clz);
            caseBuilder.setMethod(method);
            caseBuilder.setMethodSig(method);
            caseBuilder.setMethodParams(args);
            onGongingStubing = caseBuilder;
//            Class<?> clz = mockDict.get(obj);
            boolean hasMock = hasRegisteredMock(clz.getName(), processMethodDesc(method), args);
            if (hasMock) {
                return getRegisteredMock(clz.getName(), processMethodDesc(method), args);
            } else {
                return getDefault(method.getReturnType());
            }
        }
    }

    public static String processMethodDesc(Method method) {
        StringBuilder res = new StringBuilder("(");
        for (Class<?> type : method.getParameterTypes()) {
            res.append(mapTypeToDesc(type));
        }
        res.append(")");
        res.append(mapTypeToDesc(method.getReturnType()));
        return res.toString();
    }

    private static String mapTypeToDesc(Class<?> type) {
        if (type.isPrimitive()) {
            String res = "";
            switch (type.getName()) {
                case "boolean":
                    res = "Z";
                    break;
                case "byte":
                    res = "B";
                    break;
                case "char":
                    res = "C";
                    break;
                case "short":
                    res = "S";
                    break;
                case "int":
                    res = "I";
                    break;
                case "float":
                    res = "F";
                    break;
                case "double":
                    res = "D";
                    break;
                case "long":
                    res = "J";
                    break;
                default:
                    throw new RuntimeException("Unrecognized primitive $type");
            }
            return res;
        } else if (type.isArray()) {
            return type.getName().replace('.', '/');
        } else {
            return ('L' + type.getName() + ';').replace('.', '/');
        }
    }


    private static Object getDefault(Class<?> returnType) {
        switch (returnType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte)0;
            case "char":
                return '0';
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "float":
                return 0f;
            case "double":
                return 0d;
            case "long":
                return 0l;
            default:
                return null;
        }
    }

    public static boolean hasRegisteredMock(String name, String methodSig, Object[] args) {
        return getRegisteredMock(name,methodSig, args) != null;
    }

    public static Object getRegisteredMock(String name, String methodSig, Object[] args) {
        String slashedClassName = name.replace(".", "/");
        if (mockCaches.containsKey(slashedClassName)) {
            Map<String, List<IMockCase>> methodMocks = mockCaches.get(slashedClassName);
            if (methodMocks.containsKey(methodSig)) {
                List<IMockCase> cases = methodMocks.get(methodSig);
                for (IMockCase mockCase : cases) {
                    List<Object> params = Arrays.asList(args);
                    if (mockCase.match(params)) {
                        return mockCase.getValue();
                    }
                }
            }
        }
        return null;
    }
}
