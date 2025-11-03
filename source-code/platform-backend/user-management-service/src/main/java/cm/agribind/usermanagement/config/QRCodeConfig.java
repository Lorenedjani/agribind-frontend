package cm.agribind.usermanagement.config;

import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QRCodeConfig {

    @Value("${agribind.qrcode.default-size:300}")
    private int defaultQrSize;

    @Value("${agribind.qrcode.default-format:PNG}")
    private String defaultQrFormat;

    @Value("${agribind.qrcode.expiration-minutes:30}")
    private int qrExpirationMinutes;

    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }

    @Bean
    public QRCodeProperties qrCodeProperties() {
        return new QRCodeProperties(defaultQrSize, defaultQrFormat, qrExpirationMinutes);
    }

    public static class QRCodeProperties {
        private final int defaultSize;
        private final String defaultFormat;
        private final int expirationMinutes;

        public QRCodeProperties(int defaultSize, String defaultFormat, int expirationMinutes) {
            this.defaultSize = defaultSize;
            this.defaultFormat = defaultFormat;
            this.expirationMinutes = expirationMinutes;
        }

        public int getDefaultSize() {
            return defaultSize;
        }

        public String getDefaultFormat() {
            return defaultFormat;
        }

        public int getExpirationMinutes() {
            return expirationMinutes;
        }
    }
}