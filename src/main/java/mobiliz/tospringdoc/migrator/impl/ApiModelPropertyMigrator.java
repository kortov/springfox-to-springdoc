package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

public class ApiModelPropertyMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {

        replaceOrAddImport(expr, ApiModelProperty.class, Schema.class);
        expr.setName(Schema.class.getSimpleName());
        NodeList<MemberValuePair> newPairs = new NodeList<>();
        newPairs.addAll(expr.getPairs());

        for (MemberValuePair pair : expr.getPairs()) {
            String name = pair.getNameAsString();
            if (Attributes.VALUE.equals(name)) {
                pair.setName(Attributes.DESCRIPTION);
            } else if (Attributes.NOTES.equals(name))
            // TODO: выяснить на что заменить notes
            {
                pair.setName(Attributes.TITLE);
            } else if (Attributes.REQUIRED.equals(name)) {
                expr.tryAddImportToParentCompilationUnit(Schema.RequiredMode.class);
                boolean required = pair.getValue().asBooleanLiteralExpr().getValue();
                newPairs.remove(pair);
                newPairs.add(new MemberValuePair(Attributes.REQUIRED_MODE, createRequiredExpr(required)));
            }
        }

        expr.setPairs(newPairs);
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        replaceOrAddImport(expr, ApiModelProperty.class, Schema.class);
        expr.setName(Schema.class.getSimpleName());
    }

    private FieldAccessExpr createRequiredExpr(boolean required) {
        var requiredMode = required ? Schema.RequiredMode.REQUIRED : Schema.RequiredMode.NOT_REQUIRED;
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setScope(new NameExpr(Schema.RequiredMode.class.getSimpleName()));
        expr.setName(requiredMode.name());
        return expr;
    }

}
