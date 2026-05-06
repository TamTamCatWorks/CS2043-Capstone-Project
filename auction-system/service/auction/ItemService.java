@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final Map<String, ItemCreator> registry;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.registry = Map.of(
            "ART", new ArtCreator(),
            "ELECTRONICS", new ElectronicsCreator(),
            "VEHICLE", new VehicleCreator()
        );
    }

    @Transactional
    public Item create(ItemRequest req) {

        if (req.itemType() == null || req.itemType().isBlank()) {
            throw new IllegalArgumentException("Item type is required.");
        }

        User seller = userRepository.findById(req.sellerId())
            .orElseThrow(() -> new NoSuchElementException("Seller not found."));

        ItemCreator creator = registry.get(req.itemType().toUpperCase());

        if (creator == null) {
            throw new IllegalArgumentException("Unsupported item type: " + req.itemType());
        }

        return itemRepository.save(creator.create(req, seller));
    }

    @Transactional(readOnly = true)
    public Item findById(String id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Item not found."));
    }
}