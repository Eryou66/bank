package com.dz.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Trie 字典树 + 正向最大匹配，用于金融口语术语归一化。
 * 优先匹配长词，避免短词误匹配。
 */
public class TrieTree {

    static class Node {
        final Map<Character, Node> next = new HashMap<>();
        String standardTerm;
    }

    public record Match(int length, String standardTerm) {}

    private final Node root = new Node();
    private int maxLength = 0;

    public void put(String colloquial, String standardTerm) {
        maxLength = Math.max(maxLength, colloquial.length());
        Node cur = root;
        for (char c : colloquial.toCharArray()) {
            cur = cur.next.computeIfAbsent(c, k -> new Node());
        }
        cur.standardTerm = standardTerm;
    }

    /** 从 from 位置做正向最长匹配，未命中返回 null */
    public Match matchLongest(String text, int from) {
        Node cur = root;
        int matchedLen = 0;
        String term = null;
        for (int i = from; i < text.length() && i - from < maxLength; i++) {
            cur = cur.next.get(text.charAt(i));
            if (cur == null) {
                break;
            }
            if (cur.standardTerm != null) {
                matchedLen = i - from + 1;
                term = cur.standardTerm;
            }
        }
        return term == null ? null : new Match(matchedLen, term);
    }
}
