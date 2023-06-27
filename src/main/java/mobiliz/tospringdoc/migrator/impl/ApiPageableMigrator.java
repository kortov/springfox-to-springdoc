package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

public class ApiPageableMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        System.err.println(
            "Found custom ApiPageable annotation, replace it with @PageableAsQueryParam and add @Parameter(hidden = true) to pageable");
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        System.err.println(
            "Found custom ApiPageable annotation, replace it with @PageableAsQueryParam and add @Parameter(hidden = true) to pageable");
    }
}
