package com.example.dakhlokharj;

public class Order {
    private final String orderName, buyer;
    private final int price, year, month, day, hour, minute, second;
    private int id;

    public Order(int id, String product, int price, String buyer,
                 int year, int month, int day,
                 int hour, int minute, int second) {
        this.orderName = product;
        this.buyer = buyer;
        this.price = price;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderName() {
        return orderName;
    }

    public String getBuyer() {
        return buyer;
    }

    public int getPrice() {
        return price;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public int compareTo(Order otherOrder) {
        if (this.year > otherOrder.getYear()) {
            return 1;
        } else if (this.year < otherOrder.getYear()) {
            return -1;
        }
        if (this.month > otherOrder.getMonth()) {
            return 1;
        } else if (this.month < otherOrder.getMonth()) {
            return -1;
        }
        if (this.day > otherOrder.getDay()) {
            return 1;
        } else if (this.day < otherOrder.getDay()) {
            return -1;
        }
        if (this.hour > otherOrder.getHour()) {
            return 1;
        } else if (this.hour < otherOrder.getHour()) {
            return -1;
        }
        if (this.minute > otherOrder.getMinute()) {
            return 1;
        } else if (this.minute < otherOrder.getMinute()) {
            return -1;
        }
        if (this.second > otherOrder.getSecond()) {
            return 1;
        } else if (this.second < otherOrder.getSecond()) {
            return -1;
        }
        return 0;
    }
}
