package uy.edu.um.doors;

import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;

import java.io.*;

public class MyFileManager {


    public MyList<String> readFile(String filePath) {
        MyList<String> lines = new MyLinkedListImpl<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error leyendo archivo: " + e.getMessage());
        }
        return lines;
    }

    public void writeFile(MyList<String> content, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            for (int i = 0; i < content.size(); i++) {
                bw.write(content.get(i));
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error escribiendo archivo: " + e.getMessage());
        }
    }

}
