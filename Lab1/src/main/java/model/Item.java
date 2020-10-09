package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
public class Item {
    private String name;
    private Integer quantity;
    private Integer price;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return item.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity);
    }
}
