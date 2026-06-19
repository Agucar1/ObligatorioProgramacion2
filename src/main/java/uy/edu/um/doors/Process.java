package uy.edu.um.doors;

/*import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;*/
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
/*@Setter
@Getter
@NoArgsConstructor*/
//pid;uid;name;events
public class Process implements Comparable<Process>{
    private int pid;
    private int uid;
    private String state;
    private String name;
    private int priority;
    private MyList<String> events = new MyLinkedListImpl<>();

    @Override
    public int compareTo(Process otro) {
        return Integer.compare(this.priority, otro.priority);
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public MyList<String> getEvents() {
        return events;
    }

    public void setEvents(MyList<String> events) {
        this.events = events;
    }
}
