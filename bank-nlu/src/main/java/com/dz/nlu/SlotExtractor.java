package com.dz.nlu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SlotExtractor {

    /** 金额：安检第 6 步已把「5万」换算成「50000元」，这里直接收尾 */
    private static final Pattern AMOUNT = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:元|块钱|块)");

    /** 账号：16~19 位数字 */
    private static final Pattern ACCOUNT = Pattern.compile("\\d{16,19}");

    private static final Pattern ABSOLUTE_DATE = Pattern.compile("(\\d{4})\\s*年\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日?");
    private static final Pattern RELATIVE_DATE = Pattern.compile("(今天|明天|昨天|上周|本月|上个月|下个月|今年|去年)");

    /** 产品词典 */
    private static final List<String> PRODUCT_DICT = List.of(
            "定期存款", "活期存款", "理财产品", "基金", "国债", "大额存单", "信用卡", "借记卡"
    );

    public Map<SlotType, Slot> extract(String normalizedQuery){
        Map<SlotType, Slot> slots = new EnumMap<>(SlotType.class);
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return slots;
        }

        Matcher amount = AMOUNT.matcher(normalizedQuery);
        if (amount.find()){
            BigDecimal value = new BigDecimal(amount.group(1));
            if (value.compareTo(BigDecimal.ZERO) > 0){
                slots.put(SlotType.ACCOUNT, new Slot(SlotType.ACCOUNT, amount.group(), value.toPlainString()));
            }else {
                log.warn("[NLU] 金额非正数, 丢弃{}", amount.group());
            }
        }

        Matcher account = ACCOUNT.matcher(normalizedQuery);
        if (account.find()){
            String raw = account.group();
            slots.put(SlotType.ACCOUNT, new Slot(SlotType.ACCOUNT, raw, raw));
        }
        Matcher absDate = ABSOLUTE_DATE.matcher(normalizedQuery);
        if (absDate.find()) {
            String normalized = String.format("%s-%02d-%02d",
                    absDate.group(1), Integer.parseInt(absDate.group(2)), Integer.parseInt(absDate.group(3)));
            slots.put(SlotType.DATE, new Slot(SlotType.DATE, absDate.group(), normalized));
        } else {
            Matcher relDate = RELATIVE_DATE.matcher(normalizedQuery);
            if (relDate.find()) {
                slots.put(SlotType.DATE, new Slot(SlotType.DATE, relDate.group(), relDate.group()));
            }
        }

        for (String product : PRODUCT_DICT) {
            if (normalizedQuery.contains(product)) {
                slots.put(SlotType.PRODUCT, new Slot(SlotType.PRODUCT, product, product));
                break;
            }
        }

        return slots;
    }

    /** 返回 intent 声明的必填槽位中仍缺失的部分（文档：缺失必填槽位触发多轮引导） */
    public List<SlotType> missingRequired(IntentCode intent, Map<SlotType, Slot> slots) {
        if (intent == null) {
            return List.of();
        }
        List<SlotType> missing = new ArrayList<>();
        for (SlotType required : intent.getRequiredSlots()) {
            if (!slots.containsKey(required)) {
                missing.add(required);
            }
        }
        return missing;
    }
}

