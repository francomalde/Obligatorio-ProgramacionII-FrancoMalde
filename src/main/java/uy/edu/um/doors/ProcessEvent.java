package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class ProcessEvent {
    private String tipo;
    private MyList<String> instrucciones;

    public ProcessEvent(String type, MyList<String> instructions) {
        this.tipo = type;
        this.instrucciones = instructions;
    }

    public String getTipo() {
        return tipo;
    }

    public MyList<String> getInstrucciones() {
        return instrucciones;
    }

    public String instruccionesToString() {
        String result = "[";
        for (int i = 0; i < instrucciones.size(); i++) {
            result = result + instrucciones.get(i);
            if (i < instrucciones.size() - 1) {
                result = result + ", ";
            }
        }
        result = result + "]";
        return result;
    }

    @Override
    public String toString() {
        return "EVENT: " + tipo + " | Instructions " + instruccionesToString();
    }

}

