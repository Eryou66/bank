package com.dz.dialog;

import com.dz.nlu.Slot;
import com.dz.nlu.SlotType;

import java.util.List;
import java.util.Map;

/**
 * 状态机的决策结果。
 * complete=true  → 槽位齐了，交给路由层执行
 * complete=false → 还缺槽位，guide 是要反问用户的话术
 */
public record DialogDecision(
        boolean complete,
        boolean reset,
        DialogState state,
        Map<SlotType, Slot> slots,
        List<SlotType> missing,
        String guide
) {
    public static DialogDecision completed(Map<SlotType, Slot> slots) {
        return new DialogDecision(true, false, DialogState.START, slots, List.of(), null);
    }

    public static DialogDecision needSlot(DialogState state, Map<SlotType, Slot> slots,
                                          List<SlotType> missing, String guide) {
        return new DialogDecision(false, false, state, slots, missing, guide);
    }

    public static DialogDecision reset(String guide) {
        return new DialogDecision(false, true, DialogState.START, Map.of(), List.of(), guide);
    }
}

