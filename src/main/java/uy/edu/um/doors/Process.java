package uy.edu.um.doors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;


@Getter
@Setter
@NoArgsConstructor
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

}