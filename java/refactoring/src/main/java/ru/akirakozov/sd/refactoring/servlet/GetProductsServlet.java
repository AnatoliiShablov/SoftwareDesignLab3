package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.html.PageBuilder;
import ru.akirakozov.sd.refactoring.sql.SqlWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SqlWorker.query("SELECT * FROM PRODUCT", rs -> {
            PageBuilder pb = new PageBuilder();

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                pb.appendProductInfo(name, price);
            }

            response.getWriter().print(pb);
        });

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
