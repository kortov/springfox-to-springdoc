package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract class AbstractApiParamMigrator extends AbstractAnnotationMigrator {

    protected abstract Class<? extends Annotation> getFoxAnnotation();

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        Optional<String> declarationType = expr.findAncestor(CompilationUnit.class)
                                               .flatMap(CompilationUnit::getPrimaryType)
                                               .flatMap(TypeDeclaration::getFullyQualifiedName);
        NodeList<MemberValuePair> newPairs = new NodeList<>();
        for (MemberValuePair pair : expr.getPairs()) {
            switch (pair.getNameAsString()) {
                case Attributes.NAME:
                case Attributes.REQUIRED:
                case Attributes.EXAMPLE:
                case Attributes.ALLOW_EMPTY_VALUE:
                    newPairs.add(pair);
                    break;
                case Attributes.VALUE:
                    newPairs.add(new MemberValuePair(Attributes.DESCRIPTION, pair.getValue()));
                    break;
                case Attributes.PARAM_TYPE:
                    expr.tryAddImportToParentCompilationUnit(ParameterIn.class);
                    String paramType = pair.getValue().asStringLiteralExpr().getValue();
                    newPairs.add(new MemberValuePair(Attributes.IN, createInExpr(paramType)));
                    break;
                default:
                    System.out.printf("@%s:%s property cannot be migrated in class %s%n", expr.getNameAsString(),
                                      pair.getNameAsString(), declarationType.orElse(null));
            }
        }
        expr.setPairs(newPairs);
        replaceAnnotation(expr);
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        replaceAnnotation(expr);
    }

    private void replaceAnnotation(AnnotationExpr expr) {
        expr.setName(Parameter.class.getSimpleName());
        replaceOrAddImport(expr, getFoxAnnotation(), Parameter.class);
    }

    private FieldAccessExpr createInExpr(String paramTye) {
        ParameterIn in;
        switch (paramTye) {
            case "path":
                in = ParameterIn.PATH;
                break;
            case "query":
                in = ParameterIn.QUERY;
                break;
            case "header":
                in = ParameterIn.HEADER;
                break;
            default:
                throw new RuntimeException(String.format("%s is not valid or supported for paramType"));
        }
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setScope(new NameExpr(ParameterIn.class.getSimpleName()));
        expr.setName(in.name());
        return expr;
    }

}
