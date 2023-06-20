package mobiliz.tospringdoc.migrator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import mobiliz.tospringdoc.migrator.impl.ApiIgnoreMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiImplicitParamMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiModelMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiModelPropertyMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiOperationMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiParamMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiResponseMigrator;
import mobiliz.tospringdoc.migrator.impl.ApiResponsesMigrator;
import mobiliz.tospringdoc.migrator.impl.SwaggerDefinitonMigrator;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ToSpringDocVisitor extends ModifierVisitor<Object> {

    private static Map<String, AbstractAnnotationMigrator> ANNO_MIGRATE_MAP = new HashMap<>();
    private Set<CompilationUnit> changedUnits = new HashSet<>();

    static {
        ANNO_MIGRATE_MAP.put(Api.class.getSimpleName(), new ApiMigrator());
        ANNO_MIGRATE_MAP.put(SwaggerDefinition.class.getSimpleName(), new SwaggerDefinitonMigrator());
        ANNO_MIGRATE_MAP.put(ApiIgnore.class.getSimpleName(), new ApiIgnoreMigrator());
        ANNO_MIGRATE_MAP.put(ApiImplicitParam.class.getSimpleName(), new ApiImplicitParamMigrator());
        ANNO_MIGRATE_MAP.put(ApiModel.class.getSimpleName(), new ApiModelMigrator());
        ANNO_MIGRATE_MAP.put(ApiModelProperty.class.getSimpleName(), new ApiModelPropertyMigrator());
        ANNO_MIGRATE_MAP.put(ApiOperation.class.getSimpleName(), new ApiOperationMigrator());
        ANNO_MIGRATE_MAP.put(ApiParam.class.getSimpleName(), new ApiParamMigrator());
        ANNO_MIGRATE_MAP.put(ApiResponse.class.getSimpleName(), new ApiResponseMigrator());
        ANNO_MIGRATE_MAP.put(ApiResponses.class.getSimpleName(), new ApiResponsesMigrator());
    }

    @Override
    public Visitable visit(NormalAnnotationExpr n, Object arg) {
        String name = n.getNameAsString();
        if (ANNO_MIGRATE_MAP.containsKey(name)) {
            ANNO_MIGRATE_MAP.get(name).migrate(n);
            n.findAncestor(CompilationUnit.class).ifPresent(p -> changedUnits.add(p));

        }
        return n;
    }

    @Override
    public Visitable visit(SingleMemberAnnotationExpr n, Object arg) {
        String name = n.getNameAsString();
        if (ANNO_MIGRATE_MAP.containsKey(name)) {
            ANNO_MIGRATE_MAP.get(name).migrate(n);
            n.findAncestor(CompilationUnit.class).ifPresent(p -> changedUnits.add(p));
        }
        return n;
    }

    @Override
    public Visitable visit(MarkerAnnotationExpr n, Object arg) {
        String name = n.getNameAsString();
        if (ANNO_MIGRATE_MAP.containsKey(name)) {
            ANNO_MIGRATE_MAP.get(name).migrate(n);
            n.findAncestor(CompilationUnit.class).ifPresent(p -> changedUnits.add(p));
        }
        return n;
    }

    public Set<CompilationUnit> getChangedUnits() {
        return changedUnits;
    }
}
