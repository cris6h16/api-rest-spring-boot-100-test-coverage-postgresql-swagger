package org.cris6h16.apirestspringboot.Controllers.Utils;

import org.springframework.http.ResponseEntity;

public class ResponseUtils {

    public static String getFailBodyMsg(ResponseEntity<String> res) {
        return res.getBody().split("\"")[3];
    }

    public static Long getIdFromLocationHeader(ResponseEntity<Void> res) {
        String[] parts = res.getHeaders().getLocation().toString().split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
}
