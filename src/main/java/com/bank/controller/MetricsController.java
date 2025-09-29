package com.bank.controller;

import org.springframework.web.bind.annotation.*;
import com.bank.service.AccountService;
import com.bank.dto.response.AccountMetricsResponse;
import com.bank.exception.ErrorMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@RestController
@RequestMapping("/metrics")
@Tag(name = "Metrics", description = "Account metrics operations")
public class MetricsController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/accounts")
    @Operation(summary = "Get account metrics by balance")
    public ResponseEntity<AccountMetricsResponse> getAccountMetrics(
            @RequestParam(required = false) BigDecimal greaterThan,
            @RequestParam(required = false) BigDecimal lessThan) {

        if (greaterThan == null && lessThan == null) {
            throw new IllegalArgumentException(ErrorMessages.METRICS_PARAMETERS_REQUIRED);
        }
        AccountMetricsResponse metrics = accountService.getAccountMetrics(greaterThan, lessThan);
        return ResponseEntity.ok(metrics);
    }
}
