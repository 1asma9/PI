package edu.destination.services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripeService {

    // ⚠️ Remplacez par votre clé secrète Stripe (sk_test_...)
    private static final String SECRET_KEY = "sk_test_51TLAdUCEh9WAm2eVZ0LidL31mXUtNHBo3uMD24GhWXDgG5SGfApiVNQNoziV1IrH3VAf6mHjlwXLmPD3YJdajY9n00B6CkNVcc";

    private static final String SUCCESS_URL = "http://localhost:8080/success?voyageId={VOYAGE_ID}";
    private static final String CANCEL_URL  = "http://localhost:8080/cancel";

    public static String createCheckoutUrl(int voyageId, double prixEuros, String description) throws Exception {
        Stripe.apiKey = SECRET_KEY;

        long prixCentimes = Math.round(prixEuros * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL.replace("{VOYAGE_ID}", String.valueOf(voyageId)))
                .setCancelUrl(CANCEL_URL)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(prixCentimes)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(description)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}