package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.http.HttpResponseBuilder;
import ru.akirakozov.sd.refactoring.sql.SqlWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class AddProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        long price = Long.parseLong(request.getParameter("price"));

        SqlWorker.update("INSERT INTO PRODUCT " +
                "(NAME, PRICE) VALUES (\"" + name + "\"," + price + ")");

        HttpResponseBuilder.createResponse(response, HttpServletResponse.SC_OK, "text/html", "OK");
    }
}
