package lk.ijse.dep.web.model;

public class Item {
    private String code;
    private String description;
    private int qty;
    private String unitprice;

    public Item() {
    }

    public Item(String code, String description, int qty, String unitprice) {
        this.code = code;
        this.description = description;
        this.qty = qty;
        this.unitprice = unitprice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getUnitprice() {
        return unitprice;
    }

    public void setUnitprice(String unitprice) {
        this.unitprice = unitprice;
    }

    @Override
    public String toString() {
        return "Item{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", qty=" + qty +
                ", unitPrice=" + unitprice +
                '}';
    }
}
