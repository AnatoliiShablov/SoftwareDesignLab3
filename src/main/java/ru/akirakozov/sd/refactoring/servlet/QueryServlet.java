package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.html.PageBuilder;
import ru.akirakozov.sd.refactoring.http.HttpResponseBuilder;
import ru.akirakozov.sd.refactoring.sql.SqlWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String command = request.getParameter("command");
        final PageBuilder pb = new PageBuilder();
        String text = "";

        switch (command) {
            case "max" -> SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1", rs -> {
                pb.appendHeader1("Product with max price: ");
                if (rs.next()) {
                    pb.appendProductInfo(rs.getString("name"), rs.getInt("price"));
                }
            });
            case "min" -> SqlWorker.query("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1", rs -> {
                pb.appendHeader1("Product with min price: ");
                if (rs.next()) {
                    pb.appendProductInfo(rs.getString("name"), rs.getInt("price"));
                }
            });
            case "sum" -> SqlWorker.query("SELECT SUM(price) FROM PRODUCT", rs -> {
                pb.appendLine("Summary price: ");
                if (rs.next()) {
                    pb.appendLine(rs.getInt(1));
                }
            });
            case "count" -> SqlWorker.query("SELECT COUNT(*) FROM PRODUCT", rs -> {
                pb.appendLine("Number of products: ");
                if (rs.next()) {
                    pb.appendLine(rs.getInt(1));
                }
            });
            default -> text = "Unknown command: " + command;
        }
        HttpResponseBuilder.createResponse(response, HttpServletResponse.SC_OK, "text/html", text.isEmpty() ? pb : text);
    }

}
