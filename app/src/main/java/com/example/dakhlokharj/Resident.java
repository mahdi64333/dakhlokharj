package com.example.dakhlokharj;

public class Resident {
    private final int id;

    private String name;
    private Boolean active;

    public Resident(int id, String name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    @Override
    public String toString() {
        return "Resident{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
