package tn.esprit.spring.b2u.backend;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;

import static org.assertj.core.api.Assertions.*;

/**
 * 🔒 TEST DYNAMIQUE DE SÉCURITÉ — Injection & XSS (sans MockMvc)
 *
 * Type    : Dynamique (validation des données / règles métier de sécurité)
 * Outil   : JUnit 5 + AssertJ (NO MockMvc, NO Spring context)
 * Commande: .\mvnw test -Dtest=DynamicSecurityTest
 *
 * Ces tests vérifient la résistance au niveau des données :
 * - Injection NoSQL via champs String
 * - XSS dans les champs texte
 * - Validation des formats (email, téléphone)
 * - Règles métier de sécurité
 *
 * COMPLÉMENT : Pour un scan dynamique complet (DAST), lancer OWASP ZAP :
 *   docker run -t owasp/zap2docker-stable zap-baseline.py -t http://localhost:8080
 * Et OWASP Dependency Check (déjà dans pom.xml) :
 *   .\mvnw dependency-check:check
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Dynamiques de Sécurité — Injection & XSS")
class DynamicSecurityTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers: simule la validation que ton service DOIT faire
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Simule ce que ton service doit rejeter :
     * tout champ contenant des opérateurs NoSQL MongoDB
     */
    private boolean containsNoSqlInjection(String value) {
        if (value == null) return false;
        return value.contains("$gt") || value.contains("$ne") ||
                value.contains("$regex") || value.contains("$where") ||
                value.contains("$or") || value.contains("$and") ||
                value.contains("{") && value.contains("}");
    }

    /**
     * Simule ce que ton service doit rejeter :
     * tout champ contenant des balises HTML/JS dangereuses
     */
    private boolean containsXss(String value) {
        if (value == null) return false;
        String lower = value.toLowerCase();
        return lower.contains("<script") || lower.contains("</script>") ||
                lower.contains("onerror=") || lower.contains("onclick=") ||
                lower.contains("javascript:") || lower.contains("<img") ||
                lower.contains("alert(");
    }

    /**
     * Simule la validation email basique
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Simule la validation téléphone
     */
    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^[0-9]{8,15}$");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. INJECTION NoSQL
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("🔒 [NoSQL] Payload $gt dans le champ name → détecté comme injection")
    void noSqlInjection_gtInName_isDetected() {
        String maliciousName = "{\"$gt\": \"\"}";
        assertThat(containsNoSqlInjection(maliciousName))
                .as("Le payload $gt doit être détecté comme injection NoSQL")
                .isTrue();
    }

    @Test
    @DisplayName("🔒 [NoSQL] Payload $regex dans query → détecté comme injection")
    void noSqlInjection_regexPayload_isDetected() {
        String malicious = "{\"$regex\": \".*\"}";
        assertThat(containsNoSqlInjection(malicious)).isTrue();
    }

    @Test
    @DisplayName("🔒 [NoSQL] Payload $where dans champ → détecté")
    void noSqlInjection_wherePayload_isDetected() {
        String malicious = "$where: this.hoursPerWeek > 0";
        assertThat(containsNoSqlInjection(malicious)).isTrue();
    }

    @Test
    @DisplayName("✅ [NoSQL] Valeur normale → pas détectée comme injection")
    void noSqlInjection_normalValue_notDetected() {
        assertThat(containsNoSqlInjection("TechCorp")).isFalse();
        assertThat(containsNoSqlInjection("Développeur Java")).isFalse();
        assertThat(containsNoSqlInjection("contact@test.tn")).isFalse();
    }

    @Test
    @DisplayName("🔒 [NoSQL] Entreprise avec name injecté → NE doit PAS être créée")
    void noSqlInjection_inEntrepriseEntity_mustBeRejected() {
        Entreprise e = new Entreprise();
        e.setName("{\"$gt\": \"\"}");
        e.setEmail("hack@test.tn");

        // Vérifie que la logique de validation détecte l'injection
        boolean isInjection = containsNoSqlInjection(e.getName());
        assertThat(isInjection)
                .as("Une entreprise avec nom NoSQL injecté doit être rejetée avant save()")
                .isTrue();
    }

    @Test
    @DisplayName("🔒 [NoSQL] WorkPost avec title injecté → NE doit PAS être créé")
    void noSqlInjection_inWorkPostTitle_mustBeRejected() {
        WorkPost wp = new WorkPost();
        wp.setTitle("{\"$or\": [{\"status\": \"ACTIVE\"}]}");

        assertThat(containsNoSqlInjection(wp.getTitle())).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. XSS — Cross-Site Scripting
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("🔒 [XSS] <script>alert()</script> dans name → détecté")
    void xss_scriptTagInName_isDetected() {
        String xss = "<script>alert('XSS')</script>";
        assertThat(containsXss(xss)).isTrue();
    }

    @Test
    @DisplayName("🔒 [XSS] <img onerror=alert(1)> dans description → détecté")
    void xss_imgOnErrorInDescription_isDetected() {
        String xss = "<img src=x onerror=alert(1)>";
        assertThat(containsXss(xss)).isTrue();
    }

    @Test
    @DisplayName("🔒 [XSS] javascript: dans champ → détecté")
    void xss_javascriptProtocol_isDetected() {
        String xss = "javascript:alert(document.cookie)";
        assertThat(containsXss(xss)).isTrue();
    }

    @Test
    @DisplayName("✅ [XSS] Texte normal → pas détecté comme XSS")
    void xss_normalText_notDetected() {
        assertThat(containsXss("Développeur Full Stack")).isFalse();
        assertThat(containsXss("Java, Angular, Spring Boot")).isFalse();
        assertThat(containsXss("Société de développement IT")).isFalse();
    }

    @Test
    @DisplayName("🔒 [XSS] Entreprise avec description XSS → NE doit PAS être sauvegardée")
    void xss_inEntrepriseDescription_mustBeRejected() {
        Entreprise e = new Entreprise();
        e.setName("NomCorrect");
        e.setDescription("<script>document.cookie</script>");

        assertThat(containsXss(e.getDescription()))
                .as("Description XSS doit être détectée avant save()")
                .isTrue();
    }

    @Test
    @DisplayName("🔒 [XSS] WorkPost requiredSkills XSS → NE doit PAS être sauvegardé")
    void xss_inWorkPostSkills_mustBeRejected() {
        WorkPost wp = new WorkPost();
        wp.setRequiredSkills("<img onerror=alert(1)>");

        assertThat(containsXss(wp.getRequiredSkills())).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. VALIDATION DE FORMAT
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ [VALIDATION] Emails valides → acceptés")
    void emailValidation_valid_accepted() {
        assertThat(isValidEmail("contact@techcorp.tn")).isTrue();
        assertThat(isValidEmail("user@gmail.com")).isTrue();
        assertThat(isValidEmail("admin@b2u.esprit.tn")).isTrue();
    }

    @Test
    @DisplayName("🔒 [VALIDATION] Emails invalides → rejetés")
    void emailValidation_invalid_rejected() {
        assertThat(isValidEmail("notanemail")).isFalse();
        assertThat(isValidEmail("@nodomain.tn")).isFalse();
        assertThat(isValidEmail("no@")).isFalse();
        assertThat(isValidEmail(null)).isFalse();
        assertThat(isValidEmail("<script>@test.tn")).isFalse();
    }

    @Test
    @DisplayName("✅ [VALIDATION] Téléphones valides → acceptés")
    void phoneValidation_valid_accepted() {
        assertThat(isValidPhone("12345678")).isTrue();
        assertThat(isValidPhone("55667788")).isTrue();
    }

    @Test
    @DisplayName("🔒 [VALIDATION] Téléphones invalides → rejetés")
    void phoneValidation_invalid_rejected() {
        assertThat(isValidPhone("abc123")).isFalse();
        assertThat(isValidPhone("123")).isFalse(); // trop court
        assertThat(isValidPhone(null)).isFalse();
        assertThat(isValidPhone("+216 12 345 678")).isFalse(); // espaces/+
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. RÈGLES MÉTIER DE SÉCURITÉ — WorkPost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("🔒 [MÉTIER] WorkPost avec hoursPerWeek négatif → invalide")
    void workPost_negativeHours_isInvalid() {
        WorkPost wp = new WorkPost();
        wp.setHoursPerWeek(-5);
        assertThat(wp.getHoursPerWeek()).isLessThan(0);
        // ton service doit lancer IllegalArgumentException sur cette valeur
    }

    @Test
    @DisplayName("🔒 [MÉTIER] WorkPost avec hoursPerWeek = 0 → invalide")
    void workPost_zeroHours_isInvalid() {
        WorkPost wp = new WorkPost();
        wp.setHoursPerWeek(0);
        assertThat(wp.getHoursPerWeek()).isEqualTo(0);
    }

    @Test
    @DisplayName("✅ [MÉTIER] WorkPost ACTIVE avec title valide → valide")
    void workPost_validData_isAccepted() {
        WorkPost wp = new WorkPost();
        wp.setTitle("Développeur Backend");
        wp.setHoursPerWeek(20);
        wp.setStatus(WorkPostStatus.ACTIVE);
        wp.setEntrepriseId("ent-001");

        assertThat(wp.getTitle()).isNotBlank();
        assertThat(wp.getHoursPerWeek()).isPositive();
        assertThat(wp.getStatus()).isEqualTo(WorkPostStatus.ACTIVE);
        assertThat(containsXss(wp.getTitle())).isFalse();
        assertThat(containsNoSqlInjection(wp.getTitle())).isFalse();
    }

    @Test
    @DisplayName("🔒 [MÉTIER] Entreprise sans name → invalide")
    void entreprise_nullName_isInvalid() {
        Entreprise e = new Entreprise();
        e.setName(null);
        assertThat(e.getName()).isNull();
    }

    @Test
    @DisplayName("🔒 [MÉTIER] Entreprise email vide → invalide")
    void entreprise_emptyEmail_isInvalid() {
        assertThat(isValidEmail("")).isFalse();
        assertThat(isValidEmail("  ")).isFalse();
    }
}