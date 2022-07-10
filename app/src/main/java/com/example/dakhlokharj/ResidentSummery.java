package com.example.dakhlokharj;

public class ResidentSummery extends Resident {
    private int debt, credit, balance;

    public ResidentSummery(int id, String name, Boolean active) {
        super(id, name, active);
        this.debt = 0;
        this.credit = 0;
        this.balance = 0;
    }

    public void setDebt(int debt) {
        this.debt = debt;
        this.balance = this.credit - this.debt;

    }

    public void setCredit(int credit) {
        this.credit = credit;
        this.balance = this.credit - this.debt;
    }

    public int getDebt() {
        return debt;
    }

    public int getCredit() {
        return credit;
    }

    public int getBalance() {
        return balance;
    }
}
