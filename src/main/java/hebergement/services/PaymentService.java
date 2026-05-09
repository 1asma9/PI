package hebergement.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class PaymentService {

    static {
        String key = System.getenv("STRIPE_SECRET_KEY");
        if (key == null || key.isBlank()) {
            throw new RuntimeException("STRIPE_SECRET_KEY manquante (variable d'environnement).");
        }
        Stripe.apiKey = key;
    }

    public Session createCheckoutSession(int reservationId,
                                         double amountEUR,
                                         String clientEmail,
                                         int callbackPort) throws StripeException {

        long amount = Math.round(amountEUR * 100);

        String successUrl = "http://127.0.0.1:" + callbackPort + "/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl  = "http://127.0.0.1:" + callbackPort + "/cancel?session_id={CHECKOUT_SESSION_ID}";

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setCustomerEmail(clientEmail)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("eur")
                                                        .setUnitAmount(amount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Réservation #" + reservationId)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .putMetadata("reservation_id", String.valueOf(reservationId))
                        .build();

        return Session.create(params);
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}