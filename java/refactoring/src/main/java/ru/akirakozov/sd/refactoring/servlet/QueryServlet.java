package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.sql.SqlWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1", rs -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with max price: </h1>");

                while (rs.next()) {
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    response.getWriter().println(name + "\t" + price + "</br>");
                }
                response.getWriter().println("</body></html>");
            });
        } else if ("min".equals(command)) {
            SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1", rs -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with min price: </h1>");

                while (rs.next()) {
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    response.getWriter().println(name + "\t" + price + "</br>");
                }
                response.getWriter().println("</body></html>");
            });
        } else if ("sum".equals(command)) {
            SqlWorker.query("SELECT SUM(price) FROM PRODUCT", rs -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Summary price: ");

                if (rs.next()) {
                    response.getWriter().println(rs.getInt(1));
                }
                response.getWriter().println("</body></html>");
            });
        } else if ("count".equals(command)) {
            SqlWorker.query("SELECT COUNT(*) FROM PRODUCT", rs -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Number of products: ");

                if (rs.next()) {
                    response.getWriter().println(rs.getInt(1));
                }
                response.getWriter().println("</body></html>");
            });
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
