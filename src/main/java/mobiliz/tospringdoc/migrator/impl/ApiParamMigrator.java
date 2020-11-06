package mobiliz.tospringdoc.migrator.impl;

import io.swagger.annotations.ApiParam;
import java.lang.annotation.Annotation;

public class ApiParamMigrator extends AbstractApiParamMigrator {
    @Override
    protected Class<? extends Annotation> getFoxAnnotation() {
        return ApiParam.class;
    }
}
