import java.io.*;
import java.util.*;
import java.util.stream.*;

public class XManagerImpl implements XManager {
    private List<Product> products;
    private static final String FILE_PATH = "Product.bin";
    private int nextId;

    public XManagerImpl() {
        products = new ArrayList<>();
        loadFromFile();
        nextId = products.stream().mapToInt(Product::getId).max().orElse(0) + 1;
        if (products.isEmpty()) {
            seedData();
        }
    }

    private void seedData() {
        String[] categories = {"Electronics", "Clothing", "Food", "Books", "Sports", "Home", "Toys", "Automotive"};
        String[] adjectives = {"Premium", "Classic", "Modern", "Smart", "Pro", "Ultra", "Mini", "Mega"};
        String[] nouns = {"Laptop", "Shirt", "Coffee", "Novel", "Shoes", "Chair", "Robot", "Wheel",
                "Phone", "Jacket", "Tea", "Guide", "Bag", "Desk", "Drone", "Helmet",
                "Tablet", "Pants", "Juice", "Manual", "Watch", "Lamp", "Puzzle", "Tire",
                "Camera", "Coat", "Sugar", "Atlas", "Gloves", "Sofa", "Train", "Mirror"};

        Random rand = new Random(42);
        for (int i = 1; i <= 100; i++) {
            String adj = adjectives[rand.nextInt(adjectives.length)];
            String noun = nouns[rand.nextInt(nouns.length)];
            String name = adj + " " + noun + " " + i;
            String cat = categories[rand.nextInt(categories.length)];
            double price = Math.round((10 + rand.nextDouble() * 990) * 100.0) / 100.0;
            int qty = rand.nextInt(500) + 1;
            String desc = "High-quality " + noun.toLowerCase() + " for everyday use. Model #" + (1000 + i);
            products.add(new Product(nextId++, name, cat, price, qty, desc));
        }
        saveToFile();
    }

    @Override
    public boolean addX(Product x) {
        try {
            if (x == null) throw new IllegalArgumentException("Sản phẩm không được null");
            if (x.getName() == null || x.getName().trim().isEmpty())
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            if (x.getPrice() < 0)
                throw new IllegalArgumentException("Giá sản phẩm không được âm");
            if (x.getQuantity() < 0)
                throw new IllegalArgumentException("Số lượng không được âm");

            x.setId(nextId++);
            products.add(x);
            saveToFile();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi thêm sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean editX(Product x) {
        try {
            if (x == null) throw new IllegalArgumentException("Sản phẩm không được null");
            if (x.getName() == null || x.getName().trim().isEmpty())
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            if (x.getPrice() < 0)
                throw new IllegalArgumentException("Giá sản phẩm không được âm");
            if (x.getQuantity() < 0)
                throw new IllegalArgumentException("Số lượng không được âm");

            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId() == x.getId()) {
                    products.set(i, x);
                    saveToFile();
                    return true;
                }
            }
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + x.getId());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi sửa sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delX(Product x) {
        try {
            if (x == null) throw new IllegalArgumentException("Sản phẩm không được null");
            boolean removed = products.removeIf(p -> p.getId() == x.getId());
            if (!removed) throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + x.getId());
            saveToFile();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> searchX(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) return new ArrayList<>(products);
            String kw = keyword.trim().toLowerCase();
            return products.stream()
                    .filter(p ->
                            p.getName().toLowerCase().contains(kw) ||
                            p.getCategory().toLowerCase().contains(kw) ||
                            p.getDescription().toLowerCase().contains(kw) ||
                            String.valueOf(p.getPrice()).contains(kw) ||
                            String.valueOf(p.getId()).contains(kw))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm kiếm: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> sortedX(double price) {
        try {
            return products.stream()
                    .sorted(price >= 0
                            ? Comparator.comparingDouble(Product::getPrice)
                            : Comparator.comparingDouble(Product::getPrice).reversed())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi sắp xếp: " + e.getMessage(), e);
        }
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            products = (List<Product>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Không thể đọc file, khởi tạo danh sách mới: " + e.getMessage());
            products = new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(products);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage(), e);
        }
    }
}
