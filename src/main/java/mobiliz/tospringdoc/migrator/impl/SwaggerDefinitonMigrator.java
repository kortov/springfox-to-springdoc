package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import io.swagger.annotations.SwaggerDefinition;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

public class SwaggerDefinitonMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        removeImport(expr, SwaggerDefinition.class);
        expr.getParentNode().ifPresent(parent -> {
            if (parent instanceof NodeWithAnnotations) {
                parent.remove(expr);
            }
        });
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        // nothing
    }
}
