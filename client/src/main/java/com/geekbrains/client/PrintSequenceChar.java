package com.geekbrains.client;

public class PrintSequenceChar {
    static volatile char c = 'A';
    static Object mainObject = new Object();

    static class ChangeChar implements Runnable {
        private char currentChar;
        private char nextChar;

        public ChangeChar(char currentChar, char nextChar) {
            this.currentChar = currentChar;
            this.nextChar = nextChar;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                synchronized (mainObject) {
                    try {
                        while (c != currentChar)
                            mainObject.wait();
                        System.out.print(currentChar);
                        c = nextChar;
                        mainObject.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        new Thread(new ChangeChar('A', 'B')).start();
        new Thread(new ChangeChar('B', 'C')).start();
        new Thread(new ChangeChar('C', 'A')).start();
    }
}
