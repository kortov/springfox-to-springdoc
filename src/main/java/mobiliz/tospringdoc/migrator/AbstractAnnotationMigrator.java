package mobiliz.tospringdoc.migrator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import mobiliz.tospringdoc.core.Attributes;

import java.lang.annotation.Annotation;

public abstract class AbstractAnnotationMigrator implements AnnotationMigrator {

    public void replaceOrAddImport(Node node, Class<? extends Annotation> foxAnno, Class<? extends Annotation> docAnno
    ) {
        node.findAncestor(CompilationUnit.class).ifPresent(p -> {
            if (p.getImports() != null) {
                p.getImports().removeIf(i -> i.getNameAsString().equals(foxAnno.getCanonicalName()));
            }
            p.addImport(docAnno);
        });
    }

    public void removeImport(Node node, Class<? extends Annotation> foxAnno) {
        node.findAncestor(CompilationUnit.class).ifPresent(p -> {
            if (p.getImports() != null) {
                p.getImports().removeIf(i -> i.getNameAsString().equals(foxAnno.getCanonicalName()));
            }
        });
    }

    @Override
    public void migrate(SingleMemberAnnotationExpr expr) {
        NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr();
        normalAnnotationExpr.setName(expr.getName());
        normalAnnotationExpr.addPair(Attributes.VALUE, expr.getMemberValue());
        expr.getParentNode().ifPresent(parent -> {
            if (parent instanceof NodeWithAnnotations nwa) {
                parent.remove(expr);
                nwa.addAnnotation(normalAnnotationExpr);
                migrate(normalAnnotationExpr);
            }
        });
    }
}
