package com.dz.route;

import com.dz.nlu.IntentCode;
import com.dz.nlu.RouteChain;
import com.dz.nlu.Slot;
import com.dz.nlu.SlotType;

import java.util.List;
import java.util.Map;

/** 路由决策结果 */
public record RouteDecision(
        RouteChain chain,
        IntentCode intent,
        double confidence,
        Map<SlotType, Slot> slots,
        List<SlotType> missing,
        String guide
) {
    public String intentCode() {
        return intent == null ? "UNKNOWN" : intent.name();
    }
}
