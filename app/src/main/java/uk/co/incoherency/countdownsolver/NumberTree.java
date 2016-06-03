package uk.co.incoherency.countdownsolver;

import java.util.Arrays;

/**
 * Created by jes on 23/11/14.
 */
public class NumberTree implements Comparable<NumberTree> {
    public long value = -1;
    public NumberTree[] sources;
    public char op;
    public boolean is_valid;

    public NumberTree(long value) {
        this.value = value;
        this.sources = null;
        this.op = '=';
        this.is_valid = true;
    }

    public NumberTree(char op, NumberTree n1, NumberTree n2) {
        this.sources = new NumberTree[2];
        this.sources[0] = n1;
        this.sources[1] = n2;
        this.op = op;
        computeValue();
    }

    public void computeValue() {
        is_valid = true;

        long a = sources[0].value;
        long b = sources[1].value;

        switch(op) {
            case '+': value = a+b; break;
            case '-': value = a-b; break;
            case '*': value = a*b; break;
            case '_': value = b-a; break;

            case '/':
                if (b == 0 || a%b != 0) {
                    is_valid = false;
                    break;
                }
                value = a/b;
                break;

            case '?':
                if (a == 0 || b%a != 0) {
                    is_valid = false;
                    break;
                }
                value = b/a;
                break;
        }

        if (value < 0)
            is_valid = false;
    }

    public int compareTo(NumberTree x) {
        if (value < x.value)
            return -1;
        else if (value == x.value)
            return 0;
        else
            return 1;
    }

    public String stringify() {
        if (op == '=')
            return "" + value;

        String r = "(" + sources[0].stringify();
        for (int i = 1; i < sources.length; i++) {
            r += " " + op + " " + sources[1].stringify();
        }
        r += ")";

        return r;
    }

    public void addSource(NumberTree s) {
        NumberTree[] newsources = Arrays.copyOf(sources, sources.length+1);
        newsources[sources.length] = s;
        sources = newsources;
    }

    public void deleteSource(int x) {
        NumberTree[] newsources = new NumberTree[sources.length-1];
        int k = 0;
        for (int i = 0; i < sources.length; i++) {
            if (i != x)
                newsources[k++] = sources[i];
        }
        sources = newsources;
    }
}
