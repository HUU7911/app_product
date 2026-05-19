import java.util.List;

public interface XManager {
    boolean addX(Product x);
    boolean editX(Product x);
    boolean delX(Product x);
    List<Product> searchX(String name);
    List<Product> sortedX(double price);
}
