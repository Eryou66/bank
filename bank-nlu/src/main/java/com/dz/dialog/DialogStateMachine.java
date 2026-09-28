package com.dz.dialog;

import com.dz.api.ErrorCode;
import com.dz.exception.BizException;
import com.dz.nlu.IntentCode;
import com.dz.nlu.IntentResult;
import com.dz.nlu.Slot;
import com.dz.nlu.SlotExtractor;
import com.dz.nlu.SlotType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 多轮对话状态机（文档 2.2.4）。
 *
 * 流转规则：
 *   1. 首次提问 → 识别意图 → 检查槽位完整性
 *   2. 槽位完整 → 直接进入路由链路
 *   3. 槽位缺失 → 生成引导话术，更新状态，等待补充
 *   4. 二次回复 → 读取上下文，填充槽位，再次校验
 *   5. 中途切换意图 → 重置状态机
 * 兜底：超过 3 轮未完成状态迁移 → 重置为 START。
 * 并发：Redisson 分布式锁保护状态更新，锁超时 10 秒。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialogStateMachine {

    /** 文档：超过 3 轮（3 次对话未完成迁移）自动重置 */
    private static final int MAX_TURNS = 3;
    private static final String RESET_GUIDE = "抱歉，我还没理解您的需求，请重新描述一下。";
    /** 文档：锁超时 10 秒 */
    private static final long LOCK_LEASE_SECONDS = 10;

    private final DialogSessionStore store;
    private final SlotExtractor slotExtractor;
    private final RedissonClient redissonClient;

    public DialogDecision handle(String sessionId, IntentResult intentResult, Map<SlotType, Slot> newSlots) {
        RLock lock = redissonClient.getLock("lock:session:" + sessionId);
        boolean locked;
        try {
            locked = lock.tryLock(0, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
        if (!locked) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        }

        try {
            DialogSession session = store.load(sessionId);
            IntentCode intent = resolveIntent(session, intentResult);

            if (intent == null) {
                store.delete(sessionId);
                return DialogDecision.reset(RESET_GUIDE);
            }

            // 中途切换意图 → 重置状态机，开启新流程
            if (session.getIntentCode() != null && !Objects.equals(session.getIntentCode(), intent.name())) {
                log.info("[对话] 意图切换，重置状态机：{} → {}", session.getIntentCode(), intent);
                session = new DialogSession(sessionId);
            }

            // 槽位继承：历史槽位 + 本轮新抽取（新的覆盖旧的）
            Map<SlotType, Slot> merged = new EnumMap<>(session.getSlots());
            if (newSlots != null) {
                merged.putAll(newSlots);
            }

            session.setIntentCode(intent.name());
            session.setTurnCount(session.getTurnCount() + 1);
            session.getSlots().clear();
            session.getSlots().putAll(merged);

            // 超过 3 轮仍不完整 → 重置
            List<SlotType> missing = slotExtractor.missingRequired(intent, merged);
            if (!missing.isEmpty() && session.getTurnCount() >= MAX_TURNS) {
                log.warn("[对话] 连续 {} 轮未完成，重置会话 {}", session.getTurnCount(), sessionId);
                store.delete(sessionId);
                return DialogDecision.reset(RESET_GUIDE);
            }

            if (missing.isEmpty()) {
                session.setState(DialogState.START);
                store.save(session);
                return DialogDecision.completed(merged);
            }

            // 取第一个缺失槽位反问，一次只问一个（体验更好）
            SlotType next = missing.get(0);
            DialogState nextState = stateOf(next);
            session.setState(nextState);
            store.save(session);
            log.info("[对话] 会话 {} 缺槽位 {}，进入状态 {}", sessionId, next, nextState);
            return DialogDecision.needSlot(nextState, merged, missing, guideOf(next));
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** 本轮没识别出意图时，沿用上一轮的意图（用户多半是在回答反问） */
    private IntentCode resolveIntent(DialogSession session, IntentResult result) {
        if (result != null && result.intent() != null) {
            return result.intent();
        }
        String previous = session.getIntentCode();
        if (previous == null || previous.isBlank()) {
            return null;
        }
        try {
            return IntentCode.valueOf(previous);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private DialogState stateOf(SlotType slotType) {
        return switch (slotType) {
            case ACCOUNT -> DialogState.ASK_ACCOUNT;
            case AMOUNT -> DialogState.ASK_AMOUNT;
            case DATE -> DialogState.ASK_DATE;
            case PRODUCT -> DialogState.ASK_PRODUCT;
        };
    }

    private String guideOf(SlotType slotType) {
        return switch (slotType) {
            case ACCOUNT -> "请问是哪个账号？";
            case AMOUNT -> "请问金额是多少？";
            case DATE -> "请问日期或期限是什么时候？";
            case PRODUCT -> "请问是哪款产品？";
        };
    }
}
