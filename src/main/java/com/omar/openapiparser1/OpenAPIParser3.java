package com.omar.openapiparser1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author oalfuraydi
 */
public class OpenAPIParser3 {

    private static OpenAPI openAPI;

    public static void main(String[] args) throws JsonProcessingException, IOException {
        String basrurl;
        File filepath = java.nio.file.Paths.get("src\\main\\resources\\OpenAPI3Simple.yaml").toFile();
        SwaggerParseResult result = new OpenAPIParser().readLocation(filepath.toString(), null, null);
        openAPI = result.getOpenAPI();
        Paths paths = openAPI.getPaths();
        StringBuilder builder = new StringBuilder();
        basrurl = openAPI.getServers().get(0).getUrl();
        System.err.println("baseurl: " + basrurl);
        List<Request> requests = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            String pathKey = entry.getKey();
            PathItem path = entry.getValue();
            String pathUrl = basrurl.concat(pathKey);
            if (path.getGet() != null) {
                Operation get = path.getGet();
                Request getRequest = getRequestFromOpreation(get, pathUrl, "get");
                requests.add(getRequest);
            }
            if (path.getPost() != null) {
                Operation post = path.getPost();
                Request postRequest = getRequestFromOpreation(post, pathUrl, "post");
                requests.add(postRequest);
            }
            if (path.getDelete() != null) {
                Operation del = path.getDelete();
                Request delRequest = getRequestFromOpreation(del, pathUrl, "delete");
                requests.add(delRequest);
            }
            if (path.getPut() != null) {
                Operation put = path.getPut();
                Request putRequest = getRequestFromOpreation(put, pathUrl, "put");
                requests.add(putRequest);
            }

        }
        requests.forEach(e -> System.out.println(e.toString()));
    }

    public static Request getRequestFromOpreation(Operation op, String basrurl, String verb) {
        //System.out.println(op.getRequestBody() != null ? op.getRequestBody().toString() : "");
        String requestBodyString = "";
        StringBuilder header = new StringBuilder();
        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromHttpUrl(basrurl);

        if (op.getRequestBody() != null) {
            RequestBody requestBody = op.getRequestBody();
            Content content = requestBody.getContent();
            MediaType mType = content.get("application/json");
            String[] refs = mType.getSchema().get$ref().split("/");
            Schema schema = openAPI.getComponents().getSchemas().get(refs[refs.length - 1]);
            ObjectNode reqNode = MapObject(schema);
            requestBodyString = reqNode.toPrettyString();
        }
        if (op.getParameters() != null) {
            for (Parameter parameter : op.getParameters()) {
                switch (parameter.getIn()) {
                    case "header" -> {
                        HeaderParameter hp = (HeaderParameter) parameter;
                        header.append(hp.getName())
                            .append(":")
                            .append(hp.getExample() == null ? "" : hp.getExample().toString())
                            .append(",")
                            .append("\n");
                    }

                    case "query" -> {
                        QueryParameter qp = (QueryParameter) parameter;
                        componentsBuilder.queryParam(qp.getName(), qp.getExample() == null ? qp.getSchema().getType() : qp.getExample().toString());
                    }
                    case "path" -> {
                        PathParameter pp = (PathParameter) parameter;
                        if (pp.getExample() == null && pp.getSchema().getExample() == null) {
                            StringBuilder varibleBuilder = new StringBuilder(pp.getName());
                            varibleBuilder.append("_").append(pp.getSchema().getType().toUpperCase()).append("-").append("VALUE");
                            componentsBuilder.replacePath(varibleBuilder.toString());
                        } else if (pp.getExample() != null) {
                            componentsBuilder.replacePath(pp.getExample().toString());
                        } else {
                            componentsBuilder.replacePath(pp.getSchema().getExample().toString());
                        }
                    }
                }

            }
        }
        return new Request(requestBodyString, header.toString(), componentsBuilder.toUriString(), verb);
    }

    public static ObjectNode MapObject(Schema innerSchema) {
        Map<String, Schema> properties = innerSchema.getProperties();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Schema property = entry.getValue();
            property.setName(propertyName);
            property.setType(property.getType() == null ? "ref" : property.getType());
            root = ProcessProperty(property, root);
        }
        return root;
    }

    public static ObjectNode ProcessProperty(Schema property, ObjectNode root) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode child = mapper.createObjectNode();
        if (!property.getType().equals("ref")) {
            switch (property.getType()) {
                case "string" -> {
                    StringSchema stringProperty = (StringSchema) property;
                    if (stringProperty.getEnum() != null) {
                        child.put(stringProperty.getName(), stringProperty.getEnum().toString());
                    } else if (stringProperty.getExample() != null) {
                        child.put(stringProperty.getName(), stringProperty.getExample().toString());
                    } else {
                        child.put(stringProperty.getName(), stringProperty.getType());
                    }

                }
                case "array" -> {
                    ArraySchema arrayProperty = (ArraySchema) property;
                    arrayProperty.getItems().setType(arrayProperty.getItems().getType() == null ? "ref" : arrayProperty.getItems().getType());
                    if (arrayProperty.getItems().getType().equals("ref")) {
                        Schema items = arrayProperty.getItems();
                        String[] refs = items.get$ref().split("/");
                        Schema innerSchema = openAPI.getComponents().getSchemas().get(refs[refs.length - 1]);
                        ArrayNode array = child.putArray(arrayProperty.getName());
                        ObjectNode mappObject = MapObject(innerSchema);
                        array.add(mappObject);
                    } else {
                        StringSchema item = (StringSchema) arrayProperty.getItems();
                        ArrayNode array = child.putArray(item.getName());
                        array.add(item.getExample() == null ? item.getType() : item.getExample().toString());
                    }
                }
                case "object" -> {
                    ObjectSchema op = (ObjectSchema) property;
                    ObjectNode putObject = child.putObject(op.getName());
                    for (Map.Entry<String, Schema> entry : op.getProperties().entrySet()) {
                        Schema objectProperty = entry.getValue();
                        objectProperty.setName(entry.getKey());
                        ObjectNode Process = ProcessProperty(objectProperty, putObject);
                        putObject.setAll(Process);
                    }
                    //child.setAll(putObject);
                    //jacksonNode = jacksonNode.setAll(child);
                }
                default -> {
                    child.put(property.getName(), property.getType());
                }
            }
            root = root.setAll(child);
        } else if (property.getType().equals("ref")) {
            String[] refs = property.get$ref().split("/");
            Schema innerSchema = openAPI.getComponents().getSchemas().get(refs[refs.length - 1]);
            // Model innerbody = parseed.getDefinitions().get(ref.getSimpleRef());
            child = root.putObject(property.getName());
            ObjectNode mappObject = MapObject(innerSchema);
            child.setAll(mappObject);
            //jacksonNode.setAll(child);
        }
        return root;
    }
}
