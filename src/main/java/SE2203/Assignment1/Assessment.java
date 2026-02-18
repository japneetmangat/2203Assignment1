package SE2203.Assignment1;

/**
 * Domain layer: Contains only data and basic getters/setters[cite: 118].
 */
public class Assessment {
    private String name;
    private String type;
    private double weight;
    private boolean marked;
    private Double mark;

    public Assessment() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public boolean isMarked() { return marked; }
    public void setMarked(boolean marked) { this.marked = marked; }

    public Double getMark() { return mark; }
    public void setMark(Double mark) { this.mark = mark; }
}