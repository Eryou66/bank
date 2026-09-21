package com.dz.algorithm;

import java.util.*;

/**
 * DFA / Aho-Corasick 自动机：单次遍历完成多模式匹配，时间复杂度 O(n)。
 * 用于 Prompt 注入 L1 规则层与违规敏感内容检测。
 */
public class DfaMatcher {

    static class Node{
        final Map<Character, Node> next = new HashMap<>();
        Node fail;
        String word;   // 如果不是null就表示这是一个命中词
    }

    private final Node root = new Node();

    public DfaMatcher(Collection<String> words){
        for (String w : words) {
            if (w == null || w.isBlank()){
                continue;
            }
            Node cur = root;
            for (char c : w.toCharArray()) {
                cur = cur.next.computeIfAbsent(c, l -> new Node());
            }
            cur.word = w;
        }
        buildFailPointer();
    }

    private void buildFailPointer() {
        Deque<Node> queue = new ArrayDeque<>();
        for (Node child : root.next.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (Map.Entry<Character, Node> entry : cur.next.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();
                Node fail = cur.fail;
                while (fail != null && fail != root && !fail.next.containsKey(c)){
                    fail = fail.fail;
                }
                child.fail = (fail != null && fail.next.containsKey(c)) ? fail.next.get(c) : root;
                if (child.fail == child){
                    child.fail = root;
                }
                queue.add(child);
            }
        }
    }

    /**
     * 返回第一个命中的词， 没命中就返回null
     * @param text
     * @return
     */
    public String findFirst(String text){
        if(text == null || text.isEmpty()){
            return null;
        }
        Node cur = root;
        for (char c : text.toCharArray()) {
            while(cur != root && !cur.next.containsKey(c)){
                cur = cur.fail;
            }
            cur = cur.next.getOrDefault(c, root);
            Node t = cur;
            while (t != root) {
                if(t.word != null){
                    return t.word;
                }
                t = t.fail;
            }
        }
        return null;
    }

}
