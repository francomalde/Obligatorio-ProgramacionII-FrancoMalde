package uy.edu.um.doors;

import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.EmptyStackException;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessManagerImpl implements ProcessManager{

    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA
    private MyHash<Integer, DoorUser> users;
    private MyQueue<DoorProcess> newProcesses;
    private MyHeap<DoorProcess> pendingProcesses;
    private DoorProcess runningProcess;
    private MyStack<DoorProcess> finishedProcesses;

    public ProcessManagerImpl() {
        this.users = new MyHashImpl<>();
        this.newProcesses = new MyQueueImpl<>();
        this.pendingProcesses = new MyHeapImpl<>(false);
        this.runningProcess = null;
        this.finishedProcesses = new MyStackImpl<>();
    }

    private void loadUsers(String usersCsvPath) {
        MyFileManager fileManager = new MyFileManager();
        MyList<String> lines = fileManager.readFile(usersCsvPath);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] data = line.split(";", 3);
            int uid = Integer.parseInt(data[0]);
            String alias = data[1];
            String type = data[2];
            DoorUser user = new DoorUser(uid, alias, type);
            users.put(uid, user);
        }
    }

    private void loadProcesses(String processCsvPath) {
        MyFileManager fileManager = new MyFileManager();
        MyList<String> lines = fileManager.readFile(processCsvPath);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] data = line.split(";", 4);
            int pid = Integer.parseInt(data[0]);
            int uid = Integer.parseInt(data[1]);
            String name = data[2];
            String eventsText = data[3];
            DoorUser user = users.get(uid);
            MyList<ProcessEvent> events = parseEvents(eventsText);
            DoorProcess process = new DoorProcess(pid, name, user, events);
            newProcesses.enqueue(process);
        }
    }

    private MyList<ProcessEvent> parseEvents(String eventsText) {
        MyList<ProcessEvent> events = new MyLinkedListImpl<>();
        eventsText = eventsText.replace("{", "");
        eventsText = eventsText.replace("}", "");
        String[] eventParts = eventsText.split("#");
        for (int i = 0; i < eventParts.length; i++) {
            String eventText = eventParts[i].trim();
            String[] eventData = eventText.split(":");
            String type = eventData[0].trim();
            String instructionsText = eventData[1].trim();
            instructionsText = instructionsText.replace("[", "");
            instructionsText = instructionsText.replace("]", "");
            String[] instructionParts = instructionsText.split(",");
            MyList<String> instructions = new MyLinkedListImpl<>();
            for (int j = 0; j < instructionParts.length; j++) {
                String instruction = instructionParts[j].trim();
                instructions.add(instruction);
            }
            ProcessEvent event = new ProcessEvent(type, instructions);
            events.add(event);
        }
        return events;
    }

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        loadUsers(usersCsvPath);
        loadProcesses(processCsvPath);

        System.out.println("Usuarios cargados: " + users.size());
        System.out.println("Procesos nuevos cargados: " + newProcesses.size());
    }

    @Override
    public void prepareProcesses() {
        int preparedCount = 0;
        while (!newProcesses.isEmpty()) {
            DoorProcess process = null;
            try {
                process = newProcesses.dequeue();
            } catch (EmptyQueueException e) {
                throw new RuntimeException(e);
            }
            process.calcularPriority();
            process.setState("PENDING");
            pendingProcesses.insert(process);
            preparedCount++;
        }
        System.out.println("Procesos preparados: " + preparedCount);
        System.out.println("Procesos pendientes: " + pendingProcesses.size());
    }

    @Override
    public void executeNextProcess() {
        if (runningProcess != null) {
            System.out.println("Ya hay un proceso en ejecucion:");
            System.out.println(runningProcess.basicInfo());
            return;
        }
        if (pendingProcesses.isEmpty()) {
            System.out.println("No hay procesos pendientes para ejecutar.");
            return;
        }
        DoorProcess process = pendingProcesses.remove();
        process.setState("RUNNING");
        runningProcess = process;
        System.out.println("Proceso en ejecucion:");
        System.out.println(runningProcess.basicInfo());
    }

    @Override
    public void finishProcessOk() {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecucion para finalizar.");
            return;
        }
        runningProcess.setState("FINISHED");
        runningProcess.setFinishState("OK");
        addFinishedProcess(runningProcess);
        System.out.println("Proceso finalizado correctamente:");
        System.out.println(runningProcess.finishedInfo());
        runningProcess = null;
    }

    @Override
    public void finishProcessError() {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecucion para finalizar.");
            return;
        }
        runningProcess.setState("FINISHED");
        runningProcess.setFinishState("ERROR");
        addFinishedProcess(runningProcess);
        System.out.println("Proceso finalizado con error:");
        System.out.println(runningProcess.finishedInfo());
        runningProcess = null;
    }

    private void addFinishedProcess(DoorProcess process) {
        if (finishedProcesses.size() == MAX_FINISHED_PROCESS_ON_RAM) {
            logFinishedStackOverflow();
        }
        finishedProcesses.push(process);
    }

    private void logFinishedStackOverflow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "]: Finished process stack overflow");
        while (!finishedProcesses.isEmpty()) {
            DoorProcess process;
            try {
                 process = finishedProcesses.pop();
            } catch (EmptyStackException e) {
                throw new RuntimeException(e);
            }
            System.out.println(process.finishedInfo());
        }
    }

    private boolean isAdminUser(int uid) {
        DoorUser user = users.get(uid);
        if (user == null) {
            return false;
        }
        return user.getType().equals("ADMIN");
    }

    @Override
    public void terminateProcess(int uid) {
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecucion para terminar.");
            return;
        }
        int processUserId = runningProcess.getUser().getUid();
        if (processUserId != uid && !isAdminUser(uid)) {
            System.out.println("El usuario UID:" + uid + " no tiene permisos para terminar este proceso.");
            return;
        }
        runningProcess.setState("FINISHED");
        runningProcess.setFinishState("TERMINATED");
        addFinishedProcess(runningProcess);
        System.out.println("Proceso terminado:");
        System.out.println(runningProcess.finishedInfo());
        runningProcess = null;
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
}
