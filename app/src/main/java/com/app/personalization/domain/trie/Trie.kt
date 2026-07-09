package com.app.personalization.domain.trie

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isWord = false
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String) {
        var curr = root
        for (c in word) {
            curr = curr.children.getOrPut(c) { TrieNode() }
        }
        curr.isWord = true
    }

    fun searchPrefix(prefix: String, limit: Int): List<String> {
        var curr = root
        for (c in prefix) {
            curr = curr.children[c] ?: return emptyList()
        }
        val results = ArrayList<String>()
        dfs(curr, StringBuilder(prefix), results, limit)
        return results
    }

    private fun dfs(node: TrieNode, sb: java.lang.StringBuilder, results: ArrayList<String>, limit: Int) {
        if (results.size >= limit) return
        if (node.isWord) {
            results.add(sb.toString())
        }
        for ((char, child) in node.children) {
            sb.append(char)
            dfs(child, sb, results, limit)
            sb.deleteCharAt(sb.length - 1)
        }
    }
}
