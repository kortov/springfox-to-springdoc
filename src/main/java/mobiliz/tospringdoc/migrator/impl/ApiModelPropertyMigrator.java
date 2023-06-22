package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

import java.time.Duration;
import java.util.Optional;

public class ApiModelPropertyMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {

        if (isFieldOfClass(expr, Duration.class)) {
            Optional<String> declarationType = expr.findAncestor(CompilationUnit.class)
                                                   .flatMap(CompilationUnit::getPrimaryType)
                                                   .flatMap(TypeDeclaration::getFullyQualifiedName);
            System.err.println(
                "Found Duration field declaration with ApiModelProperty, customize its schema manually in class " +
                declarationType.orElse(null));
        }

        replaceOrAddImport(expr, ApiModelProperty.class, Schema.class);
        expr.setName(Schema.class.getSimpleName());
        NodeList<MemberValuePair> newPairs = new NodeList<>();
        newPairs.addAll(expr.getPairs());

        MemberValuePair valuePair = null;
        MemberValuePair notesPair = null;
        for (MemberValuePair pair : expr.getPairs()) {
            String name = pair.getNameAsString();
            if (Attributes.VALUE.equals(name)) {
                valuePair = pair;
                pair.setName(Attributes.DESCRIPTION);
            } else if (Attributes.NOTES.equals(name)) {
                notesPair = pair;
                newPairs.remove(pair);
            } else if (Attributes.REQUIRED.equals(name)) {
                expr.tryAddImportToParentCompilationUnit(Schema.RequiredMode.class);
                boolean required = pair.getValue().asBooleanLiteralExpr().getValue();
                newPairs.remove(pair);
                newPairs.add(new MemberValuePair(Attributes.REQUIRED_MODE, createRequiredExpr(required)));
            }
        }
        if (valuePair != null && notesPair != null) {
            String value = valuePair.getValue().asStringLiteralExpr().getValue();
            String note = notesPair.getValue().asStringLiteralExpr().getValue();
            valuePair.getValue().asStringLiteralExpr().setValue(value + ". " + note);
        }
        if (notesPair != null && valuePair == null) {
            System.err.println("There is a notes set:" + notesPair.getValue().asStringLiteralExpr().getValue() +
                               " without value for annotation:" + expr.getNameAsString());
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

    /**
     * Поле с типом нужного класса
     */
    private static boolean isFieldOfClass(AnnotationExpr expr, Class<?> clazz) {
        return expr.getParentNode()
                   .filter(FieldDeclaration.class::isInstance)
                   .map(FieldDeclaration.class::cast)
                   .filter(c -> c.getVariables().stream().map(VariableDeclarator::getType).map(Type::asString)
                                 .anyMatch(n -> clazz.getSimpleName().equals(n)))
                   .isPresent();
    }

}
