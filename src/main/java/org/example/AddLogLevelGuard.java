package org.example;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;

public class AddLogLevelGuard extends Recipe {
    private static final String LOGGER_NAME = "logger";

    public String getDisplayName() {
        return "Add Log Level Guards";
    }


    public String getDescription() {
        return "Add Guards";
    }

    @Override
    public JavaVisitor<ExecutionContext> getVisitor() {
        return new LogLevelGuardVisitor();
    }
    private static class LogLevelGuardVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher logMethodMatcher = new MethodMatcher("org.sl4j.Logger log(..)");

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
            J.MethodInvocation mi = super.visitMethodInvocation(method, context);
            if (method.getSelect() instanceof J.Identifier &&
                    ((J.Identifier) method.getSelect()).getSimpleName().equals(LOGGER_NAME) &&
                    method.getSimpleName().equals("debug")) {
                return method;
            }
            JavaTemplate logLevelGuardTemplate = JavaTemplate.builder("${loggerName}.isDebugEnabled() ? ${cursor} : null")
                    .imports("org.slf4j.Logger")
                    .build();
            mi = logLevelGuardTemplate.apply(getCursor(), mi.getCoordinates().replace(), mi.getArguments().get(0)); //Template Invocation
            return mi;
        }
    }
}
