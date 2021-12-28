
package io.github.josevjunior.simplejdbc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamedParameterSQL {

    /**
     * The parsed query string. Ready to be parsed in preparedstatements
     */
    private final String parsedQuery;
    
    /**
     * Map that holds the parameter name and the set of index
     */
    private final Map<String, int[]> paramMap;

    private NamedParameterSQL(String parsedQuery, Map<String, int[]> paramMap) {
        this.parsedQuery = parsedQuery;
        this.paramMap = paramMap;
    }

    public String getParsedQuery() {
        return parsedQuery;
    }
    
    public int[] getParamIndex(String name) {
        int[] arr = paramMap.get(name);
        if(arr == null) {
            return new int[0];
        }
        
        return arr;
    }
    
    public static NamedParameterSQL parse(String query) {
        
        Map<String, int[]> paramMap = new HashMap<>();
        query = parse(query, paramMap);
        
        return new NamedParameterSQL(query, paramMap);
    }
    
    private static final String parse(String query, Map<String, int[]> paramMap) {
        // I was originally using regular expressions, but they didn't work well
        // for ignoring parameter-like strings inside quotes.
        Map<String, List<Integer>> paramMapAux = new HashMap<String, List<Integer>>();
        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    List<Integer> indexList = paramMapAux.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList<Integer>();
                        paramMapAux.put(name, indexList);
                    }
                    indexList.add(index);

                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        for (Map.Entry<String, List<Integer>> entry : paramMapAux.entrySet()) {
            List<Integer> list = entry.getValue();
            int[] indexes = new int[list.size()];
            int i = 0;
            for (Integer x : list) {
                indexes[i++] = x;
            }
            paramMap.put(entry.getKey(), indexes);
        }

        return parsedQuery.toString();
    }
    
}
