package io.github.tesla.filter.support.plugins;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import io.github.tesla.common.dto.EndpointDTO;
import io.github.tesla.common.dto.ServiceDTO;
import io.github.tesla.filter.AbstractRequestPlugin;
import io.github.tesla.filter.support.annnotation.EndpointRequestPlugin;
import io.github.tesla.filter.utils.ClassUtils;

public class EndpointRequestPluginMetadata extends RequestPluginMetadata {

    private static final long serialVersionUID = 1L;

    EndpointRequestPluginMetadata(Class<? extends AbstractRequestPlugin> clz) {
        EndpointRequestPlugin annotation = AnnotationUtils.findAnnotation(clz, EndpointRequestPlugin.class);
        this.filterType = annotation.filterType();
        this.filterName = annotation.filterName();
        this.filterOrder = annotation.filterOrder();
        this.filterClass = clz;
        this.ignoreClassType = StringUtils.isBlank(annotation.ignoreClassType()) ? null : annotation.ignoreClassType();
        this.definitionClazz = annotation.definitionClazz();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EndpointRequestPluginMetadata getMetadataByType(String filterType) {
        if (StringUtils.isBlank(filterType)) {
            return null;
        }
        Set<Class<?>> allClasses = ClassUtils.findAllClasses(packageName, EndpointRequestPlugin.class);
        for (Class clz : allClasses) {
            if (filterType.equals(AnnotationUtils.findAnnotation(clz, EndpointRequestPlugin.class).filterType())) {
                return (EndpointRequestPluginMetadata)REQUESTPLUGINMETADATA_INSTANCE_CACHE.putIfAbsent(clz.getName(),
                    new EndpointRequestPluginMetadata(clz));
            }
        }
        return null;
    }

    public static String validate(String pluginType, String paramJson, ServiceDTO serviceDTO, EndpointDTO endpointDTO) {
        try {
            EndpointRequestPluginMetadata metadata = getMetadataByType(pluginType);
            if (metadata == null || metadata.definitionClazz == null) {
                return paramJson;
            }
            return metadata.definitionClazz.getDeclaredConstructor().newInstance().validate(paramJson, serviceDTO,
                endpointDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
