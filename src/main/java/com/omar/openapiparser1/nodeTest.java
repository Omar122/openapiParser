package com.omar.openapiparser1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class nodeTest {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("name","value");
        ObjectNode root1 = proces(root);

    }

    public static ObjectNode proces(ObjectNode jsonNodes){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode child = mapper.createObjectNode();
        child.put(null,"Ajab");
        System.out.println(child.toString());
        jsonNodes.setAll(child);
        return jsonNodes;
    }
}
