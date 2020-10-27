package model;

public class Node {
    // if the identifier is a constant, well then we will have
    // stored inside the identifier the string version of the
    // constant
    String identifier;
    Integer index;
    Node  nextNode;
    Node prevNode;

    public Node(String identifier, Integer index, Node prevNode){
        this.identifier = identifier;
        this.index = index;
        this.prevNode = prevNode;
        this.nextNode = null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(Node prevNode) {
        this.prevNode = prevNode;
    }
}
