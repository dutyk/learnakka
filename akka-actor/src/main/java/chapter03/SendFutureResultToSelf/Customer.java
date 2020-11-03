package chapter03.SendFutureResultToSelf;

public class Customer {
    public final String id;
    public final long version;
    public final String name;
    public final String address;

    public Customer(String id, long version, String name, String address) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.address = address;
    }
}
