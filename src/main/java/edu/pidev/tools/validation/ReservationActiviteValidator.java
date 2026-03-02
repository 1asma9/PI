package edu.pidev.tools.validation;

import edu.pidev.entities.ReservationActivite;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReservationActiviteValidator {

    private static final Set<String> STATUTS_VALIDES =
            new HashSet<>(Arrays.asList("EN_ATTENTE", "CONFIRMEE", "ANNULEE"));

    public static void validate(ReservationActivite r) {

        if (r == null)
            throw new ValidationException("Réservation vide.");

        if (r.getDateReservation() == null)
            throw new ValidationException("La date de réservation est obligatoire.");

        if (r.getDateReservation().isBefore(LocalDate.now()))
            throw new ValidationException("La date ne peut pas être dans le passé.");

        if (r.getNombrePersonnes() <= 0)
            throw new ValidationException("Le nombre de personnes doit être > 0.");

        if (r.getStatut() == null || r.getStatut().trim().isEmpty())
            throw new ValidationException("Le statut est obligatoire.");

        String st = r.getStatut().trim().toUpperCase();
        if (!STATUTS_VALIDES.contains(st))
            throw new ValidationException("Statut invalide. Choisir: " + STATUTS_VALIDES);

        r.setStatut(st); // ✅ normalisation

        if (r.getIdActivite() <= 0)
            throw new ValidationException("idActivite invalide (doit être > 0).");
    }
}
