package cm.agribind.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class LocalizationConfig {

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            new Locale("fr"), // French
            new Locale("en"), // English
            new Locale("ful"), // Fulfulde
            new Locale("ewe"), // Ewondo
            new Locale("dua")  // Duala
    );

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(SUPPORTED_LOCALES);
        localeResolver.setDefaultLocale(new Locale("fr")); // Default to French
        return localeResolver;
    }

    @Bean
    public LocalizationService localizationService() {
        return new LocalizationService();
    }

    public static class LocalizationService {

        private static final String DEFAULT_LANGUAGE = "fr";

        public String getSupportedLanguages() {
            return String.join(",", SUPPORTED_LOCALES.stream()
                    .map(Locale::getLanguage)
                    .toList());
        }

        public boolean isLanguageSupported(String language) {
            return SUPPORTED_LOCALES.stream()
                    .anyMatch(locale -> locale.getLanguage().equals(language));
        }

        public String getDefaultLanguage() {
            return DEFAULT_LANGUAGE;
        }

        public String resolveLanguage(String preferredLanguage) {
            if (preferredLanguage != null && isLanguageSupported(preferredLanguage)) {
                return preferredLanguage;
            }
            return DEFAULT_LANGUAGE;
        }
    }
}