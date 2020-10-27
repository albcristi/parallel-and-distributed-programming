package model;

import com.sun.tools.javac.util.Pair;

public class SymbolTable {
    private MyHashTable hashTable;

    public SymbolTable(Integer m){
        hashTable = new MyHashTable(m);
    }

    public Pair<Integer, Integer>  add(String identifier){
        return hashTable.addElement(identifier);
    }

    public Pair<Integer, Integer> search(String identifier){
        return hashTable.search(identifier);
    }

    public Integer getSize(){
        return hashTable.getSize();
    }

    public Node[] getElements(){
        return hashTable.getElements();
    }

}
