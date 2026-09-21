package com.dz.security.step;

import com.dz.algorithm.TrieTree;
import com.dz.api.ErrorCode;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TermNormalizeStep implements SecurityStep {

    /** 口语->标准术语 */
    private static final Map<String, String> TERM_MAP = new LinkedHashMap<>();

    static{
        TERM_MAP.put("死期", "定期存款");
        TERM_MAP.put("活期", "活期存款");
        TERM_MAP.put("卡扣费", "银行卡代扣");
        TERM_MAP.put("理财", "理财产品");
        TERM_MAP.put("利率", "年化收益率");
        TERM_MAP.put("开户", "开立账户");
    }

    /** 数字单位换算：5万 / 5W → 50000元 */
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[wW万]");

    private final TrieTree trie = new TrieTree();

    public TermNormalizeStep() {
        TERM_MAP.forEach(trie::put);
    }

    @Override
    public int order() {
        return 6;
    }

    @Override
    public String name() {
        return "金融术语归一化";
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.TERM_NORMALIZE_FAILED;
    }

    /** 执行时机在 PII 脱敏之后、RAG 检索之前，确保归一化后的 Query 用于检索和意图识别 */
    @Override
    public void check(SecurityContext context) {
        String query = convertAmount(context.getQuery());
        context.setQuery(maxMatchReplace(query));
    }

    private String convertAmount(String query){
        Matcher matcher = AMOUNT_PATTERN.matcher(query);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            BigDecimal value = new BigDecimal(matcher.group(1)).multiply(new BigDecimal("10000"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.stripTrailingZeros().toPlainString()) + "元");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** Trie + 正向最大匹配 **/
    private String maxMatchReplace(String text){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while(i < text.length()){
            TrieTree.Match match = trie.matchLongest(text, i);
            if (match == null) {
                sb.append(text.charAt(i));
                i++;
            } else {
                sb.append(match.standardTerm());
                i += match.length();
            }
        }
        return sb.toString();
    }
}
