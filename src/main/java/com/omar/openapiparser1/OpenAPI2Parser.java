package com.omar.openapiparser1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.Swagger20Parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.web.util.UriComponentsBuilder;

public class OpenAPI2Parser {

    private static Swagger parseed;

    public static void main(String[] args) throws IOException {

        String schemes, host, basrurl;
        Path path = Paths.get("src\\main\\resources").toAbsolutePath();
        List<Path> list = Files.list(path).toList();
        BufferedReader reader = Files.newBufferedReader(list.get(0));
        Stream<String> line = reader.lines();
        String yaml = line.collect(Collectors.joining("\n"));
        Swagger20Parser parser = new Swagger20Parser();
        parseed = parser.parse(yaml);
        schemes = parseed.getSchemes().get(0).toValue();
        host = parseed.getHost();
        basrurl = parseed.getBasePath();
        Map<String, io.swagger.models.Path> paths = parseed.getPaths();

        StringBuilder builder = new StringBuilder();
        String main = builder.append(schemes).append("://").append(host).append(basrurl).toString();
        List<Request> requests = new ArrayList<>();

        for (Map.Entry<String, io.swagger.models.Path> entry : paths.entrySet()) {
            String key = entry.getKey();
            io.swagger.models.Path val = entry.getValue();
            String concatUrl = main.concat(key);
            if (val.getGet() != null) {
                Operation get = val.getGet();
                Request getRequest = getRequestFromOpreation(get, concatUrl, "get");
                requests.add(getRequest);
            }
            if (val.getPost() != null) {
                Operation post = val.getPost();
                Request postRequest = getRequestFromOpreation(post, concatUrl, "post");
                requests.add(postRequest);
            }
            if (val.getDelete() != null) {
                Operation del = val.getDelete();
                Request delRequest = getRequestFromOpreation(del, concatUrl, "delete");
                requests.add(delRequest);
            }
            if (val.getPut() != null) {
                Operation put = val.getPut();
                Request putRequest = getRequestFromOpreation(put, concatUrl, "put");
                requests.add(putRequest);
            }
        }
        requests.forEach(e -> System.out.println(e.toString()));

    }

    public static Request getRequestFromOpreation(Operation op, String urlString, String verb) throws JsonProcessingException, MalformedURLException {
        List<Parameter> getParameters = op.getParameters();
        String bodyString = null;
        StringBuilder header = new StringBuilder();
        String replaceAll = urlString.replaceAll("\\{(.*?)\\}", "");
        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromHttpUrl(replaceAll);
        for (Parameter parameter : getParameters) {
            switch (parameter.getIn()) {
                case "header" -> {
                    HeaderParameter hp = (HeaderParameter) parameter;
                    header.append(hp.getName())
                        .append(":")
                        .append(hp.getEnum() == null ? "" : hp.getEnum().toString())
                        .append(",")
                        .append("\n");
                }
                case "body" -> {
                    BodyParameter bp = (BodyParameter) parameter;
                    RefModel refbody = (RefModel) bp.getSchema();
                    Model body = parseed.getDefinitions().get(refbody.getSimpleRef());
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode MappObject = MapObject((Model) body);
                    bodyString = mapper.writeValueAsString(MappObject);
                }
                case "query" -> {
                    QueryParameter qp = (QueryParameter) parameter;
                    componentsBuilder.queryParam(qp.getName(), qp.getExample() == null ? qp.getType() : qp.getExample().toString());
                }
                case "path" -> {
                    PathParameter pp = (PathParameter) parameter;
                    componentsBuilder.path(pp.getType());
                }

            }
        }
        return new Request(bodyString, header.toString(), componentsBuilder.toUriString(), verb);
    }

    public static ObjectNode MapObject(Model body) {
        Map<String, Property> properties = body.getProperties();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        for (Map.Entry<String, Property> entry1 : properties.entrySet()) {
            Property property = entry1.getValue();
            property.setName(entry1.getKey());
            root = ProcessProperty(property, root);
        }

        return root;
    }

    public static ObjectNode ProcessProperty(Property property, ObjectNode jacksonNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode child = mapper.createObjectNode();

        if (!property.getType().equals("ref")) {
            switch (property.getType()) {
                case "string" -> {
                    StringProperty stringProperty = (StringProperty) property;
                    if (stringProperty.getEnum() != null) {
                        child.put(stringProperty.getName(), stringProperty.getEnum().toString());
                    } else if (stringProperty.getExample() != null) {
                        child.put(stringProperty.getName(), stringProperty.getExample().toString());
                    } else {
                        child.put(stringProperty.getName(), stringProperty.getType());
                    }
                }
                case "array" -> {
                    ArrayProperty arrayProperty = (ArrayProperty) property;
                    if (arrayProperty.getItems().getType().equals("ref")) {
                        RefProperty items = (RefProperty) arrayProperty.getItems();
                        Model innerbody = parseed.getDefinitions().get(items.getSimpleRef());
                        ArrayNode array = child.putArray(property.getName());
                        ObjectNode mappObject = MapObject(innerbody);
                        array.add(mappObject);
                    } else {
                        StringProperty item = (StringProperty) arrayProperty.getItems();
                        ArrayNode array = child.putArray(property.getName());
                        array.add(item.getExample() == null ? item.getType() : item.getExample().toString());
                    }
                }
                case "object" -> {
                    ObjectProperty op = (ObjectProperty) property;
                    ObjectNode putObject = child.putObject(op.getName());
                    for (Map.Entry<String, Property> entry : op.getProperties().entrySet()) {
                        Property objectProperty = entry.getValue();
                        objectProperty.setName(entry.getKey());
                        ObjectNode Process = ProcessProperty(objectProperty, putObject);
                        putObject.setAll(Process);
                    }

                    //child.setAll(putObject);
                    //jacksonNode = jacksonNode.setAll(child);
                }
                default ->
                    child.put(property.getName(), property.getType());
            }
            jacksonNode = jacksonNode.setAll(child);
        } else if (property.getType().equals("ref")) {
            RefProperty ref = (RefProperty) property;
            Model innerbody = parseed.getDefinitions().get(ref.getSimpleRef());
            child = jacksonNode.putObject(ref.getName());
            ObjectNode mappObject = MapObject(innerbody);
            child.setAll(mappObject);
            //jacksonNode.setAll(child);
        }
        return jacksonNode;
    }

}
