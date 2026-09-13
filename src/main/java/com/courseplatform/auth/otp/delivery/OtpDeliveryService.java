package com.courseplatform.auth.otp.delivery;

import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;

public interface OtpDeliveryService {

    OtpChannel getChannel();

    void deliver(String recipient, String rawOtp, OtpPurpose purpose);
}
