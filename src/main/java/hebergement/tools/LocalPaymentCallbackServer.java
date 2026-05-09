package hebergement.tools;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hebergement.services.PaymentService;
import hebergement.services.ReservationService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class LocalPaymentCallbackServer {

    private HttpServer server;
    private int port = -1;

    public int getPort() {
        return port;
    }

    public void start(ReservationService rs, PaymentService ps) throws IOException {
        if (server != null) return;

        int[] portsToTry = {4243, 4244, 4245, 8088, 8090};
        IOException last = null;

        for (int p : portsToTry) {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", p), 0);
                port = p;

                server.createContext("/success", ex -> handleSuccess(ex, rs, ps));
                server.createContext("/cancel",  ex -> handleCancel(ex, rs, ps));

                server.setExecutor(null);
                server.start();

                System.out.println("[PAYMENT] Callback server started on http://127.0.0.1:" + port);
                return;

            } catch (BindException be) {
                last = be;
                server = null;
                port = -1;
            }
        }

        throw new IOException("Aucun port libre pour le serveur callback.", last);
    }

    // ✅ SUCCESS
    private void handleSuccess(HttpExchange ex, ReservationService rs, PaymentService ps) throws IOException {
        try {
            URI uri = ex.getRequestURI();
            String sessionId = getQueryParam(uri.getQuery(), "session_id");

            if (sessionId == null) {
                respond(ex, 400, "session_id manquant");
                return;
            }

            var session = ps.retrieveSession(sessionId);

            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                respond(ex, 200, "Paiement non confirmé (status=" + session.getPaymentStatus() + ").");
                return;
            }

            String reservationIdStr = session.getMetadata().get("reservation_id");
            int reservationId = Integer.parseInt(reservationIdStr);

            rs.updateStatus(reservationId, "confirmee");

            System.out.println("✅ Réservation #" + reservationId + " confirmée !");

            respond(ex, 200,
                    "<html><body style='font-family:sans-serif;text-align:center;padding:40px'>" +
                            "<h2 style='color:green'>✅ Paiement réussi !</h2>" +
                            "<p>Réservation #" + reservationId + " confirmée.</p>" +
                            "<p>Vous pouvez fermer cette page.</p>" +
                            "</body></html>"
            );

        } catch (Exception e) {
            e.printStackTrace();
            respond(ex, 500, "Erreur serveur paiement: " + e.getMessage());
        }
    }

    // ❌ CANCEL
    private void handleCancel(HttpExchange ex, ReservationService rs, PaymentService ps) throws IOException {
        try {
            URI uri = ex.getRequestURI();
            String sessionId = getQueryParam(uri.getQuery(), "session_id");

            if (sessionId != null) {
                try {
                    var session = ps.retrieveSession(sessionId);
                    String reservationIdStr = session.getMetadata().get("reservation_id");
                    if (reservationIdStr != null) {
                        int reservationId = Integer.parseInt(reservationIdStr);
                        rs.updateStatus(reservationId, "annulee");
                        System.out.println("❌ Réservation #" + reservationId + " annulée.");
                    }
                } catch (Exception e) {
                    System.out.println("Erreur cancel: " + e.getMessage());
                }
            }

            respond(ex, 200,
                    "<html><body style='font-family:sans-serif;text-align:center;padding:40px'>" +
                            "<h2 style='color:red'>❌ Paiement annulé.</h2>" +
                            "<p>Vous pouvez fermer cette page.</p>" +
                            "</body></html>"
            );

        } catch (Exception e) {
            e.printStackTrace();
            respond(ex, 500, "Erreur serveur cancel: " + e.getMessage());
        }
    }

    private static String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
        server = null;
        port = -1;
    }
}