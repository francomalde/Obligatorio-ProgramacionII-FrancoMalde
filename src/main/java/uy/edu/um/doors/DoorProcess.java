package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class DoorProcess implements Comparable<DoorProcess>{
    private int pid;
    private String name;
    private DoorUser user;
    private int priority;
    private String state;
    private String finishState;
    private MyList<ProcessEvent> events;

    public DoorProcess(int pid, String name, DoorUser user, MyList<ProcessEvent> events) {
        this.pid = pid;
        this.name = name;
        this.user = user;
        this.events = events;
        this.priority = 0;
        this.state = "NEW";
        this.finishState = "";
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public DoorUser getUser() {
        return user;
    }

    public int getPriority() {
        return priority;
    }

    public String getState() {
        return state;
    }

    public String getFinishState() {
        return finishState;
    }

    public MyList<ProcessEvent> getEvents() {
        return events;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setFinishState(String finishState) {
        this.finishState = finishState;
    }

    public void calcularPriority() {
        int cpuCount = 0;
        int ramCount = 0;
        int diskCount = 0;
        for (int i = 0; i < events.size(); i++) {
            ProcessEvent event = events.get(i);
            if (event.getTipo().equals("CPU")) {
                cpuCount++;
            } else if (event.getTipo().equals("RAM")) {
                ramCount++;
            } else if (event.getTipo().equals("DISK")) {
                diskCount++;
            }
        }
        int totalEvents = events.size();
        int userWeight = user.getWeight();
        this.priority = (((cpuCount * 8) + (ramCount * 2) + (diskCount * 2))/totalEvents) + (userWeight * totalEvents);
    }

    public String basicInfo() {
        return "PID=" + pid + " | " + name + " | STATE: " + state + " | " + user.toString() + " | P=" + priority;
    }

    public String finishedInfo() {
        return "PID=" + pid + " " + name + " | STATE: " + finishState + " | " + user.toString();
    }

    public String fullInfo() {
        String result = basicInfo();
        for (int i = 0; i < events.size(); i++) {
            result = result + "\n" + events.get(i).toString();
        }
        return result;
    }

    @Override
    public int compareTo(DoorProcess other) {
        return Integer.compare(this.priority, other.priority);

    }
}
