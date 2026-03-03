package com.rbi.loanservice.dto;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.RiskBand;
import java.util.List;
import java.util.UUID;

public class LoanApplicationResponse {

    private UUID applicationId;
    private ApplicationStatus status;

    // null for rejected applications
    private RiskBand riskBand;

    // present only when approved
    private OfferResponse offer;

    // present only when rejected
    private List<String> rejectionReasons;

    public LoanApplicationResponse() {}

    // Factory for approved response
    public static LoanApplicationResponse approved(UUID id, RiskBand riskBand, OfferResponse offer) {
        LoanApplicationResponse r = new LoanApplicationResponse();
        r.applicationId = id;
        r.status = ApplicationStatus.APPROVED;
        r.riskBand = riskBand;
        r.offer = offer;
        return r;
    }

    // Factory for rejected response
    public static LoanApplicationResponse rejected(UUID id, List<String> reasons) {
        LoanApplicationResponse r = new LoanApplicationResponse();
        r.applicationId = id;
        r.status = ApplicationStatus.REJECTED;
        r.riskBand = null;
        r.rejectionReasons = reasons;
        return r;
    }

    public UUID getApplicationId() { return applicationId; }
    public ApplicationStatus getStatus() { return status; }
    public RiskBand getRiskBand() { return riskBand; }
    public OfferResponse getOffer() { return offer; }
    public List<String> getRejectionReasons() { return rejectionReasons; }
}
