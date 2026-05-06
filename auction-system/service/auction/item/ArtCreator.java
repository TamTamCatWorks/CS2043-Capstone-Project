public class ArtCreator extends ItemCreator {
    @Override
    protected Item buildItem(ItemRequest req, User seller) {
        Map<String, Object> d = req.details();
        return new Art(
            req.name(), req.description(), req.startingPrice(), req.condition(), seller,
            get(d, "artist"), getInt(d, "yearCreated"), get(d, "medium"), getBoolean(d, "hasCertificate")
        );
    }
}