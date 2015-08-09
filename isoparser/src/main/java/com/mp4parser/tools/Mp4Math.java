package com.mp4parser.tools;

public class Mp4Math {
    public static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }

    public static long lcm(long[] input)
    {
        long result = input[0];
        for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }

    public static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

}
