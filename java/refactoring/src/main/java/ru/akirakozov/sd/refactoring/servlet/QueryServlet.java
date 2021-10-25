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
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1", rs -> {
                PageBuilder pb = new PageBuilder();
                pb.appendHeader1("Product with max price: ");

                while (rs.next()) {
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    pb.appendProductInfo(name, price);
                }
                response.getWriter().print(pb);
            });
        } else if ("min".equals(command)) {
            SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1", rs -> {
                PageBuilder pb = new PageBuilder();
                pb.appendHeader1("Product with min price: ");

                while (rs.next()) {
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    pb.appendProductInfo(name, price);
                }
                response.getWriter().print(pb);
            });
        } else if ("sum".equals(command)) {
            SqlWorker.query("SELECT SUM(price) FROM PRODUCT", rs -> {
                PageBuilder pb = new PageBuilder();
                pb.appendLine("Summary price: ");
                if (rs.next()) {
                    pb.appendLine(rs.getInt(1));
                }

                response.getWriter().print(pb);
            });
        } else if ("count".equals(command)) {
            SqlWorker.query("SELECT COUNT(*) FROM PRODUCT", rs -> {
                PageBuilder pb = new PageBuilder();
                pb.appendLine("Number of products: ");
                if (rs.next()) {
                    pb.appendLine(rs.getInt(1));
                }
                response.getWriter().print(pb);
            });
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
