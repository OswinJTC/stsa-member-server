package com.StockTracker.StockTracker.MemberNumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberNumberService {

    @Autowired
    private MemberNumberRepository memberNumberRepository;

    public void initializeMemberNumbers() {
        // Initialize 499 numbers for Taiwanese
        for (int i = 1; i <= 499; i++) {
            String number = String.format("%03d", i);  // Format to always have 3 digits
            MemberNumber memberNumber = new MemberNumber(null, "Taiwanese", number);
            memberNumberRepository.save(memberNumber);
        }

        // Initialize 99 numbers for Singaporean
        for (int i = 1; i <= 99; i++) {
            String number = String.format("%03d", i);  // Format to always have 3 digits
            MemberNumber memberNumber = new MemberNumber(null, "Singaporean", number);
            memberNumberRepository.save(memberNumber);
        }

        // Initialize 99 numbers for PR
        for (int i = 1; i <= 99; i++) {
            String number = String.format("%03d", i);  // Format to always have 3 digits
            MemberNumber memberNumber = new MemberNumber(null, "PR", number);
            memberNumberRepository.save(memberNumber);
        }
    }

    public MemberNumber getNextAvailableNumber(String type) {
        return memberNumberRepository.findFirstByTypeOrderByIdAsc(type)
                .orElseThrow(() -> new IllegalStateException("No available numbers for type: " + type));
    }

    public void deleteNumberById(String id) {
        memberNumberRepository.deleteById(id);
    }
}
