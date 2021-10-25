package ru.akirakozov.sd.refactoring.http;

import ru.akirakozov.sd.refactoring.html.PageBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpResponseBuilder {
    public static <T> void createResponse(HttpServletResponse response, int status, String contentType, T text) throws IOException {
        response.setStatus(status);
        response.setContentType(contentType);
        response.getWriter().println(text);
    }
}
