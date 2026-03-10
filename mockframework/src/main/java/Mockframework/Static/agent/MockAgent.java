package Mockframework.Static.agent;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import Mockframework.Static.staticmock.StaticMethodStub;

public final class MockAgent {
    private static final String PROJECT_PACKAGE_PREFIX = "Mockframework.";
    private static final boolean DEBUG = Boolean.getBoolean("mock.framework.debug");

    private MockAgent() {
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("[mock-framework-static] MockAgent initialized");

        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(
                        ElementMatchers.nameStartsWith("net.bytebuddy.")
                                .or(ElementMatchers.nameStartsWith("jdk."))
                                .or(ElementMatchers.nameStartsWith("sun."))
                                .or(ElementMatchers.nameStartsWith("java."))
                                .or(ElementMatchers.nameStartsWith("Mockframework.Static.agent."))
                                .or(ElementMatchers.nameStartsWith("Mockframework.Static.staticmock."))
                                .or(ElementMatchers.nameStartsWith("Mockframework.Core."))
                                .or(ElementMatchers.named("Mockframework.Static.Mockito"))
                                .or(ElementMatchers.named("Mockframework.Static.MockedStatic"))
                                .or(ElementMatchers.named("Mockframework.Static.OngoingStubbing"))
                                .or(ElementMatchers.named("Mockframework.Static.StaticInvocation"))
                                .or(ElementMatchers.nameStartsWith("org.gradle."))
                                .or(ElementMatchers.nameStartsWith("worker.org.gradle."))
                                .or(ElementMatchers.nameStartsWith("org.junit."))
                                .or(ElementMatchers.nameStartsWith("org.opentest4j."))
                                .or(ElementMatchers.nameStartsWith("org.apiguardian."))
                )
                .with(new AgentBuilder.Listener.Adapter() {
                    @Override
                    public void onDiscovery(
                            String typeName,
                            ClassLoader classLoader,
                            net.bytebuddy.utility.JavaModule module,
                            boolean loaded
                    ) {
                        if (DEBUG) {
                            System.out.println("[mock-framework-static] Loading class: " + typeName);
                        }
                    }

                    @Override
                    public void onError(
                            String typeName,
                            ClassLoader classLoader,
                            net.bytebuddy.utility.JavaModule module,
                            boolean loaded,
                            Throwable throwable
                    ) {
                        System.err.println(
                                "[mock-framework-static] Agent error for class " + typeName + ": " + throwable
                        );
                    }
                })
                .type(MockAgent::isTransformable)
                .transform(
                        (builder, typeDescription, classLoader, module, protectionDomain) -> builder
                                .method(
                                        ElementMatchers.isStatic()
                                                .and(ElementMatchers.not(ElementMatchers.isTypeInitializer()))
                                                .and(ElementMatchers.not(ElementMatchers.isNative()))
                                )
                                .intercept(MethodDelegation.to(StaticMethodStub.class))
                )
                .installOn(instrumentation);
    }

    private static boolean isTransformable(
            TypeDescription typeDescription,
            ClassLoader classLoader,
            net.bytebuddy.utility.JavaModule module,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain
    ) {
        String typeName = typeDescription.getName();
        if (!typeName.startsWith(PROJECT_PACKAGE_PREFIX)) {
            return false;
        }

        return canLoadStaticStub(classLoader);
    }

    private static boolean canLoadStaticStub(ClassLoader classLoader) {
        if (classLoader == null) {
            return false;
        }
        try {
            Class.forName(StaticMethodStub.class.getName(), false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}