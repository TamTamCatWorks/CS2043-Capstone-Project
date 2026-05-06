public class VehicleCreator extends ItemCreator {
    @Override
    protected Item buildItem(ItemRequest req, User seller) {
        Map<String, Object> d = req.details();
        return new Vehicle(
            req.name(), req.description(), req.startingPrice(), req.condition(), seller,
            get(d, "make"), get(d, "model"), getInt(d, "year"),
            getInt(d, "mileageKm"), get(d, "color"), get(d, "fuelType")
        );
    }
}