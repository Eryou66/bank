package com.dz.dialog;

import com.dz.nlu.Slot;
import com.dz.nlu.SlotType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 会话存储：Redis Hash。
 * Key  = session:{sessionId}
 * 字段 = state / intentCode / turnCount / lastUpdate / slot_xxx
 * TTL  = 1800 秒（用户 30 分钟无交互自动过期，会话重置）
 *
 * 显式指定 StringCodec：避免用 Redisson 默认编解码写出二进制值，Redis 里直接可读。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DialogSessionStore {

    private static final String KEY_PREFIX = "session:";
    private static final Duration TTL = Duration.ofSeconds(1800);

    private final RedissonClient redissonClient;

    public DialogSession load(String sessionId) {
        RMap<String, String> map = map(sessionId);
        DialogSession session = new DialogSession(sessionId);

        String intentCode = map.get("intentCode");
        if (intentCode != null && !intentCode.isBlank()) {
            session.setIntentCode(intentCode);
            session.setState(DialogState.valueOf(map.getOrDefault("state", DialogState.START.name())));
        }
        String turn = map.get("turnCount");
        session.setTurnCount(turn == null ? 0 : Integer.parseInt(turn));

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().startsWith("slot_")) {
                String slotName = entry.getKey().substring("slot_".length());
                SlotType type = SlotType.valueOf(slotName.toUpperCase());
                session.getSlots().put(type, new Slot(type, entry.getValue(), entry.getValue()));
            }
        }
        return session;
    }

    public void save(DialogSession session) {
        RMap<String, String> map = map(session.getSessionId());
        map.put("state", session.getState().name());
        map.put("intentCode", String.valueOf(session.getIntentCode()));
        map.put("turnCount", String.valueOf(session.getTurnCount()));
        map.put("lastUpdate", String.valueOf(System.currentTimeMillis()));
        for (Map.Entry<SlotType, Slot> entry : session.getSlots().entrySet()) {
            map.put("slot_" + entry.getKey().name().toLowerCase(), entry.getValue().value());
        }
        // 每次写入都刷新 TTL，保证"30 分钟无交互"的语义
        map.expire(TTL);
    }

    public void delete(String sessionId) {
        map(sessionId).delete();
    }

    private RMap<String, String> map(String sessionId) {
        return redissonClient.getMap(KEY_PREFIX + sessionId, StringCodec.INSTANCE);
    }
}
