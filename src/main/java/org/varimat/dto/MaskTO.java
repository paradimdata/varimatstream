package org.varimat.dto;

public class MaskTO {
    private byte[][] perlimB2A;
    private byte[][] perlimB2B;
    private byte[][] perlimFFA;
    private byte[][] perlimFFB;
    private byte[][] perlimG1A;
    private byte[][] perlimG1B;
    private byte[][] perlimG2A;
    private byte[][] perlimG2B;

    public MaskTO(byte[][] perlimB2A, byte[][] perlimB2B,
                  byte[][] perlimFFA, byte[][] perlimFFB,
                  byte[][] perlimG1A, byte[][] perlimG1B,
                  byte[][] perlimG2A, byte[][] perlimG2B) {
        this.perlimB2A = perlimB2A;
        this.perlimB2B = perlimB2B;
        this.perlimFFA = perlimFFA;
        this.perlimFFB = perlimFFB;
        this.perlimG1A = perlimG1A;
        this.perlimG1B = perlimG1B;
        this.perlimG2A = perlimG2A;
        this.perlimG2B = perlimG2B;
    }

    public byte[][] getPerlimB2A() {
        return perlimB2A;
    }

    public void setPerlimB2A(byte[][] perlimB2A) {
        this.perlimB2A = perlimB2A;
    }

    public byte[][] getPerlimB2B() {
        return perlimB2B;
    }

    public void setPerlimB2B(byte[][] perlimB2B) {
        this.perlimB2B = perlimB2B;
    }

    public byte[][] getPerlimFFA() {
        return perlimFFA;
    }

    public void setPerlimFFA(byte[][] perlimFFA) {
        this.perlimFFA = perlimFFA;
    }

    public byte[][] getPerlimFFB() {
        return perlimFFB;
    }

    public void setPerlimFFB(byte[][] perlimFFB) {
        this.perlimFFB = perlimFFB;
    }

    public byte[][] getPerlimG1A() {
        return perlimG1A;
    }

    public void setPerlimG1A(byte[][] perlimG1A) {
        this.perlimG1A = perlimG1A;
    }

    public byte[][] getPerlimG1B() {
        return perlimG1B;
    }

    public void setPerlimG1B(byte[][] perlimG1B) {
        this.perlimG1B = perlimG1B;
    }

    public byte[][] getPerlimG2A() {
        return perlimG2A;
    }

    public void setPerlimG2A(byte[][] perlimG2A) {
        this.perlimG2A = perlimG2A;
    }

    public byte[][] getPerlimG2B() {
        return perlimG2B;
    }

    public void setPerlimG2B(byte[][] perlimG2B) {
        this.perlimG2B = perlimG2B;
    }
}
