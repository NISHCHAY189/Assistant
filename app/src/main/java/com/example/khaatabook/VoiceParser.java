package com.example.khaatabook;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceParser {

    public static class ParseResult {
        public String type; // "lend", "payment", "new_customer"
        public Integer customerId;
        public String customerName;
        public String item;
        public Double qty;
        public String unit;
        public Integer price;
        public Integer amount;
        public String phone;

        @Override
        public String toString() {
            return "Type: " + type + ", Customer: " + customerName + ", Item: " + item + ", Amount: " + amount;
        }
    }

    public static ParseResult parse(String text, List<Customer> customers) {
        String lower = text.toLowerCase();
        ParseResult result = new ParseResult();

        // 1. Find existing customer
        for (Customer c : customers) {
            String firstName = c.getName().split(" ")[0].toLowerCase();
            if (lower.contains(firstName)) {
                result.customerId = c.getId();
                result.customerName = c.getName();
                break;
            }
        }

        // 2. New Customer check
        Pattern newCustPattern = Pattern.compile("new customer (\\w+)(?: phone (\\d+))?", Pattern.CASE_INSENSITIVE);
        Matcher newCustMatcher = newCustPattern.matcher(text);
        if (newCustMatcher.find()) {
            result.type = "new_customer";
            result.customerName = newCustMatcher.group(1);
            result.phone = newCustMatcher.group(2);
            return result;
        }

        // 3. Payment check
        String[] payKeywords = {"diye", "paid", "payment", "rupaye diye", "de diye", "bheja"};
        boolean isPayment = false;
        for (String k : payKeywords) {
            if (lower.contains(k)) {
                isPayment = true;
                break;
            }
        }

        if (isPayment) {
            result.type = "payment";
            Pattern amtPattern = Pattern.compile("(\\d+)\\s*(?:rupaye|rs|₹)", Pattern.CASE_INSENSITIVE);
            Matcher amtMatcher = amtPattern.matcher(text);
            if (amtMatcher.find()) {
                result.amount = Integer.parseInt(amtMatcher.group(1));
            }
            return result;
        }

        // 4. Lend/Item check
        String[] lendKeywords = {"liya", "le gaya", "leke gaya", "dena", "gave", "taken", "ne liya"};
        boolean isLend = false;
        for (String k : lendKeywords) {
            if (lower.contains(k)) {
                isLend = true;
                break;
            }
        }

        if (isLend || result.customerId != null) {
            result.type = "lend";

            String[] items = {"rice", "dal", "sugar", "oil", "wheat", "flour", "salt", "atta", "maida", "besan", "poha", "tea", "milk", "ghee", "soap", "biscuit"};
            for (String item : items) {
                if (lower.contains(item)) {
                    result.item = item.substring(0, 1).toUpperCase() + item.substring(1);
                    break;
                }
            }

            Pattern qtyPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(kg|gm|litre|liter|l|piece|pcs|dozen|pkt|packet)", Pattern.CASE_INSENSITIVE);
            Matcher qtyMatcher = qtyPattern.matcher(text);
            if (qtyMatcher.find()) {
                result.qty = Double.parseDouble(qtyMatcher.group(1));
                result.unit = qtyMatcher.group(2).toLowerCase();
            }

            Pattern pricePattern = Pattern.compile("(\\d+)\\s*(?:rupaye|rs)\\s*(?:kilo|kg|litre|per|each)?", Pattern.CASE_INSENSITIVE);
            Matcher priceMatcher = pricePattern.matcher(text);
            if (priceMatcher.find()) {
                result.price = Integer.parseInt(priceMatcher.group(1));
            }
        }

        return result;
    }
}
