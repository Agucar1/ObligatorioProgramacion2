package uy.edu.um;

import uy.edu.um.doors.ProcessConsole;
import uy.edu.um.doors.ProcessManagerImpl;

public class Main {
    public static void main(String[] args) {
        //pload -p process.csv -u users.csv
        //pprepare
        ProcessConsole pc = new ProcessConsole(new ProcessManagerImpl());
        pc.init();
    }
}