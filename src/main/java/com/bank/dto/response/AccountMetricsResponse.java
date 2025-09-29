package com.bank.dto.response;

/**
 * Response DTO for the GET /metrics/accounts endpoint.
 * Returns statistics about accounts based on balance conditions.
 */
public class AccountMetricsResponse {

    private Long count;
    private String condition;

    public AccountMetricsResponse() {
    }

    public AccountMetricsResponse(Long count, String condition) {
        this.count = count;
        this.condition = condition;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
