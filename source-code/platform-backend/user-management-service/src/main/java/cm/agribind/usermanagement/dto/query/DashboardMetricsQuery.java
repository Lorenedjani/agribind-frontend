package cm.agribind.usermanagement.dto.query;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardMetricsQuery {

    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private String timePeriod = "MONTH"; // DAY, WEEK, MONTH, QUARTER, YEAR
    private Boolean includeCharts = true;
    private Boolean includeTrends = true;
}