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

    public TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                if (method.getSelect() instanceof J.Identifier &&
                        ((J.Identifier) method.getSelect()).getSimpleName().equals(LOGGER_NAME) &&
                        method.getSimpleName().equals("debug")) {
                    return method;
                }
                return super.visitMethodInvocation(method, ctx);
            }
        };
    }

    public JavaVisitor<ExecutionContext> getVisitor() {
        Cursor cursor = this.getVisitor().getCursor();
        JavaTemplate logLevelGuardTemplate = JavaTemplate.builder("${loggerName}.isDebugEnabled() ? ${cursor} : null")
                .imports("org.slf4j.Logger")
                .build();

        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                if (method.getSelect() instanceof J.Identifier &&
                        ((J.Identifier) method.getSelect()).getSimpleName().equals(LOGGER_NAME) &&
                        method.getSimpleName().equals("debug")) {
                    return logLevelGuardTemplate.apply(getCursor(),method.getCoordinates().replace(),method.getArguments().get(0));
//                            .withTemplateParameter("loggerName", method.getSelect())
//                            .withCursor(method);
                }
                return super.visitMethodInvocation(method, ctx);
            }
        };
    }

}
