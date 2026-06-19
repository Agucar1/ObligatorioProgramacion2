package uy.edu.um.doors;

import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.EmptyHeapException;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.EmptyStackException;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;
import uy.edu.um.util.MyFileManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessManagerImpl implements ProcessManager{
    private MyFileManager fm = new MyFileManager();
    private MyHeap<Process> mayorPrioridad = new MyHeapImpl<>(false);
    private MyStack<Process> finishedProcesses = new MyStackImpl<>();
    private MyHash<Integer,Process> procesosHash = new MyHashImpl<>();
    private MyHash<Integer, Users> userHash = new MyHashImpl<>();
    private MyQueue<Process> newProcesses = new MyQueueImpl<>();
    private Process runningProcess;
    private final int stackMaximum = 3;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    @Override
    // Carga procesos y usuarios desde archivos CSV
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        MyList<String> usersLineas = fm.readFile(usersCsvPath);
        MyList<String> processLineas = fm.readFile(processCsvPath);
        System.out.println("Estamos dentro");

        // Carga procesos (empieza desde i=1 para saltar cabecera)
        for (int i = 1; i < processLineas.size(); i++) {
            String line = processLineas.get(i);

            String[] partes = line.split(";");

            int pid = Integer.parseInt(partes[0]);
            Process unProcesos = new Process();
            unProcesos.setState("NEW");
            unProcesos.setPriority(0);
            unProcesos.setPid(pid);
            unProcesos.setUid(Integer.parseInt(partes[1]));
            unProcesos.setName(partes[2]);
            String[] eventos = partes[3].split("#");

            //Parsea eventos: separa y limpia caracteres especiales { } [ ]
            for (int j = 0; j < eventos.length; j++) {
                String event = eventos[j]
                        .replace("{", "")
                        .replace("}", "")
                        .replace("[", "")
                        .replace("]", "");

                unProcesos.getEvents().add(event);
            }

            // Almacena el proceso en el HashMap para acceso rápido por PID
            procesosHash.put(unProcesos.getPid(), unProcesos);
            // Encola el proceso como NEW (esperando a que se calcule su prioridad)
            newProcesses.enqueue(unProcesos);
        }

        //cargar usuarios
        for (int i = 1; i < usersLineas.size(); i++) {
            String linea = usersLineas.get(i);
            String[] parteUser = linea.split(";");

            int uid = Integer.parseInt(parteUser[0]);
            Users newUsers = new Users();
            newUsers.setUid(uid);
            newUsers.setAlias(parteUser[1]);
            newUsers.setType(parteUser[2]);

            // Inserta el usuario en el HashMap con el UID como clave (key) y el objeto Users como valor
            userHash.put(newUsers.getUid(), newUsers);
        }

    }

    @Override
    // Saca todos los procesos de la cola NEW, calcula su prioridad y los pasa al heap de pendientes
    public void prepareProcesses() {
        while (!newProcesses.isEmpty()) {
            try {
                Process processGet = newProcesses.dequeue();
                Users user = userHash.get(processGet.getUid());
                int totalEvento = processGet.getEvents().size();
                int pesoUsuario = 0;
                int cpu = 0;
                int ram = 0;
                int disk = 0;

                // Cuenta cuántos eventos de cada tipo tiene el proceso
                for (int i = 0; i < processGet.getEvents().size(); i++) {
                    String tipoEvento = processGet.getEvents().get(i);
                    if (tipoEvento.startsWith("CPU:")) {
                        cpu++;
                    } else if (tipoEvento.startsWith("RAM:")) {
                        ram++;
                    } else if (tipoEvento.startsWith("DISK:")) {
                        disk++;
                    }
                }

                // Asigna peso según tipo de usuario
                if (user.getType().equals("ADMIN")) {
                    pesoUsuario = 32;
                } else{
                    pesoUsuario = 16;
                }


                // Calcula prioridad según fórmula: (8*CPU + 2*RAM + 2*DISK)/total + pesoUsuario*total
                int prioridad =((8 * cpu) + (2 * ram) + (2 * disk)) / totalEvento
                                    + (pesoUsuario * totalEvento);


                processGet.setState("PENDING");
                processGet.setPriority(prioridad);
                // Inserta en heap (se ordena automáticamente por prioridad)
                mayorPrioridad.insert(processGet);

                System.out.println("NEW size: " + newProcesses.size());
                System.out.println("CPU: " + cpu + " RAM: " + ram + " DISK: " + disk);
                System.out.println("PRIORITY: " + prioridad);
            } catch (EmptyQueueException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void executeNextProcess() {
        // Si ya hay un proceso ejecutándose, no hace nada (sistema monotarea)
        if (runningProcess != null) {
            System.out.println("Ya existe un proceso en ejecución.");
            return;
        }
        try {
            // Extrae el proceso de mayor prioridad del heap
            Process removerMayor = mayorPrioridad.remove();
            // Cambia su estado a RUNNING y lo asigna como proceso actual
            removerMayor.setState("RUNNING");
            runningProcess = removerMayor;

            Users user = userHash.get(removerMayor.getUid());

            String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Muestra por consola el proceso en ejecución y todos sus eventos
            System.out.println("[" + time + "]: EXECUTING PROCESS: PID=" + removerMayor.getPid()
                    + " | " + removerMayor.getName()
                    + " | USER:" + user.getAlias()
                    + " UID:" + user.getUid());

            for (int i = 0; i < removerMayor.getEvents().size(); i++) {
                String event = removerMayor.getEvents().get(i);
                System.out.println("EVENT: " + event);
            }
        }catch(EmptyHeapException e){
            System.out.println("No hay procesos pendientes");
        }
    }

    @Override
    public void finishProcessOk() {
        youVeBeenTerminated("OK");
        runningProcess=null;
    }

    @Override
    public void finishProcessError() {
        youVeBeenTerminated("ERROR");
        runningProcess=null;
    }

    @Override
    public void terminateProcess(int uid) {
        if (uid==0)
            return;
        Users usu = userHash.get(uid);
        if(usu==null)
            return;
        youVeBeenTerminated("TERMINATED");
        registrar("by USER:"+usu.getAlias()+" UID:"+uid);
        runningProcess=null;

    }

    @Override
    public void printStatus() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByUser(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByProcess(int pid) {
        System.out.println("IMPLEMENTAR");
    }

    public void youVeBeenTerminated(String estado){
        runningProcess.setState(estado);
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (finishedProcesses.size()>=stackMaximum){
            System.out.println("["+ahora.format(formato)+"]: \u001B[33mFinished process stack overflow\u001B[0m");
            registrar("["+ahora.format(formato)+"]: Finished process stack overflow");
            while(!finishedProcesses.isEmpty()){
                try{
                    registrar(finishedProcesses.pop().toString());
                } catch (EmptyStackException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        finishedProcesses.push(runningProcess);
        registrar("["+ahora.format(formato)+"]: ENDING PROCESS: PID="+runningProcess.getPid()+ " | STATE: "+estado);
    }

    public void registrar(String mensaje){
        String nombre = "DOORS_PROCESS_LOG_"+now.format(format)+".log";
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(nombre, true));
            bw.write(mensaje);
            bw.newLine();
            bw.close();
        }catch(Exception e){
            System.out.println("Excpeción");
            e.printStackTrace();
        }
    }
}
