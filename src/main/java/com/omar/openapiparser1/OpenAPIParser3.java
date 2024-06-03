package com.omar.openapiparser1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
 

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author oalfuraydi
 */
public class OpenAPIParser3 {

    public static void main(String[] args) throws JsonProcessingException, IOException {
        String schemes, host, basrurl, verb, para;
        Path filePath = Paths.get("C:\\Users\\oalfuraydi.MURL097631B55LG\\Documents\\NetBeansProjects\\mavenproject1\\src\\main\\java\\com\\mycompany\\mavenproject1\\openapi(1).yaml");
        BufferedReader reader = Files.newBufferedReader(filePath);
        Stream<String> line = reader.lines();
        String yaml = line.collect(Collectors.joining("\n"));
        SwaggerParseResult result = new OpenAPIParser().readLocation("C:\\Users\\oalfuraydi.MURL097631B55LG\\Documents\\NetBeansProjects\\mavenproject1\\src\\main\\java\\com\\mycompany\\mavenproject1\\openapi(1).yaml", null, null);
        OpenAPI openAPI = result.getOpenAPI();
        //System.err.println(openAPI.getServers().toString());
        io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
        //
        schemes = openAPI.getServers().get(0).getUrl();
       openAPI.getServers().forEach(System.out::println);
//        System.out.println(openAPI.getServers().get(0).getVariables().toString());
       // host = openAPI.getServers().get(0).getClass().
       // basrurl = parseed.getBasePath();
       // StringBuilder builder = new StringBuilder();
        //String main = builder.append(schemes).append("//").append(host).append(basrurl).toString();
        List<Request> requests = new ArrayList<>();
        //
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            String key = entry.getKey();
            PathItem path = entry.getValue();
            //String concatUrl = main.concat(key);
            if (path.getGet() != null) {
                Operation get = path.getGet();
                Request getRequest = getRequestFromOpreation(get, schemes);
                requests.add(getRequest);
            } else if (path.getPost() != null) {
                Operation post = path.getPost();
                
                System.err.println(post.getRequestBody().getContent().toString());
                System.err.println("---");
                post.getParameters().stream().forEach(System.out::println);
                Request postRequest = getRequestFromOpreation(post, schemes);
                requests.add(postRequest);
            } else if (path.getDelete() != null) {

            } else if (path.getPut() != null) {
                Operation put = path.getPut();
                Request putRequest = getRequestFromOpreation(put, schemes);
                requests.add(putRequest);
            }
            requests.forEach(e -> System.out.println(e.toString()));
        }

    }
    
    public static Request getRequestFromOpreation(Operation op, String urlString) throws JsonProcessingException, MalformedURLException {
        List<io.swagger.v3.oas.models.parameters.Parameter> getParameters = op.getParameters();
        String bodyString = null;
        String query = null;
        StringBuilder header = new StringBuilder();
        for (Parameter parameter : getParameters) {
            String in = parameter.getIn();
            switch (in) {
                case "header":
                    String name = parameter.getName();
                    //parameter.getPattern();
                    header.append(name).append("\n");
                    break;
                case "body":
                    //BodyParameter bp = (BodyParameter) parameter;
                   // RefModel refbody = (RefModel) bp.getSchema();
                    //Model body = parseed.getDefinitions().get(refbody.getSimpleRef());
                    System.err.println("Body: "+parameter.get$ref());
                    ObjectMapper mapper = new ObjectMapper();
                   // ObjectNode MappObject = MappObject((Model) body);
                    //System.out.println(mapper.writeValueAsString(MappObject));
                   // bodyString = mapper.writeValueAsString(MappObject);
                    break;
                case "query":
                    //QueryParameter qp = (QueryParameter) parameter;
                   // System.err.println("qp "+qp.getName());
                    URL url =new URL(urlString);
                    
                    break;
                case "path":
                    break;
                default:
                    throw new AssertionError();
            }
        }
        return new Request(bodyString, header.toString(), urlString, op.getOperationId());

    }
    /*
    public static ObjectNode MappObject(Model body) {
        Map<String, Property> properties = body.getProperties();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode child = mapper.createObjectNode();
        ObjectNode mappObject = mapper.createObjectNode();
        for (Map.Entry<String, Property> entry1 : properties.entrySet()) {
            String pname = entry1.getKey();
            Property property = entry1.getValue();
            System.err.println("----");
            if (!property.getType().equals("ref")) {
                String type = property.getType();
                switch (type) {
                    case "string":
                        StringProperty stringProperty = (StringProperty) property;
                        if (stringProperty.getEnum() != null) {
                            root.put(pname, stringProperty.getEnum().toString());
                        } else if (stringProperty.getExample() != null) {
                            root.put(pname, stringProperty.getExample().toString());
                        } else {
                            root.put(pname, stringProperty.getType());
                        }
                        break;
                    case "array":
                        ArrayProperty arrayProperty = (ArrayProperty) property;
                        ArrayNode on = mapper.createArrayNode();
                        System.out.println("Arry p: " + arrayProperty.getItems().getType());
                        if (arrayProperty.getItems().getType().equals("ref")) {
                            RefProperty items = (RefProperty) arrayProperty.getItems();
                            Model innerbody = parseed.getDefinitions().get(items.getSimpleRef());
                            ArrayNode array = root.putArray(pname);
                            mappObject = MappObject(innerbody);
                            array.add(mappObject);
                        } else {
                            StringProperty item = (StringProperty) arrayProperty.getItems();
                            ArrayNode array = root.putArray(pname);
                            array.add(item.getExample() == null ? item.getType() : item.getExample().toString());
                        }
                        break;
                    default:
                        root.put(pname, property.getType());
                }
            } else if (property.getType().equals("ref")) {
                RefProperty ref = (RefProperty) property;
                Model innerbody = parseed.getDefinitions().get(ref.getSimpleRef());
                System.out.println("model inner" + innerbody.getDescription());
                child = root.putObject(pname);
                mappObject = MappObject(innerbody);
                child.setAll(mappObject);
            }
        }
        return root;
    } */

}
