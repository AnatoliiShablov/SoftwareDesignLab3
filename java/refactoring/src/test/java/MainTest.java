import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ru.akirakozov.sd.refactoring.Main;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class MainTest {
    static class Product {
        public Product(final String name, final int price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return name + "\t" + price + "</br>";
        }

        public String name;
        public int price;
    }

    final static int CONNECTION_PORT = 8081;

    final static int CONNECTION_MAX_RETRIES = 10;
    final static int CONNECTION_TIMEOUT_IN_MILLIS = 100;
    final static int RECONNECT_TIMEOUT_IN_MILLIS = 1000;


    final static String HTML_BEGIN_TAG = "<html><body>";
    final static String HTML_END_TAG = "</body></html>";

    final static String HTML_BEGIN_TAG_NL = HTML_BEGIN_TAG + System.lineSeparator();
    final static String HTML_END_TAG_NL = HTML_END_TAG + System.lineSeparator();

    final static Product[] ZERO_PRODUCTS = new Product[]{};
    final static Product[] ONE_PRODUCT = new Product[]{new Product("IPhone", 300)};
    final static Product[] FEW_PRODUCTS = new Product[]{new Product("IPhone", 300), new Product("SomethingElse", 400), new Product("SomethingElse2", 200)};


    static Thread serverThread;

    @BeforeAll
    static void startServer() throws Exception {
        serverThread = new Thread(() -> {
            try {
                Main.main(new String[0]);
            } catch (InterruptedException e) {
                System.out.println("Closing thread...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.start();

        for (int i = 0; i < CONNECTION_MAX_RETRIES; ++i) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", CONNECTION_PORT), CONNECTION_TIMEOUT_IN_MILLIS);
                return;
            } catch (IOException exception) {
                Thread.sleep(RECONNECT_TIMEOUT_IN_MILLIS);
            }
        }

        throw new Exception("Unable to connect to server");
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " PRICE          INT     NOT NULL)";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            String sql = "DROP TABLE IF EXISTS PRODUCT";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    @AfterAll
    static void finishServer() throws InterruptedException {
        serverThread.interrupt();
        serverThread.join();
    }


    URI getHttpAddress(final String path) {
        return URI.create("http://localhost:" + 8081 + path);
    }


    String getAddProductPath(final Product product) {
        return "/add-product?name=" + product.name + "&price=" + product.price;
    }

    String getCommandPath(final String command) {
        return "/query?command=" + command;
    }


    String getResponse(final String path) throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(getHttpAddress(path)).GET().build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        return response.body();
    }

    void getProductsTest(final Set<Product> products) throws IOException, InterruptedException {
        final String fullPage = getResponse("/get-products");


        assertTrue(fullPage.startsWith(HTML_BEGIN_TAG_NL));
        assertTrue(fullPage.endsWith(HTML_END_TAG_NL));

        final String productsOnly = fullPage.substring(HTML_BEGIN_TAG_NL.length(), fullPage.length() - HTML_END_TAG_NL.length());

        final Set<String> expectedSet = new HashSet<>();
        products.forEach(product -> expectedSet.add(product.toString()));

        assertEquals(expectedSet, productsOnly.lines().collect(Collectors.toSet()));
    }


    void addProductsTest(final Product product) throws IOException, InterruptedException {
        assertEquals(new ResponseBuilder().appendLine("OK").toString(), getResponse(getAddProductPath(product)));
    }

    void getQueryTest(final String command, final String expected) throws IOException, InterruptedException {
        assertEquals(expected, getResponse(getCommandPath(command)));
    }

    @Test
    void emptyTest() throws IOException, InterruptedException {
        getProductsTest(new HashSet<>());
    }

    void checkElements(final Product[] products) throws IOException, InterruptedException {
        for (Product product : products) {
            addProductsTest(product);
        }
        getProductsTest(Set.of(products));
    }

    void checkQuery(final String command, final String header, final Product[] products, final Function<Stream<Product>, String> res) throws IOException, InterruptedException {
        getQueryTest(command, new ResponseBuilder()
                .appendLine(HTML_BEGIN_TAG)
                .appendLine(header)
                .appendLine(res.apply(Arrays.stream(products)))
                .appendLine(HTML_END_TAG)
                .toString());
    }


    void checkMin(final Product[] products) throws IOException, InterruptedException {
        checkQuery("min", "<h1>Product with min price: </h1>", products,
                productStream -> productStream
                        .min(Comparator.comparingInt((Product p) -> p.price))
                        .orElseThrow()
                        .toString());
    }

    void checkMax(final Product[] products) throws IOException, InterruptedException {
        checkQuery("max", "<h1>Product with max price: </h1>", products,
                productStream -> productStream
                        .max(Comparator.comparingInt((Product p) -> p.price))
                        .orElseThrow()
                        .toString());
    }

    void checkSum(final Product[] products) throws IOException, InterruptedException {
        checkQuery("sum", "Summary price: ", products,
                productStream -> productStream
                        .map(p -> p.price)
                        .reduce(0, Integer::sum)
                        .toString());
    }

    void checkCount(final Product[] products) throws IOException, InterruptedException {
        checkQuery("count", "Number of products: ", products,
                productStream -> Long.toString(productStream.count()));
    }

    void checkUnknown(final String command) throws IOException, InterruptedException {
        getQueryTest(command, new ResponseBuilder()
                .appendLine("Unknown command: " + command)
                .toString());
    }


    @Test
    void oneElementsTest() throws IOException, InterruptedException {
        checkElements(ONE_PRODUCT);
    }

    @Test
    void fewElementsTest() throws IOException, InterruptedException {
        checkElements(FEW_PRODUCTS);
    }

    @FunctionalInterface
    public interface QueryFunction<T> {
        void apply(T t) throws IOException, InterruptedException;
    }

    void queryTestImpl(final Product[] products, final QueryFunction<Product[]> check) throws IOException, InterruptedException {
        checkElements(products);
        check.apply(products);
    }

    @Test
    void queryMinOneTest() throws IOException, InterruptedException {
        queryTestImpl(ONE_PRODUCT, this::checkMin);
    }

    @Test
    void queryMinFewTest() throws IOException, InterruptedException {
        queryTestImpl(FEW_PRODUCTS, this::checkMin);
    }

    @Test
    void queryMaxOneTest() throws IOException, InterruptedException {
        queryTestImpl(ONE_PRODUCT, this::checkMax);
    }

    @Test
    void queryMaxFewTest() throws IOException, InterruptedException {
        queryTestImpl(FEW_PRODUCTS, this::checkMax);
    }

    @Test
    void querySumZeroTest() throws IOException, InterruptedException {
        queryTestImpl(ZERO_PRODUCTS, this::checkSum);

    }

    @Test
    void querySumOneTest() throws IOException, InterruptedException {
        queryTestImpl(ONE_PRODUCT, this::checkSum);

    }

    @Test
    void querySumFewTest() throws IOException, InterruptedException {
        queryTestImpl(FEW_PRODUCTS, this::checkSum);
    }

    @Test
    void queryCountZeroTest() throws IOException, InterruptedException {
        queryTestImpl(ZERO_PRODUCTS, this::checkCount);

    }

    @Test
    void queryCountOneTest() throws IOException, InterruptedException {
        queryTestImpl(ONE_PRODUCT, this::checkCount);

    }

    @Test
    void queryCountFewTest() throws IOException, InterruptedException {
        queryTestImpl(FEW_PRODUCTS, this::checkCount);
    }

    @Test
    void queryUnknownTest() throws IOException, InterruptedException {
        checkUnknown("biliberda");
    }

}