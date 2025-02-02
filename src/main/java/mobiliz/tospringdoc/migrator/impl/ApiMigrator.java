package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ApiMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        removeImport(expr, io.swagger.annotations.Tag.class);
        replaceOrAddImport(expr, Api.class, Tag.class);
        List<MemberValuePair> pairs = new ArrayList<>(expr.getPairs());
        expr.getPairs().clear();
        List<String> tags = new ArrayList<>();
        for (MemberValuePair pair : pairs) {
            String name = pair.getNameAsString();
            if (Attributes.VALUE.equals(name)) {
                tags.add(pair.getValue().asStringLiteralExpr().getValue());
            } else if (Attributes.TAGS.equals(name)) {
                tags.addAll(getTags(pair));
            }
        }
        if (tags.size() == 1) {
            expr.setName(Tag.class.getSimpleName());
            expr.addPair(Attributes.NAME, new StringLiteralExpr(tags.get(0)));
            return;
        }


        expr.setName(Tags.class.getSimpleName());
        expr.tryAddImportToParentCompilationUnit(Tags.class);
        expr.addPair(Attributes.VALUE, new ArrayInitializerExpr(new NodeList<>(createTagExpr(tags))));
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        // useless case but developers choice
        replaceOrAddImport(expr, Api.class, Tag.class);
        expr.setName(Tag.class.getSimpleName());
    }

    private List<Expression> createTagExpr(List<String> tags) {
        return tags.stream().map(tag -> {
            NormalAnnotationExpr tagExpr = new NormalAnnotationExpr();
            tagExpr.setName(Tag.class.getSimpleName());
            tagExpr.addPair(Attributes.NAME, new StringLiteralExpr(tag));
            return tagExpr;
        }).collect(Collectors.toList());
    }


    private List<String> getTags(MemberValuePair pair) {
        if (pair.getValue() instanceof StringLiteralExpr) {
            return Collections.singletonList(pair.getValue().asStringLiteralExpr().getValue());
        }
        ArrayInitializerExpr arrayExpr = pair.getValue().asArrayInitializerExpr();
        return arrayExpr.getValues().stream()
            .map(e -> e.asStringLiteralExpr().getValue())
            .collect(Collectors.toList());
    }
}
