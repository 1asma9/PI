package edu.destination.controllers;

import edu.destination.tools.MyConnection;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import java.sql.*;
import java.util.*;

public class DestinationStatistiqueController {

    @FXML private WebView webViewStats;

    @FXML
    public void initialize() {
        try {
            StatsData data = loadStats();
            String html = buildHtml(data);
            WebEngine engine = webViewStats.getEngine();
            engine.loadContent(html, "text/html; charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    // DATA MODEL
    // ══════════════════════════════════════════════════════
    static class StatsData {
        int total, actifs, inactifs;
        long totalVisites, totalLikes, totalReservations, totalPaies, totalNonPaies;
        List<String[]> topVisites      = new ArrayList<>();
        List<String[]> topLikes        = new ArrayList<>();
        List<String[]> topReservations = new ArrayList<>();
        Map<String, Integer> parPays       = new LinkedHashMap<>();
        Map<String, Long>    visitesSaison = new LinkedHashMap<>();
        Map<String, Long>    likesSaison   = new LinkedHashMap<>();
    }

    // ══════════════════════════════════════════════════════
    // LOAD DATA FROM DB
    // ══════════════════════════════════════════════════════
    private StatsData loadStats() throws SQLException {
        StatsData d = new StatsData();
        Connection cx = MyConnection.getInstance().getCnx();

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) as total, " +
                             "SUM(statut=1) as actifs, SUM(statut=0 OR statut IS NULL) as inactifs, " +
                             "COALESCE(SUM(nb_visites),0) as tv, COALESCE(SUM(nb_likes),0) as tl " +
                             "FROM destination")) {
            if (rs.next()) {
                d.total        = rs.getInt("total");
                d.actifs       = rs.getInt("actifs");
                d.inactifs     = rs.getInt("inactifs");
                d.totalVisites = rs.getLong("tv");
                d.totalLikes   = rs.getLong("tl");
            }
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM voyage_reservations")) {
            if (rs.next()) d.totalReservations = rs.getLong(1);
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT SUM(paid=1) as paies, SUM(paid=0 OR paid IS NULL) as nonpaies " +
                             "FROM voyage_reservations")) {
            if (rs.next()) {
                d.totalPaies    = rs.getLong("paies");
                d.totalNonPaies = rs.getLong("nonpaies");
            }
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT d.nom, d.pays, d.meilleure_saison, d.nb_visites, d.nb_likes, d.statut, " +
                             "COUNT(vr.voyage_id) as nb_res " +
                             "FROM destination d " +
                             "LEFT JOIN voyage v ON v.destination_id = d.id " +
                             "LEFT JOIN voyage_reservations vr ON vr.voyage_id = v.id " +
                             "GROUP BY d.id ORDER BY d.nb_visites DESC LIMIT 5")) {
            while (rs.next()) {
                d.topVisites.add(new String[]{
                        rs.getString("nom"), rs.getString("pays"),
                        rs.getString("meilleure_saison"), String.valueOf(rs.getLong("nb_visites")),
                        String.valueOf(rs.getLong("nb_likes")), String.valueOf(rs.getLong("nb_res")),
                        String.valueOf(rs.getInt("statut"))
                });
            }
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT nom, nb_likes FROM destination ORDER BY nb_likes DESC LIMIT 5")) {
            while (rs.next())
                d.topLikes.add(new String[]{ rs.getString("nom"), String.valueOf(rs.getLong("nb_likes")) });
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT d.nom, COUNT(vr.voyage_id) as nb_res " +
                             "FROM destination d " +
                             "LEFT JOIN voyage v ON v.destination_id = d.id " +
                             "LEFT JOIN voyage_reservations vr ON vr.voyage_id = v.id " +
                             "GROUP BY d.id ORDER BY nb_res DESC LIMIT 5")) {
            while (rs.next())
                d.topReservations.add(new String[]{ rs.getString("nom"), String.valueOf(rs.getLong("nb_res")) });
        }

        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT pays, COUNT(*) as c FROM destination GROUP BY pays ORDER BY c DESC")) {
            while (rs.next()) d.parPays.put(rs.getString("pays"), rs.getInt("c"));
        }

        String[] saisons = {"Printemps", "Ete", "Automne", "Hiver"};
        for (String s : saisons) { d.visitesSaison.put(s, 0L); d.likesSaison.put(s, 0L); }
        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT meilleure_saison, SUM(nb_visites) as sv, SUM(nb_likes) as sl " +
                             "FROM destination WHERE meilleure_saison IS NOT NULL GROUP BY meilleure_saison")) {
            while (rs.next()) {
                String s = rs.getString("meilleure_saison");
                if (d.visitesSaison.containsKey(s)) {
                    d.visitesSaison.put(s, rs.getLong("sv"));
                    d.likesSaison.put(s, rs.getLong("sl"));
                }
            }
        }

        return d;
    }

    // ══════════════════════════════════════════════════════
    // SVG CHART BUILDERS
    // ══════════════════════════════════════════════════════

    /** Barres horizontales */
    private String svgBarH(List<String[]> rows, int labelCol, int valCol, String color, int w, int h) {
        if (rows.isEmpty()) return "<svg width='" + w + "' height='" + h + "'><text x='10' y='20' fill='#64748b'>Aucune donnee</text></svg>";
        long max = rows.stream().mapToLong(r -> parseLong(r[valCol])).max().orElse(1);
        if (max == 0) max = 1;
        int barH = 28, gap = 10, paddingLeft = 130, paddingRight = 20, paddingTop = 10;
        int totalH = rows.size() * (barH + gap) + paddingTop + 20;
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(w).append("' height='").append(totalH).append("'>");
        sb.append("<rect width='").append(w).append("' height='").append(totalH).append("' fill='transparent'/>");
        int availW = w - paddingLeft - paddingRight;
        for (int i = 0; i < rows.size(); i++) {
            long val = parseLong(rows.get(i)[valCol]);
            int barW = (int) (availW * val / max);
            if (barW < 2 && val > 0) barW = 2;
            int y = paddingTop + i * (barH + gap);
            String label = truncate(rows.get(i)[labelCol], 16);
            // fond barre
            sb.append("<rect x='").append(paddingLeft).append("' y='").append(y)
                    .append("' width='").append(availW).append("' height='").append(barH)
                    .append("' rx='6' fill='#2a2a4a'/>");
            // barre valeur
            sb.append("<rect x='").append(paddingLeft).append("' y='").append(y)
                    .append("' width='").append(barW).append("' height='").append(barH)
                    .append("' rx='6' fill='").append(color).append("'/>");
            // label gauche
            sb.append("<text x='").append(paddingLeft - 8).append("' y='").append(y + barH / 2 + 5)
                    .append("' text-anchor='end' fill='#cbd5e1' font-size='11' font-family='Segoe UI,sans-serif'>")
                    .append(escapeXml(label)).append("</text>");
            // valeur droite
            sb.append("<text x='").append(paddingLeft + barW + 6).append("' y='").append(y + barH / 2 + 5)
                    .append("' fill='#94a3b8' font-size='11' font-family='Segoe UI,sans-serif'>")
                    .append(val).append("</text>");
        }
        sb.append("</svg>");
        return sb.toString();
    }

    /** Barres verticales groupées (2 séries) */
    private String svgBarVGrouped(List<String> labels, List<Long> vals1, List<Long> vals2,
                                  String color1, String color2, String legend1, String legend2, int w, int h) {
        int n = labels.size();
        if (n == 0) return "<svg width='" + w + "' height='" + h + "'><text x='10' y='20' fill='#64748b'>Aucune donnee</text></svg>";
        long max = 1;
        for (long v : vals1) if (v > max) max = v;
        for (long v : vals2) if (v > max) max = v;

        int paddingTop = 20, paddingBottom = 50, paddingLeft = 40, paddingRight = 10;
        int chartH = h - paddingTop - paddingBottom;
        int chartW = w - paddingLeft - paddingRight;
        int groupW = chartW / n;
        int barW = (int)(groupW * 0.3);
        if (barW < 4) barW = 4;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(w).append("' height='").append(h).append("'>");
        sb.append("<rect width='").append(w).append("' height='").append(h).append("' fill='transparent'/>");

        // lignes grille
        for (int g = 0; g <= 4; g++) {
            int y = paddingTop + chartH - (int)(chartH * g / 4.0);
            sb.append("<line x1='").append(paddingLeft).append("' y1='").append(y)
                    .append("' x2='").append(w - paddingRight).append("' y2='").append(y)
                    .append("' stroke='#2a2a4a' stroke-width='1'/>");
            long tickVal = max * g / 4;
            sb.append("<text x='").append(paddingLeft - 4).append("' y='").append(y + 4)
                    .append("' text-anchor='end' fill='#64748b' font-size='9' font-family='Segoe UI,sans-serif'>")
                    .append(tickVal).append("</text>");
        }

        for (int i = 0; i < n; i++) {
            int gx = paddingLeft + i * groupW + groupW / 2;
            // barre 1
            int bh1 = (int)(chartH * vals1.get(i) / max);
            sb.append("<rect x='").append(gx - barW - 2).append("' y='").append(paddingTop + chartH - bh1)
                    .append("' width='").append(barW).append("' height='").append(bh1)
                    .append("' rx='4' fill='").append(color1).append("'/>");
            // barre 2
            int bh2 = (int)(chartH * vals2.get(i) / max);
            sb.append("<rect x='").append(gx + 2).append("' y='").append(paddingTop + chartH - bh2)
                    .append("' width='").append(barW).append("' height='").append(bh2)
                    .append("' rx='4' fill='").append(color2).append("'/>");
            // label
            sb.append("<text x='").append(gx).append("' y='").append(h - paddingBottom + 16)
                    .append("' text-anchor='middle' fill='#cbd5e1' font-size='10' font-family='Segoe UI,sans-serif'>")
                    .append(escapeXml(labels.get(i))).append("</text>");
        }

        // légende
        int ly = h - 10;
        sb.append("<rect x='").append(paddingLeft).append("' y='").append(ly - 10)
                .append("' width='12' height='12' rx='3' fill='").append(color1).append("'/>");
        sb.append("<text x='").append(paddingLeft + 16).append("' y='").append(ly)
                .append("' fill='#94a3b8' font-size='10' font-family='Segoe UI,sans-serif'>").append(legend1).append("</text>");
        sb.append("<rect x='").append(paddingLeft + 80).append("' y='").append(ly - 10)
                .append("' width='12' height='12' rx='3' fill='").append(color2).append("'/>");
        sb.append("<text x='").append(paddingLeft + 96).append("' y='").append(ly)
                .append("' fill='#94a3b8' font-size='10' font-family='Segoe UI,sans-serif'>").append(legend2).append("</text>");

        sb.append("</svg>");
        return sb.toString();
    }

    /** Barres verticales simples */
    private String svgBarV(List<String> labels, List<Long> vals, String color, int w, int h) {
        int n = labels.size();
        if (n == 0) return "<svg width='" + w + "' height='" + h + "'><text x='10' y='20' fill='#64748b'>Aucune donnee</text></svg>";
        long max = vals.stream().mapToLong(Long::longValue).max().orElse(1);
        if (max == 0) max = 1;

        int paddingTop = 20, paddingBottom = 40, paddingLeft = 40, paddingRight = 10;
        int chartH = h - paddingTop - paddingBottom;
        int chartW = w - paddingLeft - paddingRight;
        int barW = Math.max(4, chartW / n - 10);

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(w).append("' height='").append(h).append("'>");
        sb.append("<rect width='").append(w).append("' height='").append(h).append("' fill='transparent'/>");

        for (int g = 0; g <= 4; g++) {
            int y = paddingTop + chartH - (int)(chartH * g / 4.0);
            sb.append("<line x1='").append(paddingLeft).append("' y1='").append(y)
                    .append("' x2='").append(w - paddingRight).append("' y2='").append(y)
                    .append("' stroke='#2a2a4a' stroke-width='1'/>");
            long tickVal = max * g / 4;
            sb.append("<text x='").append(paddingLeft - 4).append("' y='").append(y + 4)
                    .append("' text-anchor='end' fill='#64748b' font-size='9' font-family='Segoe UI,sans-serif'>")
                    .append(tickVal).append("</text>");
        }

        for (int i = 0; i < n; i++) {
            int gx = paddingLeft + i * (chartW / n) + (chartW / n) / 2;
            int bh = (int)(chartH * vals.get(i) / max);
            sb.append("<rect x='").append(gx - barW / 2).append("' y='").append(paddingTop + chartH - bh)
                    .append("' width='").append(barW).append("' height='").append(bh)
                    .append("' rx='5' fill='").append(color).append("'/>");
            sb.append("<text x='").append(gx).append("' y='").append(h - paddingBottom + 14)
                    .append("' text-anchor='middle' fill='#cbd5e1' font-size='10' font-family='Segoe UI,sans-serif'>")
                    .append(escapeXml(truncate(labels.get(i), 10))).append("</text>");
        }
        sb.append("</svg>");
        return sb.toString();
    }

    /** Donut chart */
    private String svgDonut(List<String> labels, List<Long> vals, List<String> colors, int size) {
        long total = vals.stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            return "<svg width='" + size + "' height='" + size + "'><text x='10' y='20' fill='#64748b'>Aucune donnee</text></svg>";
        }
        int cx = size / 2, cy = size / 2 - 10;
        int r = size / 2 - 30, innerR = r - 30;
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(size).append("' height='").append(size + 30).append("'>");
        sb.append("<rect width='").append(size).append("' height='").append(size + 30).append("' fill='transparent'/>");

        double startAngle = -Math.PI / 2;
        for (int i = 0; i < vals.size(); i++) {
            double angle = 2 * Math.PI * vals.get(i) / total;
            double endAngle = startAngle + angle;
            double x1 = cx + r * Math.cos(startAngle), y1 = cy + r * Math.sin(startAngle);
            double x2 = cx + r * Math.cos(endAngle),   y2 = cy + r * Math.sin(endAngle);
            double ix1 = cx + innerR * Math.cos(startAngle), iy1 = cy + innerR * Math.sin(startAngle);
            double ix2 = cx + innerR * Math.cos(endAngle),   iy2 = cy + innerR * Math.sin(endAngle);
            int largeArc = angle > Math.PI ? 1 : 0;
            String path = String.format(Locale.US,
                    "M %.1f %.1f A %d %d 0 %d 1 %.1f %.1f L %.1f %.1f A %d %d 0 %d 0 %.1f %.1f Z",
                    x1, y1, r, r, largeArc, x2, y2,
                    ix2, iy2, innerR, innerR, largeArc, ix1, iy1);
            sb.append("<path d='").append(path).append("' fill='").append(colors.get(i % colors.size()))
                    .append("' stroke='#0f0f1a' stroke-width='2'/>");
            // valeur
            double midA = startAngle + angle / 2;
            int lx = (int)(cx + (r - 15) * Math.cos(midA));
            int ly2 = (int)(cy + (r - 15) * Math.sin(midA));
            sb.append("<text x='").append(lx).append("' y='").append(ly2 + 4)
                    .append("' text-anchor='middle' fill='#fff' font-size='11' font-weight='700' font-family='Segoe UI,sans-serif'>")
                    .append(vals.get(i)).append("</text>");
            startAngle = endAngle;
        }

        // légende en bas
        int legendY = cy + r + 20;
        for (int i = 0; i < labels.size(); i++) {
            int lx = (size / 2) - (labels.size() * 70 / 2) + i * 80;
            sb.append("<rect x='").append(lx).append("' y='").append(legendY)
                    .append("' width='12' height='12' rx='3' fill='").append(colors.get(i % colors.size())).append("'/>");
            sb.append("<text x='").append(lx + 16).append("' y='").append(legendY + 10)
                    .append("' fill='#94a3b8' font-size='11' font-family='Segoe UI,sans-serif'>")
                    .append(escapeXml(labels.get(i))).append("</text>");
        }
        sb.append("</svg>");
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════
    // BUILD HTML
    // ══════════════════════════════════════════════════════
    private String buildHtml(StatsData d) {

        // Top visites (barres horizontales)
        String svgTopVisites = svgBarH(d.topVisites, 0, 3, "#3b82f6", 480, 0);
        String svgTopLikes   = svgBarH(d.topLikes,   0, 1, "#e11d48", 480, 0);
        String svgTopRes     = svgBarH(d.topReservations, 0, 1, "#10b981", 980, 0);

        // Saison groupées
        List<String> saisonLabels = new ArrayList<>(d.visitesSaison.keySet());
        List<Long>   visitesVals  = new ArrayList<>(d.visitesSaison.values());
        List<Long>   likesVals    = new ArrayList<>(d.likesSaison.values());
        String svgSaison = svgBarVGrouped(saisonLabels, visitesVals, likesVals,
                "#3b82f6", "#e11d48", "Visites", "Likes", 480, 220);

        // Pays
        List<String> paysLabels = new ArrayList<>(d.parPays.keySet());
        List<Long>   paysVals   = new ArrayList<>();
        for (int v : d.parPays.values()) paysVals.add((long) v);
        String svgPays = svgBarV(paysLabels, paysVals, "#f97316", 480, 220);

        // Paiements donut
        String svgPaiements = svgDonut(
                Arrays.asList("Payes", "Non payes"),
                Arrays.asList(d.totalPaies, d.totalNonPaies),
                Arrays.asList("#16a34a", "#f87171"), 280);

        // Statut donut
        String svgStatut = svgDonut(
                Arrays.asList("Actives", "Inactives"),
                Arrays.asList((long) d.actifs, (long) d.inactifs),
                Arrays.asList("#16a34a", "#e11d48"), 280);

        // Tableau
        StringBuilder tableRows = new StringBuilder();
        for (int i = 0; i < d.topVisites.size(); i++) {
            String[] r = d.topVisites.get(i);
            String statut    = "1".equals(r[6]) ? "#16a34a" : "#e11d48";
            String statutTxt = "1".equals(r[6]) ? "Actif"   : "Inactif";
            tableRows.append(String.format(
                    "<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td>" +
                            "<td style='color:#3b82f6;font-weight:700'>%s</td>" +
                            "<td style='color:#e11d48;font-weight:700'>%s</td>" +
                            "<td style='color:#10b981;font-weight:700'>%s</td>" +
                            "<td><span style='padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;" +
                            "background:%s;color:#fff'>%s</span></td></tr>",
                    i + 1, r[0], r[1], r[2] != null ? r[2] : "-",
                    r[3], r[4], r[5], statut, statutTxt));
        }

        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n" +
                "<style>\n" +
                "* { margin:0; padding:0; box-sizing:border-box; }\n" +
                "body { background:#0f0f1a; font-family:'Segoe UI',sans-serif; color:#e2e8f0; padding:24px; }\n" +
                "h1 { font-size:22px; font-weight:800; color:#fff; margin-bottom:6px; }\n" +
                ".subtitle { font-size:13px; color:#64748b; margin-bottom:28px; }\n" +
                ".cards { display:grid; grid-template-columns:repeat(5,1fr); gap:14px; margin-bottom:20px; }\n" +
                ".cards3 { display:grid; grid-template-columns:repeat(3,1fr); gap:14px; margin-bottom:28px; }\n" +
                ".card { background:#1a1a2e; border:1px solid #2a2a4a; border-radius:14px; padding:20px; text-align:center; }\n" +
                ".card-val { font-size:28px; font-weight:900; }\n" +
                ".card-lbl { font-size:11px; color:#64748b; margin-top:6px; font-weight:600; text-transform:uppercase; letter-spacing:.05em; }\n" +
                ".grid2 { display:grid; grid-template-columns:1fr 1fr; gap:18px; margin-bottom:18px; }\n" +
                ".grid3c { display:grid; grid-template-columns:1fr 1fr 1fr; gap:18px; margin-bottom:18px; }\n" +
                ".panel { background:#1a1a2e; border:1px solid #2a2a4a; border-radius:14px; padding:20px; }\n" +
                ".panel-full { background:#1a1a2e; border:1px solid #2a2a4a; border-radius:14px; padding:20px; margin-bottom:18px; }\n" +
                ".panel-title { font-size:13px; font-weight:700; color:#fff; margin-bottom:16px; border-left:3px solid #635bff; padding-left:10px; }\n" +
                "table { width:100%; border-collapse:collapse; font-size:12px; }\n" +
                "thead tr { background:#12122a; }\n" +
                "th { padding:10px 12px; text-align:left; color:#64748b; font-weight:600; font-size:11px; text-transform:uppercase; letter-spacing:.05em; border-bottom:1px solid #2a2a4a; }\n" +
                "td { padding:10px 12px; border-bottom:1px solid #1e1e3a; color:#cbd5e1; vertical-align:middle; }\n" +
                "tr:last-child td { border-bottom:none; }\n" +
                "tr:hover td { background:#12122a; }\n" +
                ".svg-wrap { width:100%; overflow:hidden; }\n" +
                ".svg-wrap svg { width:100%; height:auto; }\n" +
                "</style>\n</head>\n<body>\n" +

                "<h1>Statistiques des Destinations</h1>\n" +
                "<p class='subtitle'>Tableau de bord - donnees en temps reel</p>\n" +

                // Cartes ligne 1
                "<div class='cards'>\n" +
                "  <div class='card'><div class='card-val' style='color:#f97316'>" + d.total + "</div><div class='card-lbl'>Total Destinations</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#16a34a'>" + d.actifs + "</div><div class='card-lbl'>Actives</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#e11d48'>" + d.inactifs + "</div><div class='card-lbl'>Inactives</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#3b82f6'>" + d.totalVisites + "</div><div class='card-lbl'>Visites totales</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#e11d48'>" + d.totalLikes + "</div><div class='card-lbl'>Likes totaux</div></div>\n" +
                "</div>\n" +

                // Cartes ligne 2
                "<div class='cards3'>\n" +
                "  <div class='card'><div class='card-val' style='color:#10b981'>" + d.totalReservations + "</div><div class='card-lbl'>Reservations</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#4ade80'>" + d.totalPaies + "</div><div class='card-lbl'>Paiements confirmes</div></div>\n" +
                "  <div class='card'><div class='card-val' style='color:#f87171'>" + d.totalNonPaies + "</div><div class='card-lbl'>Non payes</div></div>\n" +
                "</div>\n" +

                // Ligne 1 : Top visites + Top likes
                "<div class='grid2'>\n" +
                "  <div class='panel'><div class='panel-title' style='border-color:#3b82f6'>Top 5 - Plus visitees</div>" +
                "    <div class='svg-wrap'>" + svgTopVisites + "</div></div>\n" +
                "  <div class='panel'><div class='panel-title' style='border-color:#e11d48'>Top 5 - Plus likees</div>" +
                "    <div class='svg-wrap'>" + svgTopLikes + "</div></div>\n" +
                "</div>\n" +

                // Ligne 2 : Saison + Paiements + Statut
                "<div class='grid3c'>\n" +
                "  <div class='panel'><div class='panel-title' style='border-color:#10b981'>Visites et Likes par saison</div>" +
                "    <div class='svg-wrap'>" + svgSaison + "</div></div>\n" +
                "  <div class='panel'><div class='panel-title' style='border-color:#f59e0b'>Paiements confirmes vs Non payes</div>" +
                "    <div class='svg-wrap'>" + svgPaiements + "</div></div>\n" +
                "  <div class='panel'><div class='panel-title' style='border-color:#10b981'>Statut</div>" +
                "    <div class='svg-wrap'>" + svgStatut + "</div></div>\n" +
                "</div>\n" +

                // Destinations par pays
                "<div class='panel-full'>\n" +
                "  <div class='panel-title' style='border-color:#f97316'>Destinations par pays</div>" +
                "  <div class='svg-wrap'>" + svgPays + "</div>\n" +
                "</div>\n" +

                // Top reservations
                "<div class='panel-full'>\n" +
                "  <div class='panel-title' style='border-color:#10b981'>Top 5 - Plus reservees</div>" +
                "  <div class='svg-wrap'>" + svgTopRes + "</div>\n" +
                "</div>\n" +

                // Tableau classement
                "<div class='panel-full'>\n" +
                "  <div class='panel-title' style='border-color:#f97316'>Classement des destinations</div>\n" +
                "  <table><thead><tr>" +
                "<th>#</th><th>Destination</th><th>Pays</th><th>Saison</th>" +
                "<th>Visites</th><th>Likes</th><th>Reservations</th><th>Statut</th>" +
                "</tr></thead><tbody>" + tableRows + "</tbody></table>\n" +
                "</div>\n" +

                "</body>\n</html>";
    }

    // ══════════════════════════════════════════════════════
    // UTILS
    // ══════════════════════════════════════════════════════
    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}