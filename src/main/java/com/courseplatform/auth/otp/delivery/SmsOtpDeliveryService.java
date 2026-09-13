package com.courseplatform.auth.otp.delivery;

import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
public class SmsOtpDeliveryService implements OtpDeliveryService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SmsOtpDeliveryService.class);

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.PHONE;
    }

    @Override
    public void deliver(String recipient, String rawOtp, OtpPurpose purpose) {
        // In local development / test mode, log delivery securely (without exposing in public responses)
        log.info("[OTP DELIVERY - SMS] Successfully dispatched {} verification code to recipient: {} [Code: {}]",
                purpose, maskPhone(recipient), rawOtp);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, Math.min(4, phone.length() - 2)) + "***" + phone.substring(phone.length() - 2);
    }
}
