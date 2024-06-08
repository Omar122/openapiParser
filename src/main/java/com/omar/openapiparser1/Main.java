package com.omar.openapiparser1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    static final String SWAGGER2 = "swagger: \"2";
    static final String OPENAPI3 = "openapi: 3";

    public static void main(String[] args) throws IOException {
        List<Request> requests = new ArrayList<>();

        Path resourcePath = Paths.get("src\\main\\resources").toAbsolutePath();
        List<Path> pathlist = Files.list(resourcePath).toList();
        Pattern specVersionRegex = Pattern.compile("(openapi: 3|swagger: \"2)+");
        for (Path path : pathlist) {
            BufferedReader reader = Files.newBufferedReader(path);
            String line1 = reader.readLine();
            Matcher matcher = specVersionRegex.matcher(line1);
            if (matcher.find()) {
                switch (matcher.group()) {
                    case SWAGGER2:
                        requests = OpenAPI2Parser.Parse(path);
                        break;
                    case OPENAPI3:
                        requests = OpenAPIParser3.Parse(path.toFile());
                        break;
                    default:
                        throw new RuntimeException("Unknown file");
                }
            } else {
                throw new RuntimeException("File unknown");
            }

        }
    }
}
