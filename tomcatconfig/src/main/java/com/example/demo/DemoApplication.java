package com.example.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Threads;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Component
    static class TomcatConfigCheckCommandLineRunner implements CommandLineRunner {

        @Autowired
        private ServerProperties serverProperties;

        @Override
        public void run(String... args) throws Exception {

            Tomcat tomcat = serverProperties.getTomcat();
            Threads tomcatThreads = tomcat.getThreads();

            invokeGetterMethods(tomcat, tomcat.getClass());
            invokeGetterMethods(tomcatThreads, tomcatThreads.getClass());
        }

        private <T> void invokeGetterMethods(T instance, Class<? extends T> clazz)
            throws InvocationTargetException, IllegalAccessException {

            final var methods = clazz.getMethods();

            for (Method method : methods) {
                if (method.getName().trim().toLowerCase().startsWith("get")) {
                    Object invoke = method.invoke(instance);
                    System.out.println(method.getName() + "\t" + invoke);
                }
            }

        }
    }
}
