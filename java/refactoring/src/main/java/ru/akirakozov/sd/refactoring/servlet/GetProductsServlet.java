package ru.akirakozov.sd.refactoring.servlet;

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
            response.getWriter().println("<html><body>");

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                response.getWriter().println(name + "\t" + price + "</br>");
            }
            response.getWriter().println("</body></html>");
        });

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
