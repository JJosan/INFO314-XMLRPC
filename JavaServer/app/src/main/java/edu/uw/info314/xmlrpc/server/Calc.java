package edu.uw.info314.xmlrpc.server;

public class Calc {
    public int add(int... args) {
        if (args.length == 2) {
            try {
                Math.addExact(args[0], args[1]);
            } catch(ArithmeticException e) {
                throw new ArithmeticException("overflow");
            }
        }
        int result = 0;
        for (int arg : args) { result += arg; }
        return result;
    }
    public int subtract(int lhs, int rhs) { return lhs - rhs; }
    public int multiply(int... args) {
        if (args.length == 0) return 0;
        if (args.length == 2) {
            try {
                Math.multiplyExact(args[0], args[1]);
            } catch(ArithmeticException e) {
                throw new ArithmeticException("overflow");
            }
        }
        int result = 1;
        for (int arg : args) { result *= arg; }
        return result;
    }
    public int divide(int lhs, int rhs) {
        if (rhs == 0) {
            throw new ArithmeticException("cannot divide by 0");
        }
        return lhs / rhs;
    }
    public int modulo(int lhs, int rhs) {
        if (rhs == 0) {
            throw new ArithmeticException("cannot divide by 0");
        }
        return lhs % rhs;
    }
}
