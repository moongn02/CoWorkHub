package cn.moongn.coworkhub.common.utils;

import cn.moongn.coworkhub.common.api.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public class ResponseUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Result<String> result = Result.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
} 