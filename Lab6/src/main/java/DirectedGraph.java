import java.util.ArrayList;
import java.util.List;

public class DirectedGraph {
    private List<Integer> nodes;
    private List<List<Integer>> edges;

    public DirectedGraph(Integer noNodes){
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
        for(int i = 0; i< noNodes; i++){
            this.nodes.add(i);
            this.edges.add(new ArrayList<>());
        }
    }

    public Integer getSize(){
        return this.edges.size();
    }

    public List<Integer> getNodes(){
        return this.nodes;
    }

    public List<Integer> getNeighbours(int origin){
        return this.edges.get(origin);
    }

    public void addEdge(int origin, int dest){
        this.edges.get(origin).add(dest);
    }


}
