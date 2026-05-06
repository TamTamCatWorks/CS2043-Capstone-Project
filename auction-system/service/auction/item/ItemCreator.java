public abstract class ItemCreator {

    public Item create(ItemRequest req, User seller) {
        validate(req);
        return buildItem(req, seller);
    }

    protected abstract Item buildItem(ItemRequest req, User seller);

    protected void validate(ItemRequest req) {
        if (req.name() == null || req.name().isBlank())
            throw new IllegalArgumentException("Item name is required.");
        if (req.details() == null)
            throw new IllegalArgumentException("Item details are required.");
    }

    protected String get(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return val.toString();
    }

    protected int getInt(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return ((Number) val).intValue();
    }

    protected boolean getBoolean(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return (Boolean) val;
    }
}
