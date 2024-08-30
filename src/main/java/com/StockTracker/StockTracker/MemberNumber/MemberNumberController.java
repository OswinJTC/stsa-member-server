package com.StockTracker.StockTracker.MemberNumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MemberNumberController {

    @Autowired
    private MemberNumberService memberNumberService;

    @PostMapping("/initializeNumbers")
    public String initializeNumbers() {
        memberNumberService.initializeMemberNumbers();
        return "Member numbers initialized";
    }

}
