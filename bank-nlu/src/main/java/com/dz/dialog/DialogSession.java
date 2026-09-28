package com.dz.dialog;

import com.dz.nlu.Slot;
import com.dz.nlu.SlotType;
import lombok.Data;

import java.util.EnumMap;
import java.util.Map;

@Data
public class DialogSession {

    private String sessionId;
    /** 当前状态 */
    private DialogState state = DialogState.START;
    /** 上一轮识别出的意图编码 */
    private String intentCode;
    /** 已交互轮次，超过 3 轮未完成则重置 */
    private int turnCount;
    /** 已收集的槽位（跨轮继承） */
    private Map<SlotType, Slot> slots = new EnumMap<>(SlotType.class);
    private long lastUpdate = System.currentTimeMillis();

    public DialogSession() {
    }

    public DialogSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isFresh() {
        return state == DialogState.START && intentCode == null;
    }

}
