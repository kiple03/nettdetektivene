package org.ntnu.backend.avatar.config;

import java.time.LocalDateTime;
import java.util.List;

import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
public class AvatarDataSeeder implements CommandLineRunner{
    private final AvatarItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserInventoryItemRepository inventoryItemRepository;

    public AvatarDataSeeder(AvatarItemRepository itemRepository,
                            UserRepository userRepository,
                            UserInventoryItemRepository inventoryItemRepository){
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public void run(String... args) throws Exception{
        if(itemRepository.count() == 0){
            System.out.println("Empty avatar table. Seeding with items...");

            AvatarItem cap_blue = AvatarItem.builder().name("cap_blue").category("head").build();
            AvatarItem glasses = AvatarItem.builder().name("glasses").category("head").build();
            AvatarItem necklace = AvatarItem.builder().name("necklace").category("head").build();
            AvatarItem shirt_red = AvatarItem.builder().name("shirt_red").category("body").build();

            itemRepository.saveAll(List.of(cap_blue, glasses, necklace, shirt_red));
        }

        userRepository.findByUsername("brukerbruker").ifPresent(user -> {
            if(inventoryItemRepository.findByUserId(user.getId()).isEmpty()){
                System.out.println("Giving test user items...");

                List<AvatarItem> allItems = itemRepository.findAll();

                List<UserInventoryItem> startingInventory = allItems.stream().map(item -> 
                    UserInventoryItem.builder()
                        .user(user)
                        .avatarItem(item)
                        .unlockedAt(LocalDateTime.now())
                        .isEquipped(true)
                        .build()
                ).toList();

                inventoryItemRepository.saveAll(startingInventory);
            }
        });
    }
}
