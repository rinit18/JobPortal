package com.jobportal.utility;

import java.time.Year;

public class Data {
	public static String getMessageBody(String otp, String name) {

		return "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
				+ "    <title>Your OTP Code</title>\n" + "    <style>\n" + "        body {\n"
				+ "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" + "            margin: 0;\n"
				+ "            padding: 0;\n" + "            background-color: #f9fafb;\n" + "        }\n"
				+ "        .container {\n" + "            max-width: 600px;\n" + "            margin: 40px auto;\n"
				+ "            background-color: #ffffff;\n"
				+ "            border-radius: 16px;\n" + "            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);\n"
				+ "            overflow: hidden;\n"
				+ "        }\n" + "        .header {\n" + "            background-color: #171717;\n"
				+ "            color: #fcc419;\n" + "            padding: 24px;\n" + "            text-align: center;\n"
				+ "            border-bottom: 4px solid #fcc419;\n" + "        }\n" + "        .header h1 {\n"
				+ "            margin: 0;\n" + "            font-size: 24px;\n" + "            font-weight: 700;\n"
				+ "            letter-spacing: 0.5px;\n" + "        }\n" + "        .body {\n"
				+ "            padding: 40px 30px;\n" + "            color: #4b5563;\n" + "            text-align: center;\n"
				+ "            line-height: 1.6;\n"
				+ "        }\n" + "        .otp-container {\n" + "            margin: 30px 0;\n"
				+ "            padding: 20px;\n" + "            background-color: #fef9c3;\n"
				+ "            border-radius: 12px;\n" + "            border: 1px dashed #facc15;\n"
				+ "        }\n" + "        .otp {\n" + "            font-size: 36px;\n"
				+ "            font-weight: 800;\n" + "            color: #ca8a04;\n" + "            letter-spacing: 6px;\n"
				+ "            margin: 0;\n"
				+ "        }\n" + "        .footer {\n" + "            background-color: #f3f4f6;\n"
				+ "            padding: 20px;\n"
				+ "            font-size: 13px;\n" + "            color: #9ca3af;\n"
				+ "            text-align: center;\n" + "        }\n" + "    </style>\n" + "</head>\n" + "<body>\n"
				+ "    <div class=\"container\">\n" + "        <div class=\"header\">\n"
				+ "            <h1>CareerConnect</h1>\n" + "        </div>\n" + "        <div class=\"body\">\n"
				+ "            <h2 style=\"color: #111827; margin-top: 0;\">Verify your email address</h2>\n"
				+ "            <p>Hello <strong>"+name+"</strong>,</p>\n"
				+ "            <p>We received a request to access your account. Please use the verification code below:</p>\n"
				+ "            <div class=\"otp-container\">\n"
				+ "                <div class=\"otp\">" + otp + "</div>\n"
				+ "            </div>\n"
				+ "            <p style=\"font-size: 14px; color: #6b7280;\">This code will securely expire in 5 minutes. If you didn't request this, you can safely ignore this email.</p>\n"
				+ "        </div>\n"
				+ "        <div class=\"footer\">\n" + "            <p>&copy; " + Year.now().getValue()
				+ " CareerConnect. All rights reserved.</p>\n" + "        </div>\n" + "    </div>\n" + "</body>\n"
				+ "</html>";
	}
}
