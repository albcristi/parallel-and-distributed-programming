package model;

import com.sun.tools.javac.util.Pair;

public class MyHashTable {

    private Integer m; // a prime number

    private Node[] elements;

    public Integer getSize(){return m;}
    public Node[] getElements(){return elements;}

    public MyHashTable(Integer m){
        this.m = m;
        elements = new Node[m];
    }

    private Integer hashingFunction(String identifier){
        Integer sum = 0;
        for(int i=0; i<identifier.length(); i++){
            sum += identifier.charAt(i);
        }
        return sum % this.m;
    }

    // ADDS A NEW ELEMENT
    // RETURNS POSITION WHERE ADDED
    public Pair<Integer, Integer> addElement(String identifier){
        Integer hashValue = this.hashingFunction(identifier);
        if(this.elements[hashValue] == null){
            // case when we don't have an element already added
            // at this address (no collision)
            Node node = new Node(identifier, 0, null);
            this.elements[hashValue] = node;
            return new Pair<>(hashValue, 0);

        }
        Node currentNode = this.elements[hashValue];
        while (currentNode.nextNode != null){
            currentNode = currentNode.nextNode;
        }
        Node newNode = new Node(identifier, currentNode.index+1,currentNode);
        currentNode.nextNode = newNode;
        return new Pair<>(hashValue,newNode.index);
    }

    public Pair<Integer, Integer> search(String identifier) {
        Integer hashValue = this.hashingFunction(identifier);
        Node currentNode = this.elements[hashValue];
        if(currentNode != null){
            while (currentNode != null){
                if(currentNode.identifier.equals(identifier)){
                    return new Pair<>(hashValue, currentNode.index);
                }
                currentNode = currentNode.nextNode;
            }
        }

        return new Pair<>(-1,-1);
    }
}
